package com.mobilespace.arnavicomp.ar

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.Log
import com.google.android.filament.*
import com.google.android.filament.android.DisplayHelper
import com.google.android.filament.android.UiHelper
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.utils.Utils
import com.google.ar.core.Anchor
import java.nio.ByteBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.concurrent.thread

/**
 * Filament renderer for AR navigation with 3D models
 * Supports Filament 1.66.1 with reflection-based createInstancedAsset
 */
class FilamentRenderer(
    private val context: Context,
    private val glSurfaceView: GLSurfaceView
) : GLSurfaceView.Renderer {

    companion object {
        private const val TAG = "FilamentRenderer"
    }

    // Filament core components
    private lateinit var engine: Engine
    private lateinit var renderer: Renderer
    private lateinit var scene: Scene
    private lateinit var view: View
    private lateinit var camera: Camera
    private var swapChain: SwapChain? = null
    private var displayHelper: DisplayHelper? = null
    private var uiHelper: UiHelper? = null

    // Asset loading
    private lateinit var assetLoader: AssetLoader
    private lateinit var resourceLoader: ResourceLoader
    private var baseAsset: FilamentAsset? = null
    
    // Instance management
    private val instancedAssets = mutableListOf<FilamentAsset>()
    private val anchorToInstanceMap = mutableMapOf<Anchor, FilamentAsset>()

    // Render thread
    private var renderThread: Thread? = null
    private var isRendering = false

    init {
        glSurfaceView.setRenderer(this)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d(TAG, "onSurfaceCreated")
        
        // Initialize Filament utilities BEFORE creating Engine
        Utils.init(context)
        
        // Create Filament engine
        engine = Engine.create()
        renderer = engine.createRenderer()
        scene = engine.createScene()
        view = engine.createView()
        camera = engine.createCamera(engine.entityManager.create())

        // Setup view
        view.scene = scene
        view.camera = camera

        // Create asset loader and resource loader
        assetLoader = AssetLoader(engine, MaterialProvider(engine), EntityManager.get())
        resourceLoader = ResourceLoader(engine)

        // Load base asset from assets
        loadBaseAssetFromAssets()

        // Setup UI helper for display management
        uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK).apply {
            renderCallback = object : UiHelper.RendererCallback {
                override fun onNativeWindowChanged(surface: Surface) {
                    swapChain?.let { engine.destroySwapChain(it) }
                    swapChain = engine.createSwapChain(surface)
                    displayHelper = DisplayHelper(context)
                }

                override fun onDetachedFromSurface() {
                    swapChain?.let { engine.destroySwapChain(it) }
                    swapChain = null
                    displayHelper = null
                }

                override fun onResized(width: Int, height: Int) {
                    view.viewport = Viewport(0, 0, width, height)
                    camera.setProjection(
                        45.0,
                        width.toDouble() / height.toDouble(),
                        0.1,
                        100.0,
                        Camera.Fov.VERTICAL
                    )
                }
            }
            attachTo(glSurfaceView)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged: ${width}x${height}")
        view.viewport = Viewport(0, 0, width, height)
        
        // Setup camera projection
        camera.setProjection(
            45.0,
            width.toDouble() / height.toDouble(),
            0.1,
            100.0,
            Camera.Fov.VERTICAL
        )
    }

    override fun onDrawFrame(gl: GL10?) {
        // Render with Filament
        swapChain?.let { sc ->
            if (renderer.beginFrame(sc, 0)) {
                renderer.render(view)
                renderer.endFrame()
            }
        }
    }

    /**
     * Load base GLB asset from assets folder
     */
    private fun loadBaseAssetFromAssets() {
        try {
            val assetPath = "models/cube.glb"
            val buffer = context.assets.open(assetPath).use { stream ->
                ByteBuffer.allocateDirect(stream.available()).apply {
                    stream.read(array())
                    rewind()
                }
            }

            baseAsset = assetLoader.createAssetFromBinary(buffer)
            baseAsset?.let { asset ->
                resourceLoader.loadResources(asset)
                
                // Add base asset to scene (initially hidden or at origin)
                scene.addEntities(asset.entities)
                
                Log.d(TAG, "Base asset loaded successfully from $assetPath")
            } ?: run {
                Log.e(TAG, "Failed to create asset from binary")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading base asset: ${e.message}", e)
        }
    }

    /**
     * Create instanced asset using reflection (supports multiple API signatures)
     * Falls back to repositioning base asset if createInstancedAsset is not available
     */
    private fun createInstancedAsset(): FilamentAsset? {
        val asset = baseAsset ?: return null

        try {
            // Try reflection to find createInstancedAsset method
            // Signature 1: createInstancedAsset(FilamentAsset)
            try {
                val method = assetLoader.javaClass.getMethod("createInstancedAsset", FilamentAsset::class.java)
                val instance = method.invoke(assetLoader, asset) as FilamentAsset
                Log.d(TAG, "Created instanced asset using signature 1")
                return instance
            } catch (e: NoSuchMethodException) {
                // Try next signature
            }

            // Signature 2: createInstancedAsset(FilamentAsset, Int) - with instance count
            try {
                val method = assetLoader.javaClass.getMethod("createInstancedAsset", FilamentAsset::class.java, Int::class.java)
                val instance = method.invoke(assetLoader, asset, 1) as FilamentAsset
                Log.d(TAG, "Created instanced asset using signature 2")
                return instance
            } catch (e: NoSuchMethodException) {
                // Try next signature
            }

            // Signature 3: createInstance(FilamentAsset)
            try {
                val method = assetLoader.javaClass.getMethod("createInstance", FilamentAsset::class.java)
                val instance = method.invoke(assetLoader, asset) as FilamentAsset
                Log.d(TAG, "Created instanced asset using signature 3 (createInstance)")
                return instance
            } catch (e: NoSuchMethodException) {
                // No suitable method found
            }

            Log.w(TAG, "No createInstancedAsset method found, using fallback")
            return null

        } catch (e: Exception) {
            Log.e(TAG, "Error creating instanced asset via reflection: ${e.message}", e)
            return null
        }
    }

    /**
     * Update anchors and sync with 3D instances
     */
    fun updateAnchors(newAnchors: List<Anchor>) {
        syncInstancesWithAnchors(newAnchors)
    }

    /**
     * Synchronize 3D model instances with ARCore anchors
     */
    private fun syncInstancesWithAnchors(anchors: List<Anchor>) {
        // Remove instances for anchors that no longer exist
        val currentAnchors = anchors.toSet()
        val anchorsToRemove = anchorToInstanceMap.keys.filter { it !in currentAnchors }
        
        anchorsToRemove.forEach { anchor ->
            anchorToInstanceMap[anchor]?.let { asset ->
                scene.removeEntities(asset.entities)
                instancedAssets.remove(asset)
                assetLoader.destroyAsset(asset)
            }
            anchorToInstanceMap.remove(anchor)
        }

        // Add or update instances for new/existing anchors
        anchors.forEach { anchor ->
            if (anchor !in anchorToInstanceMap) {
                applyAnchorsToInstances(listOf(anchor))
            } else {
                // Update position for existing anchor
                updateInstancePosition(anchor)
            }
        }
    }

    /**
     * Apply anchors to instances - create new instances if possible, or use fallback
     */
    private fun applyAnchorsToInstances(anchors: List<Anchor>) {
        anchors.forEach { anchor ->
            val instance = createInstancedAsset()
            
            if (instance != null) {
                // Successfully created instance
                scene.addEntities(instance.entities)
                instancedAssets.add(instance)
                anchorToInstanceMap[anchor] = instance
                
                // Set initial position
                updateInstancePosition(anchor, instance)
                
                Log.d(TAG, "Created and positioned instance for anchor")
            } else {
                // Fallback: reposition base asset to first anchor
                if (anchorToInstanceMap.isEmpty() && baseAsset != null) {
                    anchorToInstanceMap[anchor] = baseAsset!!
                    updateInstancePosition(anchor, baseAsset!!)
                    Log.d(TAG, "Using fallback: repositioned base asset to first anchor")
                }
            }
        }
    }

    /**
     * Update instance position based on anchor pose
     */
    private fun updateInstancePosition(anchor: Anchor, asset: FilamentAsset? = null) {
        val targetAsset = asset ?: anchorToInstanceMap[anchor] ?: return
        val pose = anchor.pose
        
        // Convert ARCore pose to Filament transform matrix
        val poseMatrix = FloatArray(16)
        pose.toMatrix(poseMatrix, 0)
        
        // Apply transform to asset root entity
        val rootEntity = targetAsset.root
        val tm = engine.transformManager
        val instance = tm.getInstance(rootEntity)
        
        if (instance != 0) {
            tm.setTransform(instance, poseMatrix)
        }
    }

    /**
     * Start render loop
     */
    fun onResume() {
        isRendering = true
        startRenderLoop()
    }

    /**
     * Pause render loop
     */
    fun onPause() {
        isRendering = false
        renderThread?.interrupt()
        renderThread = null
    }

    /**
     * Cleanup resources
     */
    fun onDestroy() {
        isRendering = false
        renderThread?.interrupt()
        renderThread = null

        // Destroy instances
        instancedAssets.forEach { assetLoader.destroyAsset(it) }
        instancedAssets.clear()
        
        // Destroy base asset
        baseAsset?.let { assetLoader.destroyAsset(it) }
        baseAsset = null

        // Destroy Filament components
        engine.destroyRenderer(renderer)
        engine.destroyView(view)
        engine.destroyScene(scene)
        engine.destroyCameraComponent(camera.entity)
        
        swapChain?.let { engine.destroySwapChain(it) }
        
        uiHelper?.detach()
        
        engine.destroy()
    }

    /**
     * Start render loop in separate thread
     */
    private fun startRenderLoop() {
        renderThread = thread(name = "FilamentRenderThread") {
            while (isRendering && !Thread.currentThread().isInterrupted) {
                try {
                    Thread.sleep(16) // ~60 FPS
                    glSurfaceView.requestRender()
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
    }
}

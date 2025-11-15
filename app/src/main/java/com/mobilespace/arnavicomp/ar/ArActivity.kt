package com.mobilespace.arnavicomp.ar

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.google.ar.core.exceptions.*
import com.mobilespace.arnavicomp.R

class ArActivity : AppCompatActivity() {

    private var glSurfaceView: GLSurfaceView? = null
    private var arCoreManager: ArCoreManager? = null
    private var filamentRenderer: FilamentRenderer? = null
    private var userRequestedInstall = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create GLSurfaceView
        glSurfaceView = GLSurfaceView(this).apply {
            setEGLContextClientVersion(3)
            setEGLConfigChooser(8, 8, 8, 8, 16, 0)
            preserveEGLContextOnPause = true
        }
        setContentView(glSurfaceView)
    }

    override fun onResume() {
        super.onResume()

        // Check ARCore availability
        when (ArCoreApk.getInstance().requestInstall(this, userRequestedInstall)) {
            ArCoreApk.InstallStatus.INSTALLED -> {
                // ARCore is installed, continue
            }
            ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                userRequestedInstall = false
                return
            }
        }

        // Initialize ARCore session
        try {
            val session = Session(this)
            
            // Initialize managers
            if (arCoreManager == null) {
                arCoreManager = ArCoreManager(session)
            }
            
            if (filamentRenderer == null) {
                filamentRenderer = FilamentRenderer(this, glSurfaceView!!).apply {
                    onResume()
                }
            }
            
            arCoreManager?.onResume()
            glSurfaceView?.onResume()
            
        } catch (e: UnavailableArcoreNotInstalledException) {
            showError("ARCore not installed")
        } catch (e: UnavailableApkTooOldException) {
            showError("ARCore APK too old")
        } catch (e: UnavailableSdkTooOldException) {
            showError("SDK too old")
        } catch (e: UnavailableDeviceNotCompatibleException) {
            showError("Device not compatible with ARCore")
        } catch (e: Exception) {
            showError("Failed to create AR session: ${e.message}")
        }
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView?.onPause()
        filamentRenderer?.onPause()
        arCoreManager?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        filamentRenderer?.onDestroy()
        arCoreManager?.onDestroy()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }
}

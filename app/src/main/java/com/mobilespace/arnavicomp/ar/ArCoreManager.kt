package com.mobilespace.arnavicomp.ar

import com.google.ar.core.*
import com.google.ar.core.exceptions.CameraNotAvailableException

/**
 * Manages ARCore session and tracking
 */
class ArCoreManager(private val session: Session) {

    private var isSessionPaused = false
    private val anchors = mutableListOf<Anchor>()

    init {
        // Configure ARCore session
        val config = Config(session).apply {
            updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
        }
        session.configure(config)
    }

    fun onResume() {
        try {
            if (isSessionPaused) {
                session.resume()
                isSessionPaused = false
            }
        } catch (e: CameraNotAvailableException) {
            // Handle camera error
            e.printStackTrace()
        }
    }

    fun onPause() {
        if (!isSessionPaused) {
            session.pause()
            isSessionPaused = true
        }
    }

    fun onDestroy() {
        session.close()
        anchors.forEach { it.detach() }
        anchors.clear()
    }

    /**
     * Update ARCore frame
     */
    fun update(): Frame? {
        return try {
            session.update()
        } catch (e: CameraNotAvailableException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Add anchor at a pose
     */
    fun addAnchor(pose: Pose): Anchor? {
        return try {
            val anchor = session.createAnchor(pose)
            anchors.add(anchor)
            anchor
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get all current anchors
     */
    fun getAnchors(): List<Anchor> = anchors.toList()

    /**
     * Remove an anchor
     */
    fun removeAnchor(anchor: Anchor) {
        anchor.detach()
        anchors.remove(anchor)
    }

    /**
     * Get the current camera
     */
    fun getCamera(): Camera = session.camera

    /**
     * Hit test to find planes
     */
    fun hitTest(frame: Frame, x: Float, y: Float): List<HitResult> {
        return frame.hitTest(x, y)
    }
}

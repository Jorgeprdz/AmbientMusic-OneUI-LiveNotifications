package com.kieronquinn.app.ambientmusicmod.debug

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

/** Debug-only launcher for M0. No production recognition path is connected here. */
class NowPlayingSurfaceProbeActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_POST_NOTIFICATIONS = 3601
    }

    private lateinit var probe: NowPlayingSurfaceProbe
    private lateinit var report: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        probe = NowPlayingSurfaceProbe(this)
        probe.ensureChannel()
        setContentView(buildUi())
        refresh()
    }

    override fun onResume() {
        super.onResume()
        if (::probe.isInitialized && ::report.isInitialized) refresh()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_POST_NOTIFICATIONS &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            runProbe()
        } else {
            refresh()
        }
    }

    private fun buildUi(): ScrollView {
        val density = resources.displayMetrics.density
        val padding = (20 * density).toInt()
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        container.addView(TextView(this).apply {
            text = "M0 — Public Live Update probe"
            textSize = 22f
        })
        report = TextView(this).apply {
            textSize = 13f
            setTextIsSelectable(true)
            setPadding(0, padding / 2, 0, padding / 2)
        }
        container.addView(report)
        container.addView(button("Run M0 probe") { requestPermissionOrRun() })
        container.addView(button("Refresh diagnostics") { refresh() })
        container.addView(button("Open Live Update settings") {
            if (!probe.openPromotionSettings()) {
                Toast.makeText(this, "Promotion settings are not exposed on this build", Toast.LENGTH_LONG).show()
            }
        })
        container.addView(button("Cancel probe") {
            probe.cancel()
            refresh()
        })
        return ScrollView(this).apply { addView(container) }
    }

    private fun button(label: String, action: () -> Unit): Button {
        return Button(this).apply {
            text = label
            setOnClickListener { action() }
        }
    }

    private fun requestPermissionOrRun() {
        if (probe.hasPostNotificationsPermission()) {
            runProbe()
            return
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            REQUEST_POST_NOTIFICATIONS
        )
    }

    private fun runProbe() {
        report.text = probe.post().asText()
        report.postDelayed({ refresh() }, 1_000L)
    }

    private fun refresh() {
        report.text = probe.snapshot().asText()
    }
}

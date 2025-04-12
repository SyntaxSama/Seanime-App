package net.cubicbit.seanime

import android.os.*
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webViewManager: WebViewManager
    internal lateinit var serverChecker: ServerChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterFullScreen()

        webViewManager = WebViewManager(this)
        serverChecker = ServerChecker(this, webViewManager)

        webViewManager.initialize()
        serverChecker.checkAndStart()

        val updater = AppUpdateNotifier(this)
        updater.checkForUpdate(ReleaseVersions.CURRENT.version)

    }

    private fun enterFullScreen() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        actionBar?.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        serverChecker.shutdown()
    }
}

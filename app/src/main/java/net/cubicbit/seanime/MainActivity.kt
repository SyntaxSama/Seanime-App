package net.cubicbit.seanime

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.view.*
import android.webkit.*
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private var SERVER_URL = "http://localhost:43211"

    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var originalSystemUiVisibility = 0
    private var originalOrientation = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterFullScreen()
        checkAndStart()
    }

    private fun enterFullScreen() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        actionBar?.hide()
    }

    private fun checkAndStart() {
        executor.execute {
            if (isServerReachable()) {
                handler.post {
                    webView = WebView(this).apply {
                        settings.javaScriptEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                                if (url == SERVER_URL) return true

                                val uri = Uri.parse(url)
                                if (uri.host != null && uri.host != "localhost") {
                                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                                    return true
                                }

                                return super.shouldOverrideUrlLoading(view, url)
                            }

                            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                                handler.post { showError("Failed to load: $description") }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                                if (customView != null) {
                                    onHideCustomView()
                                    return
                                }

                                customView = view
                                originalSystemUiVisibility = window.decorView.systemUiVisibility
                                originalOrientation = requestedOrientation

                                customViewCallback = callback
                                (window.decorView as FrameLayout).addView(
                                    view,
                                    FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT
                                    )
                                )

                                window.decorView.systemUiVisibility = (
                                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                        )
                                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            }

                            override fun onHideCustomView() {
                                (window.decorView as FrameLayout).removeView(customView)
                                customView = null
                                window.decorView.systemUiVisibility = originalSystemUiVisibility
                                requestedOrientation = originalOrientation
                                customViewCallback?.onCustomViewHidden()
                                customViewCallback = null
                            }

                            override fun getDefaultVideoPoster(): Bitmap? {
                                return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                            }
                        }
                    }

                    setContentView(webView)
                    webView.loadUrl(SERVER_URL)
                }
            } else {
                handler.post { showManualSetupDialog() }
            }
        }
    }

    private fun isServerReachable(): Boolean {
        return try {
            val url = URL(SERVER_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.connect()
            val result = connection.responseCode == 200
            connection.disconnect()
            result
        } catch (e: Exception) {
            false
        }
    }

    private fun showManualSetupDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Manual Setup Required")
            setMessage("""
                NOTE! If you already installed the package skip step 2

                1. Open Termux
                2. Run: pkg install seanime -y
                3. Run: seanime
                4. Return to this app
            """.trimIndent())
            setPositiveButton("I've Done This") { _, _ -> checkAndStart() }
            setNegativeButton("Exit") { _, _ -> finish() }
            setCancelable(false)
            show()
        }
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this).apply {
            setTitle("Error")
            setMessage(message)
            setPositiveButton("Retry") { _, _ -> checkAndStart() }
            setNegativeButton("Exit") { _, _ -> finish() }
            show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdownNow()
    }
}

package net.cubicbit.seanime

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.*
import android.webkit.*
import android.widget.FrameLayout

class WebViewManager(private val activity: MainActivity) {

    lateinit var webView: WebView
        private set

    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var originalSystemUiVisibility = 0
    private var originalOrientation = 0

    @SuppressLint("SetJavaScriptEnabled")
    fun initialize() {
        webView = WebView(activity).apply {
            settings.apply {
                javaScriptEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
                allowFileAccess = true
                allowContentAccess = true
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }

            webViewClient = object : WebViewClient() {

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString() ?: return false

                    return handleSpecialUrls(url, view)
                }

                @Suppress("DEPRECATION")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    return handleSpecialUrls(url ?: return false, view)
                }

                private fun handleSpecialUrls(url: String, view: WebView?): Boolean {
                    return when {
                        url.endsWith(".mp4") || url.endsWith(".mkv") || url.endsWith(".m3u8") || url.contains("stream") || url.endsWith("torrent") -> {
                            launchMpv(url)
                            true
                        }
                        url.startsWith("http://") || url.startsWith("https://") -> {
                            view?.loadUrl(url)
                            true
                        }
                        url.startsWith("intent:") || url.startsWith("magnet:") || url.startsWith("vlc://") -> {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                activity.startActivity(intent)
                            } catch (e: Exception) {
                                DialogHelper.showError(activity, "No app found to open this link!") {}
                            }
                            true
                        }
                        else -> false
                    }
                }


                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    DialogHelper.showError(activity, "Failed to load: ${error?.description}") {
                        activity.serverChecker.checkAndStart()
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {


                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {

                    if (customView != null) {
                        onHideCustomView()
                        return
                    }

                    customView = view
                    originalSystemUiVisibility = activity.window.decorView.systemUiVisibility
                    originalOrientation = activity.requestedOrientation

                    customViewCallback = callback
                    (activity.window.decorView as FrameLayout).addView(
                        view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
                    )

                    activity.window.decorView.systemUiVisibility = (
                            View.SYSTEM_UI_FLAG_FULLSCREEN or
                                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            )
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }

                override fun onHideCustomView() {
                    (activity.window.decorView as FrameLayout).removeView(customView)
                    customView = null
                    activity.window.decorView.systemUiVisibility = originalSystemUiVisibility
                    activity.requestedOrientation = originalOrientation
                    customViewCallback?.onCustomViewHidden()
                    customViewCallback = null
                }
            }

            setDownloadListener { url, _, _, _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                activity.startActivity(intent)
            }
        }

        activity.setContentView(webView)
    }

    private fun launchMpv(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(url), "video/*")
                setPackage("is.xyz.mpv")
                putExtra("title", "Seanime")
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            DialogHelper.showError(activity, "Could not open in MPV. Is it installed?") {}
        }
    }


    fun loadUrl(url: String) {
        webView.loadUrl(url)
    }
}

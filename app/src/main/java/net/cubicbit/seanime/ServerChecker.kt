package net.cubicbit.seanime

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ServerChecker(
    private val context: Context,
    private val webViewManager: WebViewManager
) {
    private val SERVER_URL = "http://localhost:43211"
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    fun checkAndStart() {
        executor.execute {
            val reachable = isServerReachable()
            handler.post {
                if (reachable) {
                    webViewManager.loadUrl(SERVER_URL)
                } else {
                    DialogHelper.showManualSetupDialog(context) { checkAndStart() }
                }
            }
        }
    }

    private fun isServerReachable(): Boolean {
        return try {
            val connection = URL(SERVER_URL).openConnection() as HttpURLConnection
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

    fun shutdown() {
        executor.shutdownNow()
    }
}

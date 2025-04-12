package net.cubicbit.seanime

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class AppUpdateNotifier(private val activity: Activity) {

    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private val githubApiUrl = "https://api.github.com/repos/SyntaxSama/Seanime-App/releases/latest"

    fun checkForUpdate(currentVersion: String) {
        executor.execute {
            try {
                val url = URL(githubApiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 3000
                connection.readTimeout = 3000
                connection.setRequestProperty("Accept", "application/vnd.github+json")
                connection.connect()

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)
                    val latestVersion = json.getString("tag_name").removePrefix("v")

                    if (latestVersion != currentVersion) {
                        handler.post {
                            showUpdateDialog(json.getString("html_url"))
                        }
                    }
                }

                connection.disconnect()
            } catch (e: Exception) {
                println(e.stackTrace)
            }
        }
    }

    private fun showUpdateDialog(updateUrl: String) {
        AlertDialog.Builder(activity).apply {
            setTitle("Seanime App Is Outdated")
            setMessage("""
                Your currently on version ${ReleaseVersions.VERSION.version} (${ReleaseVersions.VERSION.versionName})
                
                The latest app version is ${ReleaseVersions.RELEASE.versionName} (${ReleaseVersions.RELEASE.versionName})
                if you wish to update to the latest version please tap on Update Now if you wish to
                not update and stay on this version please tap on Later!
            """.trimIndent())
            setPositiveButton("Update Now") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
                activity.startActivity(intent)
            }
            setNegativeButton("Later", null)
            show()
        }
    }

    fun shutdown() {
        executor.shutdownNow()
    }
}

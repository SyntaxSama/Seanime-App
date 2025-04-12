package net.cubicbit.seanime

import android.content.Context
import androidx.appcompat.app.AlertDialog

object DialogHelper {

    fun showManualSetupDialog(context: Context, onRetry: () -> Unit) {
        AlertDialog.Builder(context).apply {
            setTitle("Manual Setup Required")
            setMessage("""
                NOTE! If you already installed the package skip step 2

                1. Open Termux
                2. Run: pkg install seanime -y
                3. Run: seanime
                4. Return to this app
            """.trimIndent())
            setPositiveButton("I've Done This") { _, _ -> onRetry() }
            setNegativeButton("Exit") { _, _ -> (context as MainActivity).finish() }
            setCancelable(false)
            show()
        }
    }

    fun showError(context: Context, message: String, onRetry: () -> Unit) {
        AlertDialog.Builder(context).apply {
            setTitle("Error")
            setMessage(message)
            setPositiveButton("Retry") { _, _ -> onRetry() }
            setNegativeButton("Exit") { _, _ -> (context as MainActivity).finish() }
            show()
        }
    }
}

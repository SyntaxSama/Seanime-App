package net.cubicbit.seanime

enum class ReleaseVersions (val version: String, val versionName: String) {
    RELEASE("1.0.0", "Eternal"),
    VERSION("1.0.0", "Void");


    companion object {
        val CURRENT = RELEASE
    }
}
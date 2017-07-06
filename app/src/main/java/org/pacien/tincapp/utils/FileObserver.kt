package org.pacien.tincapp.utils

/**
 * @author pacien
 */
class FileObserver(path: String,
                   mask: Int = android.os.FileObserver.ALL_EVENTS,
                   private val listener: (event: Int, path: String?) -> Unit) : android.os.FileObserver(path, mask) {

    override fun onEvent(event: Int, path: String?) = listener(event, path)

    companion object {
        val CHANGE = CREATE or DELETE or MODIFY or MOVED_TO or MOVED_FROM
    }

}

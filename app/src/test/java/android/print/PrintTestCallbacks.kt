package android.print

/**
 * Test helper callbacks residing in package android.print to access
 * framework package-private constructors of PrintDocumentAdapter callbacks.
 */
class TestLayoutResultCallback : PrintDocumentAdapter.LayoutResultCallback() {
    var isFinished = false
    var info: PrintDocumentInfo? = null
    var isChanged = false
    var isCancelled = false
    var isFailed = false
    var errorMessage: CharSequence? = null

    override fun onLayoutFinished(info: PrintDocumentInfo?, changed: Boolean) {
        this.isFinished = true
        this.info = info
        this.isChanged = changed
    }

    override fun onLayoutFailed(error: CharSequence?) {
        this.isFailed = true
        this.errorMessage = error
    }

    override fun onLayoutCancelled() {
        this.isCancelled = true
    }
}

class TestWriteResultCallback : PrintDocumentAdapter.WriteResultCallback() {
    var isFinished = false
    var writtenPages: Array<out PageRange>? = null
    var isCancelled = false
    var isFailed = false
    var errorMessage: CharSequence? = null

    override fun onWriteFinished(pages: Array<out PageRange>?) {
        this.isFinished = true
        this.writtenPages = pages
    }

    override fun onWriteFailed(error: CharSequence?) {
        this.isFailed = true
        this.errorMessage = error
    }

    override fun onWriteCancelled() {
        this.isCancelled = true
    }
}

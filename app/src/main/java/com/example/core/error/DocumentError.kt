package com.example.core.error

/**
 * Sealed hierarchy of domain errors for document processing operations.
 */
sealed class DocumentError(
    open val message: String,
    open val cause: Throwable? = null
) {
    data class StorageError(
        override val message: String,
        override val cause: Throwable? = null
    ) : DocumentError(message, cause)

    data class FormatError(
        override val message: String,
        val formatName: String,
        override val cause: Throwable? = null
    ) : DocumentError(message, cause)

    data class SecurityPermissionError(
        override val message: String = "Permission to access the requested storage location was denied."
    ) : DocumentError(message)

    data class CorruptDocumentError(
        override val message: String = "Document content is corrupted or in an unrecognized format."
    ) : DocumentError(message)

    data class ExportError(
        override val message: String,
        val targetFormat: String,
        override val cause: Throwable? = null
    ) : DocumentError(message, cause)

    data class DocumentNotFound(
        val documentId: String
    ) : DocumentError("Document with ID '$documentId' could not be found.")

    data class GeneralError(
        override val message: String,
        override val cause: Throwable? = null
    ) : DocumentError(message, cause)
}

/**
 * Type-safe result wrapper for document operations.
 */
sealed class DocumentResult<out T> {
    data class Success<T>(val data: T) : DocumentResult<T>()
    data class Failure(val error: DocumentError) : DocumentResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }

    inline fun onSuccess(action: (T) -> Unit): DocumentResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onFailure(action: (DocumentError) -> Unit): DocumentResult<T> {
        if (this is Failure) action(error)
        return this
    }
}

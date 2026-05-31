package cz.bezcisobe.backend.exception;

/**
 * Implemented by exceptions that carry a MessageSource key plus optional args
 * so GlobalExceptionHandler can resolve them against the request locale.
 */
public interface LocalizedException {
    String getMessageKey();
    Object[] getArgs();
}

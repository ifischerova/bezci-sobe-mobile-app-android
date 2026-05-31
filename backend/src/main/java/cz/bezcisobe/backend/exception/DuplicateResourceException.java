package cz.bezcisobe.backend.exception;

public class DuplicateResourceException extends RuntimeException implements LocalizedException {

    private final String messageKey;
    private final Object[] args;

    public DuplicateResourceException(String message) {
        super(message);
        this.messageKey = null;
        this.args = null;
    }

    private DuplicateResourceException(String messageKey, Object[] args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }

    public static DuplicateResourceException of(String messageKey, Object... args) {
        return new DuplicateResourceException(messageKey, args);
    }

    @Override
    public String getMessageKey() { return messageKey; }

    @Override
    public Object[] getArgs() { return args; }
}

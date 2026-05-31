package cz.bezcisobe.backend.exception;

public class ResourceNotFoundException extends RuntimeException implements LocalizedException {

    private final String messageKey;
    private final Object[] args;

    public ResourceNotFoundException(String message) {
        super(message);
        this.messageKey = null;
        this.args = null;
    }

    private ResourceNotFoundException(String messageKey, Object[] args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }

    public static ResourceNotFoundException of(String messageKey, Object... args) {
        return new ResourceNotFoundException(messageKey, args);
    }

    @Override
    public String getMessageKey() { return messageKey; }

    @Override
    public Object[] getArgs() { return args; }
}

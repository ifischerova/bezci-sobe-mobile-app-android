package cz.bezcisobe.backend.exception;

public class BadRequestException extends RuntimeException implements LocalizedException {

    private final String messageKey;
    private final Object[] args;

    public BadRequestException(String message) {
        super(message);
        this.messageKey = null;
        this.args = null;
    }

    private BadRequestException(String messageKey, Object[] args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }

    public static BadRequestException of(String messageKey, Object... args) {
        return new BadRequestException(messageKey, args);
    }

    @Override
    public String getMessageKey() { return messageKey; }

    @Override
    public Object[] getArgs() { return args; }
}

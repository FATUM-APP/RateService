package fatum.rate.exception;

/**
 * A refusal of the domain, always carrying the code that explains it.
 *
 * <p>It is a {@link RuntimeException} because no caller in this service can do anything useful
 * with the failure besides letting it reach the handler that turns it into a response.</p>
 */
public class FatumServiceException extends RuntimeException {

    private final CalificationErrorCode errorCode;

    public FatumServiceException(CalificationErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public FatumServiceException(CalificationErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public CalificationErrorCode getErrorCode() {
        return errorCode;
    }
}

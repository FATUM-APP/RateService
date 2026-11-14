package fatum.rate.exception;

import org.springframework.http.HttpStatus;

public enum CalificationErrorCode {

    BLANK("A required value should not be blank", HttpStatus.BAD_REQUEST),
    INVALID_SCORE("The score must be between 1 and 5", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED("Request validation failed", HttpStatus.BAD_REQUEST),
    CONCURRENT_UPDATE("The rating could not be saved because it was updated concurrently", HttpStatus.CONFLICT),
    USER_NOT_FOUND("User not found", HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND("Order has not been completed or was not received", HttpStatus.NOT_FOUND),
    ORDER_NOT_COMPLETED("An order can only be rated once it was completed", HttpStatus.CONFLICT),
    RATING_NOT_FOUND("Rating not found", HttpStatus.NOT_FOUND),
    RATING_ALREADY_UPDATED("This rating was already updated by the user", HttpStatus.CONFLICT);

    private final String defaultMessage;
    private final HttpStatus httpStatus;

    CalificationErrorCode(String defaultMessage, HttpStatus httpStatus) {
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}

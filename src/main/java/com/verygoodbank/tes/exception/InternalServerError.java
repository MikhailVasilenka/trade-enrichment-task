package com.verygoodbank.tes.exception;

public class InternalServerError extends RuntimeException {

    public InternalServerError(final String message) {
        super(message);
    }

    public InternalServerError() {
        super();
    }
}

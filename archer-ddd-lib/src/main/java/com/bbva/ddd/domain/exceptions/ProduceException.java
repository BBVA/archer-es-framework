package com.bbva.ddd.domain.exceptions;

/**
 * Custoim producer exception to manage errors in production
 */
public class ProduceException extends RuntimeException {

    private static final long serialVersionUID = -1872568827813815386L;

    /**
     * Constructor
     */
    public ProduceException() {
        super();
    }

    /**
     * Constructor
     *
     * @param message error message
     */
    public ProduceException(final String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param message error message
     * @param cause   exception cause
     */
    public ProduceException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor
     *
     * @param cause exception cause
     */
    public ProduceException(final Throwable cause) {
        super(cause);
    }
}

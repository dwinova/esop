/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.email.exception;

public class EmailSendException extends RuntimeException {
	public EmailSendException(String message) {
		super(message);
	}
	
	public EmailSendException(String message, Throwable cause) {
		super(message, cause);
	}
}

package com.twowheeler.auth_service.CustomException;

public class TokenExpiredException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 5460433129933994382L;

	public TokenExpiredException(String message) { super(message); }
}
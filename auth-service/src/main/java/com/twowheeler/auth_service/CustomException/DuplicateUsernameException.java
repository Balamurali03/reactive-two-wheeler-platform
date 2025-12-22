package com.twowheeler.auth_service.CustomException;

public class DuplicateUsernameException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DuplicateUsernameException(String username) {
        super("Duplicate username detected: " + username);
    }
}

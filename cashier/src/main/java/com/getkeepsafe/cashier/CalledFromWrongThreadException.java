package com.getkeepsafe.cashier;

public class CalledFromWrongThreadException extends RuntimeException {
	public CalledFromWrongThreadException() {
		super();
	}

	public CalledFromWrongThreadException(String detailMessage) {
		super(detailMessage);
	}
}

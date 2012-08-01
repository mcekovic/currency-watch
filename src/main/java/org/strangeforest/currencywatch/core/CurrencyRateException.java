package org.strangeforest.currencywatch.core;

public class CurrencyRateException extends RuntimeException {

	private boolean recoverable;

	public CurrencyRateException() {
		super();
	}

	public CurrencyRateException(boolean recoverable) {
		super();
		this.recoverable = recoverable;
	}

	public CurrencyRateException(String message) {
		super(message);
	}

	public CurrencyRateException(String message, Object... params) {
		super(String.format(message, params));
	}

	public CurrencyRateException(String message, Throwable cause) {
		super(message, cause);
	}

	public CurrencyRateException(String message, Throwable cause, Object... params) {
		super(String.format(message, params), cause);
	}

	public CurrencyRateException(Throwable cause) {
		super(cause);
	}

	public boolean isRecoverable() {
		return recoverable;
	}

	public static CurrencyRateException wrap(Throwable th) {
		return th instanceof CurrencyRateException ? (CurrencyRateException)th : new CurrencyRateException(th);
	}
}
package com.chartquery;

/** The request failed before a complete HTTP response was received (network error, timeout). */
public final class ChartQueryTransportException extends ChartQueryException {
  private static final long serialVersionUID = 1L;

  /** Creates the exception. */
  public ChartQueryTransportException(String message, Throwable cause) {
    super(message, cause);
  }
}

package com.chartquery;

/** Base class for every SDK-defined exception. */
public class ChartQueryException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /** Creates the exception. */
  public ChartQueryException(String message, Throwable cause) {
    super(message, cause);
  }
}

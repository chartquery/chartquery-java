package com.chartquery;

/** The client configuration is missing or unusable. */
public final class ChartQueryConfigurationException extends ChartQueryException {
  private static final long serialVersionUID = 1L;

  /** Creates the exception. */
  public ChartQueryConfigurationException(String message) {
    super(message, null);
  }

  /** Creates the exception with a cause. */
  public ChartQueryConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}

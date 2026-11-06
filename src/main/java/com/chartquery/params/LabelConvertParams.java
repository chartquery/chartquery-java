package com.chartquery.params;

import com.chartquery.ChartQueryParams;
import com.chartquery.ChartQuerySerializationException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for {@code POST /v1/label/convert}.
 *
 * <p>Generated from the ChartQuery OpenAPI document. Do not edit by hand.
 */
public final class LabelConvertParams implements ChartQueryParams {
  private final Map<String, Object> values;

  private LabelConvertParams(Map<String, Object> values) {
    this.values = values;
  }

  /** Returns a new builder. */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public Map<String, Object> toMap() {
    return values;
  }

  /** Builder for {@link LabelConvertParams}. */
  public static final class Builder {
    private final Map<String, Object> values = new LinkedHashMap<>();

    private Builder() {}

    /** Required. Raw printer commands to convert. Also send from and to. */
    public Builder source(String value) {
      values.put("source", value);
      return this;
    }

    /** Required. Source language. Allowed values: tsc, zpl, epl, cpcl, dpl, sbpl, ipl. Default: "tsc". */
    public Builder from(String value) {
      values.put("from", value);
      return this;
    }

    /** Required. Target language. Allowed values: tsc, zpl, epl, cpcl, dpl, sbpl, escpos, starprnt, ipl. Default: "tsc". */
    public Builder to(String value) {
      values.put("to", value);
      return this;
    }

    /** Override DPI on the converted output. */
    public Builder dpi(Number value) {
      values.put("dpi", value);
      return this;
    }

    /** Override print speed, 1-18. */
    public Builder speed(Number value) {
      values.put("speed", value);
      return this;
    }

    /** Override print darkness, 0-15. */
    public Builder density(Number value) {
      values.put("density", value);
      return this;
    }

    /** Override copies. Minimum 1. */
    public Builder copies(Number value) {
      values.put("copies", value);
      return this;
    }

    /** Comma-separated list of fields to keep in the response, with dot notation for nested fields (e.g. name,location.city). Does not change the credit cost. */
    public Builder fields(String value) {
      values.put("fields", value);
      return this;
    }

    /** Process the request asynchronously: the response is a job_id to poll on /v1/jobs/{id} (see waitForJob). Default: false. */
    public Builder isAsync(Boolean value) {
      values.put("is_async", value);
      return this;
    }

    /** Return HTTP 200 even when an error occurs; the error is then in the response body. Default: false. */
    public Builder softFail(Boolean value) {
      values.put("soft_fail", value);
      return this;
    }

    /** Validates required parameters and returns the immutable {@link LabelConvertParams}. */
    public LabelConvertParams build() {
      if (values.get("source") == null) {
        throw new ChartQuerySerializationException("The \"source\" parameter is required.", "source");
      }
      if (values.get("from") == null) {
        throw new ChartQuerySerializationException("The \"from\" parameter is required.", "from");
      }
      if (values.get("to") == null) {
        throw new ChartQuerySerializationException("The \"to\" parameter is required.", "to");
      }
      return new LabelConvertParams(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(values)));
    }
  }
}

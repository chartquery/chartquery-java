package com.chartquery.params;

import com.chartquery.ChartQueryParams;
import com.chartquery.ChartQuerySerializationException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parameters for {@code POST /v1/label/compile}.
 *
 * <p>Generated from the ChartQuery OpenAPI document. Do not edit by hand.
 */
public final class LabelCompileParams implements ChartQueryParams {
  private final Map<String, Object> values;

  private LabelCompileParams(Map<String, Object> values) {
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

  /** Builder for {@link LabelCompileParams}. */
  public static final class Builder {
    private final Map<String, Object> values = new LinkedHashMap<>();

    private Builder() {}

    /** Required. Label width. Required. Default unit is mm. Example: 40. Do not send query instead of width. */
    public Builder width(Number value) {
      values.put("width", value);
      return this;
    }

    /** Label height in the same unit. Example: 30. Omit for receipt / continuous mode. */
    public Builder height(Number value) {
      values.put("height", value);
      return this;
    }

    /** Measurement unit for width and height. Default mm. Use mm unless you are working in printer dots. Allowed values: mm, inch, dot. Default: "mm". */
    public Builder unit(String value) {
      values.put("unit", value);
      return this;
    }

    /** Printer DPI. Default 203. */
    public Builder dpi(Number value) {
      values.put("dpi", value);
      return this;
    }

    /** Gap between labels in mm. Ignored for receipts. */
    public Builder gap(Number value) {
      values.put("gap", value);
      return this;
    }

    /** Print speed, 1-18. */
    public Builder speed(Number value) {
      values.put("speed", value);
      return this;
    }

    /** Print darkness, 0-15. */
    public Builder density(Number value) {
      values.put("density", value);
      return this;
    }

    /** Print direction. 0 = normal, 1 = reversed. Default: 1. */
    public Builder direction(Number value) {
      values.put("direction", value);
      return this;
    }

    /** Number of copies. Minimum 1. */
    public Builder copies(Number value) {
      values.put("copies", value);
      return this;
    }

    /** Printer profile id (tsc-te210, zebra-zd420, …). */
    public Builder printer(String value) {
      values.put("printer", value);
      return this;
    }

    /** Required. Required array. Text items need type=text, content, and size (1-10 magnification). Do not use text, fontSize, or type=barcode. Positions x/y are in dots. Example item: {"type":"text","content":"Order: 1042","x":10,"y":10,"size":2}. */
    public Builder elements(List<Map<String, Object>> value) {
      values.put("elements", value);
      return this;
    }

    /** Required. Required printer language. Example: zpl or tsc. Do not omit. Allowed values: tsc, zpl, epl, cpcl, dpl, sbpl, escpos, starprnt, ipl. Default: "tsc". */
    public Builder language(String value) {
      values.put("language", value);
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

    /** Validates required parameters and returns the immutable {@link LabelCompileParams}. */
    public LabelCompileParams build() {
      if (values.get("width") == null) {
        throw new ChartQuerySerializationException("The \"width\" parameter is required.", "width");
      }
      if (values.get("elements") == null) {
        throw new ChartQuerySerializationException("The \"elements\" parameter is required.", "elements");
      }
      if (values.get("language") == null) {
        throw new ChartQuerySerializationException("The \"language\" parameter is required.", "language");
      }
      return new LabelCompileParams(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(values)));
    }
  }
}

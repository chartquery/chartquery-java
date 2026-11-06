package com.chartquery.params;

import com.chartquery.ChartQueryParams;
import com.chartquery.ChartQuerySerializationException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for {@code GET /v1/label/render/{id}}.
 *
 * <p>Generated from the ChartQuery OpenAPI document. Do not edit by hand.
 */
public final class LabelRenderParams implements ChartQueryParams {
  private final Map<String, Object> values;

  private LabelRenderParams(Map<String, Object> values) {
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

  /** Builder for {@link LabelRenderParams}. */
  public static final class Builder {
    private final Map<String, Object> values = new LinkedHashMap<>();

    private Builder() {}

    /** Required. */
    public Builder id(String value) {
      values.put("id", value);
      return this;
    }

    /** Override output format. Allowed values: svg, png. Default: "svg". */
    public Builder format(String value) {
      values.put("format", value);
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

    /** Validates required parameters and returns the immutable {@link LabelRenderParams}. */
    public LabelRenderParams build() {
      if (values.get("id") == null) {
        throw new ChartQuerySerializationException("The \"id\" parameter is required.", "id");
      }
      return new LabelRenderParams(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(values)));
    }
  }
}

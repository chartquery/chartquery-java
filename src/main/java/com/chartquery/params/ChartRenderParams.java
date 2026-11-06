package com.chartquery.params;

import com.chartquery.ChartQueryParams;
import com.chartquery.ChartQuerySerializationException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for {@code GET /v1/chart/render/{id}}.
 *
 * <p>Generated from the ChartQuery OpenAPI document. Do not edit by hand.
 */
public final class ChartRenderParams implements ChartQueryParams {
  private final Map<String, Object> values;

  private ChartRenderParams(Map<String, Object> values) {
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

  /** Builder for {@link ChartRenderParams}. */
  public static final class Builder {
    private final Map<String, Object> values = new LinkedHashMap<>();

    private Builder() {}

    /** Required. The chart ID returned in the X-Chart-Render-URL header when saving a chart. Format: cq-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx. */
    public Builder id(String value) {
      values.put("id", value);
      return this;
    }

    /** Override the chart title. */
    public Builder title(String value) {
      values.put("title", value);
      return this;
    }

    /** Override X-axis labels. Comma-separated. */
    public Builder labels(String value) {
      values.put("labels", value);
      return this;
    }

    /** Override first dataset values. Comma-separated numbers. Use data2, data3… for additional datasets. */
    public Builder data1(String value) {
      values.put("data1", value);
      return this;
    }

    /** Override output width in pixels (50–4000). */
    public Builder width(String value) {
      values.put("width", value);
      return this;
    }

    /** Override output height in pixels (50–4000). */
    public Builder height(String value) {
      values.put("height", value);
      return this;
    }

    /** Override output format. Allowed values: png, svg. Default: "png". */
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

    /** Validates required parameters and returns the immutable {@link ChartRenderParams}. */
    public ChartRenderParams build() {
      if (values.get("id") == null) {
        throw new ChartQuerySerializationException("The \"id\" parameter is required.", "id");
      }
      return new ChartRenderParams(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(values)));
    }
  }
}

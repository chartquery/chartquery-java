package com.chartquery.params;

import com.chartquery.ChartQueryParams;
import com.chartquery.ChartQuerySerializationException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for {@code GET / POST /v1/label}.
 *
 * <p>Generated from the ChartQuery OpenAPI document. Do not edit by hand.
 */
public final class LabelParams implements ChartQueryParams {
  private final Map<String, Object> values;

  private LabelParams(Map<String, Object> values) {
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

  /** Builder for {@link LabelParams}. */
  public static final class Builder {
    private final Map<String, Object> values = new LinkedHashMap<>();

    private Builder() {}

    /** Use png so ChatGPT can display the image. svg is for download or infinite scale. Allowed values: svg, png. Default: "svg". */
    public Builder format(String value) {
      values.put("format", value);
      return this;
    }

    /** If true, persist the result and return share URLs instead of an image. Leave false to get the image. Default: false. */
    public Builder share(Boolean value) {
      values.put("share", value);
      return this;
    }

    /** Required. Full label config as URL-encoded JSON. Must include width and elements. Text items use content and size (1-10), not text or fontSize. */
    public Builder query(String value) {
      values.put("query", value);
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

    /** Validates required parameters and returns the immutable {@link LabelParams}. */
    public LabelParams build() {
      if (values.get("query") == null) {
        throw new ChartQuerySerializationException("The \"query\" parameter is required.", "query");
      }
      return new LabelParams(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(values)));
    }
  }
}

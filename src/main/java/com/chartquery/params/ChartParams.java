package com.chartquery.params;

import com.chartquery.ChartQueryParams;
import com.chartquery.ChartQuerySerializationException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for {@code GET / POST /v1/chart}.
 *
 * <p>Generated from the ChartQuery OpenAPI document. Do not edit by hand.
 */
public final class ChartParams implements ChartQueryParams {
  private final Map<String, Object> values;

  private ChartParams(Map<String, Object> values) {
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

  /** Builder for {@link ChartParams}. */
  public static final class Builder {
    private final Map<String, Object> values = new LinkedHashMap<>();

    private Builder() {}

    /** Required. Full chart config as URL-encoded JSON. Pass ?version= or ?v= separately to pin the chart version. */
    public Builder query(String value) {
      values.put("query", value);
      return this;
    }

    /** Chart.js version to use. Alias: ?v=. Only v4.x is supported. Defaults to the installed version (4.5.1). Example: "4", "4.5", "4.5.1". The installed version is always returned in the X-ChartJS-Version response header. Default: "4.5.1". */
    public Builder version(String value) {
      values.put("version", value);
      return this;
    }

    /** If true, persist the result and return share URLs instead of an image. Leave false to get the image. */
    public Builder share(Boolean value) {
      values.put("share", value);
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

    /** Validates required parameters and returns the immutable {@link ChartParams}. */
    public ChartParams build() {
      if (values.get("query") == null) {
        throw new ChartQuerySerializationException("The \"query\" parameter is required.", "query");
      }
      return new ChartParams(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(values)));
    }
  }
}

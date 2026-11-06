package com.chartquery.params;

import com.chartquery.ChartQueryParams;
import com.chartquery.ChartQuerySerializationException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for {@code GET /v1/diagram/view/{id}}.
 *
 * <p>Generated from the ChartQuery OpenAPI document. Do not edit by hand.
 */
public final class DiagramViewParams implements ChartQueryParams {
  private final Map<String, Object> values;

  private DiagramViewParams(Map<String, Object> values) {
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

  /** Builder for {@link DiagramViewParams}. */
  public static final class Builder {
    private final Map<String, Object> values = new LinkedHashMap<>();

    private Builder() {}

    /** Required. The diagram ID returned in the X-Diagram-View-URL header when saving a diagram. Format: cq-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx. */
    public Builder id(String value) {
      values.put("id", value);
      return this;
    }

    /** Set share=true to persist the config and receive JSON instead of an image: { id, render_url, view_url, embed_url, expires_in }. Free plan: 3-day expiry with watermark. Paid plan: 6-month expiry, no branding. Default: false. */
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

    /** Validates required parameters and returns the immutable {@link DiagramViewParams}. */
    public DiagramViewParams build() {
      if (values.get("id") == null) {
        throw new ChartQuerySerializationException("The \"id\" parameter is required.", "id");
      }
      return new DiagramViewParams(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(values)));
    }
  }
}

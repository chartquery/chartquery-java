package com.chartquery.params;

import com.chartquery.ChartQueryParams;
import com.chartquery.ChartQuerySerializationException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for {@code GET / POST /v1/diagram/ai}.
 *
 * <p>Generated from the ChartQuery OpenAPI document. Do not edit by hand.
 */
public final class DiagramAiParams implements ChartQueryParams {
  private final Map<String, Object> values;

  private DiagramAiParams(Map<String, Object> values) {
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

  /** Builder for {@link DiagramAiParams}. */
  public static final class Builder {
    private final Map<String, Object> values = new LinkedHashMap<>();

    private Builder() {}

    /** Required. Describe your diagram in plain English (max 1000 chars). Be specific: mention the diagram type, key entities, and relationships. */
    public Builder prompt(String value) {
      values.put("prompt", value);
      return this;
    }

    /** Preferred diagram language. AI chooses if omitted. Examples: "mermaid", "d2", "plantuml", "graphviz". Allowed values: mermaid, d2, plantuml, graphviz. Default: "mermaid". */
    public Builder diagramType(String value) {
      values.put("diagram_type", value);
      return this;
    }

    /** Output format. Default: "svg". Allowed values: svg, png, jpeg, pdf. Default: "svg". */
    public Builder outputFormat(String value) {
      values.put("output_format", value);
      return this;
    }

    /** Set share=true to persist the config and receive JSON instead of an image: { id, render_url, view_url, embed_url, expires_in }. Free plan: 3-day expiry. Paid plan: 6-month expiry. Default: false. */
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

    /** Validates required parameters and returns the immutable {@link DiagramAiParams}. */
    public DiagramAiParams build() {
      if (values.get("prompt") == null) {
        throw new ChartQuerySerializationException("The \"prompt\" parameter is required.", "prompt");
      }
      return new DiagramAiParams(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(values)));
    }
  }
}

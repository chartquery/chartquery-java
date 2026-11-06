package com.chartquery;

import java.util.List;
import java.util.Map;

/**
 * Path, methods, response kind and parameter names of every documented operation.
 *
 * <p>Generated from the ChartQuery OpenAPI document. Do not edit by hand.
 */
public final class Endpoints {
  private Endpoints() {}

  /** One documented operation. */
  public record Endpoint(String path, List<String> methods, boolean json, List<String> required) {}

  /** Every documented operation keyed by its snake_case name. */
  public static final Map<String, Endpoint> ALL = Map.ofEntries(
      Map.entry("barcode", new Endpoint("/v1/barcode", List.of("GET", "POST"), false, List.of("data"))),
      Map.entry("chart", new Endpoint("/v1/chart", List.of("GET", "POST"), false, List.of("query"))),
      Map.entry("chart_ai", new Endpoint("/v1/chart/ai", List.of("GET", "POST"), true, List.of("prompt"))),
      Map.entry("chart_render", new Endpoint("/v1/chart/render/{id}", List.of("GET"), false, List.of("id"))),
      Map.entry("chart_view", new Endpoint("/v1/chart/view/{id}", List.of("GET"), true, List.of("id"))),
      Map.entry("diagram", new Endpoint("/v1/diagram", List.of("GET", "POST"), false, List.of("type", "query"))),
      Map.entry("diagram_ai", new Endpoint("/v1/diagram/ai", List.of("GET", "POST"), true, List.of("prompt"))),
      Map.entry("diagram_render", new Endpoint("/v1/diagram/render/{id}", List.of("GET"), false, List.of("id"))),
      Map.entry("diagram_view", new Endpoint("/v1/diagram/view/{id}", List.of("GET"), true, List.of("id"))),
      Map.entry("jobs", new Endpoint("/v1/jobs/{id}", List.of("GET"), true, List.of("id"))),
      Map.entry("label", new Endpoint("/v1/label", List.of("GET", "POST"), false, List.of("query"))),
      Map.entry("label_compile", new Endpoint("/v1/label/compile", List.of("POST"), true, List.of("width", "elements", "language"))),
      Map.entry("label_convert", new Endpoint("/v1/label/convert", List.of("POST"), true, List.of("source", "from", "to"))),
      Map.entry("label_parse", new Endpoint("/v1/label/parse", List.of("POST"), true, List.of("source", "language"))),
      Map.entry("label_render", new Endpoint("/v1/label/render/{id}", List.of("GET"), false, List.of("id"))),
      Map.entry("label_validate", new Endpoint("/v1/label/validate", List.of("POST"), true, List.of("source", "language"))),
      Map.entry("label_view", new Endpoint("/v1/label/view/{id}", List.of("GET"), true, List.of("id"))),
      Map.entry("qrcode", new Endpoint("/v1/qrcode", List.of("GET", "POST"), false, List.of("data"))),
      Map.entry("stats", new Endpoint("/v1/stats", List.of("GET"), true, List.of())),
      Map.entry("usage", new Endpoint("/v1/usage", List.of("GET"), true, List.of()))
  );
}

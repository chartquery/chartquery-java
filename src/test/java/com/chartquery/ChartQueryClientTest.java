package com.chartquery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.chartquery.params.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class ChartQueryClientTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  static final class Recording implements ChartQueryTransport {
    final List<Request> requests = new ArrayList<>();
    Function<Request, RawResponse> responder = request -> json(200, "{\"ok\":true}");

    @Override
    public RawResponse send(Request request) throws IOException {
      requests.add(request);
      return responder.apply(request);
    }
  }

  static RawResponse json(int status, String body) {
    return new RawResponse(
        status, Map.of("content-type", List.of("application/json; charset=utf-8")), body.getBytes(StandardCharsets.UTF_8));
  }

  private final Recording transport = new Recording();
  private final ChartQueryClient client =
      ChartQueryClient.builder().apiKey("test-key").transport(transport).retryDelay(java.time.Duration.ofMillis(1)).build();

  @Test
  void requiresApiKey() {
    assertThrows(ChartQueryConfigurationException.class, () -> ChartQueryClient.builder().apiKey(null).build());
    assertThrows(ChartQueryConfigurationException.class, () -> ChartQueryClient.builder().apiKey("   ").build());
    assertThrows(
        ChartQueryConfigurationException.class,
        () -> ChartQueryClient.builder().apiKey("k").baseUrl("ftp://example.test").build());
  }

  @Test
  void featuredOperation() throws IOException {
    transport.responder = request -> json(200, "{\"hello\":\"world\"}");
    ApiResponse response = client.chartAi(ChartAiParams.builder().prompt("Monthly revenue 2026 as a bar chart: Jan 10, Feb 14, Mar 18").build());
    assertEquals("world", response.json().get("hello").asText());
    ChartQueryTransport.Request request = transport.requests.get(0);
    assertEquals("GET", request.method());
    assertEquals("https://api.chartquery.com/v1/chart/ai", request.uri().toString().split("\\?")[0]);
    assertEquals("test-key", request.headers().get("x-api-key"));
    assertTrue(request.headers().get("User-Agent").startsWith("chartquery-java/"));
  }

  @Test
  void querySerialization() {
    ChartQueryClient local = ChartQueryClient.builder().apiKey("k").baseUrl("https://example.test/").transport(transport).build();
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("text", "a b&c");
    params.put("flag", false);
    params.put("count", 0);
    params.put("ratio", 1.5);
    params.put("whole", 2.0);
    params.put("list", List.of("x", "y"));
    params.put("nothing", null);
    local.request("GET", "/v1/anything", params);
    assertEquals(
        "https://example.test/v1/anything?text=a%20b%26c&flag=false&count=0&ratio=1.5&whole=2&list=x&list=y",
        transport.requests.get(0).uri().toString());
  }

  @Test
  void postSendsJsonBody() throws IOException {
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("query", "x");
    params.put("page", 2);
    params.put("gone", null);
    client.request("POST", "/v1/anything", params);
    ChartQueryTransport.Request request = transport.requests.get(0);
    assertEquals("POST", request.method());
    assertEquals("application/json", request.headers().get("Content-Type"));
    assertEquals(MAPPER.readTree("{\"query\":\"x\",\"page\":2}"), MAPPER.readTree(request.body()));
    assertEquals(null, request.uri().getRawQuery());
  }

  @Test
  void postKeepsGlobalParametersInQuery() throws IOException {
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("query", "x");
    params.put("is_async", true);
    params.put("fields", "a");
    client.request("POST", "/v1/anything", params);
    ChartQueryTransport.Request request = transport.requests.get(0);
    assertEquals(MAPPER.readTree("{\"query\":\"x\"}"), MAPPER.readTree(request.body()));
    assertEquals("fields=a&is_async=true", request.uri().getRawQuery());
  }

  @Test
  void pathParameters() {
    client.request("GET", "/v1/items/{id}", Map.of("id", "a/b c"));
    assertEquals("https://api.chartquery.com/v1/items/a%2Fb%20c", transport.requests.get(0).uri().toString());
    assertThrows(ChartQuerySerializationException.class, () -> client.request("GET", "/v1/items/{id}", Map.of()));
  }

  @Test
  void requiredParameters() {
    assertThrows(ChartQuerySerializationException.class, () -> ChartAiParams.builder().build());
    assertTrue(transport.requests.isEmpty());
  }

  @Test
  void unsupportedMethod() {
    assertThrows(
        ChartQueryConfigurationException.class,
        () -> client.chartRender(ChartRenderParams.builder().id("test").build(), RequestOptions.method("POST")));
  }

  @Test
  void dualOperationCanUsePost() throws IOException {
    client.barcode(BarcodeParams.builder().data("test").build(), RequestOptions.method("POST"));
    ChartQueryTransport.Request request = transport.requests.get(0);
    assertEquals("POST", request.method());
    assertEquals(null, request.uri().getRawQuery());
    assertTrue(MAPPER.readTree(request.body()).isObject());
  }

  @Test
  void everyEndpoint() throws Exception {
    Map<String, Runnable> calls = new LinkedHashMap<>();
    calls.put("barcode", () -> client.barcode(BarcodeParams.builder().data("test").build()));
    calls.put("chart", () -> client.chart(ChartParams.builder().query("test").build()));
    calls.put("chart_ai", () -> client.chartAi(ChartAiParams.builder().prompt("test").build()));
    calls.put("chart_render", () -> client.chartRender(ChartRenderParams.builder().id("test").build()));
    calls.put("chart_view", () -> client.chartView(ChartViewParams.builder().id("test").build()));
    calls.put("diagram", () -> client.diagram(DiagramParams.builder().type("mermaid").query("test").build()));
    calls.put("diagram_ai", () -> client.diagramAi(DiagramAiParams.builder().prompt("test").build()));
    calls.put("diagram_render", () -> client.diagramRender(DiagramRenderParams.builder().id("test").build()));
    calls.put("diagram_view", () -> client.diagramView(DiagramViewParams.builder().id("test").build()));
    calls.put("jobs", () -> client.jobs(JobsParams.builder().id("test").build()));
    calls.put("label", () -> client.label(LabelParams.builder().query("test").build()));
    calls.put("label_compile", () -> client.labelCompile(LabelCompileParams.builder().width(1).elements(List.of(Map.of())).language("zpl or tsc. Do not omit.").build()));
    calls.put("label_convert", () -> client.labelConvert(LabelConvertParams.builder().source("test").from("tsc").to("tsc").build()));
    calls.put("label_parse", () -> client.labelParse(LabelParseParams.builder().source("test").language("tsc or zpl.").build()));
    calls.put("label_render", () -> client.labelRender(LabelRenderParams.builder().id("test").build()));
    calls.put("label_validate", () -> client.labelValidate(LabelValidateParams.builder().source("test").language("tsc or zpl.").build()));
    calls.put("label_view", () -> client.labelView(LabelViewParams.builder().id("test").build()));
    calls.put("qrcode", () -> client.qrcode(QrcodeParams.builder().data("test").build()));
    calls.put("stats", () -> client.stats(StatsParams.builder().build()));
    calls.put("usage", () -> client.usage());
    for (Map.Entry<String, Endpoints.Endpoint> entry : Endpoints.ALL.entrySet()) {
      Runnable call = calls.get(entry.getKey());
      assertNotNull(call, entry.getKey());
      call.run();
      ChartQueryTransport.Request request = transport.requests.get(transport.requests.size() - 1);
      assertEquals(
          "https://api.chartquery.com" + entry.getValue().path().replace("{id}", "test"),
          request.uri().toString().split("\\?")[0],
          entry.getKey());
      assertEquals(entry.getValue().methods().get(0), request.method(), entry.getKey());
    }
    assertEquals(Endpoints.ALL.size(), calls.size());
  }

  @Test
  void binaryResponse() {
    transport.responder = request -> new RawResponse(200, Map.of("content-type", List.of("image/png")), new byte[] {(byte) 0x89, 'P', 'N', 'G'});
    ApiResponse response = client.request("GET", "/v1/anything", Map.of());
    assertFalse(response.isJson());
    assertEquals(4, response.bytes().length);
    assertEquals("image/png", response.contentType());
  }

  @Test
  void nonJsonSuccessForJsonOperation() {
    transport.responder = request -> new RawResponse(200, Map.of("content-type", List.of("text/html")), "<html>".getBytes(StandardCharsets.UTF_8));
    assertThrows(ChartQueryResponseDecodingException.class, () -> client.chartAi(ChartAiParams.builder().prompt("Monthly revenue 2026 as a bar chart: Jan 10, Feb 14, Mar 18").build()));
  }

  @Test
  void apiError() {
    transport.responder = request -> json(401, "{\"error\":\"Invalid API Key\"}");
    ChartQueryApiException error = assertThrows(ChartQueryApiException.class, () -> client.request("GET", "/v1/usage", Map.of()));
    assertEquals(401, error.status());
    assertEquals("Invalid API Key", error.getMessage());
    assertEquals("Invalid API Key", error.responseBody().get("error").asText());
  }

  @Test
  void nonJsonErrorBody() {
    transport.responder = request -> new RawResponse(502, Map.of(), "Bad gateway".getBytes(StandardCharsets.UTF_8));
    ChartQueryApiException error = assertThrows(ChartQueryApiException.class, () -> client.request("GET", "/v1/usage", Map.of()));
    assertEquals(502, error.status());
    assertEquals("Bad gateway", new String(error.rawResponse().body(), StandardCharsets.UTF_8));
  }

  @Test
  void transportError() {
    ChartQueryClient failing = ChartQueryClient.builder().apiKey("k").maxRetries(0).transport(request -> {
      throw new IOException("network down");
    }).build();
    assertThrows(ChartQueryTransportException.class, () -> failing.request("GET", "/v1/usage", Map.of()));
  }

  @Test
  void decodesIntoTypes() {
    transport.responder = request -> json(200, "{\"value\":42}");
    record Payload(int value) {}
    assertEquals(42, client.request("GET", "/v1/usage", Map.of()).as(Payload.class).value());
  }

  @Test
  void retriesThenSucceeds() {
    int[] calls = {0};
    ChartQueryClient flaky = ChartQueryClient.builder().apiKey("k").retryDelay(java.time.Duration.ofMillis(1)).transport(request -> {
      calls[0]++;
      if (calls[0] == 1) {
        throw new IOException("reset");
      }
      if (calls[0] == 2) {
        return new RawResponse(429, Map.of("retry-after", List.of("0"), "content-type", List.of("application/json")), "{}".getBytes(StandardCharsets.UTF_8));
      }
      return json(200, "{}");
    }).build();
    assertTrue(flaky.request("GET", "/v1/usage", Map.of()).isJson());
    assertEquals(3, calls[0]);
  }

  @Test
  void retryLimitsAndClientErrors() {
    ChartQueryClient limited = ChartQueryClient.builder().apiKey("k").transport(transport).maxRetries(2).retryDelay(java.time.Duration.ofMillis(1)).build();
    transport.responder = request -> json(503, "{}");
    assertThrows(ChartQueryApiException.class, () -> limited.request("GET", "/v1/usage", Map.of()));
    assertEquals(3, transport.requests.size());
    transport.responder = request -> json(402, "{}");
    assertThrows(ChartQueryApiException.class, () -> limited.request("GET", "/v1/usage", Map.of()));
    assertEquals(4, transport.requests.size());
    transport.responder = request -> json(500, "{}");
    assertThrows(ChartQueryApiException.class, () -> limited.request("GET", "/v1/usage", Map.of(), RequestOptions.builder().maxRetries(0).build()));
    assertEquals(5, transport.requests.size());
  }

  @Test
  void rateLimitsAreRetriedButNotQuotas() {
    Map<String, Integer> cases = new LinkedHashMap<>();
    cases.put("Rate limit exceeded: You've exceeded the rate limit on your subscription", 2);
    cases.put("Rate limit exceeded for the API key: quota daily", 1);
    cases.put("Invalid API Key", 1);
    for (Map.Entry<String, Integer> c : cases.entrySet()) {
      int[] calls = {0};
      ChartQueryClient limited = ChartQueryClient.builder().apiKey("k").retryDelay(java.time.Duration.ofMillis(1)).transport(request -> {
        calls[0]++;
        return calls[0] == 1 ? json(401, "{\"error\":\"" + c.getKey() + "\"}") : json(200, "{}");
      }).build();
      try {
        limited.request("GET", "/v1/usage", Map.of());
      } catch (ChartQueryApiException e) {
        // expected for the non-retried cases
      }
      assertEquals(c.getValue(), calls[0], c.getKey());
    }
  }

  @Test
  void acceptsGlobalParameters() {
    client.chartAi(ChartAiParams.builder().prompt("Monthly revenue 2026 as a bar chart: Jan 10, Feb 14, Mar 18").fields("a,b.c").isAsync(true).softFail(false).build());
    String query = transport.requests.get(0).uri().getRawQuery();
    assertTrue(query.contains("fields=a%2Cb.c"), query);
    assertTrue(query.contains("is_async=true"), query);
    assertTrue(query.contains("soft_fail=false"), query);
  }

  @Test
  void batchKeepsOrderAndIsolatesFailures() {
    java.util.concurrent.atomic.AtomicInteger running = new java.util.concurrent.atomic.AtomicInteger();
    java.util.concurrent.atomic.AtomicInteger peak = new java.util.concurrent.atomic.AtomicInteger();
    List<BatchResult<Integer>> results = client.batch(List.of(1, 2, 3, 4, 5, 6), 2, n -> {
      peak.accumulateAndGet(running.incrementAndGet(), Math::max);
      try {
        Thread.sleep(5);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      running.decrementAndGet();
      if (n == 4) {
        throw new IllegalStateException("boom");
      }
      return n * 10;
    });
    assertEquals(2, peak.get());
    assertEquals(6, results.size());
    assertFalse(results.get(3).ok());
    assertEquals(60, results.get(5).value());
    assertEquals(3, results.get(3).index());
  }

  @Test
  void waitForJob() {
    List<String> statuses = new ArrayList<>(List.of("Queued", "Running", "CompletedJob"));
    transport.responder = request -> json(200, "{\"status\":\"" + statuses.remove(0) + "\",\"progress\":50}");
    List<JsonNode> seen = new ArrayList<>();
    ApiResponse job = client.waitForJob("abc", java.time.Duration.ofMillis(1), java.time.Duration.ofMinutes(1), seen::add);
    assertEquals("CompletedJob", job.json().get("status").asText());
    assertEquals(3, transport.requests.size());
    assertEquals("/v1/jobs/abc", transport.requests.get(0).uri().getPath());
    assertEquals(3, seen.size());
  }

  @Test
  void waitForJobTimesOut() {
    transport.responder = request -> json(200, "{\"status\":\"Running\"}");
    assertThrows(
        ChartQueryJobTimeoutException.class,
        () -> client.waitForJob("abc", java.time.Duration.ofMillis(10), java.time.Duration.ofMillis(1), null));
  }
}

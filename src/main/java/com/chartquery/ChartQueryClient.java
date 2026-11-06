package com.chartquery;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import com.chartquery.params.*;

/**
 * Official ChartQuery API client. Create one instance and reuse it; it is thread-safe.
 *
 * <pre>{@code
 * ChartQueryClient client = ChartQueryClient.fromEnv(); // reads CHARTQUERY_API_KEY
 * ApiResponse response = client.chartAi(ChartAiParams.builder().prompt("Monthly revenue 2026 as a bar chart: Jan 10, Feb 14, Mar 18").build());
 * }</pre>
 */
public final class ChartQueryClient {
  /** Production API origin. */
  public static final String DEFAULT_BASE_URL = "https://api.chartquery.com";

  /** Default client-side deadline per request. */
  public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(120);

  /** Environment variable read by {@link #fromEnv()}. */
  public static final String API_KEY_ENVIRONMENT_VARIABLE = "CHARTQUERY_API_KEY";

  /** SDK version sent in the User-Agent header. */
  public static final String VERSION = "0.1.0";

  /** Job statuses {@link #waitForJob} stops on. */
  public static final List<String> JOB_TERMINAL_STATUSES = List.of("CompletedJob", "Error", "NotFound", "BadRequest");

  /** Tasks run at the same time by {@link #batch}. */
  public static final int DEFAULT_CONCURRENCY = 10;

  /** Retries after a network error or a 408, 429, 500, 502, 503 or 504 response. */
  public static final int MAX_RETRIES = 3;

  /** Each retry waits this many times longer than the previous one (a Retry-After header wins). */
  public static final double RETRY_BACKOFF_FACTOR = 1.5;

  /** Delay before the first retry. */
  public static final Duration DEFAULT_RETRY_DELAY = Duration.ofSeconds(1);

  /** Longest delay between two attempts. */
  public static final Duration MAX_RETRY_DELAY = Duration.ofSeconds(30);

  private static final Set<Integer> RETRYABLE_STATUSES = Set.of(408, 500, 502, 503, 504);

  private static final List<String> ERROR_MESSAGE_KEYS = List.of("error", "message", "error_message", "detail");

  private final String apiKey;
  private final String baseUrl;
  private final Duration timeout;
  private final Map<String, String> headers;
  private final ChartQueryTransport transport;
  private final int maxRetries;
  private final Duration retryDelay;

  private ChartQueryClient(Builder builder) {
    String key = builder.apiKey;
    if (key == null || key.isBlank()) {
      throw new ChartQueryConfigurationException(
          "A non-blank ChartQuery API key is required. Pass apiKey(...) or set " + API_KEY_ENVIRONMENT_VARIABLE + ".");
    }
    if (key.indexOf('\r') >= 0 || key.indexOf('\n') >= 0) {
      throw new ChartQueryConfigurationException("The ChartQuery API key cannot be used in an HTTP header.");
    }
    String base = builder.baseUrl.replaceAll("/+$", "");
    URI uri;
    try {
      uri = URI.create(base);
    } catch (IllegalArgumentException e) {
      throw new ChartQueryConfigurationException("baseUrl must be an absolute http or https URL.", e);
    }
    if (!("https".equals(uri.getScheme()) || "http".equals(uri.getScheme())) || uri.getHost() == null) {
      throw new ChartQueryConfigurationException("baseUrl must be an absolute http or https URL.");
    }
    if (builder.timeout != null && (builder.timeout.isNegative() || builder.timeout.isZero())) {
      throw new ChartQueryConfigurationException("timeout must be positive, or null to disable it.");
    }
    this.apiKey = key;
    this.baseUrl = base;
    this.timeout = builder.timeout;
    this.headers = Map.copyOf(builder.headers);
    if (builder.maxRetries < 0) {
      throw new ChartQueryConfigurationException("maxRetries must be zero or more.");
    }
    if (builder.retryDelay == null || builder.retryDelay.isNegative()) {
      throw new ChartQueryConfigurationException("retryDelay must be zero or more.");
    }
    this.maxRetries = builder.maxRetries;
    this.retryDelay = builder.retryDelay;
    this.transport = builder.transport != null ? builder.transport : ChartQueryTransport.defaultTransport();
  }

  /** Creates a client that reads the API key from {@value #API_KEY_ENVIRONMENT_VARIABLE}. */
  public static ChartQueryClient fromEnv() {
    return builder().build();
  }

  /** Creates a client with an explicit API key. */
  public static ChartQueryClient of(String apiKey) {
    return builder().apiKey(apiKey).build();
  }

  /** Returns a new builder. The API key defaults to {@value #API_KEY_ENVIRONMENT_VARIABLE}. */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Sends any request, including endpoints released after this SDK version. GET parameters travel
   * in the query string; POST parameters travel in a JSON body.
   */
  public ApiResponse request(String method, String path, Map<String, ?> params) {
    return request(method, path, params, RequestOptions.defaults());
  }

  /** {@link #request(String, String, Map)} with per-request options. */
  public ApiResponse request(String method, String path, Map<String, ?> params, RequestOptions options) {
    String verb = method == null ? "" : method.toUpperCase(Locale.ROOT);
    return send(verb, path, params == null ? Map.of() : params, options, false);
  }

  /**
   * Polls {@code GET /v1/jobs/{id}} until the job reaches a terminal status. The delay starts at {@code
   * interval} (default 3 s) and grows 1.5x per poll up to 60 s; {@code timeout} defaults to 12 hours.
   *
   * @param onProgress called with the job document after every poll, may be null
   */
  public ApiResponse waitForJob(String jobId, Duration interval, Duration timeout, Consumer<JsonNode> onProgress) {
    if (jobId == null || jobId.isBlank()) {
      throw new ChartQuerySerializationException("A non-blank job id is required.", "id");
    }
    Duration pause = interval == null ? Duration.ofSeconds(3) : interval;
    Duration deadline = timeout == null ? Duration.ofHours(12) : timeout;
    Duration maxPause = pause.compareTo(Duration.ofMinutes(1)) > 0 ? pause : Duration.ofMinutes(1);
    long started = System.nanoTime();
    while (true) {
      ApiResponse last = request("GET", "/v1/jobs/{id}", Map.of("id", jobId));
      JsonNode job = last.json();
      if (onProgress != null) {
        onProgress.accept(job);
      }
      if (JOB_TERMINAL_STATUSES.contains(job.path("status").asText(""))) {
        return last;
      }
      if (Duration.ofNanos(System.nanoTime() - started).plus(pause).compareTo(deadline) > 0) {
        throw new ChartQueryJobTimeoutException(jobId, last);
      }
      try {
        Thread.sleep(pause.toMillis());
        Duration next = Duration.ofMillis(pause.toMillis() * 3 / 2);
        pause = next.compareTo(maxPause) > 0 ? maxPause : next;
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new ChartQueryTransportException("waitForJob was interrupted.", e);
      }
    }
  }

  /** Polls a job with the default interval (3 s, growing to 60 s) and timeout (12 h). */
  public ApiResponse waitForJob(String jobId) {
    return waitForJob(jobId, null, null, null);
  }

  /**
   * Barcode. Generate a barcode image via GET. Output: SVG (default) or PNG. Supports 25+ formats: EAN-13, Code 128, ITF-14, GS1-128, postal codes and more. Credit cost: 3 credits per call.
   *
   * <p>{@code GET / POST /v1/barcode}
   */
  public ApiResponse barcode(BarcodeParams params) {
    return barcode(params, RequestOptions.defaults());
  }

  /** {@code GET / POST /v1/barcode} with per-request options. */
  public ApiResponse barcode(BarcodeParams params, RequestOptions options) {
    return call("barcode", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Chart. Render a Chart.js image via GET. Pass the full chart object as URL-encoded JSON in query. Prefer format png for ChatGPT. POST sends the same object as a JSON body, not query. Credit cost: 5 credits per call.
   *
   * <p>{@code GET / POST /v1/chart}
   */
  public ApiResponse chart(ChartParams params) {
    return chart(params, RequestOptions.defaults());
  }

  /** {@code GET / POST /v1/chart} with per-request options. */
  public ApiResponse chart(ChartParams params, RequestOptions options) {
    return call("chart", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Chart AI. Describe a chart in plain English via GET and get a rendered image back. Powered by AI. Credit cost: 10 credits per call.
   *
   * <p>{@code GET / POST /v1/chart/ai}
   */
  public ApiResponse chartAi(ChartAiParams params) {
    return chartAi(params, RequestOptions.defaults());
  }

  /** {@code GET / POST /v1/chart/ai} with per-request options. */
  public ApiResponse chartAi(ChartAiParams params, RequestOptions options) {
    return call("chart_ai", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Chart Render. Render a previously saved chart by ID. Public, no API key required. Supports URL overrides for templates (?data1=, ?labels=, ?title=…).
   *
   * <p>{@code GET /v1/chart/render/{id}}
   */
  public ApiResponse chartRender(ChartRenderParams params) {
    return chartRender(params, RequestOptions.defaults());
  }

  /** {@code GET /v1/chart/render/{id}} with per-request options. */
  public ApiResponse chartRender(ChartRenderParams params, RequestOptions options) {
    return call("chart_render", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Chart View. View a previously saved chart as an interactive HTML page. Public, no API key required. Iframe-embeddable.
   *
   * <p>{@code GET /v1/chart/view/{id}}
   */
  public ApiResponse chartView(ChartViewParams params) {
    return chartView(params, RequestOptions.defaults());
  }

  /** {@code GET /v1/chart/view/{id}} with per-request options. */
  public ApiResponse chartView(ChartViewParams params, RequestOptions options) {
    return call("chart_view", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Diagram. Render a diagram via GET. Use type (mermaid, d2, …), query (source code), and format=png for ChatGPT. POST uses diagram_type and diagram_source instead. Credit cost: 5 credits per call.
   *
   * <p>{@code GET / POST /v1/diagram}
   */
  public ApiResponse diagram(DiagramParams params) {
    return diagram(params, RequestOptions.defaults());
  }

  /** {@code GET / POST /v1/diagram} with per-request options. */
  public ApiResponse diagram(DiagramParams params, RequestOptions options) {
    return call("diagram", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Diagram AI. Describe a diagram in plain English via GET and get a rendered image back. Powered by AI. Credit cost: 10 credits per call.
   *
   * <p>{@code GET / POST /v1/diagram/ai}
   */
  public ApiResponse diagramAi(DiagramAiParams params) {
    return diagramAi(params, RequestOptions.defaults());
  }

  /** {@code GET / POST /v1/diagram/ai} with per-request options. */
  public ApiResponse diagramAi(DiagramAiParams params, RequestOptions options) {
    return call("diagram_ai", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Diagram Render. Render a previously saved diagram by ID. Public, no API key required. Supports ?format= and ?query= overrides.
   *
   * <p>{@code GET /v1/diagram/render/{id}}
   */
  public ApiResponse diagramRender(DiagramRenderParams params) {
    return diagramRender(params, RequestOptions.defaults());
  }

  /** {@code GET /v1/diagram/render/{id}} with per-request options. */
  public ApiResponse diagramRender(DiagramRenderParams params, RequestOptions options) {
    return call("diagram_render", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Diagram View. View a previously saved diagram as an HTML page. Public, no API key required. Iframe-embeddable with inline SVG rendering.
   *
   * <p>{@code GET /v1/diagram/view/{id}}
   */
  public ApiResponse diagramView(DiagramViewParams params) {
    return diagramView(params, RequestOptions.defaults());
  }

  /** {@code GET /v1/diagram/view/{id}} with per-request options. */
  public ApiResponse diagramView(DiagramViewParams params, RequestOptions options) {
    return call("diagram_view", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Jobs. Poll the status, progress (0–100) and result of a job started with is_async=true. Polling is free. Poll until status is terminal: CompletedJob, Error, NotFound or BadRequest.
   *
   * <p>{@code GET /v1/jobs/{id}}
   */
  public ApiResponse jobs(JobsParams params) {
    return jobs(params, RequestOptions.defaults());
  }

  /** {@code GET /v1/jobs/{id}} with per-request options. */
  public ApiResponse jobs(JobsParams params, RequestOptions options) {
    return call("jobs", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Label Preview. Preview a thermal label via GET. Pass the same label object as a URL-encoded JSON string in query. Inside that JSON, text items use content and size (1-10), not text or fontSize. Prefer format=png for ChatGPT. Credit cost: 3 credits per call.
   *
   * <p>{@code GET / POST /v1/label}
   */
  public ApiResponse label(LabelParams params) {
    return label(params, RequestOptions.defaults());
  }

  /** {@code GET / POST /v1/label} with per-request options. */
  public ApiResponse label(LabelParams params, RequestOptions options) {
    return call("label", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Label Compile. Compile a label to printer commands (tsc, zpl, epl, …). POST width, language, and elements. Text uses content and size (1-10), not text or fontSize. No barcode element. Example: {"width":40,"height":30,"unit":"mm","language":"zpl","element… Credit cost: 3 credits per call.
   *
   * <p>{@code POST /v1/label/compile}
   */
  public ApiResponse labelCompile(LabelCompileParams params) {
    return labelCompile(params, RequestOptions.defaults());
  }

  /** {@code POST /v1/label/compile} with per-request options. */
  public ApiResponse labelCompile(LabelCompileParams params, RequestOptions options) {
    return call("label_compile", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Label Convert. Convert printer commands from one language to another. POST source, from, and to. from is the source language (tsc, zpl, epl, …). to is the target (tsc, zpl, escpos, …). Credit cost: 5 credits per call.
   *
   * <p>{@code POST /v1/label/convert}
   */
  public ApiResponse labelConvert(LabelConvertParams params) {
    return labelConvert(params, RequestOptions.defaults());
  }

  /** {@code POST /v1/label/convert} with per-request options. */
  public ApiResponse labelConvert(LabelConvertParams params, RequestOptions options) {
    return call("label_convert", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Label Parse. Parse printer commands into structured elements. POST source and language (tsc, zpl, epl, …). Credit cost: 1 credit per call.
   *
   * <p>{@code POST /v1/label/parse}
   */
  public ApiResponse labelParse(LabelParseParams params) {
    return labelParse(params, RequestOptions.defaults());
  }

  /** {@code POST /v1/label/parse} with per-request options. */
  public ApiResponse labelParse(LabelParseParams params, RequestOptions options) {
    return call("label_parse", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Label Render. Render a previously saved label by ID. Public, no API key required. Supports ?format= override.
   *
   * <p>{@code GET /v1/label/render/{id}}
   */
  public ApiResponse labelRender(LabelRenderParams params) {
    return labelRender(params, RequestOptions.defaults());
  }

  /** {@code GET /v1/label/render/{id}} with per-request options. */
  public ApiResponse labelRender(LabelRenderParams params, RequestOptions options) {
    return call("label_render", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Label Validate. Validate printer commands. POST source (the raw commands) and language (tsc, zpl, epl, …). Credit cost: 1 credit per call.
   *
   * <p>{@code POST /v1/label/validate}
   */
  public ApiResponse labelValidate(LabelValidateParams params) {
    return labelValidate(params, RequestOptions.defaults());
  }

  /** {@code POST /v1/label/validate} with per-request options. */
  public ApiResponse labelValidate(LabelValidateParams params, RequestOptions options) {
    return call("label_validate", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Label View. View a previously saved label as an HTML page. Public, no API key required. Iframe-embeddable with inline SVG rendering.
   *
   * <p>{@code GET /v1/label/view/{id}}
   */
  public ApiResponse labelView(LabelViewParams params) {
    return labelView(params, RequestOptions.defaults());
  }

  /** {@code GET /v1/label/view/{id}} with per-request options. */
  public ApiResponse labelView(LabelViewParams params, RequestOptions options) {
    return call("label_view", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * QR Code. Generate a styled QR code via GET. Output: SVG (default) or PNG. Supports dot styles, gradients, custom corners and all QR versions. Credit cost: 3 credits per call.
   *
   * <p>{@code GET / POST /v1/qrcode}
   */
  public ApiResponse qrcode(QrcodeParams params) {
    return qrcode(params, RequestOptions.defaults());
  }

  /** {@code GET / POST /v1/qrcode} with per-request options. */
  public ApiResponse qrcode(QrcodeParams params, RequestOptions options) {
    return call("qrcode", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Stats. Infrastructure health for an engine or endpoint over the last hour. Not scoped to your account or tenant. Enabled per account: HTTP 403 means contact support to activate it.
   *
   * <p>{@code GET /v1/stats}
   */
  public ApiResponse stats(StatsParams params) {
    return stats(params, RequestOptions.defaults());
  }

  /** {@code GET /v1/stats} with per-request options. */
  public ApiResponse stats(StatsParams params, RequestOptions options) {
    return call("stats", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Usage. Return this account's Chartquery usage and credit balance. No body. Requires an API key.
   *
   * <p>{@code GET /v1/usage}
   */
  public ApiResponse usage() {
    return usage(RequestOptions.defaults());
  }

  /** {@code GET /v1/usage} with per-request options. */
  public ApiResponse usage(RequestOptions options) {
    return call("usage", Map.of(), options);
  }

  private ApiResponse call(String name, Map<String, Object> params, RequestOptions options) {
    Endpoints.Endpoint endpoint = Endpoints.ALL.get(name);
    RequestOptions opts = options == null ? RequestOptions.defaults() : options;
    String method = opts.method() == null ? endpoint.methods().get(0) : opts.method();
    if (!endpoint.methods().contains(method)) {
      throw new ChartQueryConfigurationException(
          endpoint.path() + " supports " + String.join(", ", endpoint.methods()) + ", not " + method + ".");
    }
    for (String required : endpoint.required()) {
      if (params.get(required) == null) {
        throw new ChartQuerySerializationException("The \"" + required + "\" parameter is required.", required);
      }
    }
    return send(method, endpoint.path(), params, opts, endpoint.json());
  }

  private ApiResponse send(
      String method, String path, Map<String, ?> params, RequestOptions options, boolean expectJson) {
    if (!"GET".equals(method) && !"POST".equals(method)) {
      throw new ChartQueryConfigurationException("Unsupported HTTP method: " + method + ".");
    }
    if (path == null || !path.startsWith("/")) {
      throw new ChartQueryConfigurationException("The request path must start with \"/\".");
    }
    RequestOptions opts = options == null ? RequestOptions.defaults() : options;
    Serialization.Serialized serialized = Serialization.serialize(method, path, params);
    String url = baseUrl + serialized.path() + (serialized.query().isEmpty() ? "" : "?" + serialized.query());

    Map<String, String> requestHeaders = new LinkedHashMap<>();
    requestHeaders.put("Accept", "application/json, */*;q=0.8");
    requestHeaders.put("User-Agent", "chartquery-java/" + VERSION);
    requestHeaders.putAll(headers);
    requestHeaders.putAll(opts.headers());
    requestHeaders.put("x-api-key", apiKey);
    byte[] body = null;
    if (serialized.body() != null) {
      requestHeaders.put("Content-Type", "application/json");
      body = Serialization.toJson(serialized.body());
    }
    Duration requestTimeout = opts.timeout() != null ? opts.timeout() : timeout;

    int retries = opts.maxRetries() != null ? Math.max(opts.maxRetries(), 0) : maxRetries;
    RawResponse raw;
    for (int attempt = 0; ; attempt++) {
      String retryAfter = null;
      try {
        raw = transport.send(new ChartQueryTransport.Request(method, URI.create(url), requestHeaders, body, requestTimeout));
        if (raw.status() >= 200 && raw.status() <= 299) {
          break;
        }
        if (attempt >= retries || !isRetryable(raw)) {
          throw apiException(raw);
        }
        List<String> values = raw.headers().get("retry-after");
        retryAfter = values == null || values.isEmpty() ? null : values.get(0);
      } catch (HttpTimeoutException e) {
        throw new ChartQueryTransportException("ChartQuery request timed out.", e);
      } catch (IOException e) {
        if (attempt >= retries) {
          throw new ChartQueryTransportException("ChartQuery request failed before an HTTP response was received.", e);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new ChartQueryTransportException("ChartQuery request was interrupted.", e);
      }
      pause(retryDelay(attempt, retryAfter));
    }
    ApiResponse response = new ApiResponse(raw);
    if (response.isJson()) {
      try {
        response.json();
      } catch (ChartQueryResponseDecodingException e) {
        throw new ChartQueryResponseDecodingException(
            "ChartQuery returned a successful JSON response that could not be decoded.", raw, e);
      }
    } else if (expectJson) {
      throw new ChartQueryResponseDecodingException(
          "ChartQuery returned a successful non-JSON response for a JSON operation.", raw, null);
    }
    return response;
  }

  /**
   * Server errors and plan rate limits are retried. The API reports rate limits as 401 (or 429) with a "Rate limit
   * exceeded" message; per-key quotas ("...: quota daily") do not reset within seconds.
   */
  private static boolean isRetryable(RawResponse raw) {
    if (RETRYABLE_STATUSES.contains(raw.status())) {
      return true;
    }
    if (raw.status() != 401 && raw.status() != 429) {
      return false;
    }
    String error = apiException(raw).errorMessage();
    String message = error == null ? "" : error.toLowerCase(Locale.ROOT);
    if (message.contains("quota")) {
      return false;
    }
    return message.contains("rate limit") || (raw.status() == 429 && message.isEmpty());
  }

  private Duration retryDelay(int attempt, String retryAfter) {
    if (retryAfter != null) {
      try {
        double seconds = Double.parseDouble(retryAfter.trim());
        if (seconds >= 0) {
          return Duration.ofMillis((long) (Math.min(seconds, 60) * 1000));
        }
      } catch (NumberFormatException e) {
        // An HTTP date: fall back to exponential backoff.
      }
    }
    double millis = retryDelay.toMillis() * Math.pow(RETRY_BACKOFF_FACTOR, attempt);
    return Duration.ofMillis((long) Math.min(millis, MAX_RETRY_DELAY.toMillis()));
  }

  private static void pause(Duration delay) {
    try {
      Thread.sleep(delay.toMillis());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ChartQueryTransportException("ChartQuery request was interrupted while waiting to retry.", e);
    }
  }

  /**
   * Runs {@code task} for every item on {@code concurrency} threads and returns one {@link BatchResult} per
   * item, in input order. A failing item never stops the others; the client is thread-safe.
   *
   * <pre>{@code
   * List<BatchResult<ApiResponse>> results = client.batch(ids, 5, id -> client.someEndpoint(...));
   * }</pre>
   */
  public <I, T> List<BatchResult<T>> batch(Collection<? extends I> items, int concurrency, Function<? super I, ? extends T> task) {
    if (concurrency < 1) {
      throw new ChartQueryConfigurationException("concurrency must be a positive integer.");
    }
    List<I> list = new ArrayList<>(items);
    if (list.isEmpty()) {
      return List.of();
    }
    ExecutorService pool = Executors.newFixedThreadPool(Math.min(concurrency, list.size()), runnable -> {
      Thread thread = new Thread(runnable, "chartquery-batch");
      thread.setDaemon(true);
      return thread;
    });
    try {
      List<Future<T>> futures = new ArrayList<>(list.size());
      for (I item : list) {
        futures.add(pool.submit(() -> task.apply(item)));
      }
      List<BatchResult<T>> results = new ArrayList<>(list.size());
      for (int i = 0; i < futures.size(); i++) {
        try {
          results.add(new BatchResult<>(i, futures.get(i).get(), null));
        } catch (ExecutionException e) {
          results.add(new BatchResult<>(i, null, e.getCause()));
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new ChartQueryTransportException("batch was interrupted.", e);
        }
      }
      return results;
    } finally {
      pool.shutdownNow();
    }
  }

  /** {@link #batch(Collection, int, Function)} with {@link #DEFAULT_CONCURRENCY} threads. */
  public <I, T> List<BatchResult<T>> batch(Collection<? extends I> items, Function<? super I, ? extends T> task) {
    return batch(items, DEFAULT_CONCURRENCY, task);
  }

  private static ChartQueryApiException apiException(RawResponse raw) {
    JsonNode body = null;
    String message = null;
    try {
      body = Serialization.MAPPER.readTree(raw.body());
    } catch (IOException e) {
      body = null;
    }
    if (body != null && body.isObject()) {
      for (String key : ERROR_MESSAGE_KEYS) {
        JsonNode value = body.get(key);
        if (value != null && value.isTextual() && !value.asText().isEmpty()) {
          message = value.asText();
          break;
        }
      }
    }
    return new ChartQueryApiException(raw, body, message);
  }

  /** Builder for {@link ChartQueryClient}. */
  public static final class Builder {
    private String apiKey = System.getenv(API_KEY_ENVIRONMENT_VARIABLE);
    private String baseUrl = DEFAULT_BASE_URL;
    private Duration timeout = DEFAULT_TIMEOUT;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private ChartQueryTransport transport;
    private int maxRetries = MAX_RETRIES;
    private Duration retryDelay = DEFAULT_RETRY_DELAY;

    private Builder() {}

    /** Retries after a network error or a 408/429/5xx response; 0 disables retries. Default 3. */
    public Builder maxRetries(int maxRetries) {
      this.maxRetries = maxRetries;
      return this;
    }

    /** Delay before the first retry, growing 1.5x per retry. Default 1 second. */
    public Builder retryDelay(Duration retryDelay) {
      this.retryDelay = retryDelay;
      return this;
    }

    /** API key sent in the {@code x-api-key} header. */
    public Builder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    /** API origin, for tests or proxies. */
    public Builder baseUrl(String baseUrl) {
      this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
      return this;
    }

    /** Client-side deadline per request; null disables it. */
    public Builder timeout(Duration timeout) {
      this.timeout = timeout;
      return this;
    }

    /** Header sent with every request. */
    public Builder header(String name, String value) {
      headers.put(name, value);
      return this;
    }

    /** Custom transport; defaults to {@link java.net.http.HttpClient} without redirects. */
    public Builder transport(ChartQueryTransport transport) {
      this.transport = transport;
      return this;
    }

    /** Validates the configuration and creates the client. */
    public ChartQueryClient build() {
      return new ChartQueryClient(this);
    }
  }
}

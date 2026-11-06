# ChartQuery Java SDK

The official Java SDK for the [ChartQuery](https://www.chartquery.com) chart and QR code API.

Generate chart images, diagrams, barcodes, QR codes and thermal labels on demand. PNG, SVG or PDF output.

The SDK exposes **20 typed methods**, one per documented endpoint, each with a parameter builder, plus a generic `request()`. It uses `java.net.http.HttpClient` and Jackson.

## Requirements

- Java 17 or newer

## Installation

Maven:

```xml
<dependency>
  <groupId>com.chartquery</groupId>
  <artifactId>chartquery</artifactId>
  <version>0.1.0</version>
</dependency>
```

Gradle:

```kotlin
implementation("com.chartquery:chartquery:0.1.0")
```

## Get your API key

[Sign up for ChartQuery](https://app.chartquery.com/register) or sign in, then copy your API key from the [dashboard](https://app.chartquery.com). `ChartQueryClient.fromEnv()` reads the `CHARTQUERY_API_KEY` environment variable.

## Quick start

```java
import com.chartquery.ApiResponse;
import com.chartquery.ChartQueryClient;
import com.chartquery.params.ChartAiParams;

ChartQueryClient client = ChartQueryClient.fromEnv();
ApiResponse response = client.chartAi(ChartAiParams.builder().prompt("Monthly revenue 2026 as a bar chart: Jan 10, Feb 14, Mar 18").build());
System.out.println(response.json().toPrettyString());
```

Parameter builders mirror the [API reference](https://app.chartquery.com/openapi); `build()` checks required parameters. Use `response.as(MyType.class)` to map the JSON onto your own types.

## Binary responses

Endpoints that return a file (image, PDF…) give you the bytes; `isJson()` tells both cases apart:

```java
ApiResponse response = client.qrcode(QrcodeParams.builder().data("https://chartquery.com").format("png").build());
if (!response.isJson()) {
  java.nio.file.Files.write(java.nio.file.Path.of("qrcode.png"), response.bytes());
}
```

## Async jobs

Any call accepts `is_async: true` (useful past the 60-second server limit): the response is then a `job_id`. `waitForJob` polls `GET /v1/jobs/{id}` until a terminal status (`CompletedJob`, `Error`, `NotFound`, `BadRequest`):

```java
ApiResponse submitted = client.chartAi(ChartAiParams.builder().prompt("Monthly revenue 2026 as a bar chart: Jan 10, Feb 14, Mar 18").isAsync(true).build());
ApiResponse job = client.waitForJob(
    submitted.json().get("job_id").asText(),
    Duration.ofSeconds(3),
    Duration.ofHours(12),
    progress -> System.out.println(progress.path("status").asText() + " " + progress.path("progress").asInt()));
```

## GET or POST

Methods use GET when the endpoint supports it (query string). POST sends the same parameters as JSON:

```java
client.barcode(BarcodeParams.builder().data("test").build(), RequestOptions.method("POST"));
```

## Any endpoint

```java
ApiResponse response = client.request("GET", "/v1/usage", Map.of());
```

## Errors

```java
try {
  client.chartAi(ChartAiParams.builder().prompt("Monthly revenue 2026 as a bar chart: Jan 10, Feb 14, Mar 18").build());
} catch (ChartQueryApiException e) {
  System.err.println(e.status() + ": " + e.getMessage());
}
```

| Exception | When |
| --- | --- |
| `ChartQueryConfigurationException` | Missing API key, invalid options or unsupported method. |
| `ChartQuerySerializationException` | A required parameter is missing or a value can't be sent. |
| `ChartQueryTransportException` | Network failure, timeout or interruption. |
| `ChartQueryApiException` | Non-2xx status (`status()`, `errorMessage()`, `responseBody()`, `rawResponse()`). |
| `ChartQueryResponseDecodingException` | A success response could not be decoded. |

All of them extend the unchecked `ChartQueryException`.

## Retries

Requests are retried up to 3 times after a network error, a `408`, `500`, `502`, `503` or `504`, or a rate limit — which the API returns as `401` (or `429`) with a `Rate limit exceeded` message. Per-key quota errors (`…: quota daily`) are not retried, nor are `400`, `402`, `404` or client-side timeouts. The delay starts at 1 second and grows 1.5× per retry (capped at 30 seconds); a `Retry-After` header wins.

```java
ChartQueryClient client = ChartQueryClient.builder().maxRetries(5).retryDelay(Duration.ofMillis(500)).build();
client.chartAi(ChartAiParams.builder().prompt("Monthly revenue 2026 as a bar chart: Jan 10, Feb 14, Mar 18").build(), RequestOptions.builder().maxRetries(0).build()); // one request without retries
```

## Batch requests

`batch()` runs a function per item on 10 threads (`concurrency`) and returns one `BatchResult` per item, in input order. A failing item never stops the others.

```java
List<String> inputs = List.of("Monthly revenue 2026 as a bar chart: Jan 10, Feb 14, Mar 18", "Market share as a pie chart: A 40, B 35, C 25");
List<BatchResult<ApiResponse>> results = client.batch(inputs, 5, value ->
    client.chartAi(ChartAiParams.builder().prompt(value).build()));
for (BatchResult<ApiResponse> result : results) {
  if (result.ok()) {
    System.out.println(result.value().json());
  } else {
    System.err.println(inputs.get(result.index()) + ": " + result.error().getMessage());
  }
}
```

| Constant | Default | Description |
| --- | --- | --- |
| `ChartQueryClient.DEFAULT_CONCURRENCY` | `10` | Tasks run at the same time by batch |
| `ChartQueryClient.DEFAULT_TIMEOUT` | `Duration.ofSeconds(120)` | Request timeout |
| `ChartQueryClient.MAX_RETRIES` | `3` | Retry attempts after a network error, a 408/5xx or a rate limit |
| `ChartQueryClient.RETRY_BACKOFF_FACTOR` | `1.5` | Exponential backoff multiplier |
| `ChartQueryClient.DEFAULT_RETRY_DELAY` | `Duration.ofSeconds(1)` | Delay before the first retry |

## Configuration

```java
ChartQueryClient client = ChartQueryClient.builder()
    .apiKey(System.getenv("CHARTQUERY_API_KEY"))
    .timeout(Duration.ofSeconds(60)) // default 120 s, null disables
    .header("X-Trace", "abc")
    .transport(ChartQueryTransport.fromHttpClient(myHttpClient)) // optional
    .build();
```

## Endpoints

20 methods, generated from the [OpenAPI document](https://app.chartquery.com/openapi).

| Method | HTTP | Description |
| --- | --- | --- |
| `barcode()` | GET / POST `/v1/barcode` | Generate a barcode image via GET. Output: SVG (default) or PNG. Supports 25+ formats: EAN-13, Code 128, ITF-14, GS1-128, postal codes and m… |
| `chart()` | GET / POST `/v1/chart` | Render a Chart.js image via GET. Pass the full chart object as URL-encoded JSON in query. Prefer format png for ChatGPT. POST sends the sam… |
| `chartAi()` | GET / POST `/v1/chart/ai` | Describe a chart in plain English via GET and get a rendered image back. Powered by AI. |
| `chartRender()` | GET `/v1/chart/render/{id}` | Render a previously saved chart by ID. Public, no API key required. Supports URL overrides for templates (?data1=, ?labels=, ?title=…). |
| `chartView()` | GET `/v1/chart/view/{id}` | View a previously saved chart as an interactive HTML page. Public, no API key required. Iframe-embeddable. |
| `diagram()` | GET / POST `/v1/diagram` | Render a diagram via GET. Use type (mermaid, d2, …), query (source code), and format=png for ChatGPT. POST uses diagram_type and diagram_so… |
| `diagramAi()` | GET / POST `/v1/diagram/ai` | Describe a diagram in plain English via GET and get a rendered image back. Powered by AI. |
| `diagramRender()` | GET `/v1/diagram/render/{id}` | Render a previously saved diagram by ID. Public, no API key required. Supports ?format= and ?query= overrides. |
| `diagramView()` | GET `/v1/diagram/view/{id}` | View a previously saved diagram as an HTML page. Public, no API key required. Iframe-embeddable with inline SVG rendering. |
| `jobs()` | GET `/v1/jobs/{id}` | Poll the status, progress (0–100) and result of a job started with is_async=true. Polling is free. Poll until status is terminal: Completed… |
| `label()` | GET / POST `/v1/label` | Preview a thermal label via GET. Pass the same label object as a URL-encoded JSON string in query. Inside that JSON, text items use content… |
| `labelCompile()` | POST `/v1/label/compile` | Compile a label to printer commands (tsc, zpl, epl, …). POST width, language, and elements. Text uses content and size (1-10), not text or… |
| `labelConvert()` | POST `/v1/label/convert` | Convert printer commands from one language to another. POST source, from, and to. from is the source language (tsc, zpl, epl, …). to is the… |
| `labelParse()` | POST `/v1/label/parse` | Parse printer commands into structured elements. POST source and language (tsc, zpl, epl, …). |
| `labelRender()` | GET `/v1/label/render/{id}` | Render a previously saved label by ID. Public, no API key required. Supports ?format= override. |
| `labelValidate()` | POST `/v1/label/validate` | Validate printer commands. POST source (the raw commands) and language (tsc, zpl, epl, …). |
| `labelView()` | GET `/v1/label/view/{id}` | View a previously saved label as an HTML page. Public, no API key required. Iframe-embeddable with inline SVG rendering. |
| `qrcode()` | GET / POST `/v1/qrcode` | Generate a styled QR code via GET. Output: SVG (default) or PNG. Supports dot styles, gradients, custom corners and all QR versions. |
| `stats()` | GET `/v1/stats` | Infrastructure health for an engine or endpoint over the last hour. Not scoped to your account or tenant. Enabled per account: HTTP 403 mea… |
| `usage()` | GET `/v1/usage` | Return this account's Chartquery usage and credit balance. No body. Requires an API key. |

## License

MIT — see [LICENSE](LICENSE).

package com.chartquery.examples;

import com.chartquery.ApiResponse;
import com.chartquery.ChartQueryClient;
import com.chartquery.params.ChartAiParams;
import java.time.Duration;

/** Run with CHARTQUERY_API_KEY set. */
public final class WaitForJobExample {
  private WaitForJobExample() {}

  public static void main(String[] args) throws Exception {
    ChartQueryClient client = ChartQueryClient.fromEnv();
    ApiResponse submitted = client.chartAi(ChartAiParams.builder().prompt("Monthly revenue 2026 as a bar chart: Jan 10, Feb 14, Mar 18").isAsync(true).build());
    ApiResponse job = client.waitForJob(
        submitted.json().get("job_id").asText(),
        Duration.ofSeconds(3),
        Duration.ofHours(12),
        progress -> System.out.println(progress.path("status").asText() + " " + progress.path("progress").asInt()));
    System.out.println(job.json().toPrettyString());
  }
}

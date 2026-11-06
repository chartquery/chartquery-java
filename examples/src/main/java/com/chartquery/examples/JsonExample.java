package com.chartquery.examples;

import com.chartquery.ApiResponse;
import com.chartquery.ChartQueryClient;
import com.chartquery.params.ChartAiParams;

/** Run with CHARTQUERY_API_KEY set. */
public final class JsonExample {
  private JsonExample() {}

  public static void main(String[] args) throws Exception {
    ChartQueryClient client = ChartQueryClient.fromEnv();
    ApiResponse response = client.chartAi(ChartAiParams.builder().prompt("Monthly revenue 2026 as a bar chart: Jan 10, Feb 14, Mar 18").build());
    System.out.println(response.json().toPrettyString());
  }
}

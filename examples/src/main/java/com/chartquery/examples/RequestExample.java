package com.chartquery.examples;

import com.chartquery.ApiResponse;
import com.chartquery.ChartQueryApiException;
import com.chartquery.ChartQueryClient;
import java.util.Map;

/** Run with CHARTQUERY_API_KEY set. */
public final class RequestExample {
  private RequestExample() {}

  public static void main(String[] args) throws Exception {
    ChartQueryClient client = ChartQueryClient.fromEnv();
    try {
      ApiResponse response = client.request("GET", "/v1/usage", Map.of());
      System.out.println(response.text());
    } catch (ChartQueryApiException e) {
      System.err.println("HTTP " + e.status() + ": " + e.getMessage());
    }
  }
}

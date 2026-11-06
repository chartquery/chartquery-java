package com.chartquery.examples;

import com.chartquery.ApiResponse;
import com.chartquery.ChartQueryClient;
import com.chartquery.params.QrcodeParams;
import java.nio.file.Files;
import java.nio.file.Path;

/** Run with CHARTQUERY_API_KEY set. */
public final class BinaryExample {
  private BinaryExample() {}

  public static void main(String[] args) throws Exception {
    ChartQueryClient client = ChartQueryClient.fromEnv();
    ApiResponse response = client.qrcode(QrcodeParams.builder().data("https://chartquery.com").format("png").build());
    if (response.isJson()) {
      System.out.println(response.json().toPrettyString());
    } else {
      Files.write(Path.of("qrcode.png"), response.bytes());
      System.out.println("Saved qrcode.png (" + response.contentType() + ")");
    }
  }
}

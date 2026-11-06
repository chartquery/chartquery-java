package com.chartquery;

import java.util.Map;

/** Parameters of one endpoint, in wire order. Implemented by every class in {@code com.chartquery.params}. */
public interface ChartQueryParams {
  /** Wire name to value; null values are omitted from requests. */
  Map<String, Object> toMap();
}

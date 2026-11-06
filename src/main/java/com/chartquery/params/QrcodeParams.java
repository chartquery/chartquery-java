package com.chartquery.params;

import com.chartquery.ChartQueryParams;
import com.chartquery.ChartQuerySerializationException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for {@code GET / POST /v1/qrcode}.
 *
 * <p>Generated from the ChartQuery OpenAPI document. Do not edit by hand.
 */
public final class QrcodeParams implements ChartQueryParams {
  private final Map<String, Object> values;

  private QrcodeParams(Map<String, Object> values) {
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

  /** Builder for {@link QrcodeParams}. */
  public static final class Builder {
    private final Map<String, Object> values = new LinkedHashMap<>();

    private Builder() {}

    /** Required. Data to encode in the QR code. Examples: */
    public Builder data(String value) {
      values.put("data", value);
      return this;
    }

    /** Output size in pixels (width = height). Default: 200. Examples: ?size=200 (thumbnail), ?size=300 (standard), ?size=400 (large), ?size=600 (poster). For print, use the unit param with mm/cm instead. */
    public Builder size(Number value) {
      values.put("size", value);
      return this;
    }

    /** Error correction level. Default: M. Higher levels allow more damage/obscuring before the code becomes unreadable. Allowed values: L, M, Q, H. Default: "M". */
    public Builder ecLevel(String value) {
      values.put("ec_level", value);
      return this;
    }

    /** Module (dot) shape. Default: square. Controls the visual style of each QR code module. Allowed values: square, rounded, dots, diamond, classy, classy-rounded, extra-rounded, vertical-line, horizontal-line, small-square, tiny-square. Default: "square". */
    public Builder dotType(String value) {
      values.put("dot_type", value);
      return this;
    }

    /** Module color as CSS hex. Default: #000. Examples: ?color=%236366f1 (indigo), ?color=%23dc2626 (red), ?color=%23059669 (green), ?color=%230891b2 (teal). Use %23 to URL-encode #. For gradient objects, use POST instead. */
    public Builder color(String value) {
      values.put("color", value);
      return this;
    }

    /** Overall QR code shape. Default: square. Use circle to clip the entire QR into a circle: great for profile/avatar-style codes. Example: ?shape=circle&amp;dot_type=dots Allowed values: square, circle. Default: "square". */
    public Builder shape(String value) {
      values.put("shape", value);
      return this;
    }

    /** Module size ratio between 0.1 and 1. Default: 1 (full). Values below 1 add spacing between modules for a lighter look. Example: ?dot_size=0.85 */
    public Builder dotSize(Number value) {
      values.put("dot_size", value);
      return this;
    }

    /** Background color. Default: #fff. Set to transparent for no background. Example: ?background=%230f172a for dark background. Use %23 to encode #. */
    public Builder background(String value) {
      values.put("background", value);
      return this;
    }

    /** Quiet zone width in modules around the QR code. Default: 4. Minimum recommended by the QR standard: 4. Example: ?margin=2 (compact) or ?margin=6 (extra padding). */
    public Builder margin(Number value) {
      values.put("margin", value);
      return this;
    }

    /** QR version from 1 to 40 (determines grid size and data capacity). Leave unset for auto-selection based on data length and ec_level. Example: ?version=5 */
    public Builder version(Number value) {
      values.put("version", value);
      return this;
    }

    /** Encoding mode. Default: auto (best mode selected for the data). Use numeric for digits-only, alphanumeric for uppercase + digits, byte for arbitrary text/UTF-8. Example: ?mode=numeric Allowed values: auto, numeric, alphanumeric, byte. Default: "auto". */
    public Builder mode(String value) {
      values.put("mode", value);
      return this;
    }

    /** Mask pattern from 0 to 7. Default: auto (best mask selected to minimize visual patterns). Only set manually if you need a deterministic output. Example: ?mask=3 */
    public Builder mask(Number value) {
      values.put("mask", value);
      return this;
    }

    /** Measurement unit for the size param. Default: px. Use mm, in or cm with the size param for print-ready output. Example: ?size=50&amp;unit=mm Allowed values: px, mm, in, cm. Default: "px". */
    public Builder unit(String value) {
      values.put("unit", value);
      return this;
    }

    /** Prepend an XML declaration (&lt;?xml version="1.0" encoding="UTF-8"?&gt;) to the SVG output. Useful when the SVG will be used as a standalone XML file. Default: false. Example: ?xml_declaration=true */
    public Builder xmlDeclaration(Boolean value) {
      values.put("xml_declaration", value);
      return this;
    }

    /** Accessible label added as aria-label on the SVG element. Example: ?aria_label=QR+code+linking+to+chartquery.com */
    public Builder ariaLabel(String value) {
      values.put("aria_label", value);
      return this;
    }

    /** SVG &lt;title&gt; element shown as a browser tooltip and used by screen readers. Example: ?title=ChartQuery+Website */
    public Builder title(String value) {
      values.put("title", value);
      return this;
    }

    /** SVG &lt;desc&gt; element for extended accessibility description. Example: ?desc=Scan+to+visit+https://chartquery.com */
    public Builder desc(String value) {
      values.put("desc", value);
      return this;
    }

    /** Output format. Default: svg. Use png to get a raster image. SVG is lossless and scalable; PNG is pixel-based and suitable for email, PDF or messaging. Example: ?format=png Allowed values: svg, png. Default: "svg". */
    public Builder format(String value) {
      values.put("format", value);
      return this;
    }

    /** Set share=true to persist the config and receive JSON instead of an image: { id, render_url, view_url, embed_url, expires_in }. Free plan: 3-day expiry with watermark. Paid plan: 6-month expiry, no branding. Default: false. */
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

    /** Validates required parameters and returns the immutable {@link QrcodeParams}. */
    public QrcodeParams build() {
      if (values.get("data") == null) {
        throw new ChartQuerySerializationException("The \"data\" parameter is required.", "data");
      }
      return new QrcodeParams(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(values)));
    }
  }
}

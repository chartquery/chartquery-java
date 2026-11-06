package com.chartquery.params;

import com.chartquery.ChartQueryParams;
import com.chartquery.ChartQuerySerializationException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for {@code GET / POST /v1/barcode}.
 *
 * <p>Generated from the ChartQuery OpenAPI document. Do not edit by hand.
 */
public final class BarcodeParams implements ChartQueryParams {
  private final Map<String, Object> values;

  private BarcodeParams(Map<String, Object> values) {
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

  /** Builder for {@link BarcodeParams}. */
  public static final class Builder {
    private final Map<String, Object> values = new LinkedHashMap<>();

    private Builder() {}

    /** Required. The data to encode in the barcode. Examples by type: */
    public Builder data(String value) {
      values.put("data", value);
      return this;
    }

    /** Barcode format. Default: code128. Allowed values: code128, code39, code39ext, code93, code93ext, ean13, ean8, ean5, ean2, upca, upce, itf, itf14, codabar, msi, pharmacode, code11, gs1-128, gs1-databar, gs1-databar-limited, gs1-databar-expanded, identcode, leitcode, postnet, planet…. Default: "code128". */
    public Builder type(String value) {
      values.put("type", value);
      return this;
    }

    /** Bar height in pixels. Default: 80. Typical values: 50 (compact), 80 (standard), 100–120 (tall labels). Example: ?height=100 */
    public Builder height(Number value) {
      values.put("height", value);
      return this;
    }

    /** Bar color as a CSS color string. Default: #000. Examples: ?color=%231e293b (dark slate), ?color=%230f172a (near black), ?color=%237c3aed (purple), ?color=%23dc2626 (red). Use %23 to URL-encode the # character. */
    public Builder color(String value) {
      values.put("color", value);
      return this;
    }

    /** Show human-readable text below the bars. Example: ?show_text=true. Combine with font_size and text_align for print labels. */
    public Builder showText(Boolean value) {
      values.put("show_text", value);
      return this;
    }

    /** Position of the human-readable text label. Default: bottom. Example: ?text_position=top Allowed values: bottom, top. Default: "bottom". */
    public Builder textPosition(String value) {
      values.put("text_position", value);
      return this;
    }

    /** Horizontal alignment of the text label. Default: center. Example: ?text_align=left Allowed values: center, left, right. Default: "center". */
    public Builder textAlign(String value) {
      values.put("text_align", value);
      return this;
    }

    /** Font size of the text label in pixels. Default: 14. Example: ?font_size=12 */
    public Builder fontSize(Number value) {
      values.put("font_size", value);
      return this;
    }

    /** Font family of the text label. Default: monospace. Examples: ?font_family=monospace, ?font_family=sans-serif, ?font_family=Arial */
    public Builder fontFamily(String value) {
      values.put("font_family", value);
      return this;
    }

    /** Width multiplier per module (bar). Default: 2. Increase for wider, more scannable barcodes. Example: ?bar_width=3 */
    public Builder barWidth(Number value) {
      values.put("bar_width", value);
      return this;
    }

    /** Extra horizontal spacing between bars in pixels. Default: 0. Example: ?bar_gap=1 */
    public Builder barGap(Number value) {
      values.put("bar_gap", value);
      return this;
    }

    /** Background fill color. Default: #fff. Use transparent for no background. Example: ?background=%23f8fafc */
    public Builder background(String value) {
      values.put("background", value);
      return this;
    }

    /** Uniform margin around the barcode in pixels. Default: 10. Overridden per side by margin_top/bottom/left/right. Example: ?margin=6 */
    public Builder margin(Number value) {
      values.put("margin", value);
      return this;
    }

    /** Top margin in pixels (overrides margin). Example: ?margin_top=4 */
    public Builder marginTop(Number value) {
      values.put("margin_top", value);
      return this;
    }

    /** Bottom margin in pixels (overrides margin). Example: ?margin_bottom=4 */
    public Builder marginBottom(Number value) {
      values.put("margin_bottom", value);
      return this;
    }

    /** Left margin in pixels (overrides margin). Example: ?margin_left=8 */
    public Builder marginLeft(Number value) {
      values.put("margin_left", value);
      return this;
    }

    /** Right margin in pixels (overrides margin). Example: ?margin_right=8 */
    public Builder marginRight(Number value) {
      values.put("margin_right", value);
      return this;
    }

    /** Rotate the barcode by 0, 90, 180 or 270 degrees. Default: 0. Use 90 or 270 for vertical label orientation. Example: ?rotation=90 */
    public Builder rotation(Number value) {
      values.put("rotation", value);
      return this;
    }

    /** Add horizontal bearer bars above and below the barcode. Required for ITF-14. Default: false. Example: ?bearer_bars=true */
    public Builder bearerBars(Boolean value) {
      values.put("bearer_bars", value);
      return this;
    }

    /** Measurement unit for dimensions. Default: px. Use mm, in or cm for print-ready output. Example: ?unit=mm Allowed values: px, mm, in, cm. Default: "px". */
    public Builder unit(String value) {
      values.put("unit", value);
      return this;
    }

    /** Accessible label added as aria-label on the SVG element. Improves screen reader support. Example: ?aria_label=EAN-13+barcode+for+product+4006381333931 */
    public Builder ariaLabel(String value) {
      values.put("aria_label", value);
      return this;
    }

    /** SVG &lt;title&gt; element for tooltip and accessibility. Example: ?title=Product+Barcode */
    public Builder title(String value) {
      values.put("title", value);
      return this;
    }

    /** SVG &lt;desc&gt; element for extended accessibility description. Example: ?desc=EAN-13+encoding+GTIN+for+Nivea+cream+75ml */
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

    /** Validates required parameters and returns the immutable {@link BarcodeParams}. */
    public BarcodeParams build() {
      if (values.get("data") == null) {
        throw new ChartQuerySerializationException("The \"data\" parameter is required.", "data");
      }
      return new BarcodeParams(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(values)));
    }
  }
}

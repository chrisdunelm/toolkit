package io.gapi.vgen;

import com.google.api.tools.framework.model.Diag;
import com.google.api.tools.framework.model.DiagCollector;
import com.google.api.tools.framework.model.SimpleLocation;
import com.google.api.tools.framework.model.Method;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;

/**
 * MethodConfig represents the code-gen config for a method, and includes the
 * specification of features like page streaming and parameter flattening.
 */
public class MethodConfig {

  private final PageStreamingConfig pageStreaming;
  private final FlatteningConfig flattening;
  private final String retryConfigName;

  /**
   * Creates an instance of MethodConfig based on MethodConfigProto, linking it
   * up with the provided method. On errors, null will be returned, and
   * diagnostics are reported to the diag collector.
   */
  @Nullable public static MethodConfig createMethodConfig(
      DiagCollector diagCollector,
      MethodConfigProto methodConfig,
      Method method,
      ImmutableSet<String> retryConfigNames) {

    boolean error = false;

    PageStreamingConfig pageStreaming;
    if (PageStreamingConfigProto.getDefaultInstance().equals(methodConfig.getPageStreaming())) {
      pageStreaming = null;
    } else {
      pageStreaming = PageStreamingConfig.createPageStreaming(diagCollector,
          methodConfig.getPageStreaming(), method);
      if (pageStreaming == null) {
        error = true;
      }
    }

    FlatteningConfig flattening;
    if (FlatteningConfigProto.getDefaultInstance().equals(methodConfig.getFlattening())) {
      flattening = null;
    } else {
      flattening =
          FlatteningConfig.createFlattening(diagCollector,
              methodConfig.getFlattening(), method);
      if (flattening == null) {
        error = true;
      }
    }

    String retryConfigName = methodConfig.getRetryName();
    if (!retryConfigName.isEmpty() && !retryConfigNames.contains(retryConfigName)) {
      diagCollector.addDiag(
          Diag.error(
              SimpleLocation.TOPLEVEL, "retry config used but not defined: %s", retryConfigName));
      error = true;
    }

    if (error) {
      return null;
    } else {
      return new MethodConfig(pageStreaming, flattening, retryConfigName);
    }
  }

  private MethodConfig(PageStreamingConfig pageStreaming, FlatteningConfig flattening, String retryConfigName) {
    this.pageStreaming = pageStreaming;
    this.flattening = flattening;
    this.retryConfigName = retryConfigName;
  }

  /**
   * Returns true if this method has page streaming configured.
   */
  public boolean isPageStreaming() {
    return pageStreaming != null;
  }

  /**
   * Returns the page streaming configuration of the method.
   */
  public PageStreamingConfig getPageStreaming() {
    return pageStreaming;
  }

  /**
   * Returns true if this method has flattening configured.
   */
  public boolean isFlattening() {
    return flattening != null;
  }

  /**
   * Returns the flattening configuration of the method.
   */
  public FlatteningConfig getFlattening() {
    return flattening;
  }

  /**
   * Returns the name of the retry config this method uses.
   */
  public String getRetryConfigName() {
    return retryConfigName;
  }
}
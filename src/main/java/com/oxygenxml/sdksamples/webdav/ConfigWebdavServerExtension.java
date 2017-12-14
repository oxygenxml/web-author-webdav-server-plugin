package com.oxygenxml.sdksamples.webdav;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.ecss.extensions.api.webapp.plugin.PluginConfigExtension;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
/**
 * Extension for Webdav server configuration
 */
public class ConfigWebdavServerExtension extends PluginConfigExtension {

  /**
   * Logger for logging.
   */
  private static final Logger logger = Logger.getLogger(ConfigWebdavServerExtension.class.getName());
  /**
   * Options namespace
   */
  public final static String NAMESPACE = "webdav_server_plugin_";
  /**
   * Display samples option
   */
  public final static String DISPLAY_SAMPLES = "display_samples";
  /**
   * Read-only mode option
   */
  public final static String READONLY_MODE = NAMESPACE + "readonly_mode";
  /**
   * Enforce URL option
   */
  public final static String ENFORCE_URL = NAMESPACE + "enforce_url";

  @Override
  public void init() throws ServletException {
    super.init();
    Map<String, String> defaultOptions = new HashMap<String, String>();

    defaultOptions.put(READONLY_MODE, "off");
    defaultOptions.put(ENFORCE_URL, "off");

    this.setDefaultOptions(defaultOptions);
  }

  /**
   * @return <code>true</code> if security is enabled.
   */
  public static boolean isSecurityEnabled() {
    return System.getSecurityManager() != null;
  }
  
  @Override
  public String getOptionsForm() {
    String displaySamplesOption = getOption(DISPLAY_SAMPLES, "on");
    boolean shouldDisplaySamples = "on".equals(displaySamplesOption);
    boolean readonly = "on".equals(getOption(READONLY_MODE, "off"));
    boolean enforce = "on".equals(getOption(ENFORCE_URL, "off"));
    
    PluginResourceBundle rb = ((WebappPluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace()).getResourceBundle();

    String form = "<div style='font-family:robotolight, Arial, Helvetica, sans-serif;font-size:0.85em;font-weight: lighter'>"
        + "<form style='text-align:left;line-height: 1.7em;'>"
        // Display Samples
        + "<label style='margin-bottom:6px;display:block;overflow:hidden'>"
        + "<input name='" + DISPLAY_SAMPLES + "' type='checkbox' value='on'"
        + (shouldDisplaySamples ? " checked" : "") + "> " + rb.getMessage(TranslationTags.DISPLAY_SAMPLES)
        + "</label>";
    // READONLY
    form += "<label style='margin-bottom:6px;display:block;overflow:hidden'>"
        + "<input name='" + READONLY_MODE + "' type='checkbox' value='on'"
        + (readonly ? " checked" : "") + "> " + rb.getMessage(TranslationTags.READONLY_MODE) + "</label>";
    // Enforce url
    form += "<label style='margin-bottom:6px;display:block;overflow:hidden'>"
        + "<input name='" + ENFORCE_URL + "' type='checkbox' value='on'"
        + (enforce ? " checked" : "") + "> " + rb.getMessage(TranslationTags.ENFORCE_SERVER) + "</label>"
        + "</form>" + "</div>";

    return form;
  }

  @Override
  public String getOptionsJson() {
    return
        "{" +
          "\"display_samples\": \"" + getOption(DISPLAY_SAMPLES, "on") + "\"," +
          "\"" + ENFORCE_URL + "\": \"" + getOption(ENFORCE_URL, "off") + "\"," +
          "\"" + READONLY_MODE + "\": \"" + getOption(READONLY_MODE, "off") + "\"" +
        "}";
  }
  
  @Override
  public String getPath() {
    return "webdav-server-options";
  }
}

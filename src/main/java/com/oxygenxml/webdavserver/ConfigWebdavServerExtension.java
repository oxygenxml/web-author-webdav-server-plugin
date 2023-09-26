package com.oxygenxml.webdavserver;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.ecss.extensions.api.webapp.plugin.PluginConfigExtension;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
/**
 * Extension for Webdav server configuration
 */
public class ConfigWebdavServerExtension extends PluginConfigExtension {
  /**
   * Enforce URL option
   */
  public final static String ENFORCE_URL = "webdav_server_plugin_enforce_url";

  @Override
  public void init() throws ServletException {
    super.init();
    Map<String, String> defaultOptions = new HashMap<String, String>();

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
    boolean enforce = "on".equals(getOption(ENFORCE_URL, "off"));
    
    PluginResourceBundle rb = ((WebappPluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace()).getResourceBundle();

    String form = "<div style='font-family:robotolight, Arial, Helvetica, sans-serif;font-size:0.85em;font-weight: lighter'>"
        + "<form style='text-align:left;line-height: 1.7em;'>";
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
          "\"" + ENFORCE_URL + "\": \"" + getOption(ENFORCE_URL, "off") + "\"," +
        "}";
  }
  
  @Override
  public String getPath() {
    return "webdav-server-options";
  }
}

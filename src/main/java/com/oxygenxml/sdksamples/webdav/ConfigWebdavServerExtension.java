package com.oxygenxml.sdksamples.webdav;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import ro.sync.ecss.extensions.api.webapp.plugin.PluginConfigExtension;

public class ConfigWebdavServerExtension extends PluginConfigExtension {
  
  public final static String NAMESPACE = "webdav_server_plugin_";
  
  public final static String DISPLAY_SAMPLES = "display_samples";
  public final static String READONLY_MODE = NAMESPACE + "readonly_mode";
  
  @Override
  public void init() throws ServletException {
    super.init();
    Map<String, String> defaultOptions = new HashMap<String, String>();
    
    File samplesDir = new File(getServletConfig().getServletContext().getRealPath("/"), "webdav-server/samples/");
    File gardenia = new File(samplesDir.getPath() + "/dita/flowers/topics/flowers/gardenia.dita");
    
    if(gardenia.exists()) {
      defaultOptions.put(DISPLAY_SAMPLES, "on");
    } else {
      // if the samples file does not exist by default we hide the Samples section.
      defaultOptions.put(DISPLAY_SAMPLES, "off");
      setOption(DISPLAY_SAMPLES, "off");
    }
    
    defaultOptions.put(READONLY_MODE, "off");
    
    this.setDefaultOptions(defaultOptions);
  }

  @Override
  public String getOptionsForm() {
    String displaySamplesOption = getOption(DISPLAY_SAMPLES, "on");
    boolean shouldDisplaySamples = "on".equals(displaySamplesOption);
    boolean readonly = "on".equals(getOption(READONLY_MODE, "off"));
    
    String form = "<div style='font-family:robotolight, Arial, Helvetica, sans-serif;font-size:0.85em;font-weight: lighter'>"
            + "<form style='text-align:left;line-height: 1.7em;'>"
            // Display Samples
              + "<label style='margin-bottom:6px;display:block;overflow:hidden'>"
                + "<input name='" + DISPLAY_SAMPLES + "' type='checkbox' value='on'" + 
                      (shouldDisplaySamples ? " checked" : "") + "> Display samples"
              + "</label>"
              // READONLY
              + "<label style='margin-bottom:6px;display:block;overflow:hidden'>"
                + "<input name='" + READONLY_MODE + "' type='checkbox' value='on'" + 
                         (readonly ? " checked" : "") + "> Readonly mode"
              + "</label>"
            + "</form>"
          + "</div>";
    
    return form;
  }

  @Override
  public String getOptionsJson() {
    return
        "{" +
          "\"display_samples\": \"" + getOption(DISPLAY_SAMPLES, "on") + "\"," +
          "\"" + READONLY_MODE + "\": \"" + getOption(READONLY_MODE, "off") + "\"" +
        "}";
  }

  @Override
  public String getPath() {
    return "webdav-server-options";
  }
}

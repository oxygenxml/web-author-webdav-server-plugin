package com.oxygenxml.sdksamples.webdav;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import ro.sync.ecss.extensions.api.webapp.plugin.PluginConfigExtension;

public class ConfigWebdavServerExtension extends PluginConfigExtension {
  
  public final static String DISPLAY_SAMPLES = "display_samples";
  
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
    }
    this.setDefaultOptions(defaultOptions);
  }

  @Override
  public String getOptionsForm() {
    String optionValue = getOption(DISPLAY_SAMPLES, "on");
    boolean shouldDisplaySamples = "on".equals(optionValue);
    
    return "<div style='font-family:robotolight, Arial, Helvetica, sans-serif;font-size:0.85em;font-weight: lighter'>"
            + "<form style='text-align:left;line-height: 1.7em;'>"
              + "<label style='margin-bottom:6px;display:block;overflow:hidden'>"
                + "<input name='display_samples' type='checkbox' value='on'" + 
                      (shouldDisplaySamples ? " checked" : "") + "> Display samples"
              + "</label>"
            + "</form>"
          + "</div>";
  }

  @Override
  public String getOptionsJson() {
    return
        "{" +
          "\"display_samples\": \"" + getOption(DISPLAY_SAMPLES, "on") + "\"" +
        "}";
  }

  @Override
  public String getPath() {
    return "webdav-server-options";
  }
}
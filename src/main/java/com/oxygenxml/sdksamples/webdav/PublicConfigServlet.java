package com.oxygenxml.sdksamples.webdav;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ro.sync.ecss.extensions.api.webapp.plugin.WebappServletPluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.options.WSOptionsStorage;

public class PublicConfigServlet  extends WebappServletPluginExtension {
  
  /**
   * The options storage impl.
   */
  WSOptionsStorage optionsStorage = null;
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    this.optionsStorage = PluginWorkspaceProvider.getPluginWorkspace().getOptionsStorage();
    
    resp.setStatus(HttpServletResponse.SC_OK);
    // A json object with all the set options
    resp.setCharacterEncoding("UTF-8");
    resp.getWriter().write(getPublicOptionsJson());
    resp.getWriter().flush();
  }
  
  /**
   * Returns the webdav server public options.
   * 
   * @return the public options JSon.
   */
  private String getPublicOptionsJson() {
    return "{" +
        "\"display_samples\": \"" + this.optionsStorage.getOption(ConfigWebdavServerExtension.DISPLAY_SAMPLES, "on") + "\"" +
      "}";
  }
  
  @Override
  public String getPath() {
    return "webdav-server-public-options";
  }
}

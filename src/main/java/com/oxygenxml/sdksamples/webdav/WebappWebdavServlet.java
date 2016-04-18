package com.oxygenxml.sdksamples.webdav;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ro.sync.ecss.extensions.api.webapp.plugin.WebappServletPluginExtension;

/**
 * The wrapper class for the tomcat webdav servlet.
 */
public class WebappWebdavServlet extends WebappServletPluginExtension {
  
  public static final String WEBDAV_SERVER = "webdav-server";
  private WebdavServletWrapper webdavWrapper;

  @Override
  public void init() throws ServletException {
    // wrapp the servlet config.
    this.config = new WebdavServletConfig(getServletConfig());
    
    webdavWrapper = new WebdavServletWrapper(this.getPath());
    webdavWrapper.init(this.config);
  }


  @Override
  public void service(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    webdavWrapper.service(req, resp);
  }
  
  @Override
  public String getPath() {
    return WEBDAV_SERVER;
  }
}
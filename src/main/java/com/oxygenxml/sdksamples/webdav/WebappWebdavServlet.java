package com.oxygenxml.sdksamples.webdav;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.servlets.WebdavServlet;

import ro.sync.ecss.extensions.api.webapp.plugin.WebappServletPluginExtension;

/**
 * The wrapper class for the tomcat webdav servlet.
 */
public class WebappWebdavServlet extends WebappServletPluginExtension {
  
  private WebdavServlet webdavServlet;

  @Override
  public void init() throws ServletException {
    // create the webdav workspace.
    File webdavDir = new File(
        getServletConfig().getServletContext().getRealPath(File.separator), "webdav");
    if(!webdavDir.exists()) {
      webdavDir.mkdir();
    }
    
    webdavServlet = new WebdavServlet() {
      private static final long serialVersionUID = 1L;
      // remove the servlet path from the relative path.
      @Override
      protected String getRelativePath(HttpServletRequest request) {
        String relativePath = super.getRelativePath(request);
        int pathIndex = ("/" + getPath()).length();
        // remove plugin servet path from relative path and add the workspace
        // dir as a prefix to limit browsing.
        String workDirPath = "/webdav";
        return workDirPath + relativePath.substring(pathIndex);
      }    
    };

    // wrapp the servlet config.
    this.config = new WebdavServletConfig(getServletConfig());
    webdavServlet.init(this.config);
  }

  @Override
  public void service(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    webdavServlet.service(req, resp);
  }
  
  @Override
  public String getPath() {
    return "webdav-server";
  }
}



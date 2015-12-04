package com.oxygenxml.sdksamples.webdav;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.servlets.WebdavServlet;
import org.apache.http.HttpStatus;

import ro.sync.ecss.extensions.api.webapp.plugin.WebappServletPluginExtension;

/**
 * The wrapper class for the tomcat webdav servlet.
 */
public class WebappWebdavServlet extends WebappServletPluginExtension {
  
  private static final String WEBDAV_SERVER = "webdav-server";
  private WebdavServlet webdavServlet;

  @Override
  public void init() throws ServletException {
    // create the webdav workspace.
    // the folder name must match the servletPath for filtering reasons.
    File webdavDir = new File(
        getServletConfig().getServletContext().getRealPath(File.separator), getPath());
    if(!webdavDir.exists()) {
      webdavDir.mkdir();
    }
    
    webdavServlet = new WebdavServlet() {
      private static final long serialVersionUID = 1L;
      private static final String METHOD_PROPFIND = "PROPFIND";
      // remove the servlet path from the relative path.
      @Override
      protected String getRelativePath(HttpServletRequest request) {
        String relativePath = super.getRelativePath(request);
        int pathIndex = ("/" + getPath()).length();
        // remove plugin servet path from relative path and add the workspace
        // dir as a prefix to limit browsing.
        String workDir = "/" + getPath();
        return workDir + relativePath.substring(pathIndex);
      }    
      
      @Override
      protected void service(HttpServletRequest req, HttpServletResponse resp)
          throws ServletException, IOException {
        // do not allow to list the workspace dir, only it's children should be listed.
        if(req.getMethod().equals(METHOD_PROPFIND) && 
            this.getRelativePath(req).equals("/" + WEBDAV_SERVER + "/")) {
          resp.setStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);
        } else {
          super.service(req, resp);
        }
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
    return WEBDAV_SERVER;
  }
}



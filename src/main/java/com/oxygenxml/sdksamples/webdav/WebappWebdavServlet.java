package com.oxygenxml.sdksamples.webdav;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.oxygenxml.sdksamples.webdav.handler.SamplesRootDirProvider;

import lombok.extern.slf4j.Slf4j;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.ecss.extensions.api.webapp.plugin.WebappServletPluginExtension;
import ro.sync.exml.plugin.PluginContext;

/**
 * The wrapper class for the tomcat webdav servlet.
 */
@Slf4j
public class WebappWebdavServlet extends WebappServletPluginExtension {
  
  @PluginContext
  private SamplesRootDirProvider samplesRootDirProvider;
  
  public static final String WEBDAV_SERVER = "webdav-server";
  private WebdavServletWrapper webdavWrapper;

  @Override
  public void init() throws ServletException {
    // wrapp the servlet config.
    this.config = new WebdavServletConfig(getServletConfig());
    
    File dataDir = (File) config.getServletContext().getAttribute(WebappPluginWorkspace.OXYGEN_WEBAPP_DATA_DIR);
    File samplesDir = new File(dataDir, "webdav-server/samples/");
    samplesRootDirProvider.initRootDir(samplesDir);
    
    webdavWrapper = new WebdavServletWrapper(this.getPath());
    webdavWrapper.init(this.config);
  }

  @Override
  public void service(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String method = req.getMethod();
    if (req.getHeader("X-Requested-With") == null
        && !"GET".equals(method)
        && !"PROPFIND".equals(method)) {
      String requestPath = req.getServletPath() + (req.getPathInfo() != null ? req.getPathInfo() : "");
      log.error(method + " request denied by CSRF security to " + requestPath
          + ". For more information, see: https://www.oxygenxml.com/doc/ug-waCustom/topics/wa-csrf.html");
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
   
    webdavWrapper.service(req, resp);
  }
  
  @Override
  public String getPath() {
    return WEBDAV_SERVER;
  }
}
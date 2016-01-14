package com.oxygenxml.sdksamples.webdav;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

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
  private static Map<String, String> pathsMapping;
  /**
   * plugin workspace folder.
   */
  private File webdavDir;

  @Override
  public void init() throws ServletException {
    loadMappings();
    
    // wrapp the servlet config.
    this.config = new WebdavServletConfig(getServletConfig());
    
    webdavServlet = new WebdavServlet() {
      private static final long serialVersionUID = 1L;
      private static final String METHOD_PROPFIND = "PROPFIND";

      @Override
      protected String getRelativePath(HttpServletRequest request) {
        String relativePath = super.getRelativePath(request);
        // if the request is on root, list samples
        String rootPath = "/" + getPath() + "/";
        if(relativePath.equals(rootPath)) {
          String rootMapping = pathsMapping.get("/");
          if(rootMapping != null) {
            relativePath += rootMapping;
          }
        } else if(relativePath.length() > rootPath.length()) {
          // expand mapping
          String path = relativePath.substring(rootPath.length());
          int sepIndex = path.indexOf("/");
          String pathEnd = "";
          String mappKey = path;
          if(sepIndex != -1) {
            pathEnd = mappKey.substring(sepIndex);
            mappKey = mappKey.substring(0, sepIndex);
          } else {
            mappKey = "/";
            pathEnd = "/" + path;
          }
          String value = pathsMapping.get(mappKey);
          if(value == null) {
            // if no mapping found it means it is relative to root mapping.
            value = pathsMapping.get("/") + "/" + mappKey;
          }
          relativePath = rootPath + value + pathEnd;
        }
        return relativePath;
      }    
      
      @Override
      protected void service(HttpServletRequest req, HttpServletResponse resp)
          throws ServletException, IOException {
        // do not allow to list the workspace dir, only it's children should be listed.
        if(req.getMethod().equals(METHOD_PROPFIND) && 
            this.getRelativePath(req).equals("/" + getPath() + "/")) {
          resp.setStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);
        } else {
          super.service(req, resp);
        }
      }
    };
    webdavServlet.init(this.config);
  }

  /**
   * Load the user defined folder mappings.
   */
  private void loadMappings() {
    webdavDir = new File(getServletConfig().getServletContext().getRealPath("/"),
        getPath());
    File propertiesFile = new File(webdavDir, "mapping.properties");

    if (!webdavDir.exists()) {
      webdavDir.mkdir();
      try {
        propertiesFile.createNewFile();
      } catch (IOException e) {
        System.out.println("could not create mappings.properties file");
      }
    }
    // map the samples folder to root, can be overridden in properties file.
    pathsMapping = new java.util.HashMap<String, String>();
    
    Properties properties = new Properties();
    if (propertiesFile.exists()) {
      try {
        InputStream in = new FileInputStream(propertiesFile);
        properties.load(in);
        in.close();
      } catch (IOException e) {
        System.out.println(
            "WebDAV server plugin : Unable to load the mapping.properties file.");
      }
      Enumeration<Object> keys = properties.keys();
      while (keys.hasMoreElements()) {
        String param = (String) keys.nextElement();
        pathsMapping.put(param, properties.getProperty(param));
      }
    } else {
      try {
        propertiesFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    // if there is no ROOT mapping we map to the samples folder
    // if there is no samples folder we create it.
    if(pathsMapping.get("/") == null) {
      File samplesFolder = new File(webdavDir, "samples");
      if (!samplesFolder.exists()) {
        samplesFolder.mkdir();
      }
      pathsMapping.put("/", "samples");
    }
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
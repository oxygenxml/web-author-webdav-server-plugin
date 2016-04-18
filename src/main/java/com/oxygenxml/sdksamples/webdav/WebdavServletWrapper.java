package com.oxygenxml.sdksamples.webdav;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.servlets.WebdavServlet;
import org.apache.http.HttpStatus;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.options.WSOptionsStorage;

/**
 * 
 * Wrapper for tomcat built-in webdav server class.
 *
 */
public class WebdavServletWrapper extends WebdavServlet {

  private static final long serialVersionUID = 1L;
  private static final String METHOD_PROPFIND = "PROPFIND";
  
  private Map<String, String> pathsMapping;
  
  private File webdavDir;
  private String path;
  
  WSOptionsStorage optionsStorage;
  
  /**
   * Constructor.
   * 
   * @param path the path the servlet will be mapped to.
   */
  public WebdavServletWrapper(String path) {
    this.path = path;
    
    optionsStorage = PluginWorkspaceProvider.getPluginWorkspace().getOptionsStorage();
    
    optionsStorage.addOptionListener(new ReadonlyOptionListener(this));
  }
  
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    this.loadMappings();
  }

  @Override
  protected void doLock(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    
    // disable the locking mechanism if in readOnly mode.
    if(this.readOnly) {
      resp.setStatus(HttpServletResponse.SC_OK);
    } else {
      super.doLock(req, resp);
    }
  }

  @Override
  protected String getRelativePath(HttpServletRequest request) {
    String relativePath = super.getRelativePath(request);
    // if the request is on root, list samples
    String rootPath = "/" + WebappWebdavServlet.WEBDAV_SERVER + "/";
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
        // try if another root is represented by the single path.
        String mapping = pathsMapping.get(path);
        if(mapping != null) {
          mappKey = path;
          pathEnd = "/";
        }
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
    if(req.getMethod().equals(METHOD_PROPFIND)) {
      if (this.getRelativePath(req).equals("/" + this.path + "/")) {
        // We do not support listing root folder.
        resp.setStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);
        return;
      }
      if (!req.getRequestURL().toString().endsWith("/") && readOnly) {
        // In read-only mode, we prevent locking.
        resp.setStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);
        return;
      }
    }
    super.service(req, resp);
  }

  
  /**
   * Load the user defined folder mappings from the mapping.properties file.
   */
  private void loadMappings() {
    webdavDir = new File(getServletConfig().getServletContext().getRealPath("/"),
        this.path);
    File propertiesFile = new File(webdavDir, "mapping.properties");
    if (!webdavDir.exists()) {
      webdavDir.mkdir();
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
        pathsMapping.put(param.trim(), properties.getProperty(param).trim());
      }
    } else {
      BufferedWriter writer = null;
      try {
        writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(propertiesFile)));
        writer.write("/=samples");
        writer.close();
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
    Collection<String> values = pathsMapping.values();
    for(int i = 0; i < values.size(); i++) {
      System.out.println("mapping ");
    }
  }
  
  /**
   * Setter for the readonly flag.
   * 
   * @param readonly readonly mode.
   */
  public void setReadonly(boolean readonly) {
    System.out.println("set readonly :" + readonly);
    this.readOnly = readonly;
  }
}

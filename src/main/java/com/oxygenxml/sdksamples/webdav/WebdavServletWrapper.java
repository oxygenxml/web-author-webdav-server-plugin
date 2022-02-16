package com.oxygenxml.sdksamples.webdav;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.catalina.servlets.WebdavServlet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.annotations.VisibleForTesting;

import lombok.extern.slf4j.Slf4j;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.options.WSOptionsStorage;
import ro.sync.exml.workspace.api.util.XMLUtilAccess;

/**
 * 
 * Wrapper for tomcat built-in webdav server class.
 *
 */
@Slf4j
public class WebdavServletWrapper extends WebdavServlet {

  private static final long serialVersionUID = 1L;
  private static final String METHOD_PROPFIND = "PROPFIND";

  /**
   * The HTTP status code for "locked".
   */
  private static final int SC_LOCKED = 423;
  /**
   * HTTP status code for PROPFIND response.
   */
  private static final int SC_MULTISTATUS = 207;
  
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
    
    String option = optionsStorage.getOption(ConfigWebdavServerExtension.READONLY_MODE, "off");
    this.readOnly = "on".equals(option);
    optionsStorage.addOptionListener(new ReadonlyOptionListener(this));
  }
  
  @Override
  public void init(ServletConfig config) throws ServletException {
    this.webdavDir = new File(config.getServletContext().getRealPath("/"), this.path);
    ServletContext context = config.getServletContext();
    
    // Isolate Tomcat 8.x implementation trough reflection so that it still works with Tomcat 7.0.
    if(isTomcat8()) {
      this.webdavDir = new File((File)context.getAttribute(WebappPluginWorkspace.OXYGEN_WEBAPP_DATA_DIR), "webdav-server");
      try {
        Class<?> repoManager = Class.forName("com.oxygenxml.sdksamples.webdav.repo.WebdavRepoManager");
        Method chageLocationMethod = repoManager.getMethod("changeRepoLocation", File.class, ServletContext.class);
        chageLocationMethod.invoke(null, webdavDir, context);
      } catch (ReflectiveOperationException e) {
        // The WebAuthor is running in Tomcat 7
        // so we leave the default implementation.
      }
    }
    
    super.init(config);
    this.loadMappings();
  }
  
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    // Force the download for get requests.
    String filePath = getRelativePath(request);
    if(isFile(filePath)) {
      response.setContentType("application/octet-stream");
      response.setHeader("Cache-Control", "no-cache");
      response.setHeader("Content-Description", "File-Transfer");

      String serverUrlParam = request.getParameter("serverUrl");
      boolean canAccess = serverUrlParam == null || canAccessUrl(serverUrlParam);
      if (canAccess) {
        // Set download headers.
        String fileName = getFileName(filePath);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        super.doGet(request, response);
      } else {
        response.getWriter().print("{\"trustedHostNotConfigured\": \"true\"}");
      }
    } else {
      super.doGet(request, response);
    }
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
  protected String getRelativePath(HttpServletRequest request, boolean allowEmptyPath) {
    return this.getRelativePathExpandedMappings(request);
  }
  
  @Override
  protected String getRelativePath(HttpServletRequest request) {
    return this.getRelativePathExpandedMappings(request);
  }
  
  /**
   * Computes the relative path from the request.
   * 
   * @param request the request.
   * 
   * @return the computed relative path with expanded mappings.
   */
  private String getRelativePathExpandedMappings(HttpServletRequest request) {
    String pathInfo;

    if (request.getAttribute(RequestDispatcher.INCLUDE_REQUEST_URI) != null) {
        // For includes, get the info from the attributes
        pathInfo = (String) request.getAttribute(RequestDispatcher.INCLUDE_PATH_INFO);
    } else {
        pathInfo = request.getPathInfo();
    }

    StringBuilder result = new StringBuilder();
    if (pathInfo != null) {
        result.append(pathInfo);
    }
    if (result.length() == 0) {
        result.append('/');
    }

    return adjustRelativePath(
        result.toString());
  }
  
  /**
   * Adjust the relative path to take the mappings into account.
   * 
   * @param relativePath The original relative path.
   * 
   * @return The adjusted relative path. 
   */
  private String adjustRelativePath(String relativePath) {
    log.debug("original relative path: " + relativePath);
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
    log.debug("returned relative path: " + relativePath);
    return relativePath;
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // do not allow to list the workspace dir, only it's children should be listed.
    if(req.getMethod().equals(METHOD_PROPFIND)) {
      if (this.getRelativePath(req).equals("/" + this.path + "/")) {
        // We do not support listing root folder.
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
      }
      if (!req.getRequestURL().toString().endsWith("/") && readOnly) {
        if (isOnlyTypeRequested(req.getInputStream())) {
          XMLUtilAccess xmlUtilAccess = PluginWorkspaceProvider.getPluginWorkspace().getXMLUtilAccess();
          String response = 
            "<d:multistatus xmlns:d=\"DAV:\">"
            + "<d:response>"
            +   "<d:href>" + xmlUtilAccess.escapeTextValue(this.getRelativePath(req)) + "</d:href>"
            +   "<d:propstat>"
            +     "<d:prop><d:resourcetype/></d:prop>"
            +     "<d:status>HTTP/1.1 200 OK</d:status>"
            +   "</d:propstat>"
            + "</d:response>"
            + "</d:multistatus>";
          resp.setStatus(SC_MULTISTATUS);
          resp.getWriter().print(response); // NOSOAR - The XML is properly sanitized.
        } else {
          // In read-only mode, we prevent locking.
          resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        return;
      }
    }
    super.service(req, resp);
  }


  /**
   * Check if the given URL can be accessed accordingly to the security manager.
   * @param url The url.
   * @return <code>false</code> if the URL cannote be accessed.
   */
  private boolean canAccessUrl(String url) {
    if (url != null) {
      try {
        URL serverUrl = new URL(url);
        URL selfUrl = new URL(serverUrl.getProtocol() + "://" + serverUrl.getHost() + ":" + (serverUrl.getPort() != -1 ? serverUrl.getPort() : serverUrl.getDefaultPort()));
        selfUrl.openConnection().getInputStream();
      } catch (IOException e) {
        if (e.getCause() instanceof SecurityException) {
          return false;
        } else {
          return true;
        }
      }
    } else {
      // Maybe an old plugin.
    }
    return true;
  }

  /**
   * Check if only the resource type is requested.
   * 
   * @param is The request input stream.
   * @return <code>true</code> if only the resource type is requested.
   * 
   * @throws IOException If we cannot read the input stream.
   */
  @VisibleForTesting
  static boolean isOnlyTypeRequested(InputStream is) throws IOException {
    boolean onlyType = false;
    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      dbFactory.setNamespaceAware(true);
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(is);

      NodeList props = doc.getElementsByTagNameNS("DAV:", "prop");
      if (props.getLength() == 1 && props.item(0).getNodeType() == Node.ELEMENT_NODE) {
        NodeList propChildren = props.item(0).getChildNodes();
        if (propChildren.getLength() == 1 && 
            propChildren.item(0).getLocalName().equals("resourcetype") && 
            propChildren.item(0).getNamespaceURI().equals("DAV:")) {
            onlyType = true;
        }
      }
    } catch (ParserConfigurationException | SAXException e) {
      // onlyType remains false.
    }

    return onlyType;
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if(isResourceLocked(req)) {
      resp.sendError(SC_LOCKED);
      return;
    } else if(this.readOnly) {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN);
    } else {
      String path = getRelativePath(req);
      try {
        Class<?> repoManager = Class.forName("com.oxygenxml.sdksamples.webdav.repo.WebdavRepoManager");
        Method handlePutMethod = repoManager.getMethod("handlePut", HttpServletRequest.class, String.class);
        Object wrote = handlePutMethod.invoke(null, req, path);
        if (!Boolean.TRUE.equals(wrote)) {
          // The file was not written, possibly because it was read-only in the file system.
          resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
      } catch (ReflectiveOperationException e ) {
        // The WebAuthor is running in Tomcat 7.
        super.doPut(req, resp);
      }
    }
  }
  
  /**
   * Checks if the resources was locked by another user or not.
   * 
   * @param req the resource servlet request.
   * 
   * @return whether the resource was locked by someone else or not.
   */
  private boolean isResourceLocked(HttpServletRequest req) {
    // The isLocked method from the super class is private
    // so we invoke it through reflection.
    boolean isLocked = false;
    try {
      Method isLockedMetod;
      isLockedMetod = WebdavServlet.class.getDeclaredMethod("isLocked", HttpServletRequest.class);
      isLockedMetod.setAccessible(true);
      isLocked = (Boolean)isLockedMetod.invoke(this, req);
    } catch (ReflectiveOperationException e ) {
      e.printStackTrace();
    }
    return isLocked;
  }
  
  /**
   * Load the user defined folder mappings from the mapping.properties file.
   */
  private void loadMappings() {
    File propertiesFile = new File(webdavDir, "mapping.properties");
    if (!webdavDir.exists() && !this.readOnly) {
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
        log.error("WebDAV server plugin : Unable to load the mapping.properties file.", e);
      }
      Enumeration<Object> keys = properties.keys();
      while (keys.hasMoreElements()) {
        String param = (String) keys.nextElement();
        pathsMapping.put(param.trim(), properties.getProperty(param).trim());
      }
    } else {
      if (!this.readOnly) {
        BufferedWriter writer = null;
        try {
          writer = new BufferedWriter(
              new OutputStreamWriter(new FileOutputStream(propertiesFile)));
          writer.write("/=samples");
          writer.close();
        } catch (IOException e) {
          log.error("WebDAV server plugin : Unable to write mapping.properties file.", e);
        }
      }
    }
    // if there is no ROOT mapping we map to the samples folder
    // if there is no samples folder we create it.
    File samplesFolder = new File(webdavDir, "samples");
    if(pathsMapping.get("/") == null) {
      if (!samplesFolder.exists() && !this.readOnly) {
        samplesFolder.mkdir();
      }
      pathsMapping.put("/", "samples");
    }
    String[] sampleEntries = samplesFolder.list();
    if (sampleEntries == null || sampleEntries.length == 0) {
      log.warn("Could not find any sample files in folder: " + samplesFolder.getAbsolutePath());
    }
    
    log.debug("WebDAV mappings: " + pathsMapping);
  }
  
  /**
   * Setter for the readonly flag.
   * 
   * @param readonly readonly mode.
   */
  public void setReadonly(boolean readonly) {
    log.debug("set readonly :" + readonly);
    this.readOnly = readonly;
  }
  
  /**
   * @return whether the code  is ran in a Tomcat8 environment.
   */
  private static boolean isTomcat8() {
    boolean isTomcat8 = true;
      try {
        Class.forName("org.apache.catalina.WebResourceRoot");
      } catch (ClassNotFoundException e) {
        isTomcat8 = false;
      }
    return isTomcat8;
  }
  
  /**
   * @param path the file path.
   * 
   * @return the file name.
   */
  private String getFileName(String path) {
    String fileName = "";
    try {
      Class<?> repoManager = Class.forName("com.oxygenxml.sdksamples.webdav.repo.WebdavRepoManager");
      Method getFileNameMethod = repoManager.getMethod("getFileName", String.class);
      fileName = (String)getFileNameMethod.invoke(null, path);
    } catch (ReflectiveOperationException e) {}
    return fileName;
  }
  
  /**
   * Whether the requested resources is a file.
   * 
   * @param filePath the resource path.
   * 
   * @return whether the resources is a file.
   */
  private boolean isFile(String filePath) {
    boolean isFile = false;
    try {
      Class<?> repoManager = Class.forName("com.oxygenxml.sdksamples.webdav.repo.WebdavRepoManager");
      Method isFileMethod = repoManager.getMethod("isFile", String.class);
      isFile = (Boolean)isFileMethod.invoke(null, filePath);
    } catch (ReflectiveOperationException e) {}
    
    return isFile;
  }
}

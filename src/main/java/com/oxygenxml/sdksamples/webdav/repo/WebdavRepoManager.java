package com.oxygenxml.sdksamples.webdav.repo;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.Globals;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceRoot.ResourceSetType;
import org.apache.catalina.WebResourceSet;

import ro.sync.basic.util.URLUtil;


/**
 * Class that handles the move of the repo folder to the work dir.
 * Only supported on Tomcat 8.x
 * @author mihai_coanda
 *
 */
public class WebdavRepoManager {

  private static WebResourceRoot resources;
  
  /**
   * Moves the WebDAV repo dir to the new location.
   * @param webdavDir the new webdav repo dir.
   * @param context the server context.
   */
  public static void changeRepoLocation(File webdavDir, ServletContext context) {
    resources = (WebResourceRoot)context.getAttribute(Globals.RESOURCES_ATTR);
    
    try {
      URL fileURL = new URL("file:/" + URLUtil.encodeURIComponent(webdavDir.getAbsolutePath()));
      resources.createWebResourceSet(ResourceSetType.PRE, "/webdav-server", fileURL, "/");
    } catch (MalformedURLException e) {
      // Ignore as it will never happen.
    }
  }
  
  /**
   * Handles a PUT request for the new resources.
   * 
   * @param req the PUT request.
   * @param path the repo path.
   * 
   * @return whether the handling was successful.
   * 
   * @throws IOException if something goes wrong.
   */
  public static boolean handlePut(HttpServletRequest req, String path) throws IOException {
    boolean wroteResource = false;
    WebResourceSet resourceSet = getResourceSet(path);
    ServletInputStream is = req.getInputStream();
    wroteResource = resourceSet.write(path, is, true);
    return wroteResource;
  }
  
  /**
   * Finds the resource set that contains the current path.
   * 
   * @param path the resources path.
   * 
   * @return the resource set containing the resource.
   */
  private static WebResourceSet getResourceSet(String path) {
    WebResourceSet resourceSet = null;
    WebResourceSet[] preResources = resources.getPreResources();
    for (int i = 0; i < preResources.length; i++) {
      if(preResources[i].getResource(path) != null) {
        resourceSet = preResources[i];
      }
    }
    return resourceSet;
  }
  
  /**
   * Retrieves the file name of the resource.
   * 
   * @param path the resource path
   * 
   * @return the file name.
   */
  public static String getFileName(String path) {
    WebResourceSet resourceSet = getResourceSet(path);
    WebResource resource = resourceSet.getResource(path);
    
    return resource.getName();
  }
  
  /**
   * @param path the resource path
   * 
   * @return whether the resources is a file or not.
   */
  public static boolean isFile(String path) {
    WebResourceSet resourceSet = getResourceSet(path);
    WebResource resource = resourceSet.getResource(path);
    
    return resource.isFile();
  }
}

package com.oxygenxml.sdksamples.webdav.repo;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.Globals;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceRoot.ResourceSetType;
import org.apache.catalina.WebResourceSet;

import ro.sync.util.URLUtil;

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
    ServletInputStream is = req.getInputStream();
    WebResourceSet[] preResources = resources.getPreResources();
    for (int i = 0; i < preResources.length; i++) {
      WebResourceSet resourceSet = preResources[i];
      if(resourceSet.getResource(path) != null) {
        wroteResource = resourceSet.write(path, is, true);
      }
    }
    return wroteResource;
  }
}

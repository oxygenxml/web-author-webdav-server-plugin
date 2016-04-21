package com.oxygenxml.sdksamples.webdav;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.options.WSOptionsStorage;

/**
 * Wrapper class over a ServletConfig object.
 * 
 * @author mihai_coanda
 *
 */
public class WebdavServletConfig implements ServletConfig {
  // the wrapped servlet config.
  private ServletConfig config;

  private WSOptionsStorage optionsStorage;
  
  /**
   * Represents a wrapper object over a ServletConfig, adding functionality.
   * 
   * @param config the wrapped servlet config.
   */
  public WebdavServletConfig(ServletConfig config) {
    this.config = config;
    
    // load options from storage.
    optionsStorage = PluginWorkspaceProvider.getPluginWorkspace().getOptionsStorage();
  }

  /**
   * @return the servlet name.
   */
  public String getServletName() {
    return config.getServletName();
  }

  /**
   * @return the servlet context.
   */
  public ServletContext getServletContext() {
    return config.getServletContext();
  }

  /**
   * @return the init parameter identified by the given name param.
   */
  public String getInitParameter(String name) {
    if("listings".equals(name)) {
      return "true";
    } else if("readonly".equals(name)) {
      return Boolean.toString("on".equals(optionsStorage.getOption(ConfigWebdavServerExtension.READONLY_MODE , "off")));
    } else if("debug".equals(name)) {
      return "0";
    } else {
      return config.getInitParameter(name);
    }
  }

  /**
   * @return a enumeration containing all the init params names.
   */
  public Enumeration<String> getInitParameterNames() {
    return config.getInitParameterNames();
  }
}

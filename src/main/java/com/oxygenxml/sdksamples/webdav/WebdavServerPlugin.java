package com.oxygenxml.sdksamples.webdav;

import ro.sync.exml.plugin.Plugin;
import ro.sync.exml.plugin.PluginDescriptor;

/**
 * Plugin that enables a WebDAV server in tomcat.
 */
public class WebdavServerPlugin extends Plugin {
  
  /**
   * Constructor.
   * 
   * @param descriptor The plugin descriptor.
   */
  public WebdavServerPlugin(PluginDescriptor descriptor) {
    super(descriptor);
  }
}

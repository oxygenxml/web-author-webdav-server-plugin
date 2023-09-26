package com.oxygenxml.sdksamples.webdav.handler;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import ro.sync.ecss.extensions.api.webapp.plugin.URLStreamHandlerWithContext;
import ro.sync.exml.plugin.PluginContext;
import ro.sync.exml.plugin.urlstreamhandler.URLStreamHandlerPluginExtension;

/**
 * Handles "samples" URLs.
 */
public class SamplesURLStreamHandler implements URLStreamHandlerPluginExtension {

  @PluginContext
  private SamplesRootDirProvider samplesRootDirProvider;

  @Override
  public URLStreamHandler getURLStreamHandler(String protocol) {
    if ("samples".equals(protocol)) {
      return new URLStreamHandlerWithContext() {
        @Override
        protected URLConnection openConnectionInContext(String contextId, URL url, Proxy proxy) throws IOException {
          return new SamplesURLConnection(samplesRootDirProvider.getRootDir(), url);
        }
      };
    }
    return null;
  }
}

package com.oxygenxml.sdksamples.webdav;

import ro.sync.exml.workspace.api.options.WSOptionChangedEvent;
import ro.sync.exml.workspace.api.options.WSOptionListener;
import com.oxygenxml.sdksamples.webdav.ConfigWebdavServerExtension;


/**
 * Listener for changes of the ConfigWebdavServerExtension.READONLY_MODE option.
 */
public class ReadonlyOptionListener extends WSOptionListener {
  
  private WebdavServletWrapper servletWrapper;

  public ReadonlyOptionListener(WebdavServletWrapper servletWrapper) {
    super(ConfigWebdavServerExtension.READONLY_MODE);
    this.servletWrapper = servletWrapper;
  }

  @Override
  public void optionValueChanged(WSOptionChangedEvent event) {
    boolean newValue = "on".equals(event.getNewValue());
    servletWrapper.setReadonly(newValue);
  }

}

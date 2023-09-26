package com.oxygenxml.sdksamples.webdav.handler;

import java.io.File;

/**
 * Provides the root directory directory.
 */
public class SamplesRootDirProvider {

  private File rootDir;

  /**
   * Initialize the directory.
   * @param rootDir The root directory.
   */
  public void initRootDir(File rootDir) {
    this.rootDir = rootDir;
  }

  /**
   * @return the root directory.
   */
  public File getRootDir() {
    return rootDir;
  }
}

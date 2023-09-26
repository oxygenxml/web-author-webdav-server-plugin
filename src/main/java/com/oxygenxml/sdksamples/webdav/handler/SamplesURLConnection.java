package com.oxygenxml.sdksamples.webdav.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.oxygenxml.sdksamples.webdav.ConfigWebdavServerExtension;

import ro.sync.basic.io.FileSystemUtil;
import ro.sync.ecss.extensions.api.webapp.plugin.UserActionRequiredException;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.options.WSOptionsStorage;
import ro.sync.net.protocol.FileBrowsingConnection;
import ro.sync.net.protocol.FolderEntryDescriptor;

/**
 * URL connection to a file from the samples directory.
 */
public class SamplesURLConnection extends URLConnection implements FileBrowsingConnection {

  /**
   * The samples directory.
   */
  private File rootDir;

  /**
   * True if the file is read-only.
   */
  private boolean isReadOnly;

  /**
   * Constructor.
   * @param rootDir The samples directory.
   * @param url The URL.
   */
  public SamplesURLConnection(File rootDir, URL url) {
    super(url);
    try {
      this.rootDir = rootDir.getCanonicalFile();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    
    WSOptionsStorage optionsStorage = PluginWorkspaceProvider.getPluginWorkspace().getOptionsStorage();
    String option = optionsStorage.getOption(ConfigWebdavServerExtension.READONLY_MODE, "off");
    this.isReadOnly = "on".equals(option);
  }

  private File getTargetFile() {
    File targetFile = new File(rootDir, url.getFile());
    boolean isInsideSamples;
    try {
      isInsideSamples = FileSystemUtil.isAncestor(rootDir, targetFile.getCanonicalFile());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    if (!isInsideSamples) {
      throw new IllegalArgumentException();
    }
    return targetFile;
  }

  @Override
  public List<FolderEntryDescriptor> listFolder() throws IOException, UserActionRequiredException {
    List<FolderEntryDescriptor> toReturn = new ArrayList<>();
    File[] files = getTargetFile().listFiles();
    for (File file : files) {
      Path relativize = rootDir.toPath().relativize(file.toPath());
      String relativePath = relativize.toFile().getPath().replaceAll("\\\\", "/");
      if (file.isDirectory()) {
        relativePath += "/";
      }
      toReturn.add(new FolderEntryDescriptor(relativePath));
    }
    return toReturn;
  }

  @Override
  public void connect() throws IOException {
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new FileInputStream(getTargetFile());
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    if (isReadOnly) {
      throw new IOException("sample document is read-only");
    }
    return new FileOutputStream(getTargetFile());
  }
}

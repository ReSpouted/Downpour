package org.respouted.downpour;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.respouted.downpour.connector.DefaultURLConnector;
import org.respouted.downpour.connector.URLConnector;

public class DownpourCache {
  private boolean offlineMode = false;
  
  private long maxAge = 604800000L;
  
  private File cacheDb = null;
  
  private File tempDir = null;
  
  public static final String CACHE_FILE_SUFFIX = ".downpourcache";
  
  public static final DefaultURLConnector DEFAULT_CONNECTOR = new DefaultURLConnector();
  
  public DownpourCache(File db) {
    this.cacheDb = db;
    if (db.isFile())
      throw new IllegalStateException("DB needs to be a directory"); 
    if (!db.exists())
      db.mkdirs(); 
    this.tempDir = new File(db, "temp");
    if (!this.tempDir.exists())
      this.tempDir.mkdirs(); 
  }
  
  public void cleanup() {
    if (!isOfflineMode()) {
      long currentTime = System.currentTimeMillis();
      File[] contents = this.cacheDb.listFiles();
      for (File file : contents) {
        if (file.isFile() && file.getAbsolutePath().endsWith(".downpourcache")) {
          long lastModified = file.lastModified();
          if (currentTime - getMaxAge() > lastModified)
            file.delete(); 
        } 
      } 
      contents = this.tempDir.listFiles();
      for (File file : contents) {
        if (file.isFile())
          file.delete(); 
      } 
    } 
  }
  
  public void setMaxAge(long maxAge) {
    this.maxAge = maxAge;
  }
  
  public long getMaxAge() {
    return this.maxAge;
  }
  
  public void setOfflineMode(boolean offlineMode) {
    this.offlineMode = offlineMode;
  }
  
  public boolean isOfflineMode() {
    return this.offlineMode;
  }
  
  public InputStream get(URL url, URLConnector connector, boolean force) throws NoCacheException, IOException {
    File cacheFile = getCachedFile(url);
    if (isOfflineMode()) {
      if (cacheFile.exists())
        return new FileInputStream(cacheFile); 
      throw new NoCacheException("Cache file does not contain expected content: [" + cacheFile.getPath() + "]");
    } 
    File temp = new File(this.tempDir, getCacheKey(url) + ".downpourcache");
    return connector.openURL(url, temp, cacheFile);
  }
  
  public InputStream get(URL url, URLConnector connector) throws NoCacheException, IOException {
    return get(url, connector, false);
  }
  
  public InputStream get(URL url) throws NoCacheException, IOException {
    return get(url, (URLConnector)DEFAULT_CONNECTOR);
  }
  
  public File getCachedFile(URL url) {
    return new File(this.cacheDb, getCacheKey(url) + ".downpourcache");
  }
  
  private String getCacheKey(URL url) {
    String path = url.toString();
    path = path.replaceAll("[^a-zA-Z]", "-");
    return path + '-' + url.toString().hashCode();
  }
}

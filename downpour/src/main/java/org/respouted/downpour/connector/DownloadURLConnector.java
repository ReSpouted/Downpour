package org.respouted.downpour.connector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import org.respouted.downpour.CachingInputStream;

public class DownloadURLConnector implements URLConnector {
  public InputStream openURL(URL url, File temp, File writeTo) throws IOException {
    URLConnection conn = url.openConnection();
    setHeaders(conn);
    System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19");
    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19");
    conn.connect();
    onConnected(conn);
    return (InputStream)download(conn, temp, writeTo);
  }
  
  protected CachingInputStream download(URLConnection conn, final File temp, final File writeTo) throws IOException {
    CachingInputStream cache = new CachingInputStream(conn.getInputStream(), new FileOutputStream(temp));
    cache.setExpectedBytes(conn.getContentLength());
    cache.setOnFinish(new Runnable() {
          public void run() {
            if (writeTo.exists())
              writeTo.delete(); 
            temp.renameTo(writeTo);
          }
        });
    cache.setOnFailure(new Runnable() {
          public void run() {
            temp.delete();
          }
        });
    return cache;
  }
  
  public void setHeaders(URLConnection connection) {
    connection.setConnectTimeout(5000);
    connection.setReadTimeout(5000);
  }
  
  public void onConnected(URLConnection connection) {}
}

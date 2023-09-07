package org.respouted.downpour.connector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public interface URLConnector {
  InputStream openURL(URL paramURL, File paramFile1, File paramFile2) throws IOException;
  
  void setHeaders(URLConnection paramURLConnection);
  
  void onConnected(URLConnection paramURLConnection);
}

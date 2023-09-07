package org.respouted.downpour.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

public class DefaultURLConnector extends DownloadURLConnector implements URLConnector {
  public static final DateTimeFormatter HTTP_DATE_TIME = (new DateTimeFormatterBuilder()).appendDayOfWeekShortText().appendLiteral(", ").appendDayOfMonth(2).appendLiteral(' ').appendMonthOfYearShortText().appendLiteral(' ').appendYear(4, 4).appendLiteral(' ').appendHourOfDay(2).appendLiteral(':').appendMinuteOfHour(2).appendLiteral(':').appendSecondOfMinute(2).appendLiteral(" GMT").toFormatter();
  
  public InputStream openURL(URL url, File temp, File writeTo) throws IOException {
    URLConnection conn = url.openConnection();
    HttpURLConnection httpconn = null;
    if (url.getProtocol().equalsIgnoreCase("http"))
      httpconn = (HttpURLConnection)conn; 
    DateTime modified = null;
    if (writeTo.exists()) {
      modified = new DateTime(writeTo.lastModified());
      conn.setRequestProperty("If-Modified-Since", modified.toString(HTTP_DATE_TIME));
    } 
    setHeaders(conn);
    System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19");
    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19");
    conn.connect();
    onConnected(conn);
    if (httpconn != null && httpconn.getResponseCode() == 304) {
      try {
        conn.getInputStream().close();
      } catch (IOException ignore) {}
      try {
        conn.getOutputStream().close();
      } catch (IOException ignore) {}
      return new FileInputStream(writeTo);
    } 
    if (modified != null) {
      long i = conn.getHeaderFieldDate("Last-Modified", -1L);
      DateTime serverModified = new DateTime(i, DateTimeZone.forOffsetHours(0));
      if (serverModified.isBefore((ReadableInstant)modified) || serverModified.isEqual((ReadableInstant)modified)) {
        try {
          conn.getInputStream().close();
        } catch (IOException ignore) {}
        try {
          conn.getOutputStream().close();
        } catch (IOException ignore) {}
        return new FileInputStream(writeTo);
      } 
    } 
    return (InputStream)download(conn, temp, writeTo);
  }
}

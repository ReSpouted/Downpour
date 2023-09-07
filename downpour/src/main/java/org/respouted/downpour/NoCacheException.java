package org.respouted.downpour;

import java.io.IOException;

public class NoCacheException extends IOException {
  private static final long serialVersionUID = 1L;
  
  private final String message;
  
  public NoCacheException(String message) {
    this.message = message;
  }
  
  public String getMessage() {
    return this.message;
  }
}

package org.respouted.downpour;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class CachingInputStream extends InputStream {
  private InputStream readFrom = null;
  
  private OutputStream writeTo = null;
  
  private ByteBuffer buffer = ByteBuffer.allocate(1024);
  
  private Runnable onFinish = null;
  
  private Runnable onFailure = null;
  
  private long expectedBytes = -1L;
  
  private long receivedBytes = 0L;
  
  private boolean closed = false;
  
  private boolean exception = false;
  
  public CachingInputStream(InputStream readFrom, OutputStream writeTo) {
    this.readFrom = readFrom;
    this.writeTo = writeTo;
  }
  
  public void setOnFinish(Runnable onFinish) {
    this.onFinish = onFinish;
  }
  
  public void setOnFailure(Runnable onFailure) {
    this.onFailure = onFailure;
  }
  
  public synchronized void setExpectedBytes(long expectedBytes) {
    this.expectedBytes = expectedBytes;
  }
  
  public synchronized long getReceivedBytes() {
    return this.receivedBytes;
  }
  
  public long getExpectedBytes() {
    return this.expectedBytes;
  }
  
  public synchronized int read() throws IOException {
    int data = Integer.MAX_VALUE;
    try {
      data = this.readFrom.read();
      this.receivedBytes++;
      if (data == -1) {
        this.receivedBytes--;
        return data;
      } 
      if (!this.buffer.hasRemaining()) {
        this.writeTo.write(this.buffer.array(), 0, this.buffer.capacity());
        this.buffer.position(0);
      } 
      this.buffer.put((byte)data);
      return data;
    } catch (IOException e) {
      this.exception = true;
      throw e;
    } 
  }
  
  public void close() throws IOException {
    if (!this.closed) {
      this.closed = true;
      this.readFrom.close();
      super.close();
      try {
        if (this.buffer != null) {
          this.writeTo.write(this.buffer.array(), 0, this.buffer.position());
          this.buffer = null;
        } 
        this.writeTo.close();
      } catch (IOException e) {
        this.exception = true;
        throw e;
      } finally {
        if (this.expectedBytes != -1L || !this.exception)
          if (this.expectedBytes == this.receivedBytes || this.expectedBytes == -1L || !this.exception) {
            if (this.onFinish != null)
              try {
                this.onFinish.run();
              } catch (Exception e) {
                e.printStackTrace();
              }  
          } else {
            if (this.onFailure != null)
              try {
                this.onFailure.run();
              } catch (Exception e) {
                e.printStackTrace();
              }  
            throw new IOException("File was not completely downloaded! Expected=" + getExpectedBytes() + " actual=" + getReceivedBytes());
          }  
      } 
    } 
  }
  
  public int available() throws IOException {
    return this.readFrom.available();
  }
  
  public synchronized void mark(int readlimit) {
    this.readFrom.mark(readlimit);
  }
  
  public boolean markSupported() {
    return this.readFrom.markSupported();
  }
  
  public synchronized void reset() throws IOException {
    this.readFrom.reset();
  }
  
  public long skip(long n) throws IOException {
    return this.readFrom.skip(n);
  }
}

package ttc15.tranj.examples;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SynthesizedURLDownload {
  private final Logger __logger = LoggerFactory
      .getLogger(SynthesizedURLDownload.class);
  private long __getCacheLastAccessed = 0;
  private byte[] __getCachedContent = null;

  private final URL url;

  public SynthesizedURLDownload(String url) throws MalformedURLException {
    this.url = new URL(url);
  }

  public byte[] get() throws IOException {
    long __entryTime = System.currentTimeMillis();
    if (__logger.isTraceEnabled()) {
      __logger.trace(String.format("get() [url='%s']: entry", url));
    }

    if (System.currentTimeMillis() - __getCacheLastAccessed < 1000
        && __getCachedContent != null) {
      if (__logger.isTraceEnabled()) {
        __logger.trace(String.format("get(): exit [%d ms]",
            System.currentTimeMillis() - __entryTime));
      }
      return __getCachedContent;
    }

    int __retryCount = 0;
    while (true) {
      try {
        InputStream input = null;
        try {
          input = url.openStream();

          ByteArrayOutputStream buffer = new ByteArrayOutputStream();
          byte[] chunk = new byte[4 * 1024];
          int n;

          while ((n = input.read(chunk)) > 0) {
            buffer.write(chunk, 0, n);
          }

          __getCachedContent = buffer.toByteArray();
          __getCacheLastAccessed = System.currentTimeMillis();

          if (__logger.isTraceEnabled()) {
            __logger.trace(String.format("get(): exit [%d ms]",
                System.currentTimeMillis() - __entryTime));
          }
          return __getCachedContent;
        } finally {
          if (input != null) {
            input.close();
          }
        }
      } catch (UnknownHostException e) {
        __logger.error("get(): exception", e);
        if (__logger.isTraceEnabled()) {
          __logger.trace(String.format("get(): exit [%d ms]",
              System.currentTimeMillis() - __entryTime));
        }
        throw e;
      } catch (SocketTimeoutException e) {
        __logger.error("get(): exception", e);

        __retryCount += 1;

        if (__retryCount > 3) {
          if (__logger.isTraceEnabled()) {
            __logger.trace(String.format("get(): exit [%d ms]",
                System.currentTimeMillis() - __entryTime));
          }
          throw e;
        } else {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e1) {
            __logger.error("get(): exception", e);
            if (__logger.isTraceEnabled()) {
              __logger.trace(String.format("get(): exit [%d ms]",
                  System.currentTimeMillis() - __entryTime));
            }
            throw e;
          }
        }
      }
    }
  }
}

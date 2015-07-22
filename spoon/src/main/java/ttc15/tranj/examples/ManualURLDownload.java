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

public class ManualURLDownload {
  private static final long CACHE_TIMEOUT = 1000;
  private static final int MAX_RETRY = 3;

  private final Logger logger = LoggerFactory
      .getLogger(ManualURLDownload.class);

  private long lastAccessed = 0;
  private byte[] cachedContent = null;
  private final URL url;

  public ManualURLDownload(String url) throws MalformedURLException {
    this.url = new URL(url);
  }

  public byte[] get() throws IOException {
    long entryTime = System.currentTimeMillis();

    if (logger.isTraceEnabled()) {
      logger.trace(String.format("get() [url='%s']: entry", url));
    }

    if (System.currentTimeMillis() - lastAccessed < CACHE_TIMEOUT
        && cachedContent != null) {
      if (logger.isTraceEnabled()) {
        logger.trace(String.format("get(): exit cached [%d ms]",
            System.currentTimeMillis() - entryTime));
      }
      return cachedContent;
    }

    int retryCount = 0;

    while (true) {
      InputStream input = null;
      try {
        try {
          input = url.openStream();

          ByteArrayOutputStream buffer = new ByteArrayOutputStream();
          byte[] chunk = new byte[4 * 1024];
          int n;

          while ((n = input.read(chunk)) > 0) {
            buffer.write(chunk, 0, n);
          }

          cachedContent = buffer.toByteArray();
          lastAccessed = System.currentTimeMillis();

          if (logger.isTraceEnabled()) {
            logger.trace(String.format("get(): exit [%d ms]",
                System.currentTimeMillis() - entryTime));
          }
          return cachedContent;
        } finally {
          if (input != null) {
            input.close();
          }
        }
      } catch (UnknownHostException e) {
        logger.error("get(): exception -> forced exit", e);
        throw e;
      } catch (SocketTimeoutException e) {
        if (logger.isWarnEnabled()) {
          logger.warn("get(): exception -> retrying", e);
        }

        retryCount += 1;

        if (retryCount > MAX_RETRY) {
          if (logger.isTraceEnabled()) {
            logger.trace("get(): max retry count reached");
          }
          throw e;
        } else {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e1) {
            if (logger.isWarnEnabled()) {
              logger.warn("get(): interrupted while waiting to retry");
            }
            throw e;
          }
          if (logger.isTraceEnabled()) {
            logger.trace("get(): retrying");
          }
        }
      } finally {
        if (input != null) {
          input.close();
        }
      }
    }
  }
}

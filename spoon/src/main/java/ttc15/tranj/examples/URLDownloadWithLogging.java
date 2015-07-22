package ttc15.tranj.examples;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class URLDownloadWithLogging {
  private final URL url;
  private final org.slf4j.Logger __logger = org.slf4j.LoggerFactory
      .getLogger(URLDownloadWithCache.class);

  public URLDownloadWithLogging(String url) throws MalformedURLException {
    this.url = new URL(url);
  }

  public byte[] get() throws IOException {
    long __entryTime = System.currentTimeMillis();

    if (__logger.isTraceEnabled()) {
      __logger.trace(String.format("get() [url='%s']: entry", url));
    }

    while (true) {
      InputStream input = null;
      try {
        input = url.openStream();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4 * 1024];
        int n;

        while ((n = input.read(chunk)) > 0) {
          buffer.write(chunk, 0, n);
        }

        if (__logger.isTraceEnabled()) {
          __logger.trace(String.format("get(): exit [%d ms]",
              System.currentTimeMillis() - __entryTime));
        }
        return buffer.toByteArray();
      } finally {
        if (input != null) {
          input.close();
        }
      }
    }
  }
}

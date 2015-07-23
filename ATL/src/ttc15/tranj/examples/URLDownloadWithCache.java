package ttc15.tranj.examples;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class URLDownloadWithCache {
  private final URL url;

  private long __getCacheLastAccessed = 0;
  private byte[] __getCacheContent = null;

  public URLDownloadWithCache(String url) throws MalformedURLException {
    this.url = new URL(url);
  }

  public byte[] get() throws IOException {
    if (System.currentTimeMillis() - __getCacheLastAccessed < 1000
        && __getCacheContent != null) {
      return __getCacheContent;
    }

    InputStream input = null;
    try {
      input = url.openStream();

      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      byte[] chunk = new byte[4 * 1024];
      int n;

      while ((n = input.read(chunk)) > 0) {
        buffer.write(chunk, 0, n);
      }

      __getCacheContent = buffer.toByteArray();
      __getCacheLastAccessed = System.currentTimeMillis();

      return __getCacheContent;
    } finally {
      if (input != null) {
        input.close();
      }
    }
  }
}

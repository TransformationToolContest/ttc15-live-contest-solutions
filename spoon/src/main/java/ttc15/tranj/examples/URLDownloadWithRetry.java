package ttc15.tranj.examples;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

public class URLDownloadWithRetry {
  private final URL url;

  public URLDownloadWithRetry(String url) throws MalformedURLException {
    this.url = new URL(url);
  }

  public byte[] get() throws IOException {
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

          return buffer.toByteArray();
        } finally {
          if (input != null) {
            input.close();
          }
        }
      } catch (UnknownHostException e) {
        throw e;
      } catch (SocketTimeoutException e) {
        __retryCount += 1;

        if (__retryCount > 3) {
          throw e;
        } else {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e1) {
            throw e;
          }
        }
      }
    }
  }
}

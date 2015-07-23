package ttc15.tranj.examples;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

import ttc15.tranj.annotation.Cacheable;
import ttc15.tranj.annotation.Loggable;
import ttc15.tranj.annotation.RetryOnFailure;

public class FinalURLDownload {

  private final URL url;

  public FinalURLDownload(String url) throws MalformedURLException {
    this.url = new URL(url);
  }

  @RetryOnFailure(attempts = 3, delay = 1000, retry = { SocketTimeoutException.class }, escalate = { UnknownHostException.class })
  @Cacheable(lifetime = 1000)
  @Loggable
  public byte[] get() throws IOException {
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
    } catch(Exception e) {
    	e.printStackTrace();
    	return null;
    } finally {
      if (input != null) {
        input.close();
      }
    }
  }
}

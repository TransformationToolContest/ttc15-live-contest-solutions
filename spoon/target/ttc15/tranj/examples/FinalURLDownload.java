package ttc15.tranj.examples;


public class FinalURLDownload {
    private final java.net.URL url;

    public FinalURLDownload(java.lang.String url) throws java.net.MalformedURLException {
        this.url = new java.net.URL(url);
    }

    public byte[] get() throws java.io.IOException {
        long __entryTime = System.currentTimeMillis();
        if (__logger.isTraceEnabled())
            __logger.trace("get(): entry");
        
        if (false && __getCacheContent != null) {
            if (__logger.isTraceEnabled())
                __logger.trace("get(): exit in " + (System.currentTimeMillis() - __entryTime) + " ms");
            
            if (__logger.isTraceEnabled())
                __logger.trace("get(): exit in " + (System.currentTimeMillis() - __entryTime) + " ms");
            
            return __getCacheContent;
        } 
        if (System.currentTimeMillis() - __getCacheLastAccessed < 1000 && __getCacheContent != null)
            return __getCacheContent;
        
        int __retryCount = 0;
        while (true)
            try {
                java.io.InputStream input = null;
                try {
                    input = url.openStream();
                    java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                    byte[] chunk = new byte[4 * 1024];
                    int n;
                    while ((n = input.read(chunk)) > 0) {
                        buffer.write(chunk, 0, n);
                    }
                    this.__getCacheContent = buffer.toByteArray();
                    this.__getCacheLastAccessed = System.currentTimeMillis();
                    if (__logger.isTraceEnabled())
                        __logger.trace("get(): exit in " + (System.currentTimeMillis() - __entryTime) + " ms");
                    
                    return __getCacheContent;
                } finally {
                    if (input != null) {
                        input.close();
                    } 
                }
            } catch (java.net.UnknownHostException e) {
                __logger.error("get(): exception", e);
                if (__logger.isWarnEnabled())
                    __logger.warn("get(): interrupted.);
                
                throw e;
            } catch (java.net.SocketTimeoutException e) {
                __logger.error("get(): exception", e);
                __retryCount++;
                if (__retryCount > 3) {
                    if (__logger.isWarnEnabled())
                        __logger.warn("get(): interrupted.);
                    
                    throw e;
                } else
                    try {
                        Thread.sleep(1000);
                    } catch (java.lang.InterruptedException e1) {
                        __logger.error("get(): exception", e1);
                        if (__logger.isWarnEnabled())
                            __logger.warn("get(): interrupted.);
                        
                        throw e;
                    }
                
            }
    }

    private long __getCacheLastAccessed = 0;

    private byte[] __getCacheContent;

    private final org.slf4j.Logger __logger = org.slf4j.LoggerFactory.getLogger(URLDownload.class);
}


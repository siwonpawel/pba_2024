package pl.edu.zut.pba.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;

public class CustomContentCachingRequestWrapper extends ContentCachingRequestWrapper
{

    private final byte[] buffer;

    public CustomContentCachingRequestWrapper(HttpServletRequest request)
    {
        super(request);
        buffer = new byte[request.getContentLength()];
        try
        {
            request.getInputStream().read(buffer);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        return new CachedServletInputStream(new ByteArrayInputStream(buffer));
    }

    // Inner class to implement ServletInputStream
    private class CachedServletInputStream extends ServletInputStream
    {

        private final InputStream inputStream;

        public CachedServletInputStream(InputStream inputStream)
        {
            this.inputStream = inputStream;
        }

        @Override
        public boolean isFinished()
        {
            try
            {
                return inputStream.available() == 0;
            }
            catch (IOException e)
            {
                // Handle exception as needed
                return true;
            }
        }

        @Override
        public boolean isReady()
        {
            // Since it's a ByteArrayInputStream, it's always ready
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener)
        {
            // Not implementing asynchronous reading
            throw new UnsupportedOperationException();
        }

        @Override
        public int read() throws IOException
        {
            return inputStream.read();
        }
    }
}

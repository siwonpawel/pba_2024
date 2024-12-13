package pl.edu.zut.pba.security.configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//@Component
public class HmacSignatureFilter extends OncePerRequestFilter
{

    private static final String KEY = "123456";
    public static final String HEADER = "X-HMAC-SIGNATURE";
    private final Mac mac;

    HmacSignatureFilter()
    {
        try
        {
            mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(), "HmacSHA256");

            mac.init(secretKey);
        }
        catch (NoSuchAlgorithmException | InvalidKeyException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException
    {
        boolean isNotPost = !"POST".equalsIgnoreCase(request.getMethod());
        boolean signatureHeaderIsNotPresent = request.getHeader(HEADER) == null;

        return isNotPost || signatureHeaderIsNotPresent;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
    {
        CustomContentCachingRequestWrapper requestToUse = new CustomContentCachingRequestWrapper(request);

        byte[] buff = new byte[requestToUse.getContentLength()];
        requestToUse.getInputStream().read(buff);

        String payload = new String(buff, StandardCharsets.UTF_8);
        String signature = request.getHeader(HEADER);

        if (!isValidHmac(payload, signature))
        {
            response.getWriter().write("""
                    {
                      "code": "401",
                      "message": "'%s' header is not valid signature for given payload."
                    }
                    """.formatted(HEADER));
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            return;
        }

        filterChain.doFilter(requestToUse, response);
    }

    private boolean isValidHmac(String payload, String signature)
    {
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        String calculatedSignature = bytesToHex(hash);

        return calculatedSignature.equals(signature);
    }

    private String bytesToHex(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
        {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

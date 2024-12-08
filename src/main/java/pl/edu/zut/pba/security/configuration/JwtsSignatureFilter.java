package pl.edu.zut.pba.security.configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import pl.edu.zut.pba.security.JwtsService;

@Component
@RequiredArgsConstructor
public class JwtsSignatureFilter extends OncePerRequestFilter
{

    private final JwtsService jwtsService;

    public static final String HEADER = "X-JWS-SIGNATURE";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException
    {
        boolean isNotPost = !"PUT".equalsIgnoreCase(request.getMethod());
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

        if (!jwtsService.isValidSignature(payload, signature))
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
}

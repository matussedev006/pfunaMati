package mz.edu.ujc.abastecimento.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

public final class ManipuladorRequisicaoTokenCsrf implements CsrfTokenRequestHandler {

    private final CsrfTokenRequestHandler manipuladorXor = new XorCsrfTokenRequestAttributeHandler();
    private final CsrfTokenRequestHandler manipuladorPadrao = new CsrfTokenRequestAttributeHandler();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            Supplier<CsrfToken> csrfToken
    ) {
        manipuladorXor.handle(request, response, csrfToken);
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        String headerValue = request.getHeader(csrfToken.getHeaderName());
        if (StringUtils.hasText(headerValue)) {
            try {
                String resolved = manipuladorXor.resolveCsrfTokenValue(request, csrfToken);
                if (StringUtils.hasText(resolved)) {
                    return resolved;
                }
            } catch (Exception ignored) {
            }
            return manipuladorPadrao.resolveCsrfTokenValue(request, csrfToken);
        }
        return manipuladorXor.resolveCsrfTokenValue(request, csrfToken);
    }
}

package br.com.gabezy.todoapi.config.security;

import br.com.gabezy.todoapi.domain.detail.UserDetailsImpl;
import br.com.gabezy.todoapi.domain.entity.User;
import br.com.gabezy.todoapi.domain.enumaration.ErrorCode;
import br.com.gabezy.todoapi.exceptions.ResourceNotFoundException;
import br.com.gabezy.todoapi.repositories.UserRespository;
import br.com.gabezy.todoapi.services.JwtTokenService;
import br.com.gabezy.todoapi.utils.EndpointUtil;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserRespository userRespository;

    public AuthenticationFilter(JwtTokenService jwtTokenService, UserRespository userRespository) {
        this.jwtTokenService = jwtTokenService;
        this.userRespository = userRespository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            if (isPublicEndpoint(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = recoveryToken(request);

            if (Objects.isNull(token)) {
                throw new JWTVerificationException("Invalid token");
            }

            Authentication authentication = authenticateUser(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (JWTVerificationException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private String recoveryToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (Objects.nonNull(authorizationHeader)) {
            return authorizationHeader.replace("Bearer ", "");
        }
        return null;
    }

    private Authentication authenticateUser(String token) {
        String subject = jwtTokenService.getSubjectFromToken(token);

        User user = userRespository.findByEmail(subject)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        UserDetails userDetails = new UserDetailsImpl(user);

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String requestUri = request.getRequestURI();

        boolean isPublicEndpoint = Arrays.stream(EndpointUtil.PUBLIC_ENDPOINTS).anyMatch(requestUri::startsWith);
        boolean isPostPublicEndpoint = Arrays.stream(EndpointUtil.PUBLIC_POST_ENDPOINTS).anyMatch(requestUri::startsWith)
                && request.getMethod().equals(HttpMethod.POST.name());

        return isPublicEndpoint || isPostPublicEndpoint;
    }

}

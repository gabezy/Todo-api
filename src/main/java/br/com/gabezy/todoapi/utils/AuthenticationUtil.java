package br.com.gabezy.todoapi.utils;

import br.com.gabezy.todoapi.domain.detail.UserDetailsImpl;
import br.com.gabezy.todoapi.domain.entity.User;
import br.com.gabezy.todoapi.domain.enumaration.ErrorCode;
import br.com.gabezy.todoapi.exceptions.InvalidCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

public class AuthenticationUtil {

    private AuthenticationUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.nonNull(auth) && auth.getPrincipal() instanceof UserDetailsImpl userDetails && auth.isAuthenticated()) {
            return userDetails.getUser();
        }

        throw new InvalidCredentialsException(ErrorCode.USER_NOT_AUTHENTICATED);
    }

    public static String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.nonNull(auth) && auth.getPrincipal() instanceof UserDetailsImpl userDetails && auth.isAuthenticated()) {
            return userDetails.getUsername();
        }

        throw new InvalidCredentialsException(ErrorCode.USER_NOT_AUTHENTICATED);
    }

}

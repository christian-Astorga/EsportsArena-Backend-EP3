package com.esportsarena.api.security;

import org.springframework.security.core.Authentication;

public final class AuthUtil {
    private AuthUtil() {}

    public static boolean tieneRol(Authentication auth, String rol) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + rol));
    }
}

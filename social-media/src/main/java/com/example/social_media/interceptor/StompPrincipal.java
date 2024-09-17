package com.example.social_media.interceptor;

import java.security.Principal;

public class StompPrincipal implements Principal {
    private String name;

    public StompPrincipal(Long userId) {
        this.name = userId.toString(); 
    }

    @Override
    public String getName() {
        return name;
    }
}
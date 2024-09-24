package com.example.social_media.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import com.example.social_media.dto.user.UserDto;
import com.example.social_media.security.JwtUtil;

import java.net.URI;
import java.util.Map;

@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private JwtUtil jwtUtil;

    public WebSocketAuthInterceptor(JwtUtil jwtUtil){
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        Map<String, Object> attributes) throws Exception {

        URI uri = request.getURI();
        String query = uri.getQuery();
        
        if (query != null && query.startsWith("token=")) {
            String token = query.substring(6); 

            if (jwtUtil.validateToken(token)) {
                UserDto userDto = jwtUtil.getUserDtoFromToken(token);
                StompPrincipal principal = new StompPrincipal(userDto.getUserId());
                attributes.put("principal", principal);
            } else {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }    

    @Override
    public void afterHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        Exception exception) {
    }
}

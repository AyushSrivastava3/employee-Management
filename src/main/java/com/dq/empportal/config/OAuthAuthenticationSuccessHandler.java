package com.dq.empportal.config;


import com.dq.empportal.model.User;
import com.dq.empportal.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class OAuthAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // You can set attributes here to indicate successful login
        response.getWriter().write("{\"isLoggedIn\": true}");
        DefaultOAuth2User user=(DefaultOAuth2User)authentication.getPrincipal();
        // Get user attributes
        String name = user.getAttribute("name");
        String email = user.getAttribute("email");
        response.getWriter().write(name);
        response.getWriter().write(email);
        response.setStatus(HttpServletResponse.SC_OK);

        //save it to userRepo
        User user1=new User();
        user1.setEmail(email);
        user1.setUsername(name);
        user1.setPassword(UUID.randomUUID().toString());
        User user2=userRepository.findByEmail(email).orElse(null);
        if (user2==null){
            userRepository.save(user1);
        }

        response.sendRedirect("/index.html");

    }
}

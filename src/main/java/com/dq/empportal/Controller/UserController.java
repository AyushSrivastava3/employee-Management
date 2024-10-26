package com.dq.empportal.Controller;

import com.dq.empportal.Model.User;
import com.dq.empportal.Repository.UserRepository;
import com.dq.empportal.Service.UserDetailsServiceImpl;
import com.dq.empportal.Service.UserService;
import com.dq.empportal.utilis.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    JwtUtil jwtUtil;

    private static final PasswordEncoder passwordencoder=new BCryptPasswordEncoder();
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody User user) {

        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String jwt = jwtUtil.generateToken(userDetails.getUsername());
            return new ResponseEntity<>(jwt, HttpStatus.OK);
        }catch (Exception e){
            log.error("Exception occurred while createAuthenticationToken ", e);
            return new ResponseEntity<>("Incorrect username or password", HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return new ResponseEntity<>("Username already exists", HttpStatus.BAD_REQUEST);
        }
//        if (userRepository.findByEmail(user.getEmail()) != null) {
//            return new ResponseEntity<>("Email already exists", HttpStatus.BAD_REQUEST);
//        }
        User newUser=new User();
        newUser.setEmail(user.getEmail());
        newUser.setUsername(user.getUsername());
        //user.setPassword(user.getPassword());
        newUser.setPassword(passwordencoder.encode(user.getPassword()));
        // Save the user to the database
        userRepository.save(newUser);

        // Redirect to index.html (or any login page) upon successful registration
        return new ResponseEntity<>("Registration successful. Please proceed to login.", HttpStatus.OK);
    }

    @DeleteMapping("/deleteUser")
    public ResponseEntity<String> deleteUser(@RequestParam User user){
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        userRepository.deleteByUsername(authentication.getName());
        return new ResponseEntity<>("User deleted Successfully",HttpStatus.ACCEPTED);
    }

    // API to check if the JWT token is expired
    @GetMapping("/api/check-token")
    public ResponseEntity<String> checkTokenExpiration(@RequestHeader("Authorization") String token) {
        // Directly use the token from the header without "Bearer" prefix
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body("Token is missing");
        }

        // Check if the token is expired
        if (jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(401).body("Token has expired");
        }

        // If the token is valid and not expired
        return ResponseEntity.ok("Token is valid");
    }

    @GetMapping("/current-user")
    public User getCurrentUser(HttpSession session) {
        String username = (String) session.getAttribute("username");
        String email = (String) session.getAttribute("email");

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        return user; // Return user information (consider creating a DTO for better structure)
    }

    @GetMapping("/check-oauth-session")
    public ResponseEntity<Map<String, Object>> checkOAuth2Session(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication != null && authentication.isAuthenticated()) {
            // If the user is authenticated via OAuth2
            response.put("isAuthenticated", true);
            response.put("username", authentication.getName()); // Return the authenticated username
            return ResponseEntity.ok(response);
        } else {
            // User is not authenticated
            response.put("isAuthenticated", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // Invalidate the session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // Invalidate the current session
        }
        return ResponseEntity.ok("Logged out successfully");
    }

}


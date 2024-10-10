package com.employee_management_system.Employee_Management.Service;


import com.employee_management_system.Employee_Management.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.employee_management_system.Employee_Management.Model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user=userRepository.findByUsername(username);
        if (user!=null){
            UserDetails userDetails=org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .build();
            return userDetails;
        }
        throw new UsernameNotFoundException("User not found with username :"+username);
    }
}
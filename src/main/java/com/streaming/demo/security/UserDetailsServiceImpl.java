package com.streaming.demo.security;

import com.streaming.demo.entity.User;
import com.streaming.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        if (identifier == null || identifier.isEmpty()) {
            throw new UsernameNotFoundException("Identifier is empty");
        }

        User user;
        if (identifier.contains("@")) {
            user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + identifier));
        } else {
            user = userRepository.findByMobileNo(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("User Not Found with mobile: " + identifier));
        }

        return UserDetailsImpl.build(user);
    }
}

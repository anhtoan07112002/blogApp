package com.blogApp.blogcommon.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

public interface CustomUserDetailsService extends UserDetailsService {
    @Override
    @Transactional
    UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException;

    @Transactional
    UserDetails loadUserById(Long id);
}
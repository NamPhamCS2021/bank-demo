package com.example.bankDemo.security.model;

import com.example.bankDemo.enums.UserRole;
import com.example.bankDemo.security.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class UserDetailsImpl implements UserDetails {

    private Long id;
    private String username;
    private String password;
    private UserRole role;
    private Collection<? extends GrantedAuthority> authorities;


    public static UserDetailsImpl build(final User user) {
        List<GrantedAuthority> authorities = Arrays.asList(new GrantedAuthority[] {
            new SimpleGrantedAuthority("ROLE_" + user.getRole().toString())
        });
        return new UserDetailsImpl(user.getId(), user.getUsername(), user.getPassword(), user.getRole(), authorities);
    }
    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

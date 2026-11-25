package com.exemple.kmerquiz.security;

import com.exemple.kmerquiz.entity.User;
import com.exemple.kmerquiz.entity.UserStatus;
import com.exemple.kmerquiz.utils.RoleUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Getter
public class UserPrincipal implements UserDetails {

    private final Long idUser;
    private final String email;
    private final String pseudo;
    private final String password;
    private final RoleUser roleUser;
    private final UserStatus userStatus;
    private final List<SimpleGrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.idUser = user.getIdUser();
        this.email = user.getEmail();
        this.pseudo = user.getPseudo();
        this.password = user.getPassword();
        this.roleUser = RoleUser.from(user.getRole());
        this.userStatus = user.getStatus() == null ? UserStatus.ACTIVE : user.getStatus();

        List<SimpleGrantedAuthority> list = new ArrayList<>(roleUser.getAuthorities());
        list.add(new SimpleGrantedAuthority("ROLE_" + roleUser.name()));
        this.authorities = List.copyOf(list);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return userStatus != UserStatus.BANNED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return userStatus == UserStatus.ACTIVE;
    }
}

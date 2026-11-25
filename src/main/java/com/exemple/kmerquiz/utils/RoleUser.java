package com.exemple.kmerquiz.utils;

import com.exemple.kmerquiz.entity.enums.Role;
import com.exemple.kmerquiz.security.Permission;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public enum RoleUser {
    ADMIN(EnumSet.allOf(Permission.class)),
    PLAYER(EnumSet.of(
            Permission.QUIZ_CREATE,
            Permission.QUIZ_UPDATE,
            Permission.QUIZ_READ,
            Permission.QUESTION_CREATE,
            Permission.QUESTION_READ,
            Permission.RESPONSE_READ,
            Permission.CATEGORY_READ
    ));

    private final Set<Permission> permissions;

    RoleUser(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public List<SimpleGrantedAuthority> getAuthorities() {
        return permissions.stream()
                .map(p -> new SimpleGrantedAuthority(p.getPermission()))
                .toList();
    }


    public static RoleUser from(Role role) {
        if (role == null) {
            return PLAYER;
        }
        try {
            return RoleUser.valueOf(role.name());
        } catch (IllegalArgumentException e) {
            return PLAYER;
        }
    }
}

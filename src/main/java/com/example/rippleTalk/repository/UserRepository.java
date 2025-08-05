package com.example.rippleTalk.repository;


import com.example.rippleTalk.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>
{
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username) OR LOWER(u.email) = LOWER(:email)")
    Optional<User> findByUsernameOrEmailIgnoreCase(@Param("username") String username, @Param("email") String email);

    Optional<User> findByUsername(String username);

    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.email = :email")
    void deleteByEmail(@Param("email") String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.email IN :emails")
    void deleteByEmailIn(@Param("emails") List<String> emails);

    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.username = :username")
    void deleteByUsername(@Param("username") String username);

    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.username IN :usernames")
    void deleteByUsernameIn(@Param("usernames") List<String> username);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.username = :identifier OR u.email = :identifier")
    void updateLastLogin(@Param("identifier") String identifier, @Param("lastLogin") LocalDateTime lastLogin);
}
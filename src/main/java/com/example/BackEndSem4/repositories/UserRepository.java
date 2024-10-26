package com.example.BackEndSem4.repositories;

import com.example.BackEndSem4.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "u.fullName LIKE %:keyword% " +
            "OR u.phoneNumber LIKE %:keyword% " +
            "OR u.email LIKE %:keyword% " +
            "OR u.address LIKE %:keyword%)")
    Page<User> getUserAll(@Param("keyword") String keyword,
                          Pageable pageable);


    List<User> findAllByRoleName(String roleName);
}

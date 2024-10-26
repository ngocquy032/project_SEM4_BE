package com.example.BackEndSem4.repositories;

import com.example.BackEndSem4.models.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    @Query("SELECT c FROM Contact c " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "c.email LIKE %:keyword% OR c.name LIKE %:keyword%) AND " +
            "(:status IS NULL OR :status = '' OR c.status = :status)")
    Page<Contact> getContactAll(@Param("keyword") String keyword, @Param("status") String status, Pageable pageable);


}

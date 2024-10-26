package com.example.BackEndSem4.services.contact;

import com.example.BackEndSem4.dtos.ContactDTO;
import com.example.BackEndSem4.exceptions.DataNotFoundException;
import com.example.BackEndSem4.models.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface IContactService {
    Page<Contact> getContact(String keyword, String status, Pageable pageable);

    Contact insertContact(ContactDTO contactDTO) throws DataNotFoundException;
    Contact getContactById(Long contactId) throws DataNotFoundException;

    Contact updateContact(Long contactId, ContactDTO contactDTO) throws DataNotFoundException;

    void deleteContact(Long contactId) throws DataNotFoundException;





}

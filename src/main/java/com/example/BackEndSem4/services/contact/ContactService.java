package com.example.BackEndSem4.services.contact;

import com.example.BackEndSem4.dtos.ContactDTO;
import com.example.BackEndSem4.exceptions.DataNotFoundException;
import com.example.BackEndSem4.models.Contact;
import com.example.BackEndSem4.repositories.ContactRepository;
import com.example.BackEndSem4.services.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactService implements IContactService{
    private final ContactRepository contactRepository;
    private final EmailService emailService;



    @Override
    public Page<Contact> getContact(String keyword, String status, Pageable pageable) {
        return contactRepository.getContactAll(keyword, status , pageable);
    }

    @Override
    public Contact insertContact(ContactDTO contactDTO) throws DataNotFoundException {
        Contact contact = Contact.builder()
                .name(contactDTO.getName())
                .email(contactDTO.getEmail())
                .message(contactDTO.getMessage())
                .status(Contact.AwaitReply)
                .build();

        return contactRepository.save(contact);
    }

    @Override
    public Contact getContactById(Long contactId) throws DataNotFoundException {
        return contactRepository.findById(contactId)
                .orElseThrow(() -> new DataNotFoundException("Contact with ID " + contactId + " not found."));
    }

    @Override
    public Contact updateContact(Long contactId, ContactDTO contactDTO) throws DataNotFoundException {
        Contact existingContact = contactRepository.findById(contactId)
                .orElseThrow(() -> new DataNotFoundException("Contact with ID " + contactId + " not found."));

        existingContact.setName(contactDTO.getName());
        existingContact.setEmail(contactDTO.getEmail());
        existingContact.setMessage(contactDTO.getMessage());
        existingContact.setReply(contactDTO.getReply());
        existingContact.setStatus(contactDTO.getStatus());
        Contact contact = contactRepository.save(existingContact);

        emailService.sendEmailReplyContact(contactDTO.getEmail(), contactDTO.getReply());
        return contact;
    }

    @Override
    public void deleteContact(Long contactId) throws DataNotFoundException {
        if (!contactRepository.existsById(contactId)) {
            throw new DataNotFoundException("Contact with ID " + contactId + " not found.");
        }
        contactRepository.deleteById(contactId);
    }
}
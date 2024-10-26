package com.example.BackEndSem4.dtos;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactDTO {

    private String name;
    private String email;
    private String message;
    private String reply;
    private String status;
}
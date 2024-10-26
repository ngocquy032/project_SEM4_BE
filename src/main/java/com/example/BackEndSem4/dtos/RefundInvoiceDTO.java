package com.example.BackEndSem4.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefundInvoiceDTO {


    @JsonProperty("booking_id")
    private Long bookingId;

    @JsonProperty("bank_name")
    private  String bankName;

    @JsonProperty("holder_name")
    private  String holderName;

    @JsonProperty("account_number")
    private  String accountNumber;

    @JsonProperty("status")
    private String status;
}

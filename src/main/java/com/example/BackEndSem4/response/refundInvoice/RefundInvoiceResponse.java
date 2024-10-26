package com.example.BackEndSem4.response.refundInvoice;


import com.example.BackEndSem4.models.RefundInvoice;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class RefundInvoiceResponse {

    private  Long id;

    @JsonProperty("booking_id")
    private Long bookingId;

    @JsonProperty
    private float amount;

    @JsonProperty("bank_name")
    private  String bankName;

    @JsonProperty("holder_name")
    private  String holderName;

    @JsonProperty("account_number")
    private  String accountNumber;

    @JsonProperty("status")
    private String status;

    public static RefundInvoiceResponse fromRefundInvoiceResponse(RefundInvoice refundInvoice) {

        return RefundInvoiceResponse.builder()
                .id(refundInvoice.getId())
                .bookingId(refundInvoice.getBooking().getId())
                .amount(refundInvoice.getBooking().getAmount())
                .bankName(refundInvoice.getBankName())
                .holderName(refundInvoice.getHolderName())
                .accountNumber(refundInvoice.getAccountNumber())
                .status(refundInvoice.getStatus())
                .build();
    }

}

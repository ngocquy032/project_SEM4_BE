package com.example.BackEndSem4.response.refundInvoice;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class RefundInvoiceListResponse {
    private List<RefundInvoiceResponse> refundInvoiceResponses;
    private int totalPages;
}

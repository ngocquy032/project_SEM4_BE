package com.example.BackEndSem4.models;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "refund_invoices")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefundInvoice extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;


    @Column(name = "bank_name")
    private  String bankName;

    @Column(name = "holder_name")
    private  String holderName;

    @Column(name = "account_number")
    private  String accountNumber;

    @Column
    private String status;


    public static final String WAITREFUND = "Wait Refund";
    public static final String REFUNDED = "Refunded";

}

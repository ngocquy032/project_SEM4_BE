package com.example.BackEndSem4.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Booking extends BaseEntity{
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_code")
    private String paymentCode;

    @Column
    private float amount;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "change_count")
    private Integer changeCount;

    @Column
    private String status;



    public static final String PENDING = "pending";
    public static final String PAID = "paid";
    public static final String CANCELLED = "rejected";
    public static final String WAITREFUND = "Wait Refund";
    public static final String REFUNDED = "Refunded";
}

package com.finnova.customer_service.model.entity;

import com.finnova.customer_service.model.enums.CustomerType;
import com.finnova.customer_service.model.enums.DocumentType;
import com.finnova.customer_service.model.enums.ProfileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "customers")
public class Customer {

    /**
     * Unique identifier for the customer.
     */
    @Id
    private String id;

    /**
     * Type of document.
     */
    private DocumentType documentType;

    /**
     * Document number.
     * Must be unique across the system.
     */
    @Indexed(unique = true)
    private String documentNumber;

    /**
     * Type of customer (PERSONAL or BUSINESS).
     */
    private CustomerType customerType;

    /**
     * Profile type (STANDARD, VIP, PYME).
     */
    private ProfileType profileType;

    /**
     * First name (for personal customers).
     */
    private String firstName;

    /**
     * Last name (for personal customers).
     */
    private String lastName;

    /**
     * Business name (for business customers).
     */
    private String businessName;

    /**
     * Email address.
     */
    @Indexed
    private String email;

    /**
     * Phone number.
     */
    private String phone;

    /**
     * Physical address.
     */
    private String address;

    /**
     * Indicates if the customer is active.
     */
    private Boolean active;

    /**
     * Timestamp when the customer was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the customer was last updated.
     */
    private LocalDateTime updatedAt;
}

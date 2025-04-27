package com.bezkoder.springjwt.models.HRModuleEntities;

import com.bezkoder.springjwt.models.Employee;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank; // Changed from Pattern to NotBlank if ref is mandatory
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set; // Note: Set import is not used in this entity

@Entity
@Table(name = "contracts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contractId;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le type de contrat est obligatoire") // Added NotNull
    private ContractType contractType;

    @NotNull(message = "La date de début est obligatoire") // Added NotNull
    private LocalDate startDate;

    private LocalDate endDate;
    private LocalDate renewalDate;

    @NotBlank(message = "La référence du contrat est obligatoire") // Assuming reference is mandatory
    private String reference;

    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le statut du contrat est obligatoire") // Assuming statut is mandatory
    private ContractStatus statut;

    // Validates the String format, assuming it's a URL or path
    @Lob
    // CHANGE columnDefinition from "BLOB" to "MEDIUMBLOB" or "LONGBLOB"
    @Column(name = "file_content", columnDefinition = "MEDIUMBLOB") // Using MEDIUMBLOB for larger capacity
    @Basic(fetch = FetchType.LAZY)
    private byte[] fileContent;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type") // e.g., "application/pdf"
    private String fileType;
    @ManyToOne
    @JoinColumn(name = "employee_id")
    @JsonIgnore // <-- Add this annotation

    private Employee employee;

    public Long getId() {
        return null;
    }
}
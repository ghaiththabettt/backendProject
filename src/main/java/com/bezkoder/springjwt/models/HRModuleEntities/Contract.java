package com.bezkoder.springjwt.models.HRModuleEntities;

import com.bezkoder.springjwt.models.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

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
        private ContractType contractType; // Full-time, Part-time, Temporary, etc.
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate renewalDate;
    private String reference ;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    public Long getId() {
        return null;
    }
}
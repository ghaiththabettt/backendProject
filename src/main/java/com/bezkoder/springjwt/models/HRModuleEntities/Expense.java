package com.bezkoder.springjwt.models.HRModuleEntities;

import com.bezkoder.springjwt.models.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long expenseId;

    private Double amount;
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private TypeExpense type ;
    @Enumerated(EnumType.STRING)
    private StatusExpense status ;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
}
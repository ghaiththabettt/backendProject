package com.bezkoder.springjwt.models.HRModuleEntities;

import com.bezkoder.springjwt.models.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TaskStatus status;

    @Column(nullable = false, length = 100)
    private String taskName;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private LocalDate completionDate;

    public Task(String taskName, String description, Employee employee) {
        this.taskName = taskName;
        this.description = description;
        this.employee = employee;
        this.status = TaskStatus.PENDING;
    }

    public void markAsCompleted() {
        this.status = TaskStatus.COMPLETED;
        this.completionDate = LocalDate.now();
    }
}
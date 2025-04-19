package com.bezkoder.springjwt.models.HRModuleEntities;

import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "leaves")
public class Leave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leaveId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee; // Links to the employee making the request

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false)
    private LeaveType leaveType;   // Corresponds to 'type' in Angular

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate; // Corresponds to 'from' in Angular

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;   // Corresponds to 'leaveTo' in Angular

    @Column(name = "number_of_days", nullable = false) // Store calculated or provided value
    private Double numberOfDays; // Corresponds to 'noOfDays', use Double for half days

    @Enumerated(EnumType.STRING)
    @Column(name = "duration_type", nullable = false)
    private DurationType durationType; // Corresponds to 'durationType'

    @Enumerated(EnumType.STRING)
    @Column(name = "status_leave", nullable = false)
    private StatusLeave statusLeave = StatusLeave.PENDING; // Default to PENDING, corresponds to 'status'

    @Column(length = 1000) // Increased length for reason
    private String reason;

    @Column(length = 1000)
    private String note;

    @Column(name = "requested_on", nullable = false)
    private LocalDate requestedOn; // Set when created

    @ManyToOne(fetch = FetchType.LAZY) // Can be null until actioned
    @JoinColumn(name = "actioned_by_user_id") // Changed name for clarity (Approve/Reject)
    private User actionedBy; // Corresponds to 'approvedBy' (User who approved/rejected)

    @Column(name = "action_date") // Changed name for clarity
    private LocalDate actionDate; // Corresponds to 'approvalDate' (Date of approve/reject)

    // Optional: Automatically set requestedOn date before persisting
    @PrePersist
    protected void onCreate() {
        if (requestedOn == null) {
            requestedOn = LocalDate.now();
        }
        // Simple day calculation (can be enhanced for holidays/weekends)
        if (startDate != null && endDate != null && numberOfDays == null) {
            calculateDays();
        }
    }

    // Optional: Recalculate days if dates change before update
    @PreUpdate
    protected void onUpdate() {
        if (startDate != null && endDate != null) {
            calculateDays();
        }
    }

    // Helper method to calculate days (basic version)
    private void calculateDays() {
        if (startDate != null && endDate != null && startDate.isBefore(endDate.plusDays(1))) {
            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            this.numberOfDays = (durationType == DurationType.HALF_DAY) ? daysBetween * 0.5 : (double) daysBetween;
        } else {
            this.numberOfDays = 0.0; // Or handle error
        }
    }

}
package com.bezkoder.springjwt.models.HRModuleEntities;

import com.bezkoder.springjwt.models.Employee;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "entrees_de_tempss")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EntreeDeTemps {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(length = 25, nullable = false)
    private TypeEntreeDeTemps typeEntreeDeTemps = TypeEntreeDeTemps.Travail;

    @Column(nullable = false)
    private LocalDateTime heureDebut;

    private LocalDateTime heureFin;

    @Column(name = "duree_nette_minutes")
    private Long dureeNetteMinutes = 0L;

    @Column(name = "duree_pause_minutes")
    private Long dureePauseMinutes = 0L;

    @Column(name = "loc_debut_lat")
    private Double localisationDebutLat;
    @Column(name = "loc_debut_lng")
    private Double localisationDebutLng;
    @Column(name = "loc_debut_adresse", length = 500)
    private String localisationDebutAdresse;

    @Column(name = "loc_fin_lat")
    private Double localisationFinLat;
    @Column(name = "loc_fin_lng")
    private Double localisationFinLng;
    @Column(name = "loc_fin_adresse", length = 500)
    private String localisationFinAdresse;

    // Dans EntreeDeTemps.java
    @OneToMany(mappedBy = "entreeDeTemps", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // <--- CHANGER ICI
    private List<Pause> pauses = new ArrayList<>();

    @Column(length = 1000)
    private String notes;

    // CORRECTION ICI : Ajout de columnDefinition
    @CreationTimestamp
    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)")
    private LocalDateTime createdAt;

    // CORRECTION ICI : Ajout de columnDefinition (même si nullable = true par défaut, c'est une bonne pratique pour la clarté)
    @UpdateTimestamp
    @Column(columnDefinition = "DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private RestrictionsHorloge restrictionsHorloge = RestrictionsHorloge.Flexible;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Status status = Status.En_cours;

    // Méthode utilitaire pour recalculer les durées
    public void recalculerDurees() {
        this.dureePauseMinutes = this.pauses.stream()
                .filter(pause -> pause.getDebut() != null && pause.getFin() != null) // Sécurité: ignorer pauses non terminées
                .mapToLong(Pause::getDureeMinutes)
                .sum();

        if (this.heureDebut != null && this.heureFin != null) {
            long totalMinutes = Duration.between(this.heureDebut, this.heureFin).toMinutes();
            this.dureeNetteMinutes = Math.max(0, totalMinutes - this.dureePauseMinutes);
        } else {
            this.dureeNetteMinutes = 0L;
        }
    }
}
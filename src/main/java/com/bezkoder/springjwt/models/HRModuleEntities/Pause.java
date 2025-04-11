package com.bezkoder.springjwt.models.HRModuleEntities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "Pause")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pause {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Important pour éviter chargement inutile
    @JoinColumn(name = "entree_de_temps_id", nullable = false)
    @JsonIgnore // ESSENTIEL pour éviter les boucles JSON
    private EntreeDeTemps entreeDeTemps;

    @Column(nullable = false)
    private LocalDateTime debut;

    private LocalDateTime fin; // Null si la pause est en cours

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private TypePause typePause = TypePause.Autre; // Valeur par défaut si besoin

    private String note; // Ajouté comme dans le code Node.js

    // Calcul de la durée (en minutes, par exemple)
    public Long getDureeMinutes() {
        if (debut != null && fin != null) {
            return java.time.Duration.between(debut, fin).toMinutes();
        }
        return 0L;
    }
}
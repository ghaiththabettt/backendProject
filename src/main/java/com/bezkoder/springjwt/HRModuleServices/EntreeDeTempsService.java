package com.bezkoder.springjwt.HRModuleServices;

import com.bezkoder.springjwt.dtos.HRModuleDtos.*;
import com.bezkoder.springjwt.models.Employee;
// Importez les ENUMS et Entités du BON package HRModuleEntities
import com.bezkoder.springjwt.models.HRModuleEntities.EntreeDeTemps;
import com.bezkoder.springjwt.models.HRModuleEntities.Pause;
import com.bezkoder.springjwt.models.HRModuleEntities.RestrictionsHorloge; // Votre enum
import com.bezkoder.springjwt.models.HRModuleEntities.Status;               // Votre enum
import com.bezkoder.springjwt.models.HRModuleEntities.TypeEntreeDeTemps;   // Votre enum
import com.bezkoder.springjwt.models.HRModuleEntities.TypePause;         // Votre enum
import com.bezkoder.springjwt.repository.EmployeeRepository;
import com.bezkoder.springjwt.repository.HRModuleRepository.EntreeDeTempsRepository;
import com.bezkoder.springjwt.repository.HRModuleRepository.PauseRepository;
// --- Gestion Tache --- Décommentez si Tache est réintégré ---
// import com.bezkoder.springjwt.repository.HRModuleRepository.TacheRepository;
// import com.bezkoder.springjwt.models.HRModuleEntities.Tache; // Assurez-vous que l'entité Tache existe dans ce package

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Transactional // Annotation au niveau classe pour la simplicité, sinon mettez sur chaque méthode modifiant la DB
@Service
public class EntreeDeTempsService {
    private static final Logger log = LoggerFactory.getLogger(EntreeDeTempsService.class);

    @Autowired private EntreeDeTempsRepository entreeDeTempsRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private PauseRepository pauseRepository;
    // --- Gestion Tache --- Décommentez si Tache est réintégré ---
    // @Autowired private TacheRepository tacheRepository;

    // --- MAPPERS (mapToDto, mapPauseToDto) doivent être ici ---
    // Supposons qu'ils existent comme définis précédemment
    private EntreeDeTempsDTO mapToDto(EntreeDeTemps entity) {
        EntreeDeTempsDTO dto = new EntreeDeTempsDTO();
        dto.setId(entity.getId());
        if (entity.getEmployee() != null) {
            dto.setEmployeeId(entity.getEmployee().getId());
            dto.setEmployeeFullName(entity.getEmployee().getName() + " " + entity.getEmployee().getLastName()); // Assurez-vous que Employee a getFirstName/getLastName
        }

        // --- Gestion Tache --- Décommentez si Tache est réintégré ---
         /*
         if (entity.getTache() != null) {
             dto.setTacheId(entity.getTache().getId());
             dto.setTacheTitre(entity.getTache().getTitre()); // Assurez-vous que Tache a getTitre()
         }
          */
        // ---------------------------------------------------------

        dto.setTypeEntreeDeTemps(entity.getTypeEntreeDeTemps());
        dto.setHeureDebut(entity.getHeureDebut());
        dto.setHeureFin(entity.getHeureFin());
        dto.setDureeNetteMinutes(entity.getDureeNetteMinutes());
        dto.setDureePauseMinutes(entity.getDureePauseMinutes());

        if(entity.getLocalisationDebutLat() != null || entity.getLocalisationDebutLng() != null || entity.getLocalisationDebutAdresse() != null) {
            PointageLocationDto locDebut = new PointageLocationDto();
            locDebut.setLat(entity.getLocalisationDebutLat());
            locDebut.setLng(entity.getLocalisationDebutLng());
            locDebut.setAdresseComplete(entity.getLocalisationDebutAdresse());
            dto.setLocalisationDebut(locDebut);
        }
        if(entity.getLocalisationFinLat() != null || entity.getLocalisationFinLng() != null || entity.getLocalisationFinAdresse() != null) {
            PointageLocationDto locFin = new PointageLocationDto();
            locFin.setLat(entity.getLocalisationFinLat());
            locFin.setLng(entity.getLocalisationFinLng());
            locFin.setAdresseComplete(entity.getLocalisationFinAdresse());
            dto.setLocalisationFin(locFin);
        }

        dto.setPauses(entity.getPauses().stream().map(this::mapPauseToDto).collect(Collectors.toList()));

        dto.setNotes(entity.getNotes());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setRestrictionsHorloge(entity.getRestrictionsHorloge());
        dto.setStatus(entity.getStatus());

        return dto;
    }

    private PauseDTO mapPauseToDto(Pause pause) {
        PauseDTO dto = new PauseDTO();
        dto.setId(pause.getId());
        dto.setDebut(pause.getDebut());
        dto.setFin(pause.getFin());
        dto.setTypePause(pause.getTypePause()); // TypePause enum
        dto.setNote(pause.getNote());
        dto.setDureeMinutes(pause.getDureeMinutes());
        return dto;
    }
    // ----------------------------------------------------------


    public EntreeDeTempsDTO commencerPointage(StartPointageRequest request, Long requestingEmployeeId) {
        // 1. Valider l'employé cible
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Erreur: Employé non trouvé avec ID: " + request.getEmployeeId()));

        // (Optionnel mais recommandé: Ajouter ici une vérification des droits si 'requestingEmployeeId' n'est pas le même que 'request.getEmployeeId()',
        // par exemple vérifier si l'appelant a le rôle ADMIN/RH)

        // 2. Déterminer le mode applicable
        RestrictionsHorloge mode = getApplicableRestrictionMode();

        // 3. Appliquer les validations basées sur le mode (CORRIGÉ)
        boolean pointageActifExiste = entreeDeTempsRepository.existsByEmployeeIdAndStatusIsNot(employee.getId(), Status.Termine);

        if (mode == RestrictionsHorloge.Strict || mode == RestrictionsHorloge.Modere) {
            // Vérifier s'il y a un pointage déjà actif ou en pause
            if (pointageActifExiste) {
                throw new RuntimeException("Mode " + mode + " : Un pointage est déjà en cours ou en pause pour l'employé " + employee.getId());
            }
            // Vérifier s'il y a déjà eu un pointage pour aujourd'hui
            if (entreeDeTempsRepository.existsByEmployeeIdAndDate(employee.getId(), LocalDateTime.now())) {
                throw new RuntimeException("Mode " + mode + " : Un seul pointage par jour autorisé pour l'employé " + employee.getId());
            }
        }

        // Validation spécifique au mode Strict pour la localisation
        if (mode == RestrictionsHorloge.Strict) {
            if (request.getLocalisationDebut() == null || request.getLocalisationDebut().getLat() == null || request.getLocalisationDebut().getLng() == null) {
                // Lancer une exception si la localisation GPS est requise mais manquante
                throw new RuntimeException("Mode Strict : Localisation de début (latitude/longitude) requise.");
            }
        }

        // --- Gestion Tache --- Décommentez et adaptez si Tache est réintégré ---
        /*
        Tache tache = null;
        if (request.getTacheId() != null) {
            tache = tacheRepository.findById(request.getTacheId())
                .orElseThrow(() -> new RuntimeException("Erreur: Tâche non trouvée avec ID: " + request.getTacheId()));
        } else {
             // Si la tâche est obligatoire pour démarrer un pointage (selon vos règles métier)
              throw new RuntimeException("Erreur: L'ID de la tâche est requis pour démarrer le pointage.");
        }
        */
        // ----------------------------------------------------------------------

        // 4. Créer la nouvelle entité EntreeDeTemps
        EntreeDeTemps nouvelleEntree = new EntreeDeTemps();
        nouvelleEntree.setEmployee(employee);
        // nouvelleEntree.setTache(tache); // --- Décommentez si Tache est réintégré ---
        nouvelleEntree.setRestrictionsHorloge(mode); // Appliquer le mode détecté
        nouvelleEntree.setNotes(request.getNotes());
        nouvelleEntree.setTypeEntreeDeTemps(request.getTypeEntreeDeTemps() != null ? request.getTypeEntreeDeTemps() : TypeEntreeDeTemps.Travail); // Utiliser enum
        nouvelleEntree.setStatus(Status.En_cours); // Statut initial (pourrait être changé par le mode Flexible)

        // 5. Gérer les heures spécifiques au mode Flexible
        if (mode == RestrictionsHorloge.Flexible && request.getHeureDebutProposee() != null) {
            // Utiliser l'heure proposée si elle n'est pas dans le futur
            LocalDateTime debutPropose = request.getHeureDebutProposee();
            nouvelleEntree.setHeureDebut(debutPropose.isAfter(LocalDateTime.now()) ? LocalDateTime.now() : debutPropose);

            // Vérifier si une heure de fin a aussi été proposée et si elle est valide
            if (request.getHeureFinProposee() != null) {
                LocalDateTime finPropose = request.getHeureFinProposee();
                // Condition: finPropose <= maintenant ET finPropose > heureDebut calculée
                if (!finPropose.isAfter(LocalDateTime.now()) && finPropose.isAfter(nouvelleEntree.getHeureDebut())) {
                    nouvelleEntree.setHeureFin(finPropose);
                    nouvelleEntree.setStatus(Status.Termine); // Marquer comme terminé directement
                }
                // Si heureFinProposee est invalide (dans le futur ou avant le début), elle est ignorée. Le statut reste En_cours.
            }
        } else {
            // Pour les modes Strict, Modéré, ou Flexible sans heure proposée, l'heure de début est maintenant.
            nouvelleEntree.setHeureDebut(LocalDateTime.now());
        }

        // 6. Enregistrer la localisation de début (si fournie)
        if (request.getLocalisationDebut() != null) {
            nouvelleEntree.setLocalisationDebutLat(request.getLocalisationDebut().getLat());
            nouvelleEntree.setLocalisationDebutLng(request.getLocalisationDebut().getLng());
            nouvelleEntree.setLocalisationDebutAdresse(request.getLocalisationDebut().getAdresseComplete());
        }

        // 7. Recalculer les durées *avant* de sauvegarder si le statut est déjà 'Termine' (cas Flexible)
        if (nouvelleEntree.getStatus() == Status.Termine) {
            nouvelleEntree.recalculerDurees(); // Met à jour dureeNetteMinutes et dureePauseMinutes (qui sera 0)
        }
        // NOTE : pour les pointages qui démarrent en 'En_cours', les durées sont nulles (ou 0L) au début,
        // elles seront calculées à l'arrêt du pointage ou lors de la modification des dates.

        // 8. Sauvegarder l'entité dans la base de données
        EntreeDeTemps savedEntree = entreeDeTempsRepository.save(nouvelleEntree);

        // 9. Mapper l'entité sauvegardée (qui contient maintenant l'ID généré) vers le DTO de réponse
        return mapToDto(savedEntree);
    }

    // ... (Les autres méthodes du service: mettreEnPause, reprendrePointage, arreterPointage, etc...)
    public RestrictionsHorloge getApplicableRestrictionMode() {
        Optional<EntreeDeTemps> dernierPointage = entreeDeTempsRepository.findTopByOrderByCreatedAtDesc();
        return dernierPointage.map(EntreeDeTemps::getRestrictionsHorloge).orElse(RestrictionsHorloge.Flexible);
    }


    public DernierModeResponseDto getDernierModeUtilise() {
        DernierModeResponseDto response = new DernierModeResponseDto();
        response.setRestrictionsHorloge(getApplicableRestrictionMode());
        return response;
    }

    public EntreeDeTempsDTO mettreEnPause(PauseRequest request) {
        EntreeDeTemps pointage = entreeDeTempsRepository.findById(request.getPointageId()) // Ici, request.getPointageId() VAUT 15
                .orElseThrow(() -> new RuntimeException("Pointage non trouvé: ID " + request.getPointageId()));

        // Log pour vérifier l'objet récupéré
        System.out.println(">>> [mettreEnPause] Pointage trouvé: ID=" + pointage.getId() + ", Employé=" + pointage.getEmployee().getId());

        if (pointage.getStatus() == Status.Termine) {
            throw new RuntimeException("Ce pointage est déjà terminé.");
        }
        if (pointage.getStatus() == Status.En_pause) {
            throw new RuntimeException("Ce pointage est déjà en pause.");
        }
        boolean hasActivePause = pointage.getPauses().stream().anyMatch(p -> p.getFin() == null);
        if (hasActivePause) {
            throw new RuntimeException("Une pause est déjà en cours. Veuillez reprendre d'abord.");
        }

        Pause nouvellePause = new Pause();
        nouvellePause.setEntreeDeTemps(pointage); // <-- Association CRUCIALE
        nouvellePause.setDebut(LocalDateTime.now());
        nouvellePause.setTypePause(request.getTypePause());
        nouvellePause.setNote(request.getNote());

        // Log pour vérifier l'association
        System.out.println(">>> [mettreEnPause] Nouvelle pause créée, associée à EntreeDeTemps ID: " + (nouvellePause.getEntreeDeTemps() != null ? nouvellePause.getEntreeDeTemps().getId() : "NULL"));

        // ÉTAPE 4: Ajout à la collection et mise à jour du statut
        pointage.getPauses().add(nouvellePause);
        pointage.setStatus(Status.En_pause);

        // ÉTAPE 5: Sauvegarde
        // C'est ici ou pendant cette sauvegarde que Hibernate génère l'INSERT SQL
        // qui échoue car il met NULL pour entree_de_temps_id.
        System.out.println(">>> [mettreEnPause] Tentative de sauvegarde de EntreeDeTemps ID: " + pointage.getId());
        EntreeDeTemps updatedPointage = entreeDeTempsRepository.save(pointage);
        System.out.println(">>> [mettreEnPause] Sauvegarde de EntreeDeTemps terminée.");


        return mapToDto(updatedPointage);
    }


    public EntreeDeTempsDTO reprendrePointage(ReprendreRequest request) {
        EntreeDeTemps pointage = entreeDeTempsRepository.findById(request.getPointageId())
                .orElseThrow(() -> new RuntimeException("Pointage non trouvé: ID " + request.getPointageId()));

        if (pointage.getStatus() != Status.En_pause) {
            throw new RuntimeException("Le pointage n'est pas en pause.");
        }

        Pause pauseActive = pointage.getPauses().stream()
                .filter(p -> p.getFin() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucune pause active trouvée pour reprendre."));

        pauseActive.setFin(LocalDateTime.now());
        // Recalculer la durée totale des pauses est utile si on affiche ce total
        pointage.recalculerDurees();

        pointage.setStatus(Status.En_cours);

        EntreeDeTemps updatedPointage = entreeDeTempsRepository.save(pointage);
        return mapToDto(updatedPointage);
    }


    public EntreeDeTempsDTO arreterPointage(StopPointageRequest request) {
        EntreeDeTemps pointage = entreeDeTempsRepository.findById(request.getPointageId())
                .orElseThrow(() -> new RuntimeException("Pointage non trouvé: ID " + request.getPointageId()));

        if (pointage.getStatus() == Status.Termine) {
            throw new RuntimeException("Ce pointage est déjà terminé.");
        }

        if (pointage.getRestrictionsHorloge() == RestrictionsHorloge.Strict) {
            if (request.getGpsValide() == null || !request.getGpsValide()) {
                throw new RuntimeException("Mode Strict : Localisation GPS valide requise pour arrêter.");
            }
            // reconnaissanceFacialeValidee non gérée ici
            if (request.getLocalisationFin() != null) {
                pointage.setLocalisationFinLat(request.getLocalisationFin().getLat());
                pointage.setLocalisationFinLng(request.getLocalisationFin().getLng());
                pointage.setLocalisationFinAdresse(request.getLocalisationFin().getAdresseComplete());
            } // else { throw new RuntimeException("Mode Strict : Localisation de fin requise pour arrêter."); } // Si obligatoire
        } else {
            if (request.getLocalisationFin() != null) {
                pointage.setLocalisationFinLat(request.getLocalisationFin().getLat());
                pointage.setLocalisationFinLng(request.getLocalisationFin().getLng());
                pointage.setLocalisationFinAdresse(request.getLocalisationFin().getAdresseComplete());
            }
        }


        if (pointage.getStatus() == Status.En_pause) {
            Pause pauseActive = pointage.getPauses().stream()
                    .filter(p -> p.getFin() == null)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Erreur interne : statut 'En pause' mais pas de pause active trouvée."));
            pauseActive.setFin(LocalDateTime.now()); // Terminer la pause avant d'arrêter le pointage
        }

        pointage.setHeureFin(LocalDateTime.now());
        pointage.setStatus(Status.Termine);
        // Recalculer IMPERATIVEMENT les durées après avoir mis heureFin et terminé les pauses actives
        pointage.recalculerDurees();

        EntreeDeTemps updatedPointage = entreeDeTempsRepository.save(pointage);
        return mapToDto(updatedPointage);
    }

    @Transactional(readOnly = true)
    public AnalyseTempsResponseDto analyserTemps(Long employeeId, LocalDate startDate, LocalDate endDate) {
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec ID: " + employeeId));

        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime endDateTime = (endDate != null) ? endDate.plusDays(1).atStartOfDay() : LocalDateTime.now().plusYears(1);

        List<EntreeDeTemps> entries = entreeDeTempsRepository.findByEmployeeIdAndHeureDebutBetweenOrderByHeureDebutAsc(employeeId, startDateTime, endDateTime);

        List<EntreeDeTempsDTO> detailDtos = entries.stream().map(this::mapToDto).collect(Collectors.toList());

        long totalTravailMinutes = detailDtos.stream().filter(dto -> dto.getDureeNetteMinutes() != null).mapToLong(EntreeDeTempsDTO::getDureeNetteMinutes).sum();
        long totalPauseMinutes = detailDtos.stream().filter(dto -> dto.getDureePauseMinutes() != null).mapToLong(EntreeDeTempsDTO::getDureePauseMinutes).sum();
        int nombrePointages = detailDtos.size();

        long tempsTotalMinutes = totalTravailMinutes + totalPauseMinutes;
        String productivite = "0.00%";
        if (tempsTotalMinutes > 0) {
            productivite = String.format(Locale.US, "%.2f%%", (double) totalTravailMinutes / tempsTotalMinutes * 100);
        }

        AnalyseTempsResponseDto analyse = new AnalyseTempsResponseDto();
        analyse.setTempsTotalTravailleMinutes(totalTravailMinutes);
        analyse.setPausesTotalesMinutes(totalPauseMinutes);
        analyse.setNombrePointages(nombrePointages);
        analyse.setProductivite(productivite);
        analyse.setPointagesDetail(detailDtos);

        return analyse;
    }

    @Transactional(readOnly = true)
    public List<EntreeDeTempsDTO> getAllPointages() {
        return entreeDeTempsRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }


    public EntreeDeTempsDTO modifierDatesPointage(ModifierDatesRequest request) {
        EntreeDeTemps pointage = entreeDeTempsRepository.findById(request.getPointageId())
                .orElseThrow(() -> new RuntimeException("Pointage non trouvé: ID " + request.getPointageId()));

        boolean updated = false;
        if (request.getNouvelleHeureDebut() != null) {
            if (pointage.getHeureFin() != null && request.getNouvelleHeureDebut().isAfter(pointage.getHeureFin())) {
                throw new RuntimeException("La nouvelle heure de début doit être avant l'heure de fin existante.");
            }
            if(request.getNouvelleHeureFin() != null && request.getNouvelleHeureDebut().isAfter(request.getNouvelleHeureFin())) {
                throw new RuntimeException("La nouvelle heure de début doit être avant la nouvelle heure de fin.");
            }
            pointage.setHeureDebut(request.getNouvelleHeureDebut());
            updated = true;
        }
        if (request.getNouvelleHeureFin() != null) {
            LocalDateTime debutEffective = (request.getNouvelleHeureDebut() != null) ? request.getNouvelleHeureDebut() : pointage.getHeureDebut();
            if (request.getNouvelleHeureFin().isBefore(debutEffective)) {
                throw new RuntimeException("La nouvelle heure de fin doit être après l'heure de début effective.");
            }
            pointage.setHeureFin(request.getNouvelleHeureFin());
            updated = true;
            // Si on met à jour/ajoute une heure de fin, on assume que le pointage est maintenant terminé
            pointage.setStatus(Status.Termine);
        }

        if (updated) {
            // Recalculer les durées si l'heure de fin est présente (soit initialement, soit ajoutée)
            if (pointage.getHeureFin() != null) {
                pointage.recalculerDurees();
            }
            EntreeDeTemps updatedPointage = entreeDeTempsRepository.save(pointage);
            return mapToDto(updatedPointage);
        } else {
            throw new RuntimeException("Aucune date valide fournie pour la modification.");
        }
    }

    public String deleteAllPointages() {
        long count = entreeDeTempsRepository.count();
        // La cascade ALL sur la relation OneToMany dans EntreeDeTemps devrait s'occuper des Pauses.
        entreeDeTempsRepository.deleteAll();
        return count + " entrées de temps supprimées.";
    }
    /*
    /* @Transactional(readOnly = true) // Lecture seule
    public void logActiveTimeEntriesStatus() {
        log.info("--- [SCHEDULED TASK] Début vérification pointages actifs à {} ---", LocalDateTime.now());

        // Statuts considérés comme actifs
        List<Status> activeStatuses = Arrays.asList(Status.En_cours, Status.En_pause);

        try {
            // Récupérer tous les pointages actifs avec les infos de l'employé
            List<EntreeDeTemps> activeEntries = entreeDeTempsRepository.findByStatusInWithEmployee(activeStatuses);

            if (activeEntries.isEmpty()) {
                log.info("--- [SCHEDULED TASK] Aucun pointage actif trouvé.");
            } else {
                log.info("--- [SCHEDULED TASK] {} pointage(s) actif(s) trouvé(s):", activeEntries.size());
                LocalDateTime now = LocalDateTime.now(); // Obtenir l'heure actuelle une seule fois
                for (EntreeDeTemps entry : activeEntries) {
                    // Construction sûre des informations de l'employé
                    String employeeInfo = "Employé ID N/A";
                    if (entry.getEmployee() != null) {
                        employeeInfo = "ID:" + entry.getEmployee().getId();
                        // Ajout du nom/prénom si disponible et non null
                        String firstName = entry.getEmployee().getName(); // Adaptez si les getters sont différents
                        String lastName = entry.getEmployee().getLastName();
                        if (firstName != null && !firstName.isBlank()) {
                            employeeInfo += " (" + firstName;
                            if (lastName != null && !lastName.isBlank()) {
                                employeeInfo += " " + lastName;
                            }
                            employeeInfo += ")";
                        } else if (lastName != null && !lastName.isBlank()) {
                            employeeInfo += " (" + lastName + ")";
                        }
                    }


                    // Calculer la durée écoulée depuis le début (gestion du cas où heureDebut est null)
                    long elapsedMinutes = 0;
                    if (entry.getHeureDebut() != null) {
                        try { // Ajouter un try-catch pour robustesse si 'now' est antérieur à 'heureDebut' (anomalie)
                            elapsedMinutes = Duration.between(entry.getHeureDebut(), now).toMinutes();
                        } catch (Exception e) {
                            log.warn("--- [SCHEDULED TASK] Problème de calcul de durée pour Pointage ID: {}", entry.getId(), e);
                        }
                    }

                    // Log des détails pertinents
                    log.info("  - Pointage ID: {}, Employé: {}, Statut: {}, Début: {}, Durée écoulée (approx): {} min",
                            entry.getId(),
                            employeeInfo,
                            entry.getStatus(),
                            entry.getHeureDebut() != null ? entry.getHeureDebut() : "N/A", // Afficher N/A si null
                            elapsedMinutes >= 0 ? elapsedMinutes : "Calcul impossible" // Gérer durée négative si anomalie
                    );

                    // Loguer les détails de la pause si le statut est En_pause
                    if (entry.getStatus() == Status.En_pause) {
                        // Utiliser la relation déjà chargée (FetchType.LAZY est ok ici car on est dans une transaction)
                        entry.getPauses().stream()
                                .filter(p -> p.getFin() == null) // Trouver la pause active
                                .findFirst()
                                .ifPresentOrElse(
                                        activePause -> {
                                            long pauseElapsed = 0;
                                            if (activePause.getDebut() != null) {
                                                try {
                                                    pauseElapsed = Duration.between(activePause.getDebut(), now).toMinutes();
                                                } catch (Exception e) {
                                                    log.warn("--- [SCHEDULED TASK] Problème de calcul de durée pour Pause ID: {}", activePause.getId(), e);
                                                }
                                            }
                                            log.info("    -> En Pause (Type: {}, Début: {}, Durée pause actuelle: {} min)",
                                                    activePause.getTypePause(),
                                                    activePause.getDebut() != null ? activePause.getDebut() : "N/A",
                                                    pauseElapsed >= 0 ? pauseElapsed : "Calcul impossible");
                                        },
                                        () -> log.warn("    -> Statut En_pause mais aucune pause active trouvée pour Pointage ID: {}", entry.getId()) // Log si anomalie
                                );
                    }
                }
            }
        } catch (Exception e) {
            // Loguer toute erreur survenant pendant l'exécution
            log.error("--- [SCHEDULED TASK] Erreur durant la vérification des pointages actifs: {}", e.getMessage(), e);
        } finally {
            log.info("--- [SCHEDULED TASK] Fin vérification pointages actifs ---");
        }
    }
*/
}
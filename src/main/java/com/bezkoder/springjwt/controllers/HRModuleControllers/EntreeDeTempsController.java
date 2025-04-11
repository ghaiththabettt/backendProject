package com.bezkoder.springjwt.controllers.HRModuleControllers; // Adaptez le nom du package

import com.bezkoder.springjwt.dtos.HRModuleDtos.*;
import com.bezkoder.springjwt.HRModuleServices.EntreeDeTempsService; // Utilisez le service du bon package
import com.bezkoder.springjwt.models.HRModuleEntities.RestrictionsHorloge; // Pour la réponse du dernier mode
import com.bezkoder.springjwt.payload.response.MessageResponse; // Utilisez la classe de réponse standard de Bezkoder/Spring
import com.bezkoder.springjwt.security.services.UserDetailsImpl; // Votre implémentation UserDetails
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat; // Pour LocalDate en RequestParam
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid; // Pour la validation des DTOs d'entrée

import java.time.LocalDate;
import java.util.List;

// Renommer le RequestMapping pour être cohérent avec l'Angular Service original (si nécessaire) ou garder les tirets
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/entreesDeTemps") // ou /api/entrees-de-temps
public class EntreeDeTempsController {

    @Autowired
    private EntreeDeTempsService entreeDeTempsService;

    // Méthode helper pour récupérer l'ID de l'employé connecté
    private Long getCurrentEmployeeId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userDetails.getId(); // Assurez-vous que getId() retourne bien l'ID Employee
        }
        // Gérer le cas où l'utilisateur n'est pas authentifié ou le type est incorrect
        // Vous pourriez lancer une exception spécifique ici si nécessaire
        throw new RuntimeException("Impossible de récupérer l'ID de l'employé connecté.");
    }

    // Classe interne statique pour une réponse API structurée (similaire au code Node.js)
    // Adaptez la clé ('data' ou 'donnees') si nécessaire pour le front-end Angular
    static class ApiResponse<T> {
        public boolean success;
        public String message;
        public T data; // ou 'donnees'

        public ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
        // Ajoutez un getter si la clé doit absolument être 'donnees'
        // public T getDonnees() { return data; }
    }

    // Démarrer un pointage
    @PostMapping("/commencer")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('RESPONSABLE') or hasRole('ADMIN') or hasRole('RH')")
    public ResponseEntity<?> commencerPointage(@Valid @RequestBody StartPointageRequest request) {
        try {
            Long currentUserId = getCurrentEmployeeId();
            // Assurer que l'employé pointe pour lui-même sauf si c'est un rôle supérieur (logique à affiner si nécessaire)
            if (request.getEmployeeId() == null || !request.getEmployeeId().equals(currentUserId)) {
                // Vérifier si l'utilisateur a le droit de pointer pour qqn d'autre
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                boolean isAdminOrHR = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_RH"));
                if (!isAdminOrHR) {
                    request.setEmployeeId(currentUserId); // Force l'ID de l'utilisateur connecté s'il n'est pas Admin/RH
                } else if(request.getEmployeeId() == null) {
                    // Si Admin/RH et pas d'ID fourni, il doit pointer pour lui-même
                    request.setEmployeeId(currentUserId);
                }
                // Si Admin/RH et ID fourni, on suppose qu'il pointe pour cet ID (request.getEmployeeId())
            }

            EntreeDeTempsDTO dto = entreeDeTempsService.commencerPointage(request, currentUserId); // currentUserId est l'ID de l'appelant
            return ResponseEntity.ok(new ApiResponse<>(true, "Pointage commencé avec succès.", dto));
        } catch (RuntimeException e) {
            // Log l'erreur côté serveur pour le débogage
            // log.error("Erreur lors du démarrage du pointage: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            // Catch plus large pour erreurs inattendues
            // log.error("Erreur serveur inattendue lors du démarrage du pointage", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Erreur serveur interne.", null));
        }
    }

    // Mettre en pause un pointage
    @PostMapping("/pause")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('RESPONSABLE') or hasRole('ADMIN') or hasRole('RH')")
    public ResponseEntity<?> mettreEnPause(@Valid @RequestBody PauseRequest request) {
        try {
            // Ajouter vérification : l'utilisateur connecté ne peut mettre en pause que ses propres pointages (sauf Admin/RH)
            // (Logique à implémenter dans le service ou ici)
            EntreeDeTempsDTO dto = entreeDeTempsService.mettreEnPause(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Pointage mis en pause.", dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Erreur serveur interne.", null));
        }
    }

    // Reprendre après une pause
    @PostMapping("/reprendre")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('RESPONSABLE') or hasRole('ADMIN') or hasRole('RH')")
    public ResponseEntity<?> reprendrePointage(@Valid @RequestBody ReprendreRequest request) {
        try {
            // Ajouter vérification : l'utilisateur connecté ne peut reprendre que ses propres pointages (sauf Admin/RH)
            EntreeDeTempsDTO dto = entreeDeTempsService.reprendrePointage(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Pointage repris.", dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Erreur serveur interne.", null));
        }
    }

    // Arrêter un pointage
    @PostMapping("/arreter")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('RESPONSABLE') or hasRole('ADMIN') or hasRole('RH')")
    public ResponseEntity<?> arreterPointage(@Valid @RequestBody StopPointageRequest request) {
        try {
            // Ajouter vérification : l'utilisateur connecté ne peut arrêter que ses propres pointages (sauf Admin/RH)
            EntreeDeTempsDTO dto = entreeDeTempsService.arreterPointage(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Pointage arrêté avec succès.", dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Erreur serveur interne.", null));
        }
    }

    // --- Endpoints Admin/RH ---

    @PutMapping("/modifier-dates")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('RESPONSABLE') or hasRole('ADMIN') or hasRole('RH')")
    public ResponseEntity<?> modifierDatesPointage(@Valid @RequestBody ModifierDatesRequest request) {
        try {
            EntreeDeTempsDTO dto = entreeDeTempsService.modifierDatesPointage(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Dates du pointage mises à jour.", dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Erreur serveur interne.", null));
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('RESPONSABLE') or hasRole('ADMIN') or hasRole('RH')")
    public ResponseEntity<?> getAllPointages() {
        try {
            List<EntreeDeTempsDTO> list = entreeDeTempsService.getAllPointages();
            // Utiliser 'data' comme clé pour correspondre à l'API Node.js originale si besoin
            return ResponseEntity.ok(new ApiResponse<>(true, "Liste de tous les pointages récupérée.", list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Erreur lors de la récupération de tous les pointages.", null));
        }
    }

    @DeleteMapping("/deleteAll")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('RESPONSABLE') or hasRole('ADMIN') or hasRole('RH')")
    public ResponseEntity<?> deleteAllPointages() {
        try {
            String message = entreeDeTempsService.deleteAllPointages();
            // Utiliser MessageResponse ou ApiResponse selon la préférence
            return ResponseEntity.ok(new MessageResponse(message));
            // ou: return ResponseEntity.ok(new ApiResponse<>(true, message, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Erreur lors de la suppression de tous les pointages: " + e.getMessage(), null));
        }
    }

    // --- Endpoints d'Analyse et Configuration ---

    // Obtenir le dernier mode de restriction utilisé (remplace /dernierTypeTemps)
    @GetMapping("/dernierMode")
    @PreAuthorize("isAuthenticated()") // Tout utilisateur connecté peut voir le mode actuel
    public ResponseEntity<?> getDernierModeUtilise() {
        try {
            DernierModeResponseDto response = entreeDeTempsService.getDernierModeUtilise();
            // Structure { success: true, restrictionsHorloge: ... } pour Angular
            // Note: Le DTO DernierModeResponseDto n'a que 'restrictionsHorloge'.
            // Si Angular attend { success: true, restrictionsHorloge: ... }, il faudra l'adapter ou wrapper ici.
            // Solution simple: Wrapper dans ApiResponse
            // return ResponseEntity.ok(new ApiResponse<>(true, "Dernier mode récupéré.", response));
            // Solution directe si le DTO suffit (Angular gère)
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Erreur lors de la récupération du dernier mode.", null));
        }
    }

    // Analyse du temps de travail pour un employé avec dates optionnelles
    @GetMapping("/analyser/{employeeId}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('RESPONSABLE') or hasRole('ADMIN') or hasRole('RH')")
    public ResponseEntity<?> analyserTempsUtilisateur(
            @PathVariable Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            AnalyseTempsResponseDto analyse = entreeDeTempsService.analyserTemps(employeeId, startDate, endDate);
            // Wrapper dans ApiResponse, clé 'donnees' ou 'data' selon besoin Angular
            // Utilisons 'data' pour l'instant
            return ResponseEntity.ok(new ApiResponse<>(true, "Analyse du temps récupérée.", analyse));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Erreur serveur interne lors de l'analyse.", null));
        }
    }
}
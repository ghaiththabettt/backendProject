package com.bezkoder.springjwt.controllers.HRModuleControllers;

import com.bezkoder.springjwt.dtos.HRModuleDtos.PerksDTO;
import com.bezkoder.springjwt.HRModuleServices.PerksService;
import com.bezkoder.springjwt.models.HRModuleEntities.Perks;
import com.bezkoder.springjwt.payload.response.MessageResponse;
import com.bezkoder.springjwt.repository.HRModuleRepository.PerksRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600) // Autorise les requêtes CORS depuis n'importe quelle origine
@RestController
@RequestMapping("/api/perks") // Chemin de base pour toutes les méthodes
public class PerksController {

    private final PerksService perksService;

    public PerksController(PerksService perksService) {
        this.perksService = perksService;
    }

    // CREATE: Ajouter un nouveau perk
    @PostMapping("/create")
    public ResponseEntity<?> createPerks(@RequestBody PerksDTO dto) {
        try {
            System.out.println("Received perksType: " + dto.getPerksType());
            PerksDTO createdPerks = perksService.createPerks(dto);
            return ResponseEntity.ok(createdPerks);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + ex.getMessage()));
        }
    }

    // READ: Récupérer tous les perks
    @GetMapping("/all")
    public ResponseEntity<List<PerksDTO>> getAllPerks() {
        List<PerksDTO> perksList = perksService.getAllPerks();
        return ResponseEntity.ok(perksList);
    }

    // READ: Récupérer un perk par son ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getPerksById(@PathVariable Long id) {
        try {
            PerksDTO perks = perksService.getPerksById(id);
            return ResponseEntity.ok(perks);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + ex.getMessage()));
        }
    }

    // UPDATE: Mettre à jour un perk existant
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePerks(@PathVariable Long id, @RequestBody PerksDTO perksDTO) {
        try {
            PerksDTO updatedPerks = perksService.updatePerks(id, perksDTO);
            return ResponseEntity.ok(updatedPerks);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + ex.getMessage()));
        }
    }

    // DELETE: Supprimer un perk par son ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePerks(@PathVariable Long id) {
        try {
            perksService.deletePerks(id);
            return ResponseEntity.ok(new MessageResponse("Perk deleted successfully"));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + ex.getMessage()));
        }
    }
}
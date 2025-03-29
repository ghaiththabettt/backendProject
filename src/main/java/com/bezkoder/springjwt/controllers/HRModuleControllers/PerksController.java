package com.bezkoder.springjwt.controllers.HRModuleControllers;

import com.bezkoder.springjwt.dtos.HRModuleDtos.PerksDTO;
import com.bezkoder.springjwt.HRModuleServices.PerksService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public class PerksController {
    private final PerksService perksService;

    public PerksController(PerksService perkService) {
        this.perksService = perkService;
    }

    // CREATE
    @PostMapping
    public PerksDTO createPerks(@RequestBody PerksDTO dto) {
        return perksService.createPerks(dto);
    }

    // READ (all)
    @GetMapping
    public List<PerksDTO> getAllPerks() {
        return perksService.getAllPerks();
    }

    // READ (by ID)
    @GetMapping("/{id}")
    public PerksDTO getPerksById(@PathVariable Long id) {
        return perksService.getPerksById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public PerksDTO updatePerks(@PathVariable Long id, @RequestBody PerksDTO dto) {
        return perksService.updatePerks(id, dto);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void deletePerks(@PathVariable Long id) {
        perksService.deletePerks(id);
    }
}

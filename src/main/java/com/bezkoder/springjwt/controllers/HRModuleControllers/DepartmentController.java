package com.bezkoder.springjwt.controllers.HRModuleControllers;

import com.bezkoder.springjwt.dtos.HRModuleDtos.DepartmentDTO;
import com.bezkoder.springjwt.HRModuleServices.DepartmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/Department")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    public DepartmentDTO createDepartment(@RequestBody DepartmentDTO dto) {
        return departmentService.createDepartment(dto);
    }

    @GetMapping
    public List<DepartmentDTO> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    @GetMapping("/{id}")
    public DepartmentDTO getDepartmentById(@PathVariable Long id) {
        return departmentService.getDepartmentById(id);
    }

    @PutMapping("/{id}")
    public DepartmentDTO updateDepartment(@PathVariable Long id, @RequestBody DepartmentDTO dto) {
        return departmentService.updateDepartment(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
    }
}

package com.bezkoder.springjwt.controllers.HRModuleControllers;




import com.bezkoder.springjwt.dtos.HRModuleDtos.ShiftDTO;
import com.bezkoder.springjwt.HRModuleServices.ShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    @Autowired
    private ShiftService shiftService;

    @GetMapping
    public List<ShiftDTO> getAllShifts() {
        return shiftService.getAllShifts();
    }

    @GetMapping("/{id}")
    public ShiftDTO getShiftById(@PathVariable Long id) {
        return shiftService.getShiftById(id);
    }

    @PostMapping
    public ShiftDTO createShift(@RequestBody ShiftDTO shiftDTO) {
        return shiftService.saveShift(shiftDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteShift(@PathVariable Long id) {
        shiftService.deleteShift(id);
    }
}

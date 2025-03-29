package com.bezkoder.springjwt.controllers.HRModuleControllers;


import com.bezkoder.springjwt.dtos.HRModuleDtos.ExpenseDTO;
import com.bezkoder.springjwt.HRModuleServices.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // Get all expenses
    @GetMapping
    public ResponseEntity<List<ExpenseDTO>> getAllExpenses() {
        List<ExpenseDTO> expenses = expenseService.getAllExpenses();
        return new ResponseEntity<>(expenses, HttpStatus.OK);
    }

    // Get expense by ID
    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseDTO> getExpenseById(@PathVariable Long expenseId) {
        ExpenseDTO expenseDTO = expenseService.getExpenseById(expenseId);
        if (expenseDTO == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(expenseDTO, HttpStatus.OK);
    }

    // Create a new expense
    @PostMapping
    public ResponseEntity<ExpenseDTO> createExpense(@RequestBody ExpenseDTO expenseDTO) {
        ExpenseDTO createdExpense = expenseService.createExpense(expenseDTO);
        return new ResponseEntity<>(createdExpense, HttpStatus.CREATED);
    }

    // Update an existing expense
    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseDTO> updateExpense(@PathVariable Long expenseId, @RequestBody ExpenseDTO expenseDTO) {
        ExpenseDTO updatedExpense = expenseService.updateExpense(expenseId, expenseDTO);
        if (updatedExpense == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(updatedExpense, HttpStatus.OK);
    }

    // Delete an expense
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long expenseId) {
        try {
            expenseService.deleteExpense(expenseId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}

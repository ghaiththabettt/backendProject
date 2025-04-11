package com.bezkoder.springjwt.controllers.HRModuleControllers;


import com.bezkoder.springjwt.dtos.HRModuleDtos.ExpenseDTO;
import com.bezkoder.springjwt.HRModuleServices.ExpenseService;
import com.bezkoder.springjwt.models.HRModuleEntities.Expense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "*", maxAge = 3600) // Autorise les requêtes CORS depuis n'importe quelle origine
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
    public ResponseEntity<Expense> createExpense(@RequestBody ExpenseDTO expenseDTO) {
        try {
            System.out.println("Received ExpenseDTO: " + expenseDTO);  // Ajout du log
            Expense expense = expenseService.createExpense(expenseDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(expense);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }



    // Update an existing expense
    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@PathVariable Long id, @RequestBody ExpenseDTO expenseDTO) {
        try {
            Expense updatedExpense = expenseService.updateExpense(id, expenseDTO);
            return ResponseEntity.status(HttpStatus.OK).body(updatedExpense);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
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

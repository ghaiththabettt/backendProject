package com.bezkoder.springjwt.HRModuleServices;

import com.bezkoder.springjwt.dtos.HRModuleDtos.ExpenseDTO;
import com.bezkoder.springjwt.models.HRModuleEntities.Expense;
import com.bezkoder.springjwt.models.HRModuleEntities.StatusExpense;
import com.bezkoder.springjwt.models.HRModuleEntities.TypeExpense;
import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.repository.HRModuleRepository.ExpenseRepository;
import com.bezkoder.springjwt.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository, EmployeeRepository employeeRepository) {
        this.expenseRepository = expenseRepository;
        this.employeeRepository = employeeRepository;
    }

    // Get all expenses
    public List<ExpenseDTO> getAllExpenses() {
        return expenseRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get expense by ID
    public ExpenseDTO getExpenseById(Long expenseId) {
        Optional<Expense> expense = expenseRepository.findById(expenseId);
        return expense.map(this::convertToDTO).orElse(null);
    }

    // Create a new expense
    public ExpenseDTO createExpense(ExpenseDTO expenseDTO) {
        Employee employee = employeeRepository.findById(expenseDTO.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        Expense expense = new Expense();
        expense.setAmount(expenseDTO.getAmount());
        expense.setDate(expenseDTO.getDate());
        expense.setType(TypeExpense.valueOf(expenseDTO.getType().toUpperCase()));
        expense.setStatus(StatusExpense.valueOf(expenseDTO.getStatus().toUpperCase()));
        expense.setEmployee(employee);

        Expense savedExpense = expenseRepository.save(expense);
        return convertToDTO(savedExpense);
    }

    // Update an existing expense
    public ExpenseDTO updateExpense(Long expenseId, ExpenseDTO expenseDTO) {
        Expense existingExpense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        Employee employee = employeeRepository.findById(expenseDTO.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        existingExpense.setAmount(expenseDTO.getAmount());
        existingExpense.setDate(expenseDTO.getDate());
        existingExpense.setType(TypeExpense.valueOf(expenseDTO.getType().toUpperCase()));
        existingExpense.setStatus(StatusExpense.valueOf(expenseDTO.getStatus().toUpperCase()));
        existingExpense.setEmployee(employee);

        Expense updatedExpense = expenseRepository.save(existingExpense);
        return convertToDTO(updatedExpense);
    }

    // Delete an expense
    public void deleteExpense(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
        expenseRepository.delete(expense);
    }

    // Helper method to convert Expense entity to DTO
    private ExpenseDTO convertToDTO(Expense expense) {
        ExpenseDTO expenseDTO = new ExpenseDTO();
        expenseDTO.setExpenseId(expense.getExpenseId());
        expenseDTO.setType(expense.getType().name());
        expenseDTO.setAmount(expense.getAmount());
        expenseDTO.setDate(expense.getDate());
        expenseDTO.setStatus(expense.getStatus().name());
        expenseDTO.setEmployeeId(expense.getEmployee().getId());
        return expenseDTO;
    }
}

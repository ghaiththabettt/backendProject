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
    public Expense createExpense(ExpenseDTO expenseDTO) {
        // Récupérer l'employé à partir de son ID
        Optional<Employee> employeeOpt = employeeRepository.findById(expenseDTO.getEmployeeId());
        if (!employeeOpt.isPresent()) {
            throw new IllegalArgumentException("Employee not found");
        }

        Employee employee = employeeOpt.get();

        // Créer une nouvelle instance de Expense et la remplir avec les données du DTO
        Expense expense = new Expense();
        expense.setType(TypeExpense.valueOf(expenseDTO.getType()));
        expense.setAmount(expenseDTO.getAmount());
        expense.setDate(expenseDTO.getDate());
        expense.setStatus(StatusExpense.valueOf(expenseDTO.getStatus()));
        expense.setEmployee(employee);

        // Sauvegarder la dépense dans la base de données
        return expenseRepository.save(expense);
    }

    // Méthode pour mettre à jour une dépense
    public Expense updateExpense(Long expenseId, ExpenseDTO expenseDTO) {
        // Récupérer la dépense existante
        Optional<Expense> existingExpenseOpt = expenseRepository.findById(expenseId);
        if (!existingExpenseOpt.isPresent()) {
            throw new IllegalArgumentException("Expense not found");
        }

        Expense existingExpense = existingExpenseOpt.get();

        // Mettre à jour les champs de la dépense
        existingExpense.setType(TypeExpense.valueOf(expenseDTO.getType()));
        existingExpense.setAmount(expenseDTO.getAmount());
        existingExpense.setDate(expenseDTO.getDate());
        existingExpense.setStatus(StatusExpense.valueOf(expenseDTO.getStatus()));

        // Récupérer et associer l'employé
        Optional<Employee> employeeOpt = employeeRepository.findById(expenseDTO.getEmployeeId());
        if (!employeeOpt.isPresent()) {
            throw new IllegalArgumentException("Employee not found");
        }
        existingExpense.setEmployee(employeeOpt.get());

        // Sauvegarder la dépense mise à jour
        return expenseRepository.save(existingExpense);
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

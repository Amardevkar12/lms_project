package com.lms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.lms.entity.Employee;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // ✅ Active employees
    List<Employee> findByActiveTrue();

    // ✅ Active employees excluding emails (on leave)
    List<Employee> findByActiveTrueAndEmailNotIn(List<String> emails);

    // ✅ Count active employees
    long countByActiveTrue();

    // ✅ Find by email
    Employee findByEmail(String email);

    // ✅ Check duplicate email
    boolean existsByEmail(String email);
}
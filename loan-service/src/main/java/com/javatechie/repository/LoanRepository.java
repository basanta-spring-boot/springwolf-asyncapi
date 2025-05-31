package com.javatechie.repository;

import com.javatechie.entity.LoanDO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<LoanDO, Long> {
}

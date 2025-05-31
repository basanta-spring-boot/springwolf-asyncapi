package com.javatechie.service;

import com.javatechie.constants.LoanStatus;
import com.javatechie.entity.LoanDO;
import com.javatechie.events.LoanApplicationSubmitEvent;
import com.javatechie.publisher.LoanSubmitEventPublisher;
import com.javatechie.repository.LoanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class LoanService {


    private final LoanRepository loanRepository;

    private final LoanSubmitEventPublisher loanSubmitEventPublisher;

    public LoanService(LoanRepository loanRepository,
                       LoanSubmitEventPublisher loanSubmitEventPublisher) {
        this.loanRepository = loanRepository;
        this.loanSubmitEventPublisher = loanSubmitEventPublisher;
    }

    public String processLoanApplication(LoanDO loanDO) {

        // Generate transaction ID
        String loanTransactionId = UUID.randomUUID().toString().split("-")[0];
        loanDO.setLoanTransactionId(loanTransactionId);
        loanDO.setStatus(LoanStatus.PENDING);

        log.info("Starting loan application process | transactionId: {}", loanTransactionId);

        // Save loan with initial status PENDING
        LoanDO savedLoan = loanRepository.save(loanDO);

        // Prepare kafka event for downstream services
        LoanApplicationSubmitEvent event = LoanApplicationSubmitEvent.builder()
                .loanId(savedLoan.getLoanId())
                .userId(savedLoan.getUserId())
                .amount(savedLoan.getAmount())
                .transactionId(savedLoan.getLoanTransactionId())
                .build();

        // Publish to Kafka
        loanSubmitEventPublisher
                .publishLoanSubmitKafkaEvent(event);

        return loanTransactionId;
    }


    public Optional<LoanDO> getLoanStatusById(Long loanId) {
        return loanRepository.findById(loanId);
    }

}

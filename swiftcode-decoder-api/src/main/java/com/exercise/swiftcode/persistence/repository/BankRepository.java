package com.exercise.swiftcode.persistence.repository;

import com.exercise.swiftcode.persistence.entity.Bank;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BankRepository extends MongoRepository<Bank, String> {
}
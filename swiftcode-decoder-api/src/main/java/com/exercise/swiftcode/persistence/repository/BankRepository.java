package com.exercise.swiftcode.persistence.repository;

import com.exercise.swiftcode.persistence.entity.Bank;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BankRepository extends MongoRepository<Bank, String> {
    boolean existsBySwiftCode(String swiftCode);
    void deleteBySwiftCode(String swiftCode);
    void deleteBySwiftCodeStartingWith(String prefix);
    List<Bank> findBySwiftCodeStartingWith(String swiftCodePrefix);
    Optional<Bank> findBySwiftCode(String swiftCode);
    List<Bank> findByCountryIso2Code(String countryISO2);
}
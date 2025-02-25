package com.exercise.swiftcode.persistence.repository;

import com.exercise.swiftcode.persistence.entity.CountryCode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CountryCodeRepository extends MongoRepository<CountryCode, String> {
    Optional<CountryCode> findByCountryIso2Code(String iso2Code);
}
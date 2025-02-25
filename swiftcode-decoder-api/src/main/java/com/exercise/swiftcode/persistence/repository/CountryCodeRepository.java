package com.exercise.swiftcode.persistence.repository;

import com.exercise.swiftcode.persistence.entity.CountryCode;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CountryCodeRepository extends MongoRepository<CountryCode, String> {
}
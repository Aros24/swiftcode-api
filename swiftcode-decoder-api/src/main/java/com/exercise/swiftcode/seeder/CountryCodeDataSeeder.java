package com.exercise.swiftcode.seeder;

import com.exercise.swiftcode.persistence.entity.CountryCode;
import com.exercise.swiftcode.persistence.repository.CountryCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class CountryCodeDataSeeder implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(CountryCodeDataSeeder.class);

    private final CountryCodeRepository countryRepository;
    private final MongoTemplate mongoTemplate;

    public CountryCodeDataSeeder(CountryCodeRepository countryRepository, MongoTemplate mongoTemplate) {
        this.countryRepository = countryRepository;
        this.mongoTemplate = mongoTemplate;
        logger.info("CountryCsvDataLoader created");
    }

    @Override
    public void run(String... args) {
        try {
            if (mongoTemplate.collectionExists("country_codes")) {
                logger.info("Collection 'country_codes' already exists. Skipping country data load from Locale.");
                return;
            }
        } catch (DataAccessResourceFailureException e) {
            logger.error("MongoDB unavailable. Skipping country data load.", e);
            return;
        }

        List<CountryCode> countries = loadCountriesFromLocale();
        if (countries.isEmpty()) {
            logger.warn("No country records generated from Locale.");
            return;
        }

        countryRepository.saveAll(countries);
        logger.info("Successfully loaded {} country records from Locale.", countries.size());
    }

    private List<CountryCode> loadCountriesFromLocale() {
        List<CountryCode> countries = new ArrayList<>();
        try {
            String[] isoCountries = Locale.getISOCountries();
            for (String isoCode : isoCountries) {
                Locale locale = new Locale.Builder()
                        .setRegion(isoCode)
                        .build();
                String countryName = locale.getDisplayCountry(Locale.ENGLISH);
                if (isNullOrEmpty(countryName) || isNullOrEmpty(isoCode)) {
                    logger.warn("Skipping invalid country data - ISO2: {}, Name: {}", isoCode, countryName);
                    continue;
                }
                CountryCode country = new CountryCode(
                        null,
                        countryName.toUpperCase(),
                        isoCode.toUpperCase()
                );
                countries.add(country);
            }
        } catch (Exception e) {
            logger.error("Failed to generate country data from Locale", e);
        }
        return countries;
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
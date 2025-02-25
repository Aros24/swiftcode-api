package com.exercise.swiftcode.seeder;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.exercise.swiftcode.persistence.entity.CountryCode;
import com.exercise.swiftcode.persistence.repository.CountryCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doThrow;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CountryCodeDataSeederTest {

    private CountryCodeDataSeeder countryCodeDataSeeder;

    @Mock
    private CountryCodeRepository countryRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Captor
    private ArgumentCaptor<ILoggingEvent> logCaptor;

    @BeforeEach
    void setUp() {
        countryCodeDataSeeder = new CountryCodeDataSeeder(countryRepository, mongoTemplate);
        Logger logger = (Logger) LoggerFactory.getLogger(CountryCodeDataSeeder.class);
        logger.addAppender(mockAppender);
    }

    @Test
    void testRun_SkipsLoading_WhenCollectionExists() {
        when(mongoTemplate.collectionExists("country_codes")).thenReturn(true);

        countryCodeDataSeeder.run();

        verify(mockAppender, atLeastOnce()).doAppend(logCaptor.capture());
        assertTrue(logCaptor.getAllValues().stream()
                .anyMatch(event -> event.getFormattedMessage().contains("Collection 'country_codes' already exists")));
        verifyNoInteractions(countryRepository);
    }

    @Test
    void testRun_SkipsLoading_WhenMongoDBUnavailable() {
        when(mongoTemplate.collectionExists("country_codes")).thenThrow(new DataAccessResourceFailureException("MongoDB down"));

        countryCodeDataSeeder.run();

        verify(mockAppender, atLeastOnce()).doAppend(logCaptor.capture());
        assertTrue(logCaptor.getAllValues().stream()
                .anyMatch(event -> event.getFormattedMessage().contains("MongoDB unavailable")));
        verifyNoInteractions(countryRepository);
    }

    @Test
    void testRun_LoadsCountryCodes_WhenCollectionDoesNotExist() {
        when(mongoTemplate.collectionExists("country_codes")).thenReturn(false);
        when(countryRepository.saveAll(any())).thenReturn(Collections.emptyList());

        countryCodeDataSeeder.run();

        verify(mockAppender, atLeastOnce()).doAppend(logCaptor.capture());
        assertTrue(logCaptor.getAllValues().stream()
                .anyMatch(event -> event.getFormattedMessage().contains("Successfully loaded")));
        verify(countryRepository, times(1)).saveAll(any());
    }

    @Test
    void testLoadCountriesFromLocale_ReturnsListOfCountries() {
        List<CountryCode> countries = countryCodeDataSeeder.loadCountriesFromLocale();
        assertFalse(countries.isEmpty());
    }

    @Test
    void testLoadCountriesFromLocale_HandlesException() {
        CountryCodeDataSeeder seeder = spy(countryCodeDataSeeder);
        doThrow(new RuntimeException("Locale failure"))
                .when(seeder).loadCountriesFromLocale();

        assertThrows(RuntimeException.class, seeder::loadCountriesFromLocale);
    }

    @Test
    void testIsNullOrEmpty() {
        assertTrue(countryCodeDataSeeder.isNullOrEmpty(null));
        assertTrue(countryCodeDataSeeder.isNullOrEmpty(""));
        assertFalse(countryCodeDataSeeder.isNullOrEmpty("Test"));
    }
}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doThrow;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void testRun_SkipsLoading_WhenCollectionExists() throws Exception {
        // Given
        when(mongoTemplate.collectionExists("country_codes")).thenReturn(true);

        // When
        countryCodeDataSeeder.run();

        // Then
        verify(mockAppender, atLeastOnce()).doAppend(logCaptor.capture());
        assertTrue(logCaptor.getAllValues().stream()
                .anyMatch(event -> event.getFormattedMessage().contains("Collection 'country_codes' already exists")));
        verifyNoInteractions(countryRepository);
    }

    @Test
    void testRun_SkipsLoading_WhenMongoDBUnavailable() {
        // Given
        when(mongoTemplate.collectionExists("country_codes")).thenThrow(new DataAccessResourceFailureException("MongoDB down"));

        // When & Then
        DataAccessResourceFailureException exception = assertThrows(
                DataAccessResourceFailureException.class,
                () -> countryCodeDataSeeder.run()
        );
        assertEquals("MongoDB down", exception.getMessage());

        verify(mockAppender, atLeastOnce()).doAppend(logCaptor.capture());
        assertTrue(logCaptor.getAllValues().stream()
                .anyMatch(event -> event.getFormattedMessage().contains("MongoDB unavailable")));
        verifyNoInteractions(countryRepository);
    }

    @Test
    void testRun_LoadsCountryCodes_WhenCollectionDoesNotExist() throws Exception {
        // Given
        when(mongoTemplate.collectionExists("country_codes")).thenReturn(false);
        when(countryRepository.saveAll(any())).thenReturn(Collections.emptyList());

        // When
        countryCodeDataSeeder.run();

        // Then
        verify(mockAppender, atLeastOnce()).doAppend(logCaptor.capture());
        assertTrue(logCaptor.getAllValues().stream()
                .anyMatch(event -> event.getFormattedMessage().contains("Successfully loaded")));
        verify(countryRepository, times(1)).saveAll(any());
    }

    @Test
    void testRun_ThrowsIllegalStateException_WhenNoCountriesLoaded() throws Exception {
        // Given
        when(mongoTemplate.collectionExists("country_codes")).thenReturn(false);
        CountryCodeDataSeeder spySeeder = spy(countryCodeDataSeeder);
        doReturn(new ArrayList<CountryCode>()).when(spySeeder).loadCountriesFromLocale();

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                spySeeder::run
        );
        assertEquals("No country data available for seeding", exception.getMessage());

        verify(mockAppender, atLeastOnce()).doAppend(logCaptor.capture());
        assertTrue(logCaptor.getAllValues().stream()
                .anyMatch(event -> event.getFormattedMessage().contains("No country records generated from Locale")));
        verifyNoInteractions(countryRepository);
    }

    @Test
    void testRun_ThrowsException_WhenLoadCountriesFails() throws Exception {
        // Given
        Logger logger = (Logger) LoggerFactory.getLogger(CountryCodeDataSeeder.class);
        logger.detachAndStopAllAppenders();
        logger.addAppender(mockAppender);
        when(mongoTemplate.collectionExists("country_codes")).thenReturn(false);

        try (var mockedStatic = mockStatic(Locale.class)) {
            RuntimeException mockException = new RuntimeException("Locale data unavailable");
            mockedStatic.when(Locale::getISOCountries).thenThrow(mockException);

            // When & Then
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> countryCodeDataSeeder.run()
            );
            assertEquals("Locale data unavailable", exception.getMessage());
            verify(mockAppender, atLeastOnce()).doAppend(logCaptor.capture());
            assertTrue(logCaptor.getAllValues().stream()
                    .anyMatch(event -> event.getFormattedMessage().contains("Failed to generate country data from Locale")));
            verifyNoInteractions(countryRepository);
        }
    }

    @Test
    void testLoadCountriesFromLocale_ReturnsListOfCountries() {
        // When & Then
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

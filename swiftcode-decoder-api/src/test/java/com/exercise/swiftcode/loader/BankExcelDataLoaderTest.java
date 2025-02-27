package com.exercise.swiftcode.loader;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.exercise.swiftcode.persistence.entity.Bank;
import com.exercise.swiftcode.persistence.repository.BankRepository;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.Captor;
import org.mockito.ArgumentCaptor;

import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.eq;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
public class BankExcelDataLoaderTest {

    @Mock
    private BankRepository bankRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Resource resource;

    @Mock
    private Workbook workbook;

    @Mock
    private Sheet sheet;

    @Mock
    private Row headerRow;

    @Mock
    private Row dataRow;

    @Mock
    private InputStream inputStream;

    @Spy
    @InjectMocks
    private BankExcelDataLoader bankExcelDataLoader;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Captor
    private ArgumentCaptor<ILoggingEvent> logCaptor;

    private static final String HEADER_COUNTRY_ISO2 = "COUNTRY ISO2 CODE";
    private static final String HEADER_SWIFT_CODE = "SWIFT CODE";
    private static final String HEADER_NAME = "NAME";
    private static final String HEADER_ADDRESS = "ADDRESS";
    private static final String HEADER_COUNTRY_NAME = "COUNTRY NAME";

    private static final String BANK_NAME = "Bank Name";
    private static final String BANK_ADDRESS = "Bank Address";
    private static final String BANK_COUNTRY_NAME = "COUNTRY NAME";
    private static final String BANK_COUNTRY_CODE = "CN";
    private static final String BANK_SWIFT_CODE = "TESTUS33XXX";

    @BeforeEach
    void setUp() {
        bankExcelDataLoader.setResourceLoader(resourceLoader);
        Logger logger = (Logger) LoggerFactory.getLogger(BankExcelDataLoader.class);
        logger.addAppender(mockAppender);

        lenient().when(resourceLoader.getResource("classpath:data/swift_codes.xlsx")).thenReturn(resource);
        lenient().when(resource.exists()).thenReturn(true);
    }

    @Test
    void testRun_SuccessfulLoad() throws Exception {
        // Given
        when(resourceLoader.getResource("classpath:data/swift_codes.xlsx")).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(mongoTemplate.collectionExists("banks")).thenReturn(false);

        List<Bank> mockBanks = List.of(
                Bank.builder().swiftCode(BANK_SWIFT_CODE).countryIso2Code(BANK_COUNTRY_CODE).name(BANK_NAME).address(BANK_ADDRESS).countryName(BANK_COUNTRY_NAME).build()
        );

        doReturn(mockBanks).when(bankExcelDataLoader).loadBanksFromExcel(resource);

        // When
        bankExcelDataLoader.run();

        // Then
        verify(bankRepository).saveAll(mockBanks);
        verifyNoMoreInteractions(bankRepository);
    }

    @Test
    void testRun_CollectionExists_SkipsLoad() throws Exception {
        // Given
        when(mongoTemplate.collectionExists("banks")).thenReturn(true);

        // When
        bankExcelDataLoader.run();

        // Then
        verify(mockAppender, atLeastOnce()).doAppend(logCaptor.capture());
        assertTrue(logCaptor.getAllValues().stream()
                .anyMatch(event -> event.getFormattedMessage().contains("Collection 'banks' already exists")));
        verifyNoInteractions(bankRepository);
    }

    @Test
    void testLoadBanksFromExcel_Success() throws Exception {
        // Given
        when(resource.getInputStream()).thenReturn(inputStream);
        try (var workbookFactoryMock = mockStatic(WorkbookFactory.class)) {
            workbookFactoryMock.when(() -> WorkbookFactory.create(inputStream)).thenReturn(workbook);

            when(workbook.getNumberOfSheets()).thenReturn(1);
            when(workbook.getSheetAt(0)).thenReturn(sheet);
            when(sheet.iterator()).thenReturn(List.of(headerRow, dataRow).iterator());

            Map<Integer, String> headerMapping = new HashMap<>();
            headerMapping.put(0, HEADER_SWIFT_CODE);
            doReturn(headerMapping).when(bankExcelDataLoader).createHeaderMapping(eq(headerRow), any(DataFormatter.class));

            Bank mockBank = Bank.builder().swiftCode(BANK_SWIFT_CODE).build();
            doReturn(mockBank).when(bankExcelDataLoader).processRow(eq(dataRow), eq(headerMapping), any(DataFormatter.class));

            // When
            List<Bank> banks = bankExcelDataLoader.loadBanksFromExcel(resource);

            // Then
            assertEquals(1, banks.size());
            assertEquals(mockBank, banks.getFirst());
        }
    }

    @Test
    void testRun_ThrowsException_WhenMongoDBUnavailable() {
        // Given
        DataAccessResourceFailureException mockException = new DataAccessResourceFailureException("MongoDB down");
        when(mongoTemplate.collectionExists("banks")).thenThrow(mockException);

        // When & Then
        DataAccessResourceFailureException exception = assertThrows(
                DataAccessResourceFailureException.class,
                () -> bankExcelDataLoader.run()
        );
        assertEquals("MongoDB down", exception.getMessage());
        verify(mockAppender, atLeastOnce()).doAppend(logCaptor.capture());
        assertTrue(logCaptor.getAllValues().stream()
                .anyMatch(event -> event.getFormattedMessage().contains("MongoDB unavailable")));
        verifyNoInteractions(bankRepository);
    }

    @Test
    void testRun_ThrowsException_WhenExcelFileNotFound() throws Exception {
        // Given
        when(mongoTemplate.collectionExists("banks")).thenReturn(false);
        when(resource.exists()).thenReturn(false);

        // When & Then
        FileNotFoundException exception = assertThrows(
                FileNotFoundException.class,
                () -> bankExcelDataLoader.run()
        );
        assertEquals("Excel file not found at classpath:data/swift_codes.xlsx", exception.getMessage());
        verify(mockAppender, atLeastOnce()).doAppend(logCaptor.capture());
        assertTrue(logCaptor.getAllValues().stream()
                .anyMatch(event -> event.getFormattedMessage().contains("Excel file not found at classpath:data/swift_codes.xlsx")));
        verifyNoInteractions(bankRepository);
    }

    @Test
    void testRun_ThrowsException_WhenLoadBanksFails() throws Exception {
        // Given
        Logger logger = (Logger) LoggerFactory.getLogger(BankExcelDataLoader.class);
        logger.detachAndStopAllAppenders();
        logger.addAppender(mockAppender);
        when(mongoTemplate.collectionExists("banks")).thenReturn(false);
        when(resource.getInputStream()).thenReturn(inputStream);

        try (var workbookFactoryMock = mockStatic(WorkbookFactory.class)) {
            IOException mockException = new IOException("Failed to read Excel");
            workbookFactoryMock.when(() -> WorkbookFactory.create(inputStream)).thenThrow(mockException);

            // When & Then
            IOException exception = assertThrows(
                    IOException.class,
                    () -> bankExcelDataLoader.run()
            );
            assertEquals("Failed to read Excel", exception.getMessage());
            verify(mockAppender, atLeastOnce()).doAppend(logCaptor.capture());
            assertTrue(logCaptor.getAllValues().stream()
                    .anyMatch(event -> event.getFormattedMessage().contains("Failed to read Excel file")));
            verifyNoInteractions(bankRepository);
        }
    }

    @Test
    void testLoadBanksFromExcel_NoSheets() throws Exception {
        // Given
        when(resource.getInputStream()).thenReturn(inputStream);
        try (var workbookFactoryMock = mockStatic(WorkbookFactory.class)) {
            workbookFactoryMock.when(() -> WorkbookFactory.create(inputStream)).thenReturn(workbook);
            when(workbook.getNumberOfSheets()).thenReturn(0);

            // When
            List<Bank> banks = bankExcelDataLoader.loadBanksFromExcel(resource);

            // Then
            assertTrue(banks.isEmpty());
        }
    }

    @Test
    void testCreateHeaderMapping() {
        // Given
        Cell cell1 = mock(Cell.class);
        Cell cell2 = mock(Cell.class);
        when(headerRow.iterator()).thenReturn(List.of(cell1, cell2).iterator());
        when(cell1.getColumnIndex()).thenReturn(0);
        when(cell2.getColumnIndex()).thenReturn(1);

        DataFormatter dataFormatter = mock(DataFormatter.class);
        when(dataFormatter.formatCellValue(cell1)).thenReturn(HEADER_SWIFT_CODE);
        when(dataFormatter.formatCellValue(cell2)).thenReturn(HEADER_NAME);

        // When
        Map<Integer, String> headerMapping = bankExcelDataLoader.createHeaderMapping(headerRow, dataFormatter);

        // Then
        assertEquals(2, headerMapping.size());
        assertEquals(HEADER_SWIFT_CODE, headerMapping.get(0));
        assertEquals(HEADER_NAME, headerMapping.get(1));
    }

    @Test
    void testProcessRow_ValidData() {
        // Given
        Map<Integer, String> headerMapping = new HashMap<>();
        headerMapping.put(0, HEADER_SWIFT_CODE);
        headerMapping.put(1, HEADER_COUNTRY_ISO2);
        headerMapping.put(2, HEADER_NAME);
        headerMapping.put(3, HEADER_ADDRESS);
        headerMapping.put(4, HEADER_COUNTRY_NAME);

        Cell cell1 = mock(Cell.class);
        Cell cell2 = mock(Cell.class);
        Cell cell3 = mock(Cell.class);
        Cell cell4 = mock(Cell.class);
        Cell cell5 = mock(Cell.class);

        when(dataRow.iterator()).thenReturn(List.of(cell1, cell2, cell3, cell4, cell5).iterator());
        when(cell1.getColumnIndex()).thenReturn(0);
        when(cell2.getColumnIndex()).thenReturn(1);
        when(cell3.getColumnIndex()).thenReturn(2);
        when(cell4.getColumnIndex()).thenReturn(3);
        when(cell5.getColumnIndex()).thenReturn(4);

        DataFormatter dataFormatter = mock(DataFormatter.class);
        when(dataFormatter.formatCellValue(cell1)).thenReturn(BANK_SWIFT_CODE);
        when(dataFormatter.formatCellValue(cell2)).thenReturn(BANK_COUNTRY_CODE);
        when(dataFormatter.formatCellValue(cell3)).thenReturn(BANK_NAME);
        when(dataFormatter.formatCellValue(cell4)).thenReturn(BANK_ADDRESS);
        when(dataFormatter.formatCellValue(cell5)).thenReturn(BANK_COUNTRY_NAME);

        // When
        Bank bank = bankExcelDataLoader.processRow(dataRow, headerMapping, dataFormatter);

        // Then
        assertNotNull(bank);
        assertEquals(BANK_SWIFT_CODE, bank.getSwiftCode());
        assertEquals(BANK_COUNTRY_CODE, bank.getCountryIso2Code());
        assertEquals(BANK_NAME, bank.getName());
        assertEquals(BANK_ADDRESS, bank.getAddress());
        assertEquals(BANK_COUNTRY_NAME, bank.getCountryName());
    }

    @Test
    void testProcessRow_MissingData() {
        // Given
        Map<Integer, String> headerMapping = new HashMap<>();
        headerMapping.put(0, HEADER_SWIFT_CODE);

        Cell cell1 = mock(Cell.class);
        when(dataRow.iterator()).thenReturn(List.of(cell1).iterator());
        when(cell1.getColumnIndex()).thenReturn(0);

        DataFormatter dataFormatter = mock(DataFormatter.class);
        when(dataFormatter.formatCellValue(cell1)).thenReturn(BANK_SWIFT_CODE);

        // When
        Bank bank = bankExcelDataLoader.processRow(dataRow, headerMapping, dataFormatter);

        // Then
        assertNull(bank);
    }
}
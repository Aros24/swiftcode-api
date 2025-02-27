package com.exercise.swiftcode.loader;

import com.exercise.swiftcode.persistence.entity.Bank;
import com.exercise.swiftcode.persistence.repository.BankRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Cell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

@Component
public class BankExcelDataLoader implements CommandLineRunner, ResourceLoaderAware {

    private static final Logger logger = LoggerFactory.getLogger(BankExcelDataLoader.class);

    private final BankRepository bankRepository;
    private final MongoTemplate mongoTemplate;
    private ResourceLoader resourceLoader;

    private static final String HEADER_COUNTRY_ISO2 = "COUNTRY ISO2 CODE";
    private static final String HEADER_SWIFT_CODE = "SWIFT CODE";
    private static final String HEADER_NAME = "NAME";
    private static final String HEADER_ADDRESS = "ADDRESS";
    private static final String HEADER_COUNTRY_NAME = "COUNTRY NAME";

    @Autowired
    public BankExcelDataLoader(BankRepository bankRepository, MongoTemplate mongoTemplate) {
        this.bankRepository = bankRepository;
        this.mongoTemplate = mongoTemplate;
        logger.info("BankExcelDataLoader created");
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            if (mongoTemplate.collectionExists("banks")) {
                logger.info("Collection 'banks' already exists. Skipping Excel data load.");
                return;
            }
        } catch (DataAccessResourceFailureException e) {
            logger.error("MongoDB unavailable. Skipping Excel data load.", e);
            throw e;
        }

        Resource resource = resourceLoader.getResource("classpath:data/swift_codes.xlsx");
        if (!resource.exists()) {
            logger.error("Excel file not found at classpath:data/swift_codes.xlsx");
            throw new FileNotFoundException("Excel file not found at classpath:data/swift_codes.xlsx");
        }

        List<Bank> banks = loadBanksFromExcel(resource);
        if (banks.isEmpty()) {
            logger.warn("No valid bank records found in Excel file.");
            return;
        }

        bankRepository.saveAll(banks);
        logger.info("Successfully loaded {} bank records from Excel.", banks.size());
    }

    protected List<Bank> loadBanksFromExcel(Resource resource) throws Exception {
        List<Bank> banks = new ArrayList<>();
        try (InputStream is = resource.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {
            if (workbook.getNumberOfSheets() == 0) {
                logger.warn("Excel file contains no sheets.");
                return banks;
            }

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) {
                logger.warn("Excel file is empty.");
                return banks;
            }

            DataFormatter dataFormatter = new DataFormatter();
            Row headerRow = rowIterator.next();
            Map<Integer, String> headerMapping = createHeaderMapping(headerRow, dataFormatter);

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Bank bank = processRow(row, headerMapping, dataFormatter);
                if (bank != null) {
                    banks.add(bank);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to read Excel file", e);
            throw e;
        }
        return banks;
    }

    protected Map<Integer, String> createHeaderMapping(Row headerRow, DataFormatter dataFormatter) {
        Map<Integer, String> headerMapping = new HashMap<>();
        for (Cell cell : headerRow) {
            String headerName = dataFormatter.formatCellValue(cell);
            if (!isNullOrEmpty(headerName)) {
                headerMapping.put(cell.getColumnIndex(), headerName.trim().toUpperCase());
            }
        }
        return headerMapping;
    }

    protected Bank processRow(Row row, Map<Integer, String> headerMapping, DataFormatter dataFormatter) {
        Map<String, String> recordMap = new HashMap<>();

        for (Cell cell : row) {
            String header = headerMapping.get(cell.getColumnIndex());
            if (header != null) {
                String cellValue = dataFormatter.formatCellValue(cell);
                recordMap.put(header, cellValue.trim());
            }
        }

        String countryIso2Code = recordMap.get(HEADER_COUNTRY_ISO2);
        String swiftCode = recordMap.get(HEADER_SWIFT_CODE);
        String name = recordMap.get(HEADER_NAME);
        String address = recordMap.get(HEADER_ADDRESS);
        String countryName = recordMap.get(HEADER_COUNTRY_NAME);

        if (isNullOrEmpty(swiftCode) ||
                isNullOrEmpty(countryIso2Code) ||
                isNullOrEmpty(name) ||
                isNullOrEmpty(address) ||
                isNullOrEmpty(countryName)) {
            logger.warn("Skipping record due to missing data at row {}: {}", row.getRowNum(), recordMap);
            return null;
        }

        return Bank.builder()
                .id(null)
                .countryIso2Code(countryIso2Code.toUpperCase())
                .swiftCode(swiftCode.toUpperCase())
                .name(name)
                .address(address)
                .countryName(countryName.toUpperCase())
                .build();
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
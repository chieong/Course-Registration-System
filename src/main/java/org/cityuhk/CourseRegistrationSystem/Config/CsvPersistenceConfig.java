package org.cityuhk.CourseRegistrationSystem.Config;

import org.cityuhk.CourseRegistrationSystem.Repository.Csv.CsvFileStore;
import org.cityuhk.CourseRegistrationSystem.Repository.Csv.CsvIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides shared CSV infrastructure beans when app.persistence.type=csv.
 * The CsvFileStore and CsvIdGenerator are shared across all CSV repository implementations.
 */
@Configuration
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "csv")
public class CsvPersistenceConfig {

    @Bean
    public CsvFileStore csvFileStore(@Value("${app.persistence.csv.dir:data}") String csvDir) {
        return new CsvFileStore(csvDir);
    }

    @Bean
    public CsvIdGenerator csvIdGenerator(CsvFileStore csvFileStore) {
        return new CsvIdGenerator(csvFileStore);
    }
}

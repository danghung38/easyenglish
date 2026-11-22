package com.dxh.Elearning.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class StorySchemaUpgrade implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        String database = jdbcTemplate.execute((ConnectionCallback<String>)
                connection -> connection.getMetaData().getDatabaseProductName());
        if (!"MySQL".equalsIgnoreCase(database) && !"MariaDB".equalsIgnoreCase(database)) {
            return;
        }

        // Hibernate may consider the old TINYTEXT and new LONGTEXT JDBC types equivalent.
        var narrowColumns = jdbcTemplate.queryForList("""
                SELECT COLUMN_NAME
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'stories'
                  AND COLUMN_NAME IN ('content', 'translation')
                  AND DATA_TYPE IN ('tinytext', 'text', 'varchar')
                """, String.class);
        var changes = Stream.of("content", "translation")
                .filter(column -> narrowColumns.stream().anyMatch(column::equalsIgnoreCase))
                .map(column -> "MODIFY COLUMN " + column + " LONGTEXT CHARACTER SET utf8mb4 NOT NULL")
                .toList();

        if (!changes.isEmpty()) {
            jdbcTemplate.execute("ALTER TABLE stories " + String.join(", ", changes));
            log.info("Widened legacy story text columns to LONGTEXT");
        }
    }
}

package com.dpsk.dpsk_quiz_sys_java.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * 数据库优化配置类
 * 在应用启动时自动执行数据库索引优化脚本
 */
// @Component  // 临时禁用以确保应用正常启动
public class DatabaseOptimizationConfig implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseOptimizationConfig.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("开始执行数据库性能优化...");
        
        try {
            // 执行索引创建脚本
            executeIndexCreationScript();
            
            // 验证关键索引是否存在
            validateCriticalIndexes();
            
            logger.info("数据库性能优化完成");
            
        } catch (Exception e) {
            logger.warn("数据库性能优化执行失败，但不影响应用启动: {}", e.getMessage());
            // 不抛出异常，避免影响应用启动
        }
    }
    
    /**
     * 执行索引创建脚本
     */
    private void executeIndexCreationScript() {
        try {
            ClassPathResource resource = new ClassPathResource("db/migration/V1__add_performance_indexes.sql");
            
            if (!resource.exists()) {
                logger.warn("索引创建脚本不存在: db/migration/V1__add_performance_indexes.sql");
                return;
            }
            
            String sql = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
            
            // 分割SQL语句并逐个执行
            String[] statements = sql.split(";");
            int successCount = 0;
            int skipCount = 0;
            
            for (String statement : statements) {
                String trimmedStatement = statement.trim();
                if (!trimmedStatement.isEmpty() && !trimmedStatement.startsWith("--") && !trimmedStatement.startsWith("SELECT")) {
                    try {
                        jdbcTemplate.execute(trimmedStatement);
                        successCount++;
                        logger.debug("成功执行索引创建语句: {}", trimmedStatement.substring(0, Math.min(50, trimmedStatement.length())));
                    } catch (Exception e) {
                        // 索引可能已存在，记录但不中断
                        if (e.getMessage().contains("already exists") || e.getMessage().contains("Duplicate key name")) {
                            skipCount++;
                            logger.debug("索引已存在，跳过: {}", e.getMessage());
                        } else {
                            logger.warn("执行索引创建语句失败: {}, 错误: {}", trimmedStatement.substring(0, Math.min(50, trimmedStatement.length())), e.getMessage());
                        }
                    }
                }
            }
            
            logger.info("索引创建完成 - 成功: {}, 跳过: {}", successCount, skipCount);
            
        } catch (Exception e) {
            logger.error("读取索引创建脚本失败: {}", e.getMessage());
        }
    }
    
    /**
     * 验证关键索引是否存在
     */
    private void validateCriticalIndexes() {
        try {
            // 检查关键索引是否存在
            String[] criticalIndexes = {
                "idx_exam_paper_creator_id",
                "idx_exam_question_paper_id", 
                "idx_exam_answer_user_paper",
                "idx_exam_result_user_paper",
                "idx_kb_document_uploader_id",
                "idx_kb_chunk_document_id"
            };
            
            int existingIndexCount = 0;
            
            for (String indexName : criticalIndexes) {
                try {
                    String checkSql = "SELECT COUNT(*) FROM information_schema.statistics WHERE index_name = ? AND table_schema = DATABASE()";
                    Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, indexName);
                    if (count != null && count > 0) {
                        existingIndexCount++;
                        logger.debug("关键索引存在: {}", indexName);
                    } else {
                        logger.warn("关键索引不存在: {}", indexName);
                    }
                } catch (Exception e) {
                    logger.debug("检查索引失败: {}, 错误: {}", indexName, e.getMessage());
                }
            }
            
            logger.info("关键索引验证完成 - 存在: {}/{}", existingIndexCount, criticalIndexes.length);
            
            // 如果关键索引缺失过多，给出建议
            if (existingIndexCount < criticalIndexes.length * 0.5) {
                logger.warn("关键索引缺失较多，建议手动执行索引创建脚本以提升性能");
            }
            
        } catch (Exception e) {
            logger.warn("验证关键索引失败: {}", e.getMessage());
        }
    }
    
    /**
     * 获取数据库性能统计信息
     */
    public void logDatabaseStats() {
        try {
            // 获取表大小信息
            String tableSizeSql = 
                "SELECT table_name, table_rows, ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'size_mb' " +
                "FROM information_schema.tables " +
                "WHERE table_schema = DATABASE() " +
                "ORDER BY (data_length + index_length) DESC";
            
            logger.info("=== 数据库表大小统计 ===");
            jdbcTemplate.query(tableSizeSql, (rs) -> {
                logger.info("表: {}, 行数: {}, 大小: {} MB", 
                    rs.getString("table_name"), 
                    rs.getLong("table_rows"), 
                    rs.getDouble("size_mb"));
            });
            
            // 获取索引使用情况
            String indexUsageSql = 
                "SELECT table_name, index_name, cardinality " +
                "FROM information_schema.statistics " +
                "WHERE table_schema = DATABASE() AND index_name != 'PRIMARY' " +
                "ORDER BY table_name, cardinality DESC";
            
            logger.info("=== 索引统计信息 ===");
            jdbcTemplate.query(indexUsageSql, (rs) -> {
                logger.info("表: {}, 索引: {}, 基数: {}", 
                    rs.getString("table_name"), 
                    rs.getString("index_name"), 
                    rs.getLong("cardinality"));
            });
            
        } catch (Exception e) {
            logger.debug("获取数据库统计信息失败: {}", e.getMessage());
        }
    }
}
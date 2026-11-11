package net.cocotea.cyreneadmin.autoddl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 启动时执行建表与初始化数据脚本。
 * 脚本需保证幂等（CREATE TABLE IF NOT EXISTS / INSERT IGNORE），以便可重复执行。
 *
 * @author CoCoTea
 */
public final class AutoDdlExecutor {

    private static final Logger logger = LoggerFactory.getLogger(AutoDdlExecutor.class);

    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String FILE_PREFIX = "file:";

    private AutoDdlExecutor() {
    }

    /**
     * 按顺序执行脚本，任意语句失败都会抛出异常终止启动。
     *
     * @param dataSource 数据源，脚本在数据源当前选中的库中执行
     * @param locations  脚本位置，支持 classpath: 和 file: 前缀
     */
    public static void execute(DataSource dataSource, List<String> locations) {
        if (locations == null || locations.isEmpty()) {
            logger.warn("auto-ddl 未配置脚本，跳过执行");
            return;
        }
        int total = 0;
        try (Connection connection = dataSource.getConnection()) {
            for (String location : locations) {
                List<String> statements = SqlScriptParser.split(readScript(location));
                if (statements.isEmpty()) {
                    logger.warn("auto-ddl 脚本内容为空: {}", location);
                    continue;
                }
                for (String sql : statements) {
                    // 库由数据源决定，脚本里的 CREATE DATABASE / USE 仅用于命令行初始化
                    if (isSchemaStatement(sql)) {
                        logger.debug("auto-ddl 跳过库切换语句: {}", sql);
                        continue;
                    }
                    executeStatement(connection, sql, location);
                    total++;
                }
                logger.info("auto-ddl 执行完成: {}，语句数: {}", location, statements.size());
            }
        } catch (SQLException e) {
            throw new IllegalStateException("auto-ddl 获取数据库连接失败: " + e.getMessage(), e);
        }
        logger.info("auto-ddl 全部执行完成，脚本数: {}，语句数: {}", locations.size(), total);
    }

    private static void executeStatement(Connection connection, String sql, String location) {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException("auto-ddl 执行失败[" + location + "]: " + abbreviate(sql), e);
        }
    }

    private static String readScript(String location) {
        try {
            if (location.startsWith(CLASSPATH_PREFIX)) {
                String resource = location.substring(CLASSPATH_PREFIX.length());
                if (resource.startsWith("/")) {
                    resource = resource.substring(1);
                }
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                InputStream inputStream = classLoader != null
                        ? classLoader.getResourceAsStream(resource)
                        : AutoDdlExecutor.class.getResourceAsStream("/" + resource);
                if (inputStream == null) {
                    throw new IllegalStateException("auto-ddl 脚本不存在: " + location);
                }
                try (InputStream stream = inputStream) {
                    return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
            String path = location.startsWith(FILE_PREFIX) ? location.substring(FILE_PREFIX.length()) : location;
            return Files.readString(Path.of(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("auto-ddl 读取脚本失败: " + location, e);
        }
    }

    private static boolean isSchemaStatement(String sql) {
        String normalized = sql.stripLeading().toLowerCase(Locale.ROOT);
        return normalized.startsWith("use ") || normalized.startsWith("create database");
    }

    private static String abbreviate(String sql) {
        String singleLine = sql.replaceAll("\\s+", " ");
        return singleLine.length() > 120 ? singleLine.substring(0, 120) + "..." : singleLine;
    }

    /**
     * 简单的 SQL 脚本拆分器：去除注释，并按引号外的分号拆分语句。
     */
    static final class SqlScriptParser {

        private SqlScriptParser() {
        }

        static List<String> split(String script) {
            List<String> statements = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            int index = 0;
            while (index < script.length()) {
                char c = script.charAt(index);
                char next = index + 1 < script.length() ? script.charAt(index + 1) : '\0';

                if (c == '-' && next == '-') {
                    index = skipLine(script, index);
                    current.append(' ');
                    continue;
                }
                if (c == '#') {
                    index = skipLine(script, index);
                    current.append(' ');
                    continue;
                }
                if (c == '/' && next == '*') {
                    int end = script.indexOf("*/", index + 2);
                    index = end < 0 ? script.length() : end + 2;
                    current.append(' ');
                    continue;
                }
                if (c == '\'' || c == '"' || c == '`') {
                    int end = findQuoteEnd(script, index, c);
                    current.append(script, index, end + 1);
                    index = end + 1;
                    continue;
                }
                if (c == ';') {
                    addStatement(statements, current);
                    index++;
                    continue;
                }
                current.append(c);
                index++;
            }
            addStatement(statements, current);
            return statements;
        }

        private static int skipLine(String script, int start) {
            int end = script.indexOf('\n', start);
            return end < 0 ? script.length() : end + 1;
        }

        private static int findQuoteEnd(String script, int start, char quote) {
            int index = start + 1;
            while (index < script.length()) {
                char c = script.charAt(index);
                if (c == '\\') {
                    index += 2;
                    continue;
                }
                if (c == quote) {
                    if (index + 1 < script.length() && script.charAt(index + 1) == quote) {
                        index += 2;
                        continue;
                    }
                    return index;
                }
                index++;
            }
            throw new IllegalStateException("auto-ddl 脚本存在未闭合的引号: " + quote);
        }

        private static void addStatement(List<String> statements, StringBuilder current) {
            String sql = current.toString().strip();
            if (!sql.isEmpty()) {
                statements.add(sql);
            }
            current.setLength(0);
        }
    }
}

package org.ablonewolf;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        Properties properties = new Properties();

        try {
            properties.load(Files.newInputStream(Path.of("src/main/resources/music.properties"),
                    StandardOpenOption.READ));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        var dataSource = new MysqlDataSource();
        dataSource.setServerName(properties.getProperty("serverName"));
        dataSource.setPort(Integer.parseInt(properties.getProperty("port")));
        dataSource.setDatabaseName(properties.getProperty("databaseName"));

        try (var ignored = dataSource.getConnection(
                properties.getProperty("user"),
                properties.getProperty("password"))) {
            System.out.println("Success!! Connection to music database has been established.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
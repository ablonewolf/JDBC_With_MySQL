package org.ablonewolf;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        Properties properties = new Properties();

        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream("music.properties"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        var dataSource = new MysqlDataSource();
        dataSource.setServerName(properties.getProperty("serverName"));
        dataSource.setPort(Integer.parseInt(properties.getProperty("port")));
        dataSource.setDatabaseName(properties.getProperty("databaseName"));

        try (var connection = dataSource.getConnection(
                properties.getProperty("user"),
                properties.getProperty("password"));
             Statement statement = connection.createStatement()) {
            System.out.println("Success!! Connection to music database has been established.");

            String tableName = "music.artists";
            executeSelect(statement, tableName, null, null);

            String albumName = "Tapestry";
            String columnName = "album_name";
            tableName = "music.albums";
            if (!executeSelect(statement, tableName, columnName, albumName)) {
                System.out.println("Maybe we should add this record");
                insertRecord(statement, tableName, new String[]{columnName},
                        new String[]{albumName});
            }

            albumName = "Mirage";
            columnName = "album_name";
            tableName = "music.albums";
            updateRecord(statement, tableName, columnName, albumName, columnName, albumName.toUpperCase());

            String songTitle = "Sunday morning";
            columnName = "song_title";
            tableName = "music.songs";
            deleteRecord(statement, tableName, columnName, songTitle);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //  method to execute any select statement
    private static boolean executeSelect(Statement statement, String tableName,
                                         String columnName, String columnValue)
            throws SQLException {
        String query;
        if (Objects.nonNull(columnValue) && Objects.nonNull(columnName)) {
            query = "SELECT * FROM %s WHERE %s='%s'"
                    .formatted(tableName, columnName, columnValue);
        } else {
            query = "SELECT * FROM %s".formatted(tableName);
        }

        var rs = statement.executeQuery(query);
        if (rs != null) {
            return printRecords(rs);
        }
        return false;
    }

    //    method to print records using the result set received from the execute query method
    private static boolean printRecords(ResultSet resultSet) throws SQLException {

        boolean foundData = false;
        var meta = resultSet.getMetaData();

        System.out.println("=========================================================");

        for (int i = 1; i <= meta.getColumnCount(); i++) {
            System.out.printf("%-15s", meta.getColumnName(i).toUpperCase());
        }
        System.out.println();

        while (resultSet.next()) {
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                System.out.printf("%-15s", resultSet.getString(i));
            }
            System.out.println();
            foundData = true;
        }
        return foundData;
    }

    //    method to insert new records
    private static boolean insertRecord(Statement statement, String tableName,
                                        String[] columnNames, String[] insertedValues)
            throws SQLException {

        String colNames = String.join(",", columnNames);
        String colValues = String.join("','", insertedValues);
        String query = "INSERT INTO %s (%s) VALUES ('%s')"
                .formatted(tableName, colNames, colValues);
        System.out.println(query);
        boolean insertResult = statement.execute(query);
        int recordsInserted = statement.getUpdateCount();
        if (recordsInserted > 0) {
            executeSelect(statement, tableName,
                    columnNames[0], insertedValues[0]);
        }
        return recordsInserted > 0;
    }

    private static boolean updateRecord(Statement statement, String tableName,
                                        String matchedColumn, String matchedValue,
                                        String updatedColumn, String updatedValue) throws SQLException {
        String query = "UPDATE %s SET %s = '%s' WHERE %s='%s'"
                .formatted(tableName, updatedColumn, updatedValue,
                        matchedColumn, matchedValue);

        System.out.println(query);
        statement.execute(query);
        int recordsUpdated = statement.getUpdateCount();

        if (recordsUpdated > 0) {
            executeSelect(statement, tableName, updatedColumn, updatedValue);
        }

        return recordsUpdated > 0;
    }

    private static boolean deleteRecord(Statement statement, String tableName,
                                        String columnName, String deletedValue)
            throws SQLException {
        String query = "DELETE FROM %s WHERE %s='%s'"
                .formatted(tableName, columnName, deletedValue);

        System.out.println(query);
        statement.execute(query);
        int recordsDeleted = statement.getUpdateCount();

        if (recordsDeleted > 0) {
            executeSelect(statement, tableName, columnName, deletedValue);
        }

        return recordsDeleted > 0;
    }
}
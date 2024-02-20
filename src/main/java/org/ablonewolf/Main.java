package org.ablonewolf;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

        String albumName = "Tapestry";
        String getAllArtistQuery = "select * from music.artists";
        String getAlbumByNameQuery = "select * from music.albumview where album_name='%s'".formatted(albumName);
        try (var connection = dataSource.getConnection(
                properties.getProperty("user"),
                properties.getProperty("password"));
             Statement statement = connection.createStatement()) {
            System.out.println("Success!! Connection to music database has been established.");
//          parsing the sql get result manually
            ResultSet resultSet = statement.executeQuery(getAllArtistQuery);
            System.out.println("Id Artist_Name");
            while (resultSet.next()) {
                System.out.printf("%d %s %n", resultSet.getInt(1), resultSet.getString("artist_name"));
            }
            System.out.println("==============================================================================");
//          parsing the sql get result using metadata object
            resultSet = statement.executeQuery(getAlbumByNameQuery);

            var metaData = resultSet.getMetaData();

            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                System.out.printf("%-15s", metaData.getColumnName(i).toUpperCase());
            }
            System.out.println();

            while (resultSet.next()) {
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    System.out.printf("%-15s", resultSet.getString(i));
                }
                System.out.println();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
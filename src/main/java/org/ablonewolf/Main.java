package org.ablonewolf;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
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
        try {
            dataSource.setContinueBatchOnError(false);
        } catch (SQLException e) {
            System.out.println("Unable to mark continue in case of any batch error.");
        }

        try (var connection = dataSource.getConnection(
                properties.getProperty("user"),
                properties.getProperty("password"));
             Statement statement = connection.createStatement()) {
            System.out.println("Success!! Connection to music database has been established.");

            String tableName = "music.artists";
            executeSelect(statement, tableName, null, null);
            executeSelectUsingPreparedStatement(connection, statement, "music.albumview", "artist_name",
                    "Budgie");

            String artistName = "BassBaba";
            String columnName = "artist_name";
            tableName = "music.artists";
            if (!executeSelect(statement, tableName, columnName, artistName)) {
                System.out.println("Maybe we should add this record");
                insertRecord(statement, tableName, new String[]{columnName},
                        new String[]{artistName});
            }

            String albumName = "Mystica";
            columnName = "album_name";
            tableName = "music.albums";
            updateRecord(statement, tableName, columnName, albumName, columnName, albumName.toUpperCase());

            String songTitle = "Sunday morning";
            columnName = "song_title";
            tableName = "music.songs";
            deleteRecord(statement, tableName, columnName, songTitle);

            String[] songs = new String[]{
                    "Cave In",
                    "The Bird And The Worm",
                    "Hello Seatle",
                    "Umbrella Beach",
                    "Fireflies",
                    "The Tip of the Iceberg",
                    "Vanilla Twilight"
            };
            artistName = "Owl City";
            albumName = "Ocean Eyes";
            insertArtistAlbum(statement, artistName, albumName, songs);

            deleteWholeAlbum(connection, statement, artistName, albumName);

            deleteWholeAlbumAsBatch(connection, statement, artistName, albumName);

            executeSelect(statement, "music.albumview", "album_name", albumName);
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

    private static void executeSelectUsingPreparedStatement(Connection connection, Statement statement,
                                                            String tableName, String columnName,
                                                            String columnValue) throws SQLException {
        String query;
        ResultSet resultSet;
        if (Objects.nonNull(columnValue) && Objects.nonNull(columnName)) {
            query = "SELECT * FROM %s WHERE %s=?"
                    .formatted(tableName, columnName);
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, columnValue);
            resultSet = preparedStatement.executeQuery();
        } else {
            query = "SELECT * FROM %s".formatted(tableName);
            resultSet = statement.executeQuery(query);
        }
        if (resultSet != null) {
            printRecords(resultSet);
        }
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
    private static void insertRecord(Statement statement, String tableName,
                                     String[] columnNames, String[] insertedValues)
            throws SQLException {

        String colNames = String.join(",", columnNames);
        String colValues = String.join("','", insertedValues);
        String query = "INSERT IGNORE INTO %s (%s) VALUES ('%s')"
                .formatted(tableName, colNames, colValues);
        System.out.println(query);
        int recordsInserted = statement.getUpdateCount();
        if (recordsInserted > 0) {
            executeSelect(statement, tableName,
                    columnNames[0], insertedValues[0]);
        }
    }

    private static void updateRecord(Statement statement, String tableName,
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

    }

    private static void deleteRecord(Statement statement, String tableName,
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

    }

    private static void insertArtistAlbum(Statement statement,
                                          String artistName,
                                          String albumName,
                                          String[] songs)
            throws SQLException {

        String artistInsert = "INSERT IGNORE INTO music.artists (artist_name) VALUES (%s)"
                .formatted(statement.enquoteLiteral(artistName));
        System.out.println(artistInsert);
        statement.execute(artistInsert, Statement.RETURN_GENERATED_KEYS);

        ResultSet rs = statement.getGeneratedKeys();
        int artistId = (rs != null && rs.next()) ? rs.getInt(1) : -1;
        String albumInsert = ("INSERT IGNORE INTO music.albums (album_name, artist_id)" +
                " VALUES (%s, %d)")
                .formatted(statement.enquoteLiteral(albumName), artistId);
        System.out.println(albumInsert);
        statement.execute(albumInsert, Statement.RETURN_GENERATED_KEYS);
        rs = statement.getGeneratedKeys();
        int albumId = (rs != null && rs.next()) ? rs.getInt(1) : -1;


        String songInsert = "INSERT IGNORE INTO music.songs " +
                "(track_number, song_title, album_id) VALUES (%d, %s, %d)";

        for (int i = 0; i < songs.length; i++) {
            String songQuery = songInsert.formatted(i + 1,
                    statement.enquoteLiteral(songs[i]), albumId);
            System.out.println(songQuery);

            statement.execute(songQuery);
        }
        executeSelect(statement, "music.albumview", "album_name",
                albumName);
    }

    private static void deleteWholeAlbum(Connection connection, Statement statement,
                                         String artistName, String albumName) throws SQLException {
        System.out.println("Auto commit status: " + connection.getAutoCommit());
        connection.setAutoCommit(false);
        System.out.println("Auto commit status: " + connection.getAutoCommit());
        try {
            String deleteSongs = """
                    DELETE FROM music.songs WHERE album_id =
                    (SELECT ALBUM_ID FROM music.albums WHERE album_name = '%s')
                    """
                    .formatted(albumName);

            int deletedSongs = statement.executeUpdate(deleteSongs);
            System.out.printf("Deleted %d rows from music.songs%n", deletedSongs);

            String deleteAlbum = """
                    DELETE FROM music.albums WHERE album_name = '%s"
                    """.formatted(albumName);
            int deletedAlbums = statement.executeUpdate(deleteAlbum);
            System.out.printf("Deleted %d albums from music.albums%n", deletedAlbums);

            String deleteArtist = """
                    DELETE FROM music.artists WHERE artist_name='%s'
                    """
                    .formatted(artistName);
            int deletedArtists = statement.executeUpdate(deleteArtist);
            System.out.printf("Deleted %d artists from music.artists%n", deletedArtists);
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            connection.rollback();
        } finally {
            connection.setAutoCommit(true);
            System.out.println("Auto commit status: " + connection.getAutoCommit());
        }

    }

    private static void deleteWholeAlbumAsBatch(Connection connection, Statement statement,
                                                String artistName, String albumName) throws SQLException {
        System.out.println("Auto commit status: " + connection.getAutoCommit());
        connection.setAutoCommit(false);
        System.out.println("Auto commit status: " + connection.getAutoCommit());
        try {
            String deleteSongs = """
                    DELETE FROM music.songs WHERE album_id =
                    (SELECT ALBUM_ID FROM music.albums WHERE album_name = '%s')
                    """
                    .formatted(albumName);


            String deleteAlbum = """
                    DELETE FROM music.albums WHERE album_name = '%s"
                    """.formatted(albumName);


            String deleteArtist = """
                    DELETE FROM music.artists WHERE artist_name='%s'
                    """
                    .formatted(artistName);

            statement.addBatch(deleteSongs);
            statement.addBatch(deleteAlbum);
            statement.addBatch(deleteArtist);

            int[] results = statement.executeBatch();
            System.out.println(Arrays.toString(results));
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            connection.rollback();
        } finally {
            connection.setAutoCommit(true);
            System.out.println("Auto commit status: " + connection.getAutoCommit());
        }

    }
}
package org.ablonewolf;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class PreparedStatementMain {

    private static final String ARTIST_INSERT =
            "INSERT INTO music.artists (artist_name) VALUES (?)";
    private static final String ALBUM_INSERT =
            "INSERT INTO music.albums (artist_id, album_name) VALUES (?, ?)";
    private static final String SONG_INSERT =
            "INSERT INTO music.songs (album_id, track_number, song_title) " +
                    "VALUES (?, ?, ?)";

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
                properties.getProperty("password"))) {
            System.out.println("Success!! Connection to music database has been established.");
            addDataFromFile(connection);

            String sql = "SELECT * FROM music.albumview where artist_name = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "Bob Dylan");
            ResultSet resultSet = preparedStatement.executeQuery();
            printRecords(resultSet);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void printRecords(ResultSet resultSet) throws SQLException {
        var meta = resultSet.getMetaData();

        System.out.println("===================");

        for (int i = 1; i <= meta.getColumnCount(); i++) {
            System.out.printf("%-15s", meta.getColumnName(i).toUpperCase());
        }
        System.out.println();

        while (resultSet.next()) {
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                System.out.printf("%-15s", resultSet.getString(i));
            }
            System.out.println();
        }
    }

    private static int addArtist(PreparedStatement ps, String artistName) throws SQLException {
        int artistId = -1;
        ps.setString(1, artistName);
        int insertedCount = ps.executeUpdate();
        if (insertedCount > 0) {
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                artistId = generatedKeys.getInt(1);
                System.out.println("Auto-incremented ID: " + artistId);
            }
        }
        return artistId;
    }

    private static int addAlbum(PreparedStatement ps, int artistId, String albumName) throws SQLException {
        int albumId = -1;
        ps.setInt(1, artistId);
        ps.setString(2, albumName);
        int insertedCount = ps.executeUpdate();
        if (insertedCount > 0) {
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                albumId = generatedKeys.getInt(1);
                System.out.println("Auto-incremented ID: " + albumId);
            }
        }
        return albumId;
    }

    private static void addSong(PreparedStatement ps, int albumId,
                                int trackNo, String songTitle) throws SQLException {
        ps.setInt(1, albumId);
        ps.setInt(2, trackNo);
        ps.setString(3, songTitle);
        ps.addBatch();
    }

    private static void addDataFromFile(Connection connection) throws SQLException {
        List<String> records = new ArrayList<>();

        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("NewAlbums.csv");
        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                // Read and print each line from the file
                String line;
                while ((line = reader.readLine()) != null) {
                    records.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("File not found: NewAlbums.csv");
        }

        String lastAlbum = null;
        String lastArtist = null;
        int artistId = -1;
        int albumId = -1;
        try (PreparedStatement psArtist = connection.prepareStatement(ARTIST_INSERT,
                Statement.RETURN_GENERATED_KEYS);
             PreparedStatement psAlbum = connection.prepareStatement(ALBUM_INSERT,
                     Statement.RETURN_GENERATED_KEYS);
             PreparedStatement psSong = connection.prepareStatement(SONG_INSERT,
                     Statement.RETURN_GENERATED_KEYS)
        ) {
            connection.setAutoCommit(false);

            for (String record : records) {
                String[] columns = record.split(",");
                if (lastArtist == null || !lastArtist.equals(columns[0])) {
                    lastArtist = columns[0];
                    artistId = addArtist(psArtist, lastArtist);
                }
                if (lastAlbum == null || !lastAlbum.equals(columns[1])) {
                    lastAlbum = columns[1];
                    albumId = addAlbum(psAlbum, artistId, lastAlbum);
                }
                addSong(psSong, albumId, Integer.parseInt(columns[2]), columns[3]);
            }
            int[] inserts = psSong.executeBatch();
            int totalInserts = Arrays.stream(inserts).sum();
            System.out.printf("%d song records added %n", inserts.length);
            System.out.printf("%d rows has been added in the database.%n", totalInserts);
            connection.commit();
            connection.setAutoCommit(true);
        }

    }
}

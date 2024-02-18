package org.ablonewolf;

import com.mysql.cj.jdbc.MysqlDataSource;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

public class Main {
    private final static String CONN_STRING = "jdbc:mysql://localhost:3306/music";

    public static void main(String[] args) {

//      parsing the username from the swing prompt
        String username = JOptionPane.showInputDialog(null, "Enter Database Username: ");

        JPasswordField passwordField = new JPasswordField();

//      an int variable to store the okay or cancel option
        int okayCancel = JOptionPane.showConfirmDialog(null, passwordField, "Enter Database Password",
                JOptionPane.OK_CANCEL_OPTION);

        final char[] password = (okayCancel == JOptionPane.OK_OPTION) ? passwordField.getPassword() : null;

        var dataSource = new MysqlDataSource();
//        dataSource.setURL(CONN_STRING);
        dataSource.setServerName("localhost");
        dataSource.setPort(3306);
        dataSource.setDatabaseName("music");
//        try (Connection ignored = DriverManager.getConnection(CONN_STRING, username, String.valueOf(password))) {
//            System.out.println("Success!! Connection made to the music database.");
//            Arrays.fill(password, ' ');
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }

        try {
            try (Connection ignored = dataSource.getConnection(username, String.valueOf(password))) {
                System.out.println("Success!! Connection made to the music database.");
                Arrays.fill(password, ' ');
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }
}
package com.example.java.services;

import com.example.java.entities.User;
import com.example.java.utils.MyDatabase;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private final Connection connection;

    public UserService() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT iduser, nomuser, prenomuser, role FROM `user` ORDER BY prenomuser, nomuser";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("iduser"),
                        rs.getString("nomuser"),
                        rs.getString("prenomuser"),
                        rs.getString("role")
                ));
            }
        }

        return users;
    }
}

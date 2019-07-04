package com.stroganova.movielanddb;

import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

@Repository
public class JdbcRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    int fillRoles(){
        String sql = "INSERT INTO movieland.roles(name) VALUES (:name);";
        return Utils.fill(sql, readNamesFrom("role.txt"), namedParameterJdbcTemplate);
    }

    int fillGenre(){
        String sql = "INSERT INTO movieland.genre(name) VALUES (:name);";
        return Utils.fill(sql, readNamesFrom("genre.txt"), namedParameterJdbcTemplate);
    }

    private List<SqlParameterSource> readNamesFrom(String fileName){
        List<SqlParameterSource> list = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/" + fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!"".equals(line)) {
                    list.add(new MapSqlParameterSource("name", line));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("handle Exception", e);
        }
        return list;
    }

}

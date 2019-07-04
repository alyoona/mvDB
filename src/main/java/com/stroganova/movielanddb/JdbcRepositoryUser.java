package com.stroganova.movielanddb;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcRepositoryUser {


    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public JdbcRepositoryUser(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }


    int fillUser(){
        String sql = "INSERT INTO movieland.users(email, password, first_name, last_name) " +
                "VALUES (:email, :password, :first_name, :last_name);";
        return Utils.fill(sql, readParametersFrom(new String[]{"name", "email", "password"}),namedParameterJdbcTemplate);
    }
//"firstName", "lastName"

    private List<SqlParameterSource> readParametersFrom(String[] lineNamesPerEntity) {
        List<SqlParameterSource> list = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/user.txt"))) {
            int userPropertyCount = 0;
            String line;
            MapSqlParameterSource parameterSource = new MapSqlParameterSource();
            while ((line = reader.readLine()) != null) {
                if (!"".equals(line)) {
                    String paramName = lineNamesPerEntity[userPropertyCount];
                    userPropertyCount++;

                    if("name".equals(paramName)) {
                        parameterSource.addValues(Utils.parseLine(line, new String[]{"first_name", "last_name"}, " "));
                    } else {
                        parameterSource.addValue(paramName, line);
                    }

                    if(userPropertyCount > lineNamesPerEntity.length - 1) {
                        userPropertyCount = 0;
                        list.add(parameterSource);
                        parameterSource = new MapSqlParameterSource();
                    }


                }
            }
        } catch (Exception e) {
            throw new RuntimeException("handle Exception", e);
        }
        return list;
    }







}

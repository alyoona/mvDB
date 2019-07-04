package com.stroganova.movielanddb;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class JdbcRepositoryReview {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final String selectAllMovies = "SELECT id, name_russian as name FROM movieland.movie;";
    private static final String selectAllUsers = "SELECT id, first_name||' '||last_name as name FROM movieland.users;";

    public JdbcRepositoryReview(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    int fillReview() {
        String sql = "INSERT INTO movieland.review (movie_id, user_id, description) " +
                "VALUES (:movie_id, :user_id, :description);";

        return Utils.fill(sql, getReviewSqlParameterSourcesList(new String[]{"movieName", "userName", "description"}), namedParameterJdbcTemplate);
    }

    private List<SqlParameterSource> getReviewSqlParameterSourcesList(String[] lineNamesPerEntity) {
        List<SqlParameterSource> list = new ArrayList<>();
        Map<String, Integer> nameAndIdOfMovieMap = Utils.getNameAndIdMap(selectAllMovies, namedParameterJdbcTemplate);
        Map<String, Integer> nameAndIdOfUserMap = Utils.getNameAndIdMap(selectAllUsers, namedParameterJdbcTemplate);
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/review.txt"))) {
            int reviewPropertyCount = 0;
            String line;
            MapSqlParameterSource parameterSource = new MapSqlParameterSource();
            while ((line = reader.readLine()) != null) {
                if (!"".equals(line)) {
                    String paramName = lineNamesPerEntity[reviewPropertyCount];
                    reviewPropertyCount++;

                    if ("movieName".equals(paramName)) {

                        parameterSource.addValue("movie_id", nameAndIdOfMovieMap.get(line));


                    } else if ("userName".equals(paramName)) {

                        parameterSource.addValue("user_id", nameAndIdOfUserMap.get(line));

                    } else {
                        parameterSource.addValue(paramName, line);
                    }

                    if (reviewPropertyCount > lineNamesPerEntity.length - 1) {
                        list.add(parameterSource);
                        reviewPropertyCount = 0;
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

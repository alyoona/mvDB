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
public class JdbcRepositoryPoster {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final String selectAllMovies = "SELECT id, name_russian as name FROM movieland.movie;";

    public JdbcRepositoryPoster(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    int fillPoster() {
        String sql = "INSERT INTO movieland.poster (movie_id, picture_path) " +
                "VALUES (:movie_id, :picture_path);";

        return Utils.fill(sql, getPosterSqlParameterSourcesList(), namedParameterJdbcTemplate);
    }

    private List<SqlParameterSource> getPosterSqlParameterSourcesList() {
        List<SqlParameterSource> list = new ArrayList<>();
        Map<String, Integer> nameAndIdOfMovieMap = Utils.getNameAndIdMap(selectAllMovies, namedParameterJdbcTemplate);
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/poster.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!"".equals(line)) {
                    String withDelimiter = "https";
                    String[] values = line.split(withDelimiter);
                    MapSqlParameterSource parameterSource = new MapSqlParameterSource();
                    parameterSource.addValue("movie_id", nameAndIdOfMovieMap.get(values[0].trim()));
                    String picturePath = withDelimiter + values[1];
                    parameterSource.addValue("picture_path", picturePath);
                    list.add(parameterSource);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("handle Exception", e);
        }
        return list;
    }
}

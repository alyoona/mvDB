package com.stroganova.movielanddb;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

@Repository
public class JdbcRepositoryMovie {

    private static final String selectAllGenres = "SELECT id, name FROM movieland.genre;";
    private static final String selectAllCountries = "SELECT id, name FROM movieland.country;";
    private static final String selectAllMovies = "SELECT id, (name_russian ||'/'|| name_native) as name FROM movieland.movie;";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcRepositoryMovie(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private Map<String, String[]> movieGenreMap = new HashMap<>();
    private Map<String, String[]> movieCountryMap = new HashMap<>();
    private Set<String> countrySet = new TreeSet<>();

    void fill() {
        //#1 Step
        int countInsertedMovies = fillMovie();
        System.out.println("countInsertedMovies = " + countInsertedMovies);

        //#2 Step
        int countInsertedCountries = fillCountry();
        System.out.println("countInsertedCountries = " + countInsertedCountries);

        //#3 get id values from DB:
        Map<String, Integer> nameAndIdOfMovieMap = Utils.getNameAndIdMap(selectAllMovies, namedParameterJdbcTemplate);
        Map<String, Integer> nameAndIdOfGenreMap = Utils.getNameAndIdMap(selectAllGenres, namedParameterJdbcTemplate);
        Map<String, Integer> nameAndIdOfCountryMap = Utils.getNameAndIdMap(selectAllCountries, namedParameterJdbcTemplate);

        //#4
        int countInsertedMovieGenre = fillMovieGenre(nameAndIdOfMovieMap, nameAndIdOfGenreMap, movieGenreMap);
        System.out.println("countInsertedMovieGenre = " + countInsertedMovieGenre);

        //#5
        int countInsertedMovieCountry = fillMovieCountry(nameAndIdOfMovieMap, nameAndIdOfCountryMap, movieCountryMap);
        System.out.println("countInsertedMovieCountry = " + countInsertedMovieCountry);
    }

    //#1 Step
    private int fillMovie() {
        String sql = "INSERT INTO movieland.movie (name_russian, name_native, year, description, rating, price)" +
                "VALUES (:name_russian, :name_native, TO_DATE(:year, 'YYYY'), :description, :rating, :price);";
        return Utils.fill(sql
                , getMovieSqlParameterSourcesList(
                        new String[]{"name", "year", "origin_country", "genre", "description", "rating", "price"})
                , namedParameterJdbcTemplate);
    }
    //#2 Step
    private int fillCountry() {
        String sql = "INSERT INTO movieland.country (name) VALUES (:name);";
        return Utils.fill(sql, getCountrySqlParameterSourcesList(countrySet), namedParameterJdbcTemplate);
    }

    //#4 Step
    private int fillMovieGenre(Map<String, Integer> nameAndIdOfMovieMap, Map<String, Integer> nameAndIdOfGenreMap, Map<String, String[]> movieGenreMap) {
        String sql = "INSERT INTO movieland.movie_genre (movie_id, genre_id) VALUES (:movie_id, :genre_id);";

        List<SqlParameterSource> movieGenreSqlParameterSources =
                getMovieEntitySqlParameterSourcesList("genre_id", nameAndIdOfMovieMap, nameAndIdOfGenreMap, movieGenreMap);

        return Utils.fill(sql, movieGenreSqlParameterSources, namedParameterJdbcTemplate);
    }

    //#5 Step
    private int fillMovieCountry(Map<String, Integer> nameAndIdOfMovieMap, Map<String, Integer> nameAndIdOfCountryMap, Map<String, String[]> movieCountryMap){
        String sql = "INSERT INTO movieland.movie_country (movie_id, country_id) VALUES (:movie_id, :country_id);";

        List<SqlParameterSource> movieGenreSqlParameterSources =
                getMovieEntitySqlParameterSourcesList("country_id", nameAndIdOfMovieMap, nameAndIdOfCountryMap, movieCountryMap);

        return Utils.fill(sql, movieGenreSqlParameterSources, namedParameterJdbcTemplate);
    }

    //#1.1 Step
    private List<SqlParameterSource> getMovieSqlParameterSourcesList(String[] lineNamesPerEntity) {
        List<SqlParameterSource> list = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/movie.txt"))) {
            String currentMovieName = null;
            int moviePropertyCount = 0;
            String line;
            MapSqlParameterSource parameterSource = new MapSqlParameterSource();
            while ((line = reader.readLine()) != null) {
                if (!"".equals(line)) {
                    String paramName = lineNamesPerEntity[moviePropertyCount];
                    moviePropertyCount++;
                    if ("name".equals(paramName)) {
                        currentMovieName = line;
                        parameterSource.addValues(Utils.parseLine(line, new String[]{"name_russian", "name_native"}, "/"));
                    } else if ("origin_country".equals(paramName)) {
                        String[] countries = StringUtils.tokenizeToStringArray(line, ",", true, true);
                        countrySet.addAll(Arrays.asList(countries));
                            movieCountryMap.put(currentMovieName, countries);
                    } else if ("genre".equals(paramName)) {
                        String[] genres = StringUtils.tokenizeToStringArray(line, ",", true, true);
                            movieGenreMap.put(currentMovieName, genres);
                    } else if ("rating".equals(paramName) || "price".equals(paramName)) {
                        String paramValue = StringUtils.tokenizeToStringArray(line, ":", true, true)[1];
                        parameterSource.addValue(paramName,Double.valueOf(paramValue));
                    } else {
                        parameterSource.addValue(paramName, line.trim());
                    }

                    if (moviePropertyCount > lineNamesPerEntity.length - 1) {
                        list.add(parameterSource);
                        moviePropertyCount = 0;
                        parameterSource = new MapSqlParameterSource();
                    }

                }
            }
        } catch (Exception e) {
            throw new RuntimeException("handle Exception", e);
        }
        return list;
    }

    //#2.1 Step
    private List<SqlParameterSource> getCountrySqlParameterSourcesList(Set<String> countries) {
        List<SqlParameterSource> parameterSources = new ArrayList<>();
        for (String country : countries) {
            parameterSources.add(new MapSqlParameterSource("name", country));
        }
        return parameterSources;
    }

    //#4.1 , 5.1 Step
    private List<SqlParameterSource> getMovieEntitySqlParameterSourcesList(String paramName, Map<String, Integer> nameAndIdOfMovieMap, Map<String, Integer> nameAndIdOfEntityMap, Map<String, String[]> movieEntityMap) {
        List<SqlParameterSource> movieEntitySqlParameterSources = new ArrayList<>();
        for(String movie : movieEntityMap.keySet()) {
            String[] entities = movieEntityMap.get(movie);
            for (String entity : entities) {
                MapSqlParameterSource parameterSource = new MapSqlParameterSource();
                parameterSource.addValue("movie_id", nameAndIdOfMovieMap.get(movie));
                parameterSource.addValue(paramName, nameAndIdOfEntityMap.get(entity));
                movieEntitySqlParameterSources.add(parameterSource);
            }
        }
        return movieEntitySqlParameterSources;
    }


}

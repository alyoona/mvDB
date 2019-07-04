package com.stroganova.movielanddb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


@SpringBootApplication
public class Application {

    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

        JdbcRepository repository = context.getBean(JdbcRepository.class);
        int countInsertedRoles = repository.fillRoles();
        System.out.println("countInsertedRoles = " + countInsertedRoles);

        int countInsertedGenres = repository.fillGenre();
        System.out.println("countInsertedGenres = " + countInsertedGenres);
        JdbcRepositoryUser jdbcRepositoryUser = context.getBean(JdbcRepositoryUser.class);
        int countInsertedUsers = jdbcRepositoryUser.fillUser();
        System.out.println("countInsertedUsers = " + countInsertedUsers);
        //genre should be filled before (filling movie, country, movie_country and movie_genre)
        JdbcRepositoryMovie jdbcRepositoryMovie = context.getBean(JdbcRepositoryMovie.class);
        jdbcRepositoryMovie.fill();
        //review should be filled after users and movies
        JdbcRepositoryReview jdbcRepositoryReview = context.getBean(JdbcRepositoryReview.class);
        int countReviewInserted = jdbcRepositoryReview.fillReview();
        System.out.println("countReviewInserted = " + countReviewInserted);
        //poster should be filled after movies
        JdbcRepositoryPoster jdbcRepositoryPoster = context.getBean(JdbcRepositoryPoster.class);
        int countPosterInserted = jdbcRepositoryPoster.fillPoster();
        System.out.println("countPosterInserted = " + countPosterInserted);
    }
}

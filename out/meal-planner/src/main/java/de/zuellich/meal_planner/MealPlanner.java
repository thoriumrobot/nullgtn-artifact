package de.zuellich.meal_planner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import javax.annotation.Nullable;

@SpringBootApplication
@EnableCaching
public class MealPlanner {

    public static void main(String[] args) {
        SpringApplication.run(MealPlanner.class, args);
    }
}

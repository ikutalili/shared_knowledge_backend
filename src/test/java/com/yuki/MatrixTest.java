package com.yuki;

import com.yuki.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SpringBootTest
public class MatrixTest {
    @Autowired
    private ArticleService articleService;
    @Test
    void getIndexOfArray() {
    Integer[] items = {0, 10, 30, 2, 7, 5, 90, 76, 100, 45, 55};

    // Create an array to store original indices
    Integer[] indices = new Integer[items.length];
    for (int i = 0; i < items.length; i++) {
        indices[i] = i;
    }

// Sort the indices based on the values in 'items'
    Arrays.sort(indices, (a, b) -> items[b] - items[a]);
    Arrays.sort(items, Collections.reverseOrder());
    // Get the top 10 indices
    Integer[] topTenIndices = Arrays.copyOfRange(indices, 0, 10);

// Print the top 10 indices
    System.out.println(Arrays.toString(items));
    System.out.println("Top 10 indices: " + Arrays.toString(topTenIndices));
}
    @Test
    void anotherWay() {
        Integer[] items = {0, 10, 30, 2, 7, 5, 90, 76, 100, 45, 55};

        // Create an ArrayList to store original indices
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            indices.add(i);
        }

        // Sort the indices based on the values in 'items'
        indices.sort((a, b) -> items[b] - items[a]);

        // Get the top 10 indices
        List<Integer> topTenIndices = indices.subList(0, Math.min(10, indices.size()));

        // Print the top 10 indices
        System.out.println("Top 10 indices: " + topTenIndices);

        // Print the sorted array (descending order)
        Arrays.sort(items, Collections.reverseOrder());
        System.out.println("Sorted array (descending order): " + Arrays.toString(items));
    }

    @Test
    void testService() {
        articleService.recommendArticlesForUser(50);
    }
}

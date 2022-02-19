package com.luxoft.highperformance.bookserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FeignClientTest {

    @Autowired
    BookClient bookClient;

    @Test
    void testGetPerson() {
        System.out.println(bookClient.baseline("hello"));
    }


}
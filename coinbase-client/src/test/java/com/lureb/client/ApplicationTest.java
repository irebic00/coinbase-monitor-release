package com.lureb.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@EmbeddedKafka
@ExtendWith(SpringExtension.class)
class ApplicationTest {

    @Test
    void contextLoads() {
    }
}

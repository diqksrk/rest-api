package com.example.demorestfapi.events;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class EventTest {

    @Test
    public void builder() {
        Event event = Event.builder()
                .name("Inflearn Spring Rest Apli")
                .description("REST SPIT DELDSA")
                .build();
        assertThat(event).isNotNull();
    }
}
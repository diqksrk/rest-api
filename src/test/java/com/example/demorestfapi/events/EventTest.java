package com.example.demorestfapi.events;

import org.junit.jupiter.api.Test;

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

    @Test
    public void javaBean() {
        // Given
        Event event = new Event();

        // when
        String name = "Event";
        event.setName(name);

        // then
        event.setDescription("dsadas");
        assertThat(event.getName()).isEqualTo(name);
    }

}
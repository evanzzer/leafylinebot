package com.leafy.wolfram;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.linecorp.bot.model.event.Event;

import java.util.Collections;
import java.util.List;

public class EventsModel {
    public final List<Event> events;

    @JsonCreator
    public EventsModel(@JsonProperty("events") List<Event> events) {
        this.events = events != null ? events : Collections.emptyList();
    }

    public List<Event> getEvents() {
        return events;
    }
}

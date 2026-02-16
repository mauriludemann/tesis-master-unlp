package com.unlp.payments.utils;

import java.util.List;

import lombok.Data;

@Data
public class EventTransitionMappingConfig {

   private List<EventTransitionMappingEntry> events;

}

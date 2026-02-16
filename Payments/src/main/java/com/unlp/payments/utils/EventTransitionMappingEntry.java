package com.unlp.payments.utils;

import java.util.List;

import lombok.Data;

@Data
public class EventTransitionMappingEntry {

   private String eventId;

   private List<TransitionConfig> transitions;
}

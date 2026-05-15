package com.unlp.payments.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unlp.payments.actions.IActionComponent;
import com.unlp.payments.dto.EventMetadata;
import com.unlp.payments.dto.EventRequestDTO;
import com.unlp.payments.utils.EventTransitionMappingConfig;
import com.unlp.payments.utils.EventTransitionMappingEntry;
import com.unlp.payments.utils.TransitionConfig;
import com.unlp.payments.utils.TransitionMapping;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EventTransitionMapper {

   private final Map<String, IActionComponent> actionMap;

   private final Map<String, List<TransitionMapping>> eventMappings = new HashMap<>();

   @Autowired
   private ObjectMapper objectMapper;

   public EventTransitionMapper(List<IActionComponent> actionComponents) {
      this.actionMap = actionComponents.stream()
            .collect(Collectors.toMap(IActionComponent::getEventId, Function.identity()));
   }

   @PostConstruct
   private void loadMappings() {
      try (InputStream input = getClass().getClassLoader().getResourceAsStream("event-transition-mapping-full.json")) {
         EventTransitionMappingConfig config = objectMapper.readValue(input, EventTransitionMappingConfig.class);

         for (EventTransitionMappingEntry entry : config.getEvents()) {
            IActionComponent action = actionMap.get(entry.getEventId());
            if (Objects.isNull(action)) {
               throw new RuntimeException("No action component registered for event: " + entry.getEventId());
            }

            List<TransitionMapping> transitionMappings = new ArrayList<>();
            for (TransitionConfig tConfig : entry.getTransitions()) {
               List<Integer> postFiringTransitions = Objects.nonNull(tConfig.getPostFiringTransitions()) ?
                     tConfig.getPostFiringTransitions().stream().map(TransitionConfig::getTransitionId).toList()
                     : new ArrayList<>();
               List<Integer> postFiringTimedTransitions = Objects.nonNull(tConfig.getPostFiringTimedTransitions()) ?
                     tConfig.getPostFiringTimedTransitions().stream().map(TransitionConfig::getTransitionId).toList()
                     : new ArrayList<>();

               Object expectedCondition = Objects.nonNull(tConfig.getExpectedConditionResult()) ?
                     action.buildExpectedCondition(tConfig.getExpectedConditionResult()) : null;

               transitionMappings.add(new TransitionMapping(
                     tConfig.getTransitionId(),
                     action.getConditionClass(),
                     expectedCondition,
                     action::executeAction,
                     postFiringTransitions,
                     postFiringTimedTransitions
               ));
            }
            eventMappings.put(entry.getEventId(), transitionMappings);
         }
      } catch (IOException e) {
         throw new RuntimeException("Failed to load event mappings from JSON", e);
      }
      log.info("Event mappings loaded: {}", eventMappings.size());
   }

   public List<TransitionMapping> findTransitions(EventRequestDTO eventDTO) {
      String eventId = eventDTO.getEventId();
      Object metadata = eventDTO.getMetadata();
      List<TransitionMapping> result = new ArrayList<>();
      List<TransitionMapping> mappings = eventMappings.get(eventId);

      if (Objects.isNull(mappings) || mappings.isEmpty()) {
         throw new RuntimeException("No mappings found for event: " + eventId);
      }

      if (Objects.nonNull(metadata)) {
         for (TransitionMapping mapping : mappings) {
            Object conditionResult = objectMapper.convertValue(metadata, mapping.getConditionClass()).conditionResult();

            if (mapping.getExpectedConditionResult().equals(conditionResult)) {
               result.add(mapping);
            }
         }
      } else {
         result.addAll(mappings);
      }
      return result;
   }
}

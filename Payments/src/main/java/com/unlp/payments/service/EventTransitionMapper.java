package com.unlp.payments.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unlp.payments.actions.AuthResultActionComponent;
import com.unlp.payments.actions.FundsValidationResultActionComponent;
import com.unlp.payments.actions.PaymentIntentActionComponent;
import com.unlp.payments.actions.PaymentProcessResultActionComponent;
import com.unlp.payments.dto.AuthenticationResultMetadata;
import com.unlp.payments.dto.EventMetadata;
import com.unlp.payments.dto.EventRequestDTO;
import com.unlp.payments.dto.FundsAvailableResultMetadata;
import com.unlp.payments.utils.EventTransitionMappingConfig;
import com.unlp.payments.utils.EventTransitionMappingEntry;
import com.unlp.payments.utils.SupportedEvents;
import com.unlp.payments.utils.TransitionConfig;
import com.unlp.payments.utils.TransitionMapping;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EventTransitionMapper {

   @Autowired
   private PaymentIntentActionComponent paymentIntentActionComponent;

   @Autowired
   private AuthResultActionComponent authResultActionComponent;

   @Autowired
   private FundsValidationResultActionComponent fundsValidationResultActionComponent;

   @Autowired
   private PaymentProcessResultActionComponent paymentProcessResultActionComponent;

   private final ObjectMapper mapper = new ObjectMapper();

   // eventId -> List of possible transitions (with expected condition result)
   private final Map<String, List<TransitionMapping>> eventMappings = new HashMap<>();

   @Autowired
   private ObjectMapper objectMapper;

   @PostConstruct
   private void loadMappings() {
      try (InputStream input = getClass().getClassLoader().getResourceAsStream("event-transition-mapping.json")) {
         EventTransitionMappingConfig config = mapper.readValue(input, EventTransitionMappingConfig.class);

         for (EventTransitionMappingEntry entry : config.getEvents()) {
            List<TransitionMapping> transitionMappings = new ArrayList<>();
            for (TransitionConfig tConfig : entry.getTransitions()) {
               List<Integer> postFiringTransitions = Objects.nonNull(tConfig.getPostFiringTransitions()) ?
                     tConfig.getPostFiringTransitions().stream().map(TransitionConfig::getTransitionId).toList()
                     : new ArrayList<>();
               List<Integer> postFiringTimedTransitions = Objects.nonNull(tConfig.getPostFiringTimedTransitions()) ?
                     tConfig.getPostFiringTimedTransitions().stream().map(TransitionConfig::getTransitionId).toList()
                     : new ArrayList<>();
               switch (entry.getEventId()) {
                  case SupportedEvents.AUTHENTICATION_RESULT:
                     Boolean authenticated = (Boolean) tConfig.getExpectedConditionResult().get("authenticated");
                     var authExpectedCondition = new AuthenticationResultMetadata.ConditionResult(authenticated);
                     Consumer<EventMetadata> authAction = (eventMetadata) -> {
                        authResultActionComponent.validateFunds(eventMetadata);
                     };
                     transitionMappings.add(new TransitionMapping(tConfig.getTransitionId(), AuthenticationResultMetadata.class, authExpectedCondition, authAction, postFiringTransitions, postFiringTimedTransitions));
                     break;
                  case SupportedEvents.FUNDS_VALIDATION_RESULT:
                     Boolean fundsAvailable = (Boolean) tConfig.getExpectedConditionResult().get("fundsAvailable");
                     var fundsExpectedCondition = new FundsAvailableResultMetadata.ConditionResult(fundsAvailable);
                     Consumer<EventMetadata> fundsAction = (eventMetadata) -> {
                        fundsValidationResultActionComponent.executePayment(eventMetadata);
                     };
                     transitionMappings.add(new TransitionMapping(tConfig.getTransitionId(), FundsAvailableResultMetadata.class, fundsExpectedCondition, fundsAction, postFiringTransitions, postFiringTimedTransitions));
                     break;
                  case SupportedEvents.PAYMENT_INTENT:
                     Consumer<EventMetadata> paymentIntentAction = (eventMetadata) -> {
                        paymentIntentActionComponent.validateAuth(eventMetadata);
                     };
                     transitionMappings.add(new TransitionMapping(tConfig.getTransitionId(), null, null, paymentIntentAction, postFiringTransitions, postFiringTimedTransitions));
                     break;
                  case SupportedEvents.PAYMENT_PROCESS_RESULT:
                     Consumer<EventMetadata> paymentProcessAction = (eventMetadata) -> {
                        paymentProcessResultActionComponent.notifyPaymentResult(eventMetadata);
                     };
                     transitionMappings.add(new TransitionMapping(tConfig.getTransitionId(), null, null, paymentProcessAction, postFiringTransitions, postFiringTimedTransitions));
                     break;
                  default:
                     throw new RuntimeException("Unsupported event transition mapping: " + entry.getEventId());
               }
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

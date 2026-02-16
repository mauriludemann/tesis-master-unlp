package com.unlp.payments.service;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unlp.payments.dto.EventMetadata;
import com.unlp.payments.dto.EventRequestDTO;
import com.unlp.payments.exceptions.PaymentsException;
import com.unlp.payments.utils.TransitionMapping;
import com.unlp.petri_processor.PetriMonitor;
import com.unlp.petri_processor.PetriTransition;
import com.unlp.petri_processor.exceptions.PetriMonitorException;

import jakarta.annotation.PostConstruct;

@Service
public class EventProcessorService {

   private PetriMonitor petriMonitor;

   @Autowired
   private EventTransitionMapper eventTransitionMapper;

   private final ObjectMapper objectMapper = new ObjectMapper();

   @PostConstruct
   public void postConstruct() {
      petriMonitor = new PetriMonitor();
   }

   public void handleEvent(EventRequestDTO eventDTO) throws PetriMonitorException, PaymentsException {
      List<TransitionMapping> transitionsToFire = eventTransitionMapper.findTransitions(eventDTO);
      if (transitionsToFire.isEmpty()) {
         throw new PaymentsException("No transitions found for event: " + eventDTO.getEventId());
      }
      for (TransitionMapping transition : transitionsToFire) {

         PetriTransition petriTransition = new PetriTransition(transition.getTransitionId(), eventDTO.getUuid());

         petriMonitor.fire(petriTransition);

         if (Objects.nonNull(transition.getAction())) {
            EventMetadata metadata = Objects.nonNull(eventDTO.getMetadata()) ?
                  objectMapper.convertValue(eventDTO.getMetadata(), transition.getConditionClass()) :
                  null;
            transition.getAction().accept(metadata);
            executePostActionTransitions(eventDTO.getUuid(), transition);
            executePostActionTimedTransitions(eventDTO.getUuid(), transition); // Ver que capaz esto no hace falta y pongo todo en el postActionTransitions
         }
      }
   }

   private void executePostActionTransitions(String uuid, TransitionMapping transition) {
      for (Integer postActionTransition : transition.getPostActionTransitions()) {
         new Thread(() -> {
            try {
               PetriTransition petriTransition = new PetriTransition(postActionTransition, uuid);
               petriMonitor.fire(petriTransition);
            } catch (PetriMonitorException e) {
               throw new RuntimeException(e);
            }
         }).start();
      }
   }

   private void executePostActionTimedTransitions(String uuid, TransitionMapping transition) {
      for (Integer postActionTimedTransition : transition.getPostActionTimedTransitions()) {
         new Thread(() -> {
            try {
               PetriTransition petriTransition = new PetriTransition(postActionTimedTransition, uuid);
               petriMonitor.fire(petriTransition);
            } catch (PetriMonitorException e) {
               throw new RuntimeException(e);
            }
         }).start();
      }
   }
}

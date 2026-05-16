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
import com.unlp.petri_processor.IPetriNetState;
import com.unlp.petri_processor.PetriMonitor;
import com.unlp.petri_processor.PetriTransition;
import com.unlp.petri_processor.exceptions.PetriMonitorException;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EventProcessorService {

   private PetriMonitor petriMonitor;

   @Autowired
   private EventTransitionMapper eventTransitionMapper;

   @Autowired
   private AsyncTransitionService asyncTransitionService;

   @Autowired
   private ObjectMapper objectMapper;

   @Autowired
   private IPetriNetState petriNetState;

   @PostConstruct
   public void postConstruct() {
      petriMonitor = new PetriMonitor(petriNetState);
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
            firePostActionTransitions(eventDTO.getUuid(), transition);
         }
      }
   }

   private void firePostActionTransitions(String uuid, TransitionMapping transition) {
      // Transiciones inmediatas (drenaje) se disparan sincrónicamente
      for (Integer transitionId : transition.getPostActionTransitions()) {
         try {
            PetriTransition petriTransition = new PetriTransition(transitionId, uuid);
            petriMonitor.fire(petriTransition);
         } catch (PetriMonitorException e) {
            log.error("Error firing post-action transition {} for uuid {}: {}",
                  transitionId, uuid, e.getMessage(), e);
         }
      }
      // Transiciones temporizadas se disparan asincrónicamente
      for (Integer transitionId : transition.getPostActionTimedTransitions()) {
         asyncTransitionService.fireTransition(petriMonitor, transitionId, uuid);
      }
   }
}

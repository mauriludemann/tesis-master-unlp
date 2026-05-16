package com.unlp.payments.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.unlp.petri_processor.PetriMonitor;
import com.unlp.petri_processor.PetriTransition;
import com.unlp.petri_processor.exceptions.PetriMonitorException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AsyncTransitionService {

   @Async
   public void fireTransition(PetriMonitor petriMonitor, int transitionId, String uuid) {
      try {
         log.info("Firing timed transition {} for uuid {}", transitionId, uuid);
         petriMonitor.fire(new PetriTransition(transitionId, uuid));
         log.info("Timed transition {} fired for uuid {}", transitionId, uuid);
      } catch (PetriMonitorException e) {
         log.error("Error firing timed transition {} for uuid {}: {}", transitionId, uuid, e.getMessage(), e);
      }
   }
}

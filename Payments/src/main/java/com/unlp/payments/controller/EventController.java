package com.unlp.payments.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unlp.payments.dto.EventRequestDTO;
import com.unlp.payments.exceptions.PaymentsException;
import com.unlp.payments.service.EventProcessorService;
import com.unlp.petri_processor.exceptions.PetriMonitorException;

@RestController
@RequestMapping("/v1/events")
public class EventController {

   @Autowired
   private EventProcessorService eventProcessorService;

   @PostMapping
   public void handleEvent(@RequestBody EventRequestDTO eventDTO) throws PetriMonitorException, PaymentsException {
      eventProcessorService.handleEvent(eventDTO);
   }
}

package com.unlp.payments.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unlp.payments.persistence.PetriNetStateEntity;
import com.unlp.payments.persistence.PetriNetStateRepository;
import com.unlp.payments.service.StateChangeNotifier;

@RestController
@RequestMapping("/v1/state")
@CrossOrigin(origins = "*")
public class PetriNetStateController {

   private static final String STATE_ID = "payments-petri-net";

   @Autowired
   private PetriNetStateRepository repository;

   @Autowired
   private ObjectMapper objectMapper;

   @Autowired
   private StateChangeNotifier stateChangeNotifier;

   @GetMapping
   public Map<String, Object> getState() {
      return repository.findById(STATE_ID).map(entity -> {
         try {
            Map<String, Object> state = new HashMap<>();
            state.put("marking", objectMapper.readValue(entity.getCurrentMarking(), int[].class));
            state.put("enabledTransitions", objectMapper.readValue(entity.getEnabledTransitions(),
                  new TypeReference<List<Boolean>>() {}));
            state.put("uuidMarking", objectMapper.readValue(entity.getUuidCurrentMarking(),
                  new TypeReference<Map<Integer, Set<String>>>() {}));
            return state;
         } catch (IOException e) {
            throw new RuntimeException("Failed to read state", e);
         }
      }).orElse(Map.of("marking", new int[0], "enabledTransitions", List.of(), "uuidMarking", Map.of()));
   }

   @GetMapping("/config")
   public Map<String, Object> getConfig() {
      try (InputStream input = getClass().getClassLoader().getResourceAsStream("petri-config.json")) {
         Map<String, Object> config = objectMapper.readValue(input, new TypeReference<>() {});
         Map<String, Object> result = new HashMap<>();
         result.put("mapPlacesNames", config.get("mapPlacesNames"));
         result.put("mapTransitionsNames", config.get("mapTransitionsNames"));
         return result;
      } catch (IOException e) {
         throw new RuntimeException("Failed to load config", e);
      }
   }

   @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   public SseEmitter stream() {
      return stateChangeNotifier.subscribe();
   }

   @PostMapping("/reset")
   public Map<String, String> reset() {
      repository.deleteById(STATE_ID);
      return Map.of("status", "reset");
   }
}

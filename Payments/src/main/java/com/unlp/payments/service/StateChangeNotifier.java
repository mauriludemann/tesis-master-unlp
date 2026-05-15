package com.unlp.payments.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StateChangeNotifier {

   private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

   private final ObjectMapper objectMapper;

   public StateChangeNotifier(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
   }

   public SseEmitter subscribe() {
      SseEmitter emitter = new SseEmitter(0L);
      emitters.add(emitter);
      emitter.onCompletion(() -> emitters.remove(emitter));
      emitter.onTimeout(() -> emitters.remove(emitter));
      emitter.onError(e -> emitters.remove(emitter));
      return emitter;
   }

   public void notifyStateChange(int[] marking, List<Boolean> enabledTransitions,
         Map<Integer, Set<String>> uuidMarking, String firedTransition) {
      Map<String, Object> data = Map.of(
            "marking", marking,
            "enabledTransitions", enabledTransitions,
            "uuidMarking", uuidMarking,
            "firedTransition", firedTransition != null ? firedTransition : "",
            "timestamp", System.currentTimeMillis()
      );

      for (SseEmitter emitter : emitters) {
         try {
            emitter.send(SseEmitter.event()
                  .name("state-change")
                  .data(objectMapper.writeValueAsString(data)));
         } catch (IOException e) {
            emitter.complete();
            emitters.remove(emitter);
         }
      }
   }
}

package com.unlp.payments.persistence;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unlp.payments.service.StateChangeNotifier;
import com.unlp.petri_processor.IPetriNetState;
import com.unlp.petri_processor.PetriNetSnapshot;

@Component
public class JpaPetriNetState implements IPetriNetState {

    private static final Logger log = LoggerFactory.getLogger(JpaPetriNetState.class);
    private static final String STATE_ID = "payments-petri-net";

    private final PetriNetStateRepository repository;
    private final ObjectMapper objectMapper;
    private final StateChangeNotifier stateChangeNotifier;

    @PersistenceContext
    private EntityManager entityManager;

    public JpaPetriNetState(PetriNetStateRepository repository, ObjectMapper objectMapper,
                            StateChangeNotifier stateChangeNotifier) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.stateChangeNotifier = stateChangeNotifier;
    }

    @Override
    public void save(PetriNetSnapshot snapshot) {
        try {
            entityManager.clear(); // Limpiar cache L1 para forzar lectura fresca de la DB
            PetriNetStateEntity entity = repository.findById(STATE_ID)
                    .orElse(new PetriNetStateEntity(STATE_ID));

            if (entity.getCurrentMarking() != null) {
                log.info("State BEFORE save: marking={}, enabledTransitions={}, uuidMarking={}",
                        Arrays.toString(objectMapper.readValue(entity.getCurrentMarking(), int[].class)),
                        objectMapper.readValue(entity.getEnabledTransitions(), new TypeReference<List<Boolean>>() {}),
                        objectMapper.readValue(entity.getUuidCurrentMarking(), new TypeReference<Map<Integer, Set<String>>>() {}));
            } else {
                log.info("State BEFORE save: no previous state");
            }

            entity.setCurrentMarking(objectMapper.writeValueAsString(snapshot.currentMarking()));
            entity.setUuidCurrentMarking(objectMapper.writeValueAsString(snapshot.uuidCurrentMarking()));
            entity.setEnabledTransitions(objectMapper.writeValueAsString(snapshot.enabledTransitions()));
            entity.setTimedEnablingTimes(objectMapper.writeValueAsString(snapshot.timedTransitionEnablingTimes()));
            entity.setTimedUuidEnablingTimes(objectMapper.writeValueAsString(snapshot.timedTransitionUuidEnablingTimes()));
            entity.setUpdatedAt(LocalDateTime.now());

            repository.save(entity);

            log.info("State AFTER save: marking={}, enabledTransitions={}, uuidMarking={}",
                    Arrays.toString(snapshot.currentMarking()), snapshot.enabledTransitions(), snapshot.uuidCurrentMarking());

            stateChangeNotifier.notifyStateChange(
                    snapshot.currentMarking(),
                    snapshot.enabledTransitions(),
                    snapshot.uuidCurrentMarking(),
                    null
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize PetriNet state", e);
        }
    }

    @Override
    public PetriNetSnapshot load() {
        entityManager.clear(); // Limpiar cache L1 para forzar lectura fresca de la DB
        return repository.findById(STATE_ID).map(entity -> {
            try {
                int[] currentMarking = objectMapper.readValue(
                        entity.getCurrentMarking(), int[].class);

                Map<Integer, Set<String>> uuidCurrentMarking = objectMapper.readValue(
                        entity.getUuidCurrentMarking(),
                        new TypeReference<HashMap<Integer, Set<String>>>() {});

                List<Boolean> enabledTransitions = objectMapper.readValue(
                        entity.getEnabledTransitions(),
                        new TypeReference<List<Boolean>>() {});

                Map<Integer, Long> timedEnablingTimes = objectMapper.readValue(
                        entity.getTimedEnablingTimes(),
                        new TypeReference<HashMap<Integer, Long>>() {});

                Map<Integer, Map<String, Long>> timedUuidEnablingTimes = objectMapper.readValue(
                        entity.getTimedUuidEnablingTimes(),
                        new TypeReference<HashMap<Integer, Map<String, Long>>>() {});

                return new PetriNetSnapshot(
                        currentMarking,
                        uuidCurrentMarking,
                        enabledTransitions,
                        timedEnablingTimes,
                        timedUuidEnablingTimes
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to deserialize PetriNet state", e);
            }
        }).orElse(null);
    }
}

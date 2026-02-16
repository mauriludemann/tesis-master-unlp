package com.unlp.payments.utils;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class TransitionConfig {

   private int transitionId;

   private Map<String, Object> expectedConditionResult;

   List<TransitionConfig> postFiringTransitions;

   List<TransitionConfig> postFiringTimedTransitions;

}

package com.unlp.payments.utils;

import java.util.List;
import java.util.function.Consumer;

import com.unlp.payments.dto.EventMetadata;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TransitionMapping {

   private final int transitionId;

   private final Class<? extends EventMetadata> conditionClass;

   private final Object expectedConditionResult;

   private Consumer<EventMetadata> action;

   private List<Integer> postActionTransitions;

   private List<Integer> postActionTimedTransitions;

}

package com.unlp.payments.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RegistroResultMetadata extends EventMetadata {

   private Boolean registroExitoso;

   public RegistroResultMetadata() {
      super();
   }

   @Override
   public ConditionResult conditionResult() {
      return new ConditionResult(registroExitoso);
   }

   @Data
   public static class ConditionResult {

      private final Boolean registroExitoso;

   }
}

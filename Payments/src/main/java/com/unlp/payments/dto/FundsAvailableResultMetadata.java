package com.unlp.payments.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FundsAvailableResultMetadata extends EventMetadata {

   private Boolean fundsAvailable;

   public FundsAvailableResultMetadata() {
      super();
   }

   @Override
   public FundsAvailableResultMetadata.ConditionResult conditionResult() {
      return new ConditionResult(fundsAvailable);
   }

   @Data
   public static class ConditionResult {

      private final Boolean fundsAvailable;

   }
}

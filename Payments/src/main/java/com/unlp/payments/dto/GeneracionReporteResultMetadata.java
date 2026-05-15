package com.unlp.payments.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GeneracionReporteResultMetadata extends EventMetadata {

   private Boolean generacionExitosa;

   public GeneracionReporteResultMetadata() {
      super();
   }

   @Override
   public ConditionResult conditionResult() {
      return new ConditionResult(generacionExitosa);
   }

   @Data
   public static class ConditionResult {

      private final Boolean generacionExitosa;

   }
}

package com.unlp.payments.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ModificarPagoResultMetadata extends EventMetadata {

   private Boolean modificarExitoso;

   public ModificarPagoResultMetadata() {
      super();
   }

   @Override
   public ConditionResult conditionResult() {
      return new ConditionResult(modificarExitoso);
   }

   @Data
   public static class ConditionResult {

      private final Boolean modificarExitoso;

   }
}

package com.unlp.payments.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AuthenticationResultMetadata extends EventMetadata {

   private Boolean authenticated;

   private String useCase;

   public AuthenticationResultMetadata() {
      super();
   }

   @Override
   public ConditionResult conditionResult() {
      return new ConditionResult(authenticated, useCase);
   }

   @Data
   public static class ConditionResult {

      private final Boolean authenticated;

      private final String useCase;

   }
}

package com.unlp.payments.actions;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.unlp.payments.dto.EventMetadata;
import com.unlp.payments.dto.FundsAvailableResultMetadata;
import com.unlp.payments.utils.SupportedEvents;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FundsValidationResultActionComponent implements IActionComponent {

   @Override
   public void executeAction(EventMetadata eventMetadata) {
      log.info("EXECUTING PAYMENT");
   }

   @Override
   public String getEventId() {
      return SupportedEvents.FUNDS_VALIDATION_RESULT;
   }

   @Override
   public Class<? extends EventMetadata> getConditionClass() {
      return FundsAvailableResultMetadata.class;
   }

   @Override
   public Object buildExpectedCondition(Map<String, Object> rawCondition) {
      Boolean fundsAvailable = (Boolean) rawCondition.get("fundsAvailable");
      return new FundsAvailableResultMetadata.ConditionResult(fundsAvailable);
   }
}

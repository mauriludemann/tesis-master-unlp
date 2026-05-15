package com.unlp.payments.actions;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.unlp.payments.dto.EventMetadata;
import com.unlp.payments.utils.SupportedEvents;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PaymentProcessResultActionComponent implements IActionComponent {

   @Override
   public void executeAction(EventMetadata eventMetadata) {
      log.info("NOTIFYING PAYMENT");
   }

   @Override
   public String getEventId() {
      return SupportedEvents.PAYMENT_PROCESS_RESULT;
   }

   @Override
   public Class<? extends EventMetadata> getConditionClass() {
      return null;
   }

   @Override
   public Object buildExpectedCondition(Map<String, Object> rawCondition) {
      return null;
   }
}

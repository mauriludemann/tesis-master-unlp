package com.unlp.payments.actions;

import org.springframework.stereotype.Component;

import com.unlp.payments.dto.EventMetadata;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PaymentIntentActionComponent implements IActionComponent {

   @Override
   public void executeAction(EventMetadata eventMetadata) {
      validateAuth(eventMetadata);
   }

   public void validateAuth(EventMetadata eventMetadata) {
      log.info("VALIDANDO AUTH");
   }

}

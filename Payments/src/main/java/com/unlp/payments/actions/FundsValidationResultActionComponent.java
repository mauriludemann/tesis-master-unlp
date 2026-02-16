package com.unlp.payments.actions;

import org.springframework.stereotype.Component;

import com.unlp.payments.dto.EventMetadata;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FundsValidationResultActionComponent implements IActionComponent {

   @Override
   public void executeAction(EventMetadata eventMetadata) {
      executePayment(eventMetadata);
   }

   public void executePayment(EventMetadata eventMetadata) {
      log.info("EXECUTING PAYMENT");
   }
}

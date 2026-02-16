package com.unlp.payments.actions;

import org.springframework.stereotype.Component;

import com.unlp.payments.dto.EventMetadata;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PaymentProcessResultActionComponent implements IActionComponent {

   @Override
   public void executeAction(EventMetadata eventMetadata) {
      notifyPaymentResult(eventMetadata);
   }

   public void notifyPaymentResult(EventMetadata eventMetadata) {
      log.info("NOTIFYING PAYMENT");
   }
}

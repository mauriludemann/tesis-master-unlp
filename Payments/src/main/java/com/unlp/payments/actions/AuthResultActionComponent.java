package com.unlp.payments.actions;

import org.springframework.stereotype.Component;

import com.unlp.payments.dto.EventMetadata;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AuthResultActionComponent implements IActionComponent {

   @Override
   public void executeAction(EventMetadata eventMetadata) {
      validateFunds(eventMetadata);
   }

   public void validateFunds(EventMetadata eventMetadata) {
      log.info("VALIDATING FUNDS");
   }
}

package com.unlp.payments.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.unlp.petri_processor.exceptions.PetriMonitorException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

   @ExceptionHandler(PaymentsException.class)
   public ResponseEntity<String> handlePaymentsException(PaymentsException e) {
      log.warn("Payments error: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
   }

   @ExceptionHandler(PetriMonitorException.class)
   public ResponseEntity<String> handlePetriMonitorException(PetriMonitorException e) {
      log.error("Petri monitor error: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
   }

   @ExceptionHandler(RuntimeException.class)
   public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
      log.error("Unexpected error: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
   }
}

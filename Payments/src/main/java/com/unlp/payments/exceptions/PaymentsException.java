package com.unlp.payments.exceptions;

public class PaymentsException extends Exception{

      private static final long serialVersionUID = 2L;

      public PaymentsException(String message) {
         super(message);
      }
}

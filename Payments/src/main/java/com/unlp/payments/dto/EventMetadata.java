package com.unlp.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class EventMetadata {

   public abstract Object conditionResult();
}

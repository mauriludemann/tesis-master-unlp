package com.unlp.payments.actions;

import java.util.Map;

import com.unlp.payments.dto.EventMetadata;

public interface IActionComponent {

   void executeAction(EventMetadata eventMetadata);

   String getEventId();

   Class<? extends EventMetadata> getConditionClass();

   Object buildExpectedCondition(Map<String, Object> rawCondition);
}

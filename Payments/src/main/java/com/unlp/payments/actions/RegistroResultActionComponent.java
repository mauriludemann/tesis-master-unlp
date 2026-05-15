package com.unlp.payments.actions;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.unlp.payments.dto.EventMetadata;
import com.unlp.payments.dto.RegistroResultMetadata;
import com.unlp.payments.utils.SupportedEvents;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RegistroResultActionComponent implements IActionComponent {

   @Override
   public void executeAction(EventMetadata eventMetadata) {
      log.info("PROCESANDO RESULTADO DE REGISTRO");
   }

   @Override
   public String getEventId() {
      return SupportedEvents.REGISTRO_RESULT;
   }

   @Override
   public Class<? extends EventMetadata> getConditionClass() {
      return RegistroResultMetadata.class;
   }

   @Override
   public Object buildExpectedCondition(Map<String, Object> rawCondition) {
      Boolean registroExitoso = (Boolean) rawCondition.get("registroExitoso");
      return new RegistroResultMetadata.ConditionResult(registroExitoso);
   }
}

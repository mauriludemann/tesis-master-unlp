package com.unlp.payments.actions;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.unlp.payments.dto.EventMetadata;
import com.unlp.payments.dto.ModificarPagoResultMetadata;
import com.unlp.payments.utils.SupportedEvents;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ModificarPagoResultActionComponent implements IActionComponent {

   @Override
   public void executeAction(EventMetadata eventMetadata) {
      log.info("PROCESANDO RESULTADO DE MODIFICACION DE PAGO");
   }

   @Override
   public String getEventId() {
      return SupportedEvents.MODIFICAR_PAGO_RESULT;
   }

   @Override
   public Class<? extends EventMetadata> getConditionClass() {
      return ModificarPagoResultMetadata.class;
   }

   @Override
   public Object buildExpectedCondition(Map<String, Object> rawCondition) {
      Boolean modificarExitoso = (Boolean) rawCondition.get("modificarExitoso");
      return new ModificarPagoResultMetadata.ConditionResult(modificarExitoso);
   }
}

package com.unlp.payments.actions;

import com.unlp.payments.dto.EventMetadata;

public interface IActionComponent {

   void executeAction(EventMetadata eventMetadata);
}

package com.unlp.payments.dto;

import lombok.Data;

@Data
public class EventRequestDTO {

   private String eventId;

   private String uuid;

   private Object metadata;
}

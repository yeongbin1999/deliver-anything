package com.deliveranything.domain.delivery.event.dto;

import java.util.List;

public record RiderEtaEvent(
    List<RiderNotificationDto> notifications
) {

}

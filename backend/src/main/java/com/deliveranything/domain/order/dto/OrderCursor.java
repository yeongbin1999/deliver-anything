package com.deliveranything.domain.order.dto;

import java.time.LocalDateTime;

public record OrderCursor(LocalDateTime createdAt, Long orderId) {
}

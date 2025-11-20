package com.deliveranything.domain.order.handler;

import com.deliveranything.domain.order.service.OrderService;
import com.deliveranything.domain.product.stock.event.StockCommittedEvent;
import com.deliveranything.domain.product.stock.event.StockReleasedEvent;
import com.deliveranything.domain.product.stock.event.StockReplenishedEvent;
import com.deliveranything.domain.product.stock.event.StockReserveFailedEvent;
import com.deliveranything.domain.product.stock.event.StockReservedEvent;
import com.deliveranything.global.enums.RedisTopic;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventHandler {

  private final ObjectMapper objectMapper;
  private final OrderService orderService;

  public void handle(RedisTopic topic, String json) {
    try {
      switch (topic) {
        case STOCK_RESERVED_EVENT -> {
          StockReservedEvent event = objectMapper.readValue(json, StockReservedEvent.class);
          orderService.processStockReserved(event.orderId());
        }
        case STOCK_RESERVE_FAILED_EVENT -> {
          StockReserveFailedEvent event = objectMapper.readValue(json,
              StockReserveFailedEvent.class);
          orderService.processStockReserveFailed(event.orderId(), event.reason());
        }
        case STOCK_COMMITTED_EVENT -> {
          StockCommittedEvent event = objectMapper.readValue(json, StockCommittedEvent.class);
          orderService.processStockCommitted(event.orderId());
        }
        case STOCK_RELEASED_EVENT -> {
          StockReleasedEvent event = objectMapper.readValue(json, StockReleasedEvent.class);
          orderService.processStockReleased(event.orderId());
        }
        case STOCK_REPLENISHED_EVENT -> {
          StockReplenishedEvent event = objectMapper.readValue(json, StockReplenishedEvent.class);
          orderService.processStockReplenished(event.orderId());
        }
        default -> log.warn("Unhandled stock event topic: {}", topic);
      }
    } catch (Exception e) {
      log.error("Failed to process stock event in order [{}]: {}", topic, e.getMessage(), e);
    }
  }
}

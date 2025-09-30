package com.deliveranything.domain.search.store.handler;

import com.deliveranything.domain.search.store.document.StoreDocument;
import com.deliveranything.domain.search.store.repository.StoreSearchRepository;
import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.store.store.event.StoreDeletedEvent;
import com.deliveranything.domain.store.store.event.StoreSavedEvent;
import com.deliveranything.domain.store.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreEventHandler {

  private final StoreRepository storeRepository;
  private final StoreSearchRepository storeSearchRepository;

  @TransactionalEventListener
  public void handleStoreSavedEvent(StoreSavedEvent event) {
    log.info("StoreSavedEvent received for storeId: {}", event.storeId());

    Store store = storeRepository.getById(event.storeId());

    StoreDocument storeDocument = StoreDocument.from(store);

    storeSearchRepository.save(storeDocument);

    log.info("Store (ID: {}) synchronized to Elasticsearch.", event.storeId());
  }

  @TransactionalEventListener
  public void handleStoreDeletedEvent(StoreDeletedEvent event) {
    log.info("StoreDeletedEvent received for storeId: {}", event.storeId());

    storeSearchRepository.deleteById(event.storeId());

    log.info("Store (ID: {}) deleted from Elasticsearch.", event.storeId());
  }
}
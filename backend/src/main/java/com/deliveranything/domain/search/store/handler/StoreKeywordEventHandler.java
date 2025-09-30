package com.deliveranything.domain.search.store.handler;

import com.deliveranything.domain.product.product.entity.Product;
import com.deliveranything.domain.product.product.event.ProductKeywordsChangedEvent;
import com.deliveranything.domain.product.product.repository.ProductRepository;
import com.deliveranything.domain.search.store.repository.StoreSearchRepository;
import com.deliveranything.domain.store.store.entity.Store;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class StoreKeywordEventHandler {

  private final ProductRepository productRepository;
  private final StoreSearchRepository storeSearchRepository;

  @TransactionalEventListener
  public void handleProductKeywordsChanged(ProductKeywordsChangedEvent event) {
    productRepository.findById(event.getProductId()).ifPresent(product -> {
      Store store = product.getStore();
      if (store == null) return;

      storeSearchRepository.findById(store.getId()).ifPresent(storeDocument -> {

        List<Product> allProductsInStore = productRepository.findAllByStoreId(store.getId());

        Set<String> allKeywords = allProductsInStore.stream()
            .flatMap(p -> p.getKeywords().stream())
            .collect(Collectors.toSet());

        storeDocument.setKeywords(allKeywords.stream().toList());

        storeSearchRepository.save(storeDocument);
      });
    });
  }
}
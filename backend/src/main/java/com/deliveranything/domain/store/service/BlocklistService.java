package com.deliveranything.domain.store.service;

import com.deliveranything.domain.store.repository.BlocklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class BlocklistService {

  private final BlocklistRepository blocklistRepository;

}

package com.deliveranything.global.dataSeeder;

import com.deliveranything.domain.store.entity.StoreCategory;
import com.deliveranything.domain.store.repository.StoreCategoryRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class StoreCategorySeeder implements ApplicationRunner {

  private final StoreCategoryRepository repo;

  private record Seed(String name, int order) {}
  private static final List<Seed> SEEDS = List.of(
      new Seed("음식/카페", 10),
      new Seed("편의점/마트", 20),
      new Seed("생활/잡화", 30),
      new Seed("뷰티/헬스", 40),
      new Seed("전자/IT", 50),
      new Seed("패션/의류", 60),
      new Seed("스포츠/레저", 70),
      new Seed("서점/문화", 80),
      new Seed("반려동물용품", 90),
      new Seed("꽃/원예", 100),
      new Seed("자동차/공구", 110),
      new Seed("공구/철물", 120)
  );

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    Map<String, StoreCategory> existing = repo.findAll()
        .stream()
        .collect(Collectors.toMap(StoreCategory::getName, Function.identity(), (a, b) -> a));

    for (Seed s : SEEDS) {
      StoreCategory cur = existing.get(s.name());
      if (cur == null) {
        repo.save(StoreCategory.create(s.name(), s.order()));
      } else {
        cur.reseed(s.order(), true);
      }
    }
  }
}
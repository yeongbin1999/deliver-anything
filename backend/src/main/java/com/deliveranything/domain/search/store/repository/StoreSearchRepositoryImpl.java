package com.deliveranything.domain.search.store.repository;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.deliveranything.domain.search.store.document.StoreDocument;
import com.deliveranything.domain.search.store.dto.StoreSearchRequest;
import com.deliveranything.global.common.CursorPageResponse;
import com.deliveranything.global.util.CursorUtil;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class StoreSearchRepositoryImpl implements StoreSearchRepositoryCustom {

  private final ElasticsearchOperations elasticsearchOperations;

  // 검색에 사용할 필드명은 상수로 관리
  private static final String FIELD_NAME = "name";
  private static final String FIELD_DESCRIPTION = "description";
  private static final String FIELD_KEYWORDS = "keywords";
  private static final String FIELD_CATEGORY_ID = "category_id";
  private static final String FIELD_LOCATION = "location";
  private static final String SORT_SCORE = "_score";
  private static final String SORT_ID = "_id";

  @Override
  public CursorPageResponse<StoreDocument> search(StoreSearchRequest request) {
    int querySize = request.limit() + 1;

    BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

    if(StringUtils.hasText(request.searchText())) {
      boolQueryBuilder.must(m -> m
          .multiMatch(mm -> mm
              .query(request.searchText())
              .fields(FIELD_NAME, FIELD_DESCRIPTION, FIELD_KEYWORDS)
          ));
    }

    if(request.categoryId() != null) {
      boolQueryBuilder.filter(f -> f
          .term(t -> t
              .field(FIELD_CATEGORY_ID)
              .value(request.categoryId())
          ));
    }

    boolQueryBuilder.filter(f -> f
        .geoDistance(g -> g
            .field(FIELD_LOCATION)
            .location(l -> l.latlon(ll -> ll.lat(request.lat()).lon(request.lng())))
            .distance(request.distanceKm() + "km")
        ));

    NativeQueryBuilder queryBuilder = new NativeQueryBuilder()
        .withQuery(Query.of(q -> q.bool(boolQueryBuilder.build())))
        .withPageable(PageRequest.of(0, querySize))
        .withSort(s -> s.field(f -> f.field(SORT_SCORE).order(SortOrder.Desc)))
        .withSort(s -> s.field(f -> f.field(SORT_ID).order(SortOrder.Asc)));

    if(StringUtils.hasText(request.nextPageToken())) {
      try {
        String[] decodedCursor = CursorUtil.decode(request.nextPageToken());
        if (decodedCursor != null) {
          queryBuilder.withSearchAfter(List.of(decodedCursor));
        }
      } catch (IllegalArgumentException e) {
        // 유효하지 않은 토큰일 경우 로그를 남기고 첫 페이지를 반환
        System.err.println("Invalid next page token: " + e.getMessage());
      }
    }

    NativeQuery searchQuery = queryBuilder.build();
    SearchHits<StoreDocument> searchHits = elasticsearchOperations.search(searchQuery, StoreDocument.class);

    List<StoreDocument> documents = searchHits.getSearchHits().stream()
        .map(SearchHit::getContent).collect(Collectors.toList());

    boolean hasNext = documents.size() > request.limit();
    List<StoreDocument> responseDocuments = hasNext ? documents.subList(0, request.limit()) : documents;

    String nextToken = null;
    if(hasNext) {
      SearchHit<StoreDocument> lastHit = searchHits.getSearchHits().get(responseDocuments.size() - 1);
      // _score, _id 순서에 맞게 정렬 값을 배열로 전달
      nextToken = CursorUtil.encode(lastHit.getSortValues().toArray());
    }

    return new CursorPageResponse<>(responseDocuments, nextToken, hasNext);
  }
}
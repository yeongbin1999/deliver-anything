package com.deliveranything.domain.search.store.document;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Getter
@Document(indexName = "stores")
public class StoreDocument {

  @Id
  private Long id;

  @Field(type = FieldType.Text, name = "name")
  private String name;

  @Field(type = FieldType.Text, name = "description")
  private String description;

  // 검색 필터링을 위한 카테고리 ID
  @Field(type = FieldType.Long, name = "category_id")
  private Long categoryId;

  @Field(type = FieldType.Keyword, name = "store_category")
  private String categoryName;

  @Field(type = FieldType.Text, name = "image_url")
  private String imageUrl;

  @Field(type = FieldType.Text, name = "road_address")
  private String roadAddress;

  @Field(type = FieldType.Object, name = "location")
  private GeoPoint location;

  @Field(type = FieldType.Boolean, name = "is_open_now")
  private boolean isOpenNow;

  @Field(type = FieldType.Text, name = "keywords")
  private String[] keywords;

}
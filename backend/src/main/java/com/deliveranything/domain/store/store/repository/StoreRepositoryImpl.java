package com.deliveranything.domain.store.store.repository;

import static com.deliveranything.domain.store.store.entity.QStore.store;

import com.deliveranything.domain.store.store.dto.StoreSearchCondition;
import com.deliveranything.domain.store.store.entity.Store;
import com.deliveranything.domain.store.store.enums.StoreStatus;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {

    private static final int MAX_DISTANCE_METERS = 10000; // 10km

    private final JPAQueryFactory queryFactory;
    // SRID 4326 is for WGS 84, the standard for GPS.
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    public Page<Store> search(StoreSearchCondition condition, Pageable pageable) {
        // This is the old offset-based search, kept for now.
        List<Store> content = queryFactory
            .selectFrom(store)
            .where(
                nameContains(condition.name()),
                statusEq(condition.status())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = queryFactory
            .select(store.count())
            .from(store)
            .where(
                nameContains(condition.name()),
                statusEq(condition.status())
            )
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<Tuple> searchByDistance(Double lat, Double lng, com.deliveranything.domain.store.store.enums.StoreCategoryType categoryType, String name, int limit, Double cursorDistance, Long cursorId) {
        if (lat == null || lng == null) {
            return Collections.emptyList();
        }

        Point userLocation = geometryFactory.createPoint(new Coordinate(lng, lat));

        // ST_Distance_Sphere returns distance in meters.
        NumberExpression<Double> distanceExpression = Expressions.numberTemplate(Double.class,
            "ST_Distance_Sphere({0}, {1})", userLocation, store.location);

        return queryFactory
            .select(store, distanceExpression)
            .from(store)
            .where(
                distanceExpression.loe(MAX_DISTANCE_METERS),
                cursorCondition(distanceExpression, cursorDistance, cursorId),
                categoryIdEq(categoryType),
                storeNameContains(name)
            )
            .orderBy(distanceExpression.asc(), store.id.desc())
            .limit(limit)
            .fetch();
    }

    private BooleanExpression cursorCondition(NumberExpression<Double> distance, Double cursorDistance, Long cursorId) {
        if (cursorDistance == null || cursorId == null) {
            return null; // First page
        }
        return distance.gt(cursorDistance)
            .or(distance.eq(cursorDistance).and(store.id.lt(cursorId)));
    }

    private BooleanExpression categoryIdEq(com.deliveranything.domain.store.store.enums.StoreCategoryType categoryType) {
        return categoryType != null ? store.storeCategory.eq(categoryType) : null;
    }

    private BooleanExpression storeNameContains(String name) {
        return StringUtils.hasText(name) ? store.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? store.name.contains(name) : null;
    }

    private BooleanExpression statusEq(StoreStatus status) {
        return status != null ? store.status.eq(status) : null;
    }
}
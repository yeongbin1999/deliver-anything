package com.deliveranything.global.converter;

import com.deliveranything.global.util.PointUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.locationtech.jts.geom.Point;

@Converter(autoApply = true)
public class PointConverter implements AttributeConverter<Point, String> {

  @Override
  public String convertToDatabaseColumn(Point point) {
    if (point == null) {
      return null;
    }
    // POINT(lng lat) 형식으로 저장
    return String.format("POINT(%f %f)", point.getX(), point.getY());
  }

  @Override
  public Point convertToEntityAttribute(String wkt) {
    if (wkt == null || wkt.isEmpty()) {
      return null;
    }
    // "POINT(lng lat)" → Point 객체
    String[] coords = wkt.replace("POINT(", "").replace(")", "").split(" ");
    double lng = Double.parseDouble(coords[0]);
    double lat = Double.parseDouble(coords[1]);
    return PointUtil.createPoint(lat, lng);
  }
}
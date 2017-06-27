package com.redhat.mapitspring.model;


public class MapPoint {
    private String latitude;
    private String longitude;
    private String name;
    private String description;

    private MapPoint() {
    }

    public static MapPoint of(String name, String latitude, String longitude, String description) {
        MapPoint point = new MapPoint();
        point.latitude = latitude;
        point.longitude = longitude;
        point.name = name;
        point.description = description;
        return point;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "MapPoint{" +
                "name='" + name +
                ", lat=" + latitude +
                ", lon=" + longitude +
                ", name='" + name + '\'' +
                ", desc='" + description + '\'' +
                '}';
    }
}

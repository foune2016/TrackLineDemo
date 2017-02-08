package com.foune.tracklinedemo;

/**
 * descreption:
 * company: foune.com
 * Created by xuyanliang on 2017/2/7 0007.
 */

public class TrackDetail {
    private int id;  //id
    private int tid; //tid为track的id
    private double lat; //纬度
    private double lng;  //经度
    private Track track;  //当前坐标点所属的线程

    public TrackDetail() {
    }

    public TrackDetail(int id, int tid, double lat, double lng) {
        this.id = id;
        this.tid = tid;
        this.lat = lat;
        this.lng = lng;
    }

    public TrackDetail(int tid, double lat, double lng) {
        this.tid = tid;
        this.lat = lat;
        this.lng = lng;
    }

    public TrackDetail(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    @Override
    public String toString() {
        return "TrackDetail{" +
                "id=" + id +
                ", tid=" + tid +
                ", lat=" + lat +
                ", lng=" + lng +
                ", track=" + track +
                '}';
    }
}

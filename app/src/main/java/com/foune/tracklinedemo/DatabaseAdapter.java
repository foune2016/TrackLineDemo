package com.foune.tracklinedemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * descreption:
 * company: foune.com
 * Created by xuyanliang on 2017/2/7 0007.
 */

public class DatabaseAdapter {
    private DatabaseHelper dbHelper;

    public DatabaseAdapter(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    //添加线路跟踪
    public int addTrack(Track track) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TRACK_NAME, track.getTrack_name());
        values.put(DatabaseHelper.CREATE_DATE, track.getCreate_date());
        values.put(DatabaseHelper.START_LOC, track.getStart_loc());
        values.put(DatabaseHelper.END_LOC, track.getEnd_loc());
        long id = db.insertOrThrow(DatabaseHelper.TABLE_TRACK, null, values);
        db.close();
        return (int)id;
    }

    //更新终点地址
    public void updateEndLoc(Track track) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.END_LOC, track.getEnd_loc());
        db.update(DatabaseHelper.TABLE_TRACK, values, DatabaseHelper.ID + "=?", new String[]{String.valueOf(track.getId())});
        db.close();
    }

    //添加线路跟踪明细
    public void addTrackDetail(TrackDetail trackDetail) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TID, trackDetail.getTid());
        values.put(DatabaseHelper.LAT, trackDetail.getLat());
        values.put(DatabaseHelper.LNG, trackDetail.getLng());
        db.insert(DatabaseHelper.TABLE_TRACK_DETAIL, null, values);
        db.close();
    }

    //根据ID查询线路跟踪
    public ArrayList<TrackDetail> getTrackDetails(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<TrackDetail> list = new ArrayList<TrackDetail>();
        String[] columns = new String[]{DatabaseHelper.ID,DatabaseHelper.TID, DatabaseHelper.LAT, DatabaseHelper.LNG};
        String selection = DatabaseHelper.TID + "=?";
        String[] selectionArgs = {String.valueOf(id)};
        Cursor c = db.query(DatabaseHelper.TABLE_TRACK_DETAIL,columns,selection,selectionArgs,null,null,null);
        TrackDetail detail = null;
        while(c.moveToNext()){
            detail = new TrackDetail(c.getInt(0),c.getInt(1),c.getDouble(2),c.getDouble(3));
            list.add(detail);
        }
        db.close();
        return list;
    }

    //查询所有路线
    public ArrayList<Track> getTracks(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<Track> list = new ArrayList<Track>();
        String[] columns = new String[]{DatabaseHelper.ID, DatabaseHelper.TRACK_NAME,
                DatabaseHelper.CREATE_DATE,DatabaseHelper.START_LOC,DatabaseHelper.END_LOC};
        Cursor c = db.query(DatabaseHelper.TABLE_TRACK,columns,null,null,null,null,null);
        Track track = null;
        while(c.moveToNext()){
            track = new Track(c.getInt(0), c.getString(1), c.getString(2),
                    c.getString(3), c.getString(4));
            list.add(track);
        }
        db.close();
        return list;
    }

    //根据ID删除路线跟踪
    public void delTrack(int id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_TRACK,DatabaseHelper.ID+"=?",new String[]{String.valueOf(id)});
        db.delete(DatabaseHelper.TABLE_TRACK_DETAIL,DatabaseHelper.TID+"=?",new String[]{String.valueOf(id)});
        db.close();
    }
}









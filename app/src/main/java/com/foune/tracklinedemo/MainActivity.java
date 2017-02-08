package com.foune.tracklinedemo;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.foune.tracklinedemo.R.id.mapView;

public class MainActivity extends AppCompatActivity {

    private static final int BACK_OVER = 0x1;
    private MapView mMapView;
    private BaiduMap baiduMap;
    private LocationClient mLocationClient;
    private MyLocationListener myLocationListener;
    private boolean flag = false;
    private double currentLat, currentLng; // 当前的经纬度
    private String currentAddr;// 当前所在的地址
    private EditText et_track_name;
    private DatabaseAdapter adapter;
    private int currentTrackLineID;
    private ArrayList<LatLng> list = new ArrayList<>();
    private boolean isTracking = false;
    private GeoCoder geoCoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(mapView);
        adapter = new DatabaseAdapter(this);
        initMap();
    }

    private void initMap() {
        baiduMap = mMapView.getMap();
        baiduMap.setMyLocationEnabled(true);//打开定位图层
        mLocationClient = new LocationClient(getApplicationContext()); // 声明LocationClient类
        myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener); // 注册监听函数

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(5000);// 设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
        mLocationClient.setLocOption(option);
        mLocationClient.start();// 启动SDK定位
        mLocationClient.requestLocation();// 发起定位请求

        //转换地理编码的监听器
        geoCoder = GeoCoder.newInstance();
        geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    //没有检索到结果
                } else {
                    currentAddr = reverseGeoCodeResult.getAddress();
                    Track track = new Track();
                    track.setEnd_loc(currentAddr);
                    track.setId(currentTrackLineID);
                    adapter.updateEndLoc(track);
                }
            }
        });
    }

    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation != null && flag) {
                flag = false;
                currentLat = bdLocation.getLatitude();
                currentLng = bdLocation.getLongitude();
                currentAddr = bdLocation.getAddrStr();
                //构造我的当前位置信息
                MyLocationData.Builder builder = new MyLocationData.Builder();
                builder.latitude(bdLocation.getLatitude());
                builder.longitude(bdLocation.getLongitude());
                builder.accuracy(bdLocation.getRadius());
                builder.direction(bdLocation.getDirection());
                builder.speed(bdLocation.getSpeed());
                MyLocationData locationData = builder.build();

                //将我的位置信息设置到地图上
                baiduMap.setMyLocationData(locationData);
                //配置我的位置
                LatLng latlng = new LatLng(currentLat, currentLng);
                baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, null));
                baiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(latlng, 16));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mapmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                myLocation();
                break;
            case R.id.item2:
                startLoc();
                break;
            case R.id.item3:
                endLoc();
                break;
            case R.id.item4:
                backLoc();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    AlertDialog dialog = null;

    //跟踪回放
    private void backLoc() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("跟踪路线");
        View view = getLayoutInflater().inflate(R.layout.listview, null);
        ListView listView = (ListView) view.findViewById(R.id.listView);
        ArrayList<HashMap<String, String>> data = new ArrayList<>();
        ArrayList<Track> tracks = adapter.getTracks();
        HashMap<String, String> map = null;
        for (Track track : tracks) {
            map = new HashMap<>();
            map.put("id", String.valueOf(track.getId()));
            map.put("createDate", track.getTrack_name() + "--" + track.getCreate_date());
            map.put("startEndLoc", "从[" + track.getStart_loc() + "]到[" + track.getEnd_loc() + "]");
            data.add(map);
        }
        SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.item_listview, new String[]{"id", "createDate", "startEndLoc"}
                , new int[]{R.id.tv_id, R.id.tv_trackname_createdate, R.id.tv_startEndLoc});
        listView.setAdapter(sAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv_id = (TextView) view.findViewById(R.id.tv_id);
                int _id = Integer.parseInt(tv_id.getText().toString());
                baiduMap.clear();
                new Thread(new TrackBackThread(_id)).start();
                dialog.dismiss();
            }
        });
        builder.setView(view);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog = builder.create();
        dialog.show();
    }

    //结束跟踪
    private void endLoc() {
        isTracking = false;
        Toast.makeText(this, "跟踪结束", Toast.LENGTH_SHORT).show();
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(new LatLng(currentLat, currentLng)));
    }

    //开始跟踪
    private void startLoc() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("线路跟踪");
        builder.setCancelable(true);
        final View view = getLayoutInflater().inflate(R.layout.add_track_line_dialog, null);
        builder.setView(view);
        builder.setPositiveButton("添加", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                et_track_name = (EditText) view.findViewById(R.id.et_track_name);
                String track_name = et_track_name.getText().toString();
                createTrack(track_name);
                Toast.makeText(MainActivity.this, "跟踪开始", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private void createTrack(String track_name) {
        Track track = new Track();
        track.setTrack_name(track_name);
        track.setCreate_date(DateUtils.toDate(new Date()));
        track.setStart_loc(currentAddr);
        currentTrackLineID = adapter.addTrack(track);
        TrackDetail detail = new TrackDetail(currentTrackLineID, currentLat, currentLat);
        adapter.addTrackDetail(detail);
        baiduMap.clear();
        addOverlay();
        list.add(new LatLng(currentLat, currentLng));
        isTracking = true;
        new Thread(new TrackThread()).start();
    }

    //添加标记
    private void addOverlay() {
        baiduMap.setMyLocationEnabled(false);
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.marker);
        LatLng latlng = new LatLng(currentLat, currentLng);
        OverlayOptions option = new MarkerOptions().position(latlng).icon(bitmap);
        baiduMap.addOverlay(option);
        baiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(latlng));
    }

    //我的定位
    private void myLocation() {
        Toast.makeText(this, "正在定位中", Toast.LENGTH_SHORT).show();
        flag = true;
        baiduMap.clear();//清除地图上自定义的图层
        baiduMap.setMyLocationEnabled(true);//启用我的位置图层
        mLocationClient.requestLocation();//发送定位请求
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    private class TrackThread implements Runnable {
        @Override
        public void run() {
            while (isTracking) {
                getLocation();
                TrackDetail detail = new TrackDetail(currentTrackLineID, currentLat, currentLng);
                System.out.println("detail--"+detail);
                adapter.addTrackDetail(detail);
                addOverlay();
                list.add(new LatLng(currentLat, currentLng));
                drawLine();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //画线
    private void drawLine() {
        OverlayOptions option = new PolylineOptions().points(list).color(0xFFFF0000);
        baiduMap.addOverlay(option);
        list.remove(0);
    }

    //模拟位置
    private void getLocation() {
        currentLat += Math.random() / 1000;
        currentLng += Math.random() / 1000;
    }

    private class TrackBackThread implements Runnable {
        private int id;

        public TrackBackThread(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            ArrayList<TrackDetail> details = adapter.getTrackDetails(id);
            for (TrackDetail t:details)
            System.out.println(t);
            list.clear();
            currentLat = details.get(0).getLat();
            currentLng = details.get(0).getLng();
            list.add(new LatLng(currentLat,currentLng));
            addOverlay();
            for(TrackDetail detail:details){
                currentLat = detail.getLat();
                currentLng = detail.getLng();
                list.add(new LatLng(currentLat,currentLng));
                addOverlay();
                drawLine();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            handler.sendEmptyMessage(BACK_OVER);
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case BACK_OVER:
                    Toast.makeText(MainActivity.this, "回放结束", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
}

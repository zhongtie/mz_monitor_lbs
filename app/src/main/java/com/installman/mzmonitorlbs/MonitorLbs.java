package com.installman.mzmonitorlbs;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

public class MonitorLbs extends Activity {
    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    double mLocLat, mLocLng;
    private static final float mapZoomLevel = 19.0f;

    //存储相关
    DatabaseHelper mDbHelper;
    SQLiteDatabase mDatabase;

    //地图相关
    MapView mMapView;
    BaiduMap mBaiduMap;

    // UI相关
    Button requestLocButton;
    Button setLocButton;
    Button clearLocButton;
    Button saveLocButton;
    TextView latlngTextView;
    boolean isFirstLoc = true; // 是否首次定位

    public void onCreate(Bundle savedInstanceState) {
        SDKInitializer.initialize(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_lbs);

        //存储
        mDbHelper = new DatabaseHelper(this);
        mDatabase = mDbHelper.getWritableDatabase();

        //UI初始化
        latlngTextView = (TextView) findViewById(R.id.textView);

        requestLocButton = (Button) findViewById(R.id.buttonMod);
        mCurrentMode = LocationMode.FOLLOWING;

        requestLocButton.setText("跟随");
        OnClickListener btnClickListener = new OnClickListener() {
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case NORMAL:
                        requestLocButton.setText("跟随");
                        mCurrentMode = LocationMode.FOLLOWING;
                        mBaiduMap
                                .setMyLocationConfigeration(new MyLocationConfiguration(
                                        mCurrentMode, true, mCurrentMarker));
                        break;
                    case FOLLOWING:
                        requestLocButton.setText("普通");
                        mCurrentMode = LocationMode.NORMAL;
                        mBaiduMap
                                .setMyLocationConfigeration(new MyLocationConfiguration(
                                        mCurrentMode, true, mCurrentMarker));
                        break;
/*                    case FOLLOWING:
                        requestLocButton.setText("罗盘");
                        mCurrentMode = LocationMode.COMPASS;
                        mBaiduMap
                                .setMyLocationConfigeration(new MyLocationConfiguration(
                                        mCurrentMode, true, mCurrentMarker));
                        break;*/
                    default:
                        break;
                }
            }
        };
        requestLocButton.setOnClickListener(btnClickListener);


        setLocButton = (Button) findViewById(R.id.buttonSet);
        setLocButton.setText("放置");
        OnClickListener btnMrkClickListener = new OnClickListener() {
            public void onClick(View v) {
                LatLng point = new LatLng(mLocLat, mLocLng);
                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_marker);
                addMarker(point, bitmap);
            }
        };
        setLocButton.setOnClickListener(btnMrkClickListener);

        clearLocButton = (Button) findViewById(R.id.buttonClear);
        clearLocButton.setText("清除");
        OnClickListener btnClrClickListener = new OnClickListener() {
            public void onClick(View v) {
                mBaiduMap.clear();
            }
        };
        clearLocButton.setOnClickListener(btnClrClickListener);

        saveLocButton = (Button) findViewById(R.id.buttonSave);
        saveLocButton.setText("保存");
        OnClickListener btnSaveClicklistener = new OnClickListener() {
            public void onClick(View v) {
                mDatabase.execSQL("insert into mzMonitor(title, latitude, longitude) values(?,?,?)",
                        new Object[]{"marker", mLocLat, mLocLng});
            }
        };
        saveLocButton.setOnClickListener(btnSaveClicklistener);

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap
                .setMyLocationConfigeration(new MyLocationConfiguration(
                        mCurrentMode, true, mCurrentMarker));
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);//1秒刷新一次
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(mapZoomLevel);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
            mLocLat = location.getLatitude();
            mLocLng = location.getLongitude();
            latlngTextView.setText("lat:"+mLocLat+";lng:"+mLocLng);
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    protected void addMarker(LatLng point, BitmapDescriptor bitmap){
        OverlayOptions options = new MarkerOptions().icon(bitmap).title("marker").position(point);
        mBaiduMap.addOverlay(options);
    }
}

class DatabaseHelper extends SQLiteOpenHelper{
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "moniterLbs.db";

    public DatabaseHelper(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public void onCreate(final SQLiteDatabase db) {
        String sql = "create table mzMonitor(" +
                "_id INTEGER DEFAULT '1' NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "latitude FLOAT NOT NULL," +
                "longitude FLOAT NOT NULL" +
                ")";
        db.execSQL(sql);
    }

    public void onUpgrade(final SQLiteDatabase db, int oldV, final int newV) {
    }
}
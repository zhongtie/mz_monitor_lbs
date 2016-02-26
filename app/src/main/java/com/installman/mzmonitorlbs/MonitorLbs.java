package com.installman.mzmonitorlbs;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
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
    private BDLocation mBDLocation;
    private LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    double dLocLat, dLocLng;
    private static final float mapZoomLevel = 19.0f;

    //存储相关
    DatabaseHelper mDbHelper;
    SQLiteDatabase mDatabase;
    public enum eMonitorType{BALL, GUN, SMARK};
    eMonitorType mMonitorType;

    //地图相关
    MapView mMapView;
    BaiduMap mBaiduMap;

    // UI相关
    Button buttonLocMod;
    Button buttonSetLoc;
    PopupMenu popupMenuSetLoc;
    Menu menuSetLoc;
    Button buttonClearLoc;
    Button buttonSaveLoc;
    Button buttonRequestLoc;
    Button buttonShowAll;
    TextView tvLatLng;
    boolean isFirstLoc = true; // 是否首次定位

    public void onCreate(Bundle savedInstanceState) {
        SDKInitializer.initialize(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_lbs);

        //存储
        mDbHelper = new DatabaseHelper(this);
        mDatabase = mDbHelper.getWritableDatabase();

        //UI初始化
        tvLatLng = (TextView) findViewById(R.id.textView);

        buttonLocMod = (Button) findViewById(R.id.buttonMod);
        mCurrentMode = LocationMode.FOLLOWING;
        buttonLocMod.setText("跟随");
        OnClickListener btnModListener = new OnClickListener() {
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case NORMAL:
                        buttonLocMod.setText("跟随");
                        mCurrentMode = LocationMode.FOLLOWING;
                        mBaiduMap
                                .setMyLocationConfigeration(new MyLocationConfiguration(
                                        mCurrentMode, true, mCurrentMarker));
                        break;
                    case FOLLOWING:
                        buttonLocMod.setText("普通");
                        mCurrentMode = LocationMode.NORMAL;
                        mBaiduMap
                                .setMyLocationConfigeration(new MyLocationConfiguration(
                                        mCurrentMode, true, mCurrentMarker));
                        break;
/*                    case FOLLOWING:
                        buttonLocMod.setText("罗盘");
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
        buttonLocMod.setOnClickListener(btnModListener);


        buttonSetLoc = (Button) findViewById(R.id.buttonSet);
        buttonSetLoc.setText("放置");

        OnClickListener btnMrkClickListener = new OnClickListener() {
            public void onClick(View v) {
                popupMenuSetLoc = new PopupMenu(MonitorLbs.this, findViewById(R.id.buttonSet));
                menuSetLoc = popupMenuSetLoc.getMenu();
                menuSetLoc.add(Menu.NONE, Menu.FIRST + 0, 0, "动球");
                menuSetLoc.add(Menu.NONE, Menu.FIRST + 1, 1, "固枪");
                menuSetLoc.add(Menu.NONE, Menu.FIRST + 2, 2, "智枪");
                popupMenuSetLoc.show();
                PopupMenu.OnMenuItemClickListener clOnMenuItemClick = new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        LatLng point = new LatLng(dLocLat, dLocLng);
                        BitmapDescriptor bitmap;
                        switch (item.getItemId()){
                            case Menu.FIRST + 0:
                                mMonitorType = eMonitorType.BALL;
                                addMarker(point, mMonitorType.ordinal());
                                break;
                            case Menu.FIRST + 1:
                                mMonitorType = eMonitorType.GUN;
                                addMarker(point, mMonitorType.ordinal());
                                break;
                            case Menu.FIRST + 2:
                                mMonitorType = eMonitorType.SMARK;
                                addMarker(point, mMonitorType.ordinal());
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                };
                popupMenuSetLoc.setOnMenuItemClickListener(clOnMenuItemClick);
            }
        };
        buttonSetLoc.setOnClickListener(btnMrkClickListener);

        buttonClearLoc = (Button) findViewById(R.id.buttonClear);
        buttonClearLoc.setText("清除");
        OnClickListener btnClrClickListener = new OnClickListener() {
            public void onClick(View v) {
                mBaiduMap.clear();
            }
        };
        buttonClearLoc.setOnClickListener(btnClrClickListener);

        buttonSaveLoc = (Button) findViewById(R.id.buttonSave);
        buttonSaveLoc.setText("保存");
        OnClickListener btnSaveClickListener = new OnClickListener() {
            public void onClick(View v) {
                mDatabase.execSQL("insert into mzMonitor(title, latitude, longitude, monitor_type) " +
                                "values(?,?,?,?)",
                        new Object[]{"marker", dLocLat, dLocLng, mMonitorType.ordinal()});
            }
        };
        buttonSaveLoc.setOnClickListener(btnSaveClickListener);

        buttonRequestLoc = (Button) findViewById(R.id.buttonLoc);
        buttonRequestLoc.setText("定位");
        OnClickListener btnLocClickListener = new OnClickListener() {
            public void onClick(View v) {
                mLocClient.requestLocation();
                buttonLocMod.setText("跟随");
                mCurrentMode = LocationMode.FOLLOWING;
                mBaiduMap
                        .setMyLocationConfigeration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));
                mBDLocation = mLocClient.getLastKnownLocation();

                dLocLat = mBDLocation.getLatitude();
                dLocLng = mBDLocation.getLongitude();
                tvLatLng.setText("lat:" + dLocLat + ";lng:" + dLocLng);
            }
        };
        buttonRequestLoc.setOnClickListener(btnLocClickListener);

        buttonShowAll = (Button) findViewById(R.id.buttonAll);
        buttonShowAll.setText("所有");
        OnClickListener btnAllClickListener = new OnClickListener() {
            public void onClick(View v) {
                mBaiduMap.clear();
                showAllMarker();
            }
        };
        buttonShowAll.setOnClickListener(btnAllClickListener);

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        BaiduMap.OnMapClickListener mapClickListener = new BaiduMap.OnMapClickListener() {
            public void onMapClick(LatLng latLng) {
                dLocLat = latLng.latitude;
                dLocLng = latLng.longitude;
                tvLatLng.setText("lat:" + dLocLat + ";lng:" + dLocLng);
            }

            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        };
        BaiduMap.OnMapStatusChangeListener mapStatusChangeListener = new BaiduMap.OnMapStatusChangeListener() {
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            public void onMapStatusChange(MapStatus mapStatus) {
                float zoomLvl = mapStatus.zoom;
                if(zoomLvl >= 19.0){
                    mBaiduMap.clear();
                    showAllMarker();
                }else{
                    mBaiduMap.clear();
                }
            }

            public void onMapStatusChangeFinish(MapStatus mapStatus) {

            }
        };
        BaiduMap.OnMarkerDragListener mrkDragListener = new BaiduMap.OnMarkerDragListener() {
            public void onMarkerDrag(Marker marker) {

            }

            public void onMarkerDragEnd(Marker marker) {
                Toast.makeText(getApplicationContext(), "drag end", Toast.LENGTH_SHORT).show();
                dLocLat = marker.getPosition().latitude;
                dLocLng = marker.getPosition().longitude;
                tvLatLng.setText("lat:" + dLocLat + ";lng:" + dLocLng);
            }

            public void onMarkerDragStart(Marker marker) {
                Toast.makeText(getApplicationContext(), "drag start", Toast.LENGTH_SHORT).show();
            }
        };
        mBaiduMap.setOnMapClickListener(mapClickListener);
        mBaiduMap.setOnMarkerDragListener(mrkDragListener);
        mBaiduMap.setOnMapStatusChangeListener(mapStatusChangeListener);

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
                dLocLat = location.getLatitude();
                dLocLng = location.getLongitude();
                tvLatLng.setText("lat:" + dLocLat + ";lng:" + dLocLng);
            }
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
        OverlayOptions options = new MarkerOptions().icon(bitmap).title("mrk").position(point).draggable(true);
        mBaiduMap.addOverlay(options);
    }

    protected void addMarker(LatLng point, int monitor_type){
        BitmapDescriptor bitmap;
        switch(monitor_type){
            case 0:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ball);
                addMarker(point, bitmap);
                break;
            case 1:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.gun);
                addMarker(point, bitmap);
                break;
            case 2:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.smart);
                addMarker(point, bitmap);
                break;
            default:
                break;
        }
    }

    protected void showAllMarker(){
        String sql = "select * from mzMonitor order by _id";
        Cursor cu = mDatabase.rawQuery(sql, null);
        while(cu.moveToNext()){
            int id = cu.getInt(0);
            String title = cu.getString(1);
            double lat = cu.getDouble(2);
            double lon = cu.getDouble(3);
            int montior_type = cu.getInt(4);
            addMarker(new LatLng(lat, lon), montior_type);
        }
        cu.close();
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
                "latitude DOUBLE NOT NULL," +
                "longitude DOUBLE NOT NULL," +
                "monitor_type INT NOT NULL" +
                ")";
        db.execSQL(sql);
    }

    public void onUpgrade(final SQLiteDatabase db, int oldV, final int newV) {
        String sql = "drop table mzMonitor";
        db.execSQL(sql);
    }
}
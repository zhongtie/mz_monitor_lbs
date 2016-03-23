package com.installman.mzmonitorlbs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
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
    protected LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    protected BDLocation mBDLocation;
    protected LocationMode mCurrentMode;
    //覆盖物相关
    protected BitmapDescriptor mCurrentMarker;
    protected int mrkAngle = 0;//默认的覆盖物方向
    protected String mrkTitle;
    protected double dLocLat, dLocLng;
    protected String strLocAddr;

    //存储相关
    protected DatabaseHelper mDbHelper;
    protected SQLiteDatabase mDatabase;
    protected enum eMonitorType{BALL, GUN, SMART}
    protected eMonitorType mMonitorType;

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
    private static final float mapZoomLevel = 19.0f;//设置初始地图点缩放等级
    private static final float mapMarkShwoZoomlvl = 19.0f;//设置缩放等级大于多少时显示所有覆盖物
    boolean isMarkShowAll = true; //根据zoomlvl判断是否显示所有覆盖物

    public void onCreate(Bundle savedInstanceState) {
        SDKInitializer.initialize(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_lbs);

        //后台数据库sqlite存储
        mDbHelper = new DatabaseHelper(this);
        mDatabase = mDbHelper.getWritableDatabase();

        //UI初始化
        //显示GPS坐标
        tvLatLng = (TextView) findViewById(R.id.textView);

        //

        //地图定位模式按钮
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

        //地图上放置覆盖物按钮
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
                        switch (item.getItemId()){
                            case Menu.FIRST + 0:
                                mMonitorType = eMonitorType.BALL;
                                addMarker(mrkTitle, point, mMonitorType.ordinal(), mrkAngle);
                                break;
                            case Menu.FIRST + 1:
                                mMonitorType = eMonitorType.GUN;
                                addMarker(mrkTitle, point, mMonitorType.ordinal(), mrkAngle);
                                break;
                            case Menu.FIRST + 2:
                                mMonitorType = eMonitorType.SMART;
                                addMarker(mrkTitle, point, mMonitorType.ordinal(), mrkAngle);
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

        //清除所有覆盖物
        buttonClearLoc = (Button) findViewById(R.id.buttonClear);
        buttonClearLoc.setText("清除");
        OnClickListener btnClrClickListener = new OnClickListener() {
            public void onClick(View v) {
                mBaiduMap.clear();
            }
        };
        buttonClearLoc.setOnClickListener(btnClrClickListener);

        //保存最后一个放置的覆盖物
        buttonSaveLoc = (Button) findViewById(R.id.buttonSave);
        buttonSaveLoc.setText("保存");
        OnClickListener btnSaveClickListener = new OnClickListener() {
            public void onClick(View v) {
                mDatabase.execSQL("insert into mzMonitor(title, latitude, longitude, monitor_type, monitor_angle) " +
                                "values(?,?,?,?,?)",
                        new Object[]{mrkTitle, dLocLat, dLocLng, mMonitorType.ordinal(), mrkAngle});
            }
        };
        buttonSaveLoc.setOnClickListener(btnSaveClickListener);

        //定位按钮，点击后定位到当前位置
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

                //设置全局位置变量，方便放置
                dLocLat = mBDLocation.getLatitude();
                dLocLng = mBDLocation.getLongitude();
                strLocAddr = mBDLocation.getAddrStr();
                tvLatLng.setText(strLocAddr + ";lat:" + dLocLat + ";lng:" + dLocLng);
                //预先设置覆盖物名称
                mrkTitle = Integer.toString(getMaxMarkerId());
            }
        };
        buttonRequestLoc.setOnClickListener(btnLocClickListener);

        //显示所有覆盖物按钮
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

        //自定义各种地图事件
        //在地图上点击，获取GPS坐标及名称
        BaiduMap.OnMapClickListener mapClickListener = new BaiduMap.OnMapClickListener() {
            public void onMapClick(LatLng latLng) {
                dLocLat = latLng.latitude;
                dLocLng = latLng.longitude;
                tvLatLng.setText("lat:" + dLocLat + ";lng:" + dLocLng);
                //预先设置覆盖物名称
                mrkTitle = Integer.toString(getMaxMarkerId());
            }

            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        };

        //地图缩放后，显示或清除覆盖物
        BaiduMap.OnMapStatusChangeListener mapStatusChangeListener = new BaiduMap.OnMapStatusChangeListener() {
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            public void onMapStatusChange(MapStatus mapStatus) {
                float zoomLvl = mapStatus.zoom;
                if(zoomLvl >= mapMarkShwoZoomlvl && (! isMarkShowAll)){
                    isMarkShowAll = true;
                    mBaiduMap.clear();
                    showAllMarker();
                }else{
                    mBaiduMap.clear();
                    isMarkShowAll = false;
                }
            }

            public void onMapStatusChangeFinish(MapStatus mapStatus) {

            }
        };
        //拖曳覆盖物，重新获取GPS位置
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
        //点击覆盖物，显示名称及是否进行修改
        BaiduMap.OnMarkerClickListener mrkClickListener = new BaiduMap.OnMarkerClickListener(){
            public boolean onMarkerClick(Marker marker){
                Log.d("mrkClick", "Title is:" + marker.getTitle());
                Log.d("mrkClick", "Lat is:" + marker.getPosition().latitude + ";Lon is:" + marker.getPosition().longitude);
                Intent intent = new Intent(MonitorLbs.this, MarkerInfoSimp.class);
                intent.putExtra("mrkTitle", marker.getTitle());
                startActivity(intent);
                return true;
            }
        };
        mBaiduMap.setOnMapClickListener(mapClickListener);
        mBaiduMap.setOnMarkerDragListener(mrkDragListener);
        mBaiduMap.setOnMapStatusChangeListener(mapStatusChangeListener);
        mBaiduMap.setOnMarkerClickListener(mrkClickListener);

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

    protected void addMarker(String title, LatLng point, BitmapDescriptor bitmap){
        OverlayOptions options = new MarkerOptions().icon(bitmap).title(title).position(point).draggable(true);
        mBaiduMap.addOverlay(options);
    }

    protected void addMarker(String title, LatLng point, int monitor_type, int monitor_angle){
        BitmapDescriptor bitmap;
        switch(monitor_type){
            case 0:
                switch (monitor_angle){
                    case 0:
                        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ball_0);
                        break;
                    case 90:
                        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ball_90);
                        break;
                    case 18:
                        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ball_180);
                        break;
                    case 270:
                        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ball_270);
                        break;
                    default:
                        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ball_0);
                        break;
                }
                break;
            case 1:
                switch (monitor_angle){
                    case 0:
                        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.gun_0);
                        break;
                    case 90:
                        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.gun_90);
                        break;
                    case 18:
                        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.gun_180);
                        break;
                    case 270:
                        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.gun_270);
                        break;
                    default:
                        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.gun_0);
                        break;
                }
                break;
            case 2:
                switch (monitor_angle){
                    case 0:
                        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.smart_0);
                        break;
                    case 90:
                        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.smart_90);
                        break;
                    case 18:
                        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.smart_180);
                        break;
                    case 270:
                        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.smart_270);
                        break;
                    default:
                        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.smart_0);
                        break;
                }
                break;
            default:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ball_0);
                break;
        }
        addMarker(title, point, bitmap);
    }

    protected int getMaxMarkerId(){
        int id = 0;
        String sql = "select * from mzMonitor order by _id desc limit 1";
        Cursor cu = mDatabase.rawQuery(sql, null);
        while(cu.moveToNext()){
            id = cu.getInt(0);
        }
        cu.close();
        Log.d("getMaxMarkerId", "id:" + id);
        return id;
    }

    protected void showAllMarker(){
        String sql = "select * from mzMonitor order by _id";
        Cursor cu = mDatabase.rawQuery(sql, null);
        while(cu.moveToNext()){
            int id = cu.getInt(0);
            String title = cu.getString(1);
            double lat = cu.getDouble(2);
            double lon = cu.getDouble(3);
            int monitor_type = cu.getInt(4);
            int monitor_angle = cu.getInt(5);
            addMarker(title, new LatLng(lat, lon), monitor_type, monitor_angle);
        }
        cu.close();
    }
}

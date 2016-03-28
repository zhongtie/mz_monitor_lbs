package com.installman.mzmonitorlbs;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
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
    protected LocationClient gLocClient;
    public MyLocationListenner gMyLocListener = new MyLocationListenner();
    protected BDLocation gBDLocation;
    protected LocationMode gCurrentMode;
    //覆盖物相关
    MzMonitor gMonitor;
    protected BitmapDescriptor gMrkBitmapDesc;
    protected int gMrkAngle = 0;//默认的覆盖物方向
    protected String gMrkTitle;
    protected double dLocLat, dLocLng;
    protected String strLocAddr;
    protected MzMonitor.eMonitorType gMonitorType;

    //存储相关
    protected DatabaseHelper gDbHelper;
    protected SQLiteDatabase gDatabase;

    //地图相关
    MapView gMapView;
    BaiduMap gBaiduMap;

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
    private static final float mapMarkShwoZoomlvl = 15.0f;//设置缩放等级大于多少时显示所有覆盖物
    boolean isMarkShowAll = true; //根据zoomlvl判断是否显示所有覆盖物

    public void onCreate(Bundle savedInstanceState) {
        SDKInitializer.initialize(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_lbs);

        //后台数据库sqlite存储
        gDbHelper = new DatabaseHelper(this);
        gDatabase = gDbHelper.getWritableDatabase();

        //UI初始化
        //显示GPS坐标
        tvLatLng = (TextView) findViewById(R.id.textView);

        //地图定位模式按钮
        buttonLocMod = (Button) findViewById(R.id.buttonMod);
        gCurrentMode = LocationMode.FOLLOWING;
        buttonLocMod.setText("跟随");
        OnClickListener btnModClick = new OnClickListener() {
            public void onClick(View v) {
                switch (gCurrentMode) {
                    case NORMAL:
                        buttonLocMod.setText("跟随");
                        gCurrentMode = LocationMode.FOLLOWING;
                        gBaiduMap
                                .setMyLocationConfigeration(new MyLocationConfiguration(
                                        gCurrentMode, true, gMrkBitmapDesc));
                        break;
                    case FOLLOWING:
                        buttonLocMod.setText("普通");
                        gCurrentMode = LocationMode.NORMAL;
                        gBaiduMap
                                .setMyLocationConfigeration(new MyLocationConfiguration(
                                        gCurrentMode, true, gMrkBitmapDesc));
                        break;
                    default:
                        break;
                }
            }
        };
        buttonLocMod.setOnClickListener(btnModClick);

        //地图上放置覆盖物按钮
        buttonSetLoc = (Button) findViewById(R.id.buttonSet);
        buttonSetLoc.setText("放置");

        OnClickListener btnMrkClick = new OnClickListener() {
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
                                gMonitorType = MzMonitor.eMonitorType.BALL;
                                addMarker(gMrkTitle, point, gMonitorType.ordinal(), gMrkAngle);
                                break;
                            case Menu.FIRST + 1:
                                gMonitorType = MzMonitor.eMonitorType.GUN;
                                addMarker(gMrkTitle, point, gMonitorType.ordinal(), gMrkAngle);
                                break;
                            case Menu.FIRST + 2:
                                gMonitorType = MzMonitor.eMonitorType.SMART;
                                addMarker(gMrkTitle, point, gMonitorType.ordinal(), gMrkAngle);
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
        buttonSetLoc.setOnClickListener(btnMrkClick);

        //清除所有覆盖物
        buttonClearLoc = (Button) findViewById(R.id.buttonClear);
        buttonClearLoc.setText("清除");
        OnClickListener btnClrClick = new OnClickListener() {
            public void onClick(View v) {
                gBaiduMap.clear();
            }
        };
        buttonClearLoc.setOnClickListener(btnClrClick);

        //保存最后一个放置的覆盖物
        buttonSaveLoc = (Button) findViewById(R.id.buttonSave);
        buttonSaveLoc.setText("保存");
        OnClickListener btnSaveClick = new OnClickListener() {
            public void onClick(View v) {
                gMrkTitle = Integer.toString(getMaxMarkerId());
                gDatabase.execSQL("insert into mzMonitor(title, latitude, longitude, monitor_type, monitor_angle) " +
                                "values(?,?,?,?,?)",
                        new Object[]{gMrkTitle, dLocLat, dLocLng, gMonitorType.ordinal(), gMrkAngle});
            }
        };
        buttonSaveLoc.setOnClickListener(btnSaveClick);

        //定位按钮，点击后定位到当前位置
        buttonRequestLoc = (Button) findViewById(R.id.buttonLoc);
        buttonRequestLoc.setText("定位");
        OnClickListener btnLocClick = new OnClickListener() {
            public void onClick(View v) {
                gLocClient.requestLocation();
                buttonLocMod.setText("跟随");
                gCurrentMode = LocationMode.FOLLOWING;
                gBaiduMap
                        .setMyLocationConfigeration(new MyLocationConfiguration(
                                gCurrentMode, true, gMrkBitmapDesc));
                gBDLocation = gLocClient.getLastKnownLocation();

                //设置全局位置变量，方便放置
                dLocLat = gBDLocation.getLatitude();
                dLocLng = gBDLocation.getLongitude();
                strLocAddr = gBDLocation.getAddrStr();
                tvLatLng.setText(strLocAddr + ";lat:" + dLocLat + ";lng:" + dLocLng);
                //预先设置覆盖物名称
                gMrkTitle = Integer.toString(getMaxMarkerId());
            }
        };
        buttonRequestLoc.setOnClickListener(btnLocClick);

        //显示所有覆盖物按钮
        buttonShowAll = (Button) findViewById(R.id.buttonAll);
        buttonShowAll.setText("所有");
        OnClickListener btnAllClick = new OnClickListener() {
            public void onClick(View v) {
                gBaiduMap.clear();
                showAllMarker();
            }
        };
        buttonShowAll.setOnClickListener(btnAllClick);

        // 地图初始化
        gMapView = (MapView) findViewById(R.id.bmapView);
        gBaiduMap = gMapView.getMap();

        //自定义各种地图事件
        //在地图上点击，获取GPS坐标及名称
        BaiduMap.OnMapClickListener mapClick = new BaiduMap.OnMapClickListener() {
            public void onMapClick(LatLng latLng) {
                dLocLat = latLng.latitude;
                dLocLng = latLng.longitude;
                tvLatLng.setText("lat:" + dLocLat + ";lng:" + dLocLng);
                //预先设置覆盖物名称
                gMrkTitle = Integer.toString(getMaxMarkerId());
            }

            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        };

        //地图缩放后，显示或清除覆盖物
        BaiduMap.OnMapStatusChangeListener mapStatusChange = new BaiduMap.OnMapStatusChangeListener() {
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            public void onMapStatusChange(MapStatus mapStatus) {
                float zoomLvl = mapStatus.zoom;
                if(zoomLvl <= mapMarkShwoZoomlvl){
                    gBaiduMap.clear();
                }else{
                }
            }

            public void onMapStatusChangeFinish(MapStatus mapStatus) {

            }
        };
        //拖曳覆盖物，重新获取GPS位置
        BaiduMap.OnMarkerDragListener mrkDrag = new BaiduMap.OnMarkerDragListener() {
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
        BaiduMap.OnMarkerClickListener mrkClick = new BaiduMap.OnMarkerClickListener(){
            public boolean onMarkerClick(Marker marker){
                Log.d("mrkClick", "Title is:" + marker.getTitle());
                Log.d("mrkClick", "Lat is:" + marker.getPosition().latitude + ";Lon is:" + marker.getPosition().longitude);
                Intent intent = new Intent(MonitorLbs.this, MarkerInfoSimp.class);
                intent.putExtra("gMrkTitle", marker.getTitle());
                startActivity(intent);
                return true;
            }
        };
        gBaiduMap.setOnMapClickListener(mapClick);
        gBaiduMap.setOnMarkerDragListener(mrkDrag);
        gBaiduMap.setOnMapStatusChangeListener(mapStatusChange);
        gBaiduMap.setOnMarkerClickListener(mrkClick);

        // 开启定位图层
        gBaiduMap.setMyLocationEnabled(true);
        gBaiduMap
                .setMyLocationConfigeration(new MyLocationConfiguration(
                        gCurrentMode, true, gMrkBitmapDesc));
        // 定位初始化
        gLocClient = new LocationClient(this);
        gLocClient.registerLocationListener(gMyLocListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);//1秒刷新一次
        gLocClient.setLocOption(option);
        gLocClient.start();
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || gMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            gBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(mapZoomLevel);
                gBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                dLocLat = location.getLatitude();
                dLocLng = location.getLongitude();
                tvLatLng.setText("lat:" + dLocLat + ";lng:" + dLocLng);
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    protected void onPause() {
        gMapView.onPause();
        super.onPause();
    }

    protected void onResume() {
        gMapView.onResume();
        super.onResume();
    }

    protected void onDestroy() {
        // 退出时销毁定位
        gLocClient.stop();
        // 关闭定位图层
        gBaiduMap.setMyLocationEnabled(false);
        gMapView.onDestroy();
        gMapView = null;
        super.onDestroy();
    }

    protected void addMarker(String title, LatLng point, BitmapDescriptor bitmap){
        OverlayOptions options = new MarkerOptions().icon(bitmap).title(title).position(point).draggable(true);
        gBaiduMap.addOverlay(options);
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
                    case 180:
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
                    case 180:
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
                    case 180:
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
        int id = 1;//当数据库初始化没有记录时id为1
        String sql = "select * from mzMonitor order by _id desc limit 1";
        Cursor cu = gDatabase.rawQuery(sql, null);
        while(cu.moveToNext()){
            id = cu.getInt(0) + 1;
        }
        cu.close();
        Log.d("getMaxMarkerId", "id:" + id);
        return id;
    }

    protected void showAllMarker(){
        String sql = "select * from mzMonitor order by _id";
        Cursor cu = gDatabase.rawQuery(sql, null);
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

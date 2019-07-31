package com.mt.mapdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private AMap mAmap;
    private Button mBtRedrawArea, locationPosition;
    private boolean mIsEdit;

    private List<LatLng> mLatLnglist;
    private ArrayList<Marker> mMarkerList;
    private AppCompatTextView locationMessage;
    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationOption;
    private LatLng meLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtRedrawArea = findViewById(R.id.button);
        locationPosition = findViewById(R.id.button2);
        locationMessage = findViewById(R.id.location);

        LocationByGaode();    //定位配置

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        //初始化地图控制器对象
        if (mAmap == null) {
            mAmap = mMapView.getMap();
        }

        initLatLngData();
      /*  addArea(Color.parseColor("#050505"), Color.parseColor("#55FF3030"), mLatLnglist);
        mMarkerList = addMarker(false, mLatLnglist);*/
        initListener();

        if (appLocationPersion()) {
            locationClient.startLocation();
        }
    }

    private void initListener() {

        mBtRedrawArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 从地图上删除所有的overlay（marker，circle，polyline 等对象）
                mAmap.clear();
                addArea(Color.parseColor("#050505"), Color.parseColor("#55FF3030"), mLatLnglist);
                mIsEdit = false;
                mMarkerList = addMarker(false, mLatLnglist);
            }
        });

        //添加坐标点
        locationPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 从地图上删除所有的overlay（marker，circle，polyline 等对象）
                mAmap.clear();
                addArea(Color.parseColor("#050505"), Color.parseColor("#55FF3030"), mLatLnglist);
                mIsEdit = true;
                mMarkerList = addMarker(true, mLatLnglist);
            }
        });

        mAmap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mIsEdit) {
                    // 从地图上删除所有的overlay（marker，circle，polyline 等对象）
                    mAmap.clear();
                    int length = mLatLnglist.size();
                    mLatLnglist.add(length, latLng);
                    addArea(Color.parseColor("#050505"), Color.parseColor("#55FF3030"), mLatLnglist);
                    mIsEdit = true;
                    mMarkerList = addMarker(true, mLatLnglist);
                }
            }
        });

        // marker拖动事件监听接口
        mAmap.setOnMarkerDragListener(new AMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                Log.d("AAAAAAAAAAAAA", marker.getPosition().latitude + "");
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                // 从地图上删除所有的overlay（marker，circle，polyline 等对象）
                mAmap.clear();

                LatLng meLocation = marker.getPosition();
                for (int i = 0; i < mMarkerList.size(); i++) {
                    String markerTitle = marker.getTitle();
                    String oldMarkerTitle = mMarkerList.get(i).getTitle();
                    if (markerTitle.equals(oldMarkerTitle)) {
                        mLatLnglist.set(i, marker.getPosition());
                    }
                }
                addArea(Color.parseColor("#050505"), Color.parseColor("#55FF3030"), mLatLnglist);
                mIsEdit = true;
                mMarkerList = addMarker(true, mLatLnglist);
               /* if (!SpatialRelationUtil.isPolygonContainsPoint1(mLatLnglist,meLocation)) {
                    Toast.makeText(MainActivity.this,"超出管辖区域",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this,"已管辖区内",Toast.LENGTH_SHORT).show();
                }*/
                // 删除当前marker并销毁Marker的图片等资源
                marker.destroy();
            }
        });
    }


    private ArrayList<Marker> addMarker(boolean visible, List<LatLng> latLnglist) {
        ArrayList<MarkerOptions> markerOptionsList = new ArrayList<>();
        for (int i = 0; i < latLnglist.size(); i++) {
            // 在地图上添一组图片标记（marker）对象，并设置是否改变地图状态以至于所有的marker对象都在当前地图可视区域范围内显示
            MarkerOptions markerOptions = new MarkerOptions();
            // 设置Marker覆盖物的位置坐标。Marker经纬度坐标不能为Null，坐标无默认值
            markerOptions.position(latLnglist.get(i));
            // 设置Marker覆盖物是否可见
            markerOptions.visible(visible);
            // 设置Marker覆盖物是否可拖拽
            markerOptions.draggable(visible);
            // 设置 Marker覆盖物 的标题
            markerOptions.title(i + "");
            markerOptionsList.add(markerOptions);
        }

      /*  latLnglist.add(meLocation);
        MarkerOptions markerOptions1 = new MarkerOptions();
        // 设置Marker覆盖物的位置坐标。Marker经纬度坐标不能为Null，坐标无默认值
        markerOptions1.position(meLocation);
        // 设置Marker覆盖物是否可见
        markerOptions1.visible(visible);
        // 设置Marker覆盖物是否可拖拽
        markerOptions1.draggable(visible);
        markerOptions1.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_me));
        // 设置 Marker覆盖物 的标题
        markerOptions1.title("订单");
        markerOptionsList.add(markerOptions1);
*/

        // 在地图上添一组图片标记（marker）对象，并设置是否改变地图状态以至于所有的marker对象都在当前地图可视区域范围内显示
        return mAmap.addMarkers(markerOptionsList, true);
    }


    private void initLatLngData() {
        mLatLnglist = new ArrayList<>();
       /* int position = 0;
        mLatLnglist.add(position++, new LatLng(23.103171, 113.232999));
        mLatLnglist.add(position++, new LatLng(23.095631, 113.224331));
        mLatLnglist.add(position++, new LatLng(23.091131, 113.240166));
        mLatLnglist.add(position++, new LatLng(23.099539, 113.241797));*/
    }

    private void addArea(int strokeColor, int fillColor, List<LatLng> latLnglist) {
        // 定义多边形的属性信息
        PolygonOptions polygonOptions = new PolygonOptions();


        // 添加多个多边形边框的顶点
        for (LatLng latLng : latLnglist) {
            polygonOptions.add(latLng);
        }
        // 设置多边形的边框颜色，32位 ARGB格式，默认为黑色
        polygonOptions.strokeColor(strokeColor);
        // 设置多边形的边框宽度，单位：像素
        polygonOptions.strokeWidth(5);
        // 设置多边形的填充颜色，32位ARGB格式
        polygonOptions.fillColor(fillColor); // 注意要加前两位的透明度
        // 在地图上添加一个多边形（polygon）对象
        mAmap.addPolygon(polygonOptions);
    }

    private void LocationByGaode() {
        //初始化Client
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = getDefaultOption();
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
    }

    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(true);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        mOption.setGeoLanguage(AMapLocationClientOption.GeoLanguage.DEFAULT);//可选，设置逆地理信息的语言，默认值为默认语言（根据所在地区选择语言）
        return mOption;
    }

    AMapLocationListener locationListener = new AMapLocationListener() {

        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != location) {
                //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
                if (location.getErrorCode() == 0) {
                    locationMessage.setText("当前经纬度为： " + location.getLatitude() + " ， " + location.getLongitude());

                    meLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.draggable(true);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_me));
                    markerOptions.position(meLocation);
                    markerOptions.title("订单");
                    mAmap.addMarker(markerOptions);
                } else {
                    Toast.makeText(MainActivity.this, "定位失败", Toast.LENGTH_SHORT).show();
                }

            }
        }
    };

    private boolean appLocationPersion() {
        //判断时候开启定位权限
        LocationManager lm = (LocationManager) MainActivity.this.getSystemService(MainActivity.this.LOCATION_SERVICE);
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (ok) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // 没有权限，申请权限。
                // 申请授权。
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        400);
                return true;
            } else {
                return true;
            }
        } else {
            Toast.makeText(MainActivity.this, "请先开启定位权限", Toast.LENGTH_SHORT).show();

        }
        return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }
}

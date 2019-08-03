package com.mt.mapdemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolygonOptions;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.OnResponseListener;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.RequestQueue;
import com.yanzhenjie.nohttp.rest.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import top.yokey.miuidialog.MiuiInputDialog;
import top.yokey.miuidialog.MiuiInputListener;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private AMap mAmap;
    private Button mBtRedrawArea,button1;
    private TextView tv_title;
    private TextView change_code;
    private boolean mIsEdit = true;

    private List<LatLng> mLatLnglist;
    private ArrayList<Marker> mMarkerList;
    private LatLng meLocation;
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;
    private LoadingDialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtRedrawArea = findViewById(R.id.button);
        tv_title = findViewById(R.id.tv_title);
        change_code = findViewById(R.id.change_code);
        button1 = findViewById(R.id.button1);

        LoadingDialog.Builder loadBuilder = new LoadingDialog.Builder(this)
                .setCancelable(true)
                .setCancelOutside(false);
        loadingDialog = loadBuilder.create();

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        //初始化地图控制器对象
        if (mAmap == null) {
            mAmap = mMapView.getMap();
        }

        mAmap.moveCamera(CameraUpdateFactory.zoomTo(18));
        initLatLngData();
        initListener();

        sharedPreferences = getSharedPreferences("map", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        String code = sharedPreferences.getString("code", "");
      /*  if ("".equals(code)) {
            showCodeDialog("");
        }*/


    }

    private void showCodeDialog(String content) {
        new MiuiInputDialog.Builder(this)
                .setCancelable(false)//是否点击外部消失
                .setTitle("请输入编号")//标题
                .setContent(content)//内容
                .setNegativeButton("取消", new MiuiInputListener() {
                    @Override
                    public void onClick(String content,Dialog dialog) {

                    }
                })
                .setPositiveButton("确认", new MiuiInputListener() {
                    @Override
                    public void onClick(String content,Dialog dialog) {
                        if (content.length() == 0) {
                            Toast.makeText(MainActivity.this, "请输入编号", Toast.LENGTH_SHORT).show();
                        } else {
                            dialog.dismiss();
                            uploadData(content);
                        }
                    }
                })//右边的按钮
                .show();
    }

    private void initListener() {

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 从地图上删除所有的overlay（marker，circle，polyline 等对象）
                if (mLatLnglist.size() != 0) {
                    mAmap.clear();
                    mLatLnglist.remove(mLatLnglist.size()-1);
                    addArea(Color.parseColor("#050505"), Color.parseColor("#55FF3030"), mLatLnglist);
                    mMarkerList = addMarker(mIsEdit, mLatLnglist);
                }
            }
        });

        mBtRedrawArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 从地图上删除所有的overlay（marker，circle，polyline 等对象）
              /*  mAmap.clear();
                addArea(Color.parseColor("#050505"), Color.parseColor("#55FF3030"), mLatLnglist);
                mMarkerList = addMarker(mIsEdit, mLatLnglist);*/
                if (mLatLnglist.size() < 3) {
                    Toast.makeText(MainActivity.this, "最少选择三个点", Toast.LENGTH_SHORT).show();
                } else {
                    showCodeDialog("");
                }
               /* if (mIsEdit) {

                    if (mLatLnglist.size() < 3) {
                        Toast.makeText(MainActivity.this, "最少选择三个点", Toast.LENGTH_SHORT).show();
                    } else {

                        mBtRedrawArea.setText("规划商圈");
                        tv_title.setVisibility(View.GONE);
                        change_code.setVisibility(View.VISIBLE);

                        uploadData();
                        mIsEdit = !mIsEdit;
                    }

                } else {

                    mBtRedrawArea.setText("确定");
                    tv_title.setVisibility(View.VISIBLE);
                    change_code.setVisibility(View.GONE);
                    mIsEdit = !mIsEdit;
                }*/

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

                for (int i = 0; i < mMarkerList.size(); i++) {
                    String markerTitle = marker.getTitle();
                    String oldMarkerTitle = mMarkerList.get(i).getTitle();
                    if (markerTitle.equals(oldMarkerTitle)) {
                        mLatLnglist.set(i, marker.getPosition());
                    }
                }
                addArea(Color.parseColor("#050505"), Color.parseColor("#55FF3030"), mLatLnglist);
                mMarkerList = addMarker(mIsEdit, mLatLnglist);

                // 删除当前marker并销毁Marker的图片等资源
                marker.destroy();
            }
        });

        change_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = sharedPreferences.getString("code", "");
                showCodeDialog(code);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void uploadData(String code) {
        StringBuilder stringBuffer = new StringBuilder();

        RequestQueue mRequestQueue = NoHttp.newRequestQueue();
        Request<String> request = NoHttp.createStringRequest("https://shop.81dja.com/store/take/marketMap", RequestMethod.POST);
        for (LatLng latLng : mLatLnglist) {
            stringBuffer.append(latLng.latitude);
            stringBuffer.append(",");
            stringBuffer.append(latLng.longitude);
            stringBuffer.append(";");
        }
        String data =  stringBuffer.toString();
        String effectiveData = data.substring(0,data.lastIndexOf(";"));
        request.add("data", effectiveData);
        request.add("code", code);

        mRequestQueue.add(1, request, new OnResponseListener<String>() {
            @Override
            public void onStart(int what) {
                loadingDialog.show();
            }

            @Override
            public void onSucceed(int what, Response<String> response) {
                Log.d("AAAAAAAAAAAA",response.get());
                String data = response.get();
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    String code = jsonObject.getString("code");
                    String msg = jsonObject.getString("msg");
                    Toast.makeText(MainActivity.this,msg, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                loadingDialog.dismiss();
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onFinish(int what) {
                loadingDialog.dismiss();
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

package com.zorolee.android.geoclock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;

public class MainActivity extends Activity implements OnGetSuggestionResultListener {
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    private MyLocationListener mMyLocationListener;
    private AutoCompleteTextView mAutoCompleteTextView;
    private Button mSearchButton;
    private static boolean mShowDown = true;
    private SuggestionSearch mSuggestionSearch;
    public ArrayAdapter<String> mSuggestionAdapter;
    private GeoCoder mGeoCoder;
    private OnGetGeoCoderResultListener mGeoCoderResultListener;
    private LatLng mTargetLatLng;
    private MarkerOptions mTargetMarkerOptions;
    private Marker mTargetMarker;

    static int i = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapView.showZoomControls(false);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.zoomTo(19)); //The accuracy is 20 meters,3~21
        mSuggestionSearch = SuggestionSearch.newInstance();
        mLocationClient = new LocationClient(getApplicationContext());
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);
        initLocation();
        mLocationClient.start();
        mSearchButton = (Button)findViewById(R.id.search_button);
        mAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.edit_search);
        mGeoCoder = GeoCoder.newInstance();
        mGeoCoderResultListener = new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult mGeoCodeResult) {

                if (mGeoCodeResult == null || mGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {

                }
                mTargetLatLng =  mGeoCodeResult.getLocation();
                if (mTargetLatLng != null) {
                    BitmapDescriptor mTargetBitMap = BitmapDescriptorFactory.fromResource(R.drawable.m_bitmap);
                    mTargetMarkerOptions = new MarkerOptions().position(mTargetLatLng)
                            .icon(mTargetBitMap)
                            .zIndex(9).draggable(false);
                    mTargetMarker = (Marker) mBaiduMap.addOverlay(mTargetMarkerOptions);
                    Log.d("Main",mTargetLatLng.latitude + "," + mTargetLatLng.longitude);
                } else {
                    Toast.makeText(MainActivity.this,"输入地址有误",Toast.LENGTH_SHORT).show();//TODO 很多情况下，得不出正确的搜索结果
                }
            }
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult mReverseGeoCodeResult) {

            }
        };
        mGeoCoder.setOnGetGeoCodeResultListener(mGeoCoderResultListener);

        initAutoComplete(mAutoCompleteTextView);

        mSuggestionSearch.setOnGetSuggestionResultListener(this);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBaiduMap.clear();//删除上次查询所有的Overlay
                String mSearchWord = mAutoCompleteTextView.getText().toString();
                if (mSearchWord != null) {
                    mGeoCoder.geocode(new GeoCodeOption().city("武汉").address(mSearchWord));
                    MapStatus mMapStatus = new MapStatus.Builder().target(mTargetLatLng).zoom(19).build();
                    MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                    mBaiduMap.setMapStatus(mMapStatusUpdate);



                    Log.d("Main",mSearchWord);

                }//TO BE FIXED, need press SearchButton twice, to update the status.
            }
        });
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker mMarker) {
                Log.d("Main","marker was touched");
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                mBuilder.setMessage("离此地距离约200米处提醒？");
                mBuilder.setTitle("提示");
                mBuilder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO: 17-1-22,设置，如果用户所在地距离目的地200米，则振动提醒。
                        dialog.dismiss();
                    }
                });
                mBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                mBuilder.create().show();
                return false;
                //开发者需要根据marker来判断相应哪个对象的点击事件
            }
        });

    }

    @Override
    public void onGetSuggestionResult(SuggestionResult suggestionResult) {
        if (suggestionResult != null && suggestionResult.getAllSuggestions() != null) {
            mSuggestionAdapter.clear();
            for (SuggestionResult.SuggestionInfo mInfo : suggestionResult.getAllSuggestions()) {
                if (mInfo.key != null) {
                    mSuggestionAdapter.add(mInfo.key);
                }
            }
            mSuggestionAdapter.notifyDataSetChanged();//here is the problem
        }
    }
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=5000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，返回的定位结果是否包含地址信息
        option.setNeedDeviceDirect(true);
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);

    }

    private void initAutoComplete(AutoCompleteTextView auto) {
        mSuggestionAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line);
        auto.setAdapter(mSuggestionAdapter);
        auto.setDropDownHeight(550);
        auto.setThreshold(1);
        auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutoCompleteTextView view = (AutoCompleteTextView) v;
                if (mShowDown == true) {
                    view.showDropDown();
                    mShowDown = false;
                } else {
                    mShowDown = true;
                }
            }
        });
        auto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    return;
                }
                mSuggestionSearch.requestSuggestion(new SuggestionSearchOption().
                        keyword(s.toString()).city("武汉"));
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
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
        mSuggestionSearch.destroy();
        mGeoCoder.destroy();
        mLocationClient.unRegisterLocationListener(mMyLocationListener);
        mLocationClient.stop();
        mMapView.onDestroy();
        super.onDestroy();
//        mLocationClient.removeNotifyEvent(mNotifyListener);
    }



    class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation != null) {
                LatLng mLatLng = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                MyLocationData mMyLocationData = new MyLocationData.Builder().
                        accuracy(bdLocation.getRadius()).
                        latitude(bdLocation.getLatitude()).
                        longitude(bdLocation.getLongitude()).build();
                mBaiduMap.setMyLocationData(mMyLocationData);
//                mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration
//                        (MyLocationConfiguration.LocationMode.FOLLOWING, true, null,0X99CCFF,0X99CCFF));
                if (i == 0) {
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(mLatLng));
                    i++;
                }

            }
        }
    }

    //    private void initAutoComplete(String field,AutoCompleteTextView auto) {
//        SharedPreferences sp = getSharedPreferences("network_url", 0);//SharePreferences, a new class
//        String longhistory = sp.getString(field, "");
//        String[] hisArrays = longhistory.split(",");
//        mSuggestionAdapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_dropdown_item_1line, hisArrays);
//        //只保留最近的20条的记录
//        if (hisArrays.length > 20) {
//            String[] newArrays = new String[20];
//            System.arraycopy(hisArrays, 0, newArrays, 0, 20);
//            mSuggestionAdapter = new ArrayAdapter<>(this,
//                    android.R.layout.simple_dropdown_item_1line, newArrays);
//        }
//        auto.setAdapter(mSuggestionAdapter);
//        auto.setDropDownHeight(550);//look
//        auto.setThreshold(1);
//        auto.setCompletionHint("最近的5条记录");
//        auto.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AutoCompleteTextView view = (AutoCompleteTextView) v;
//                if (mShowDown == true) {
//                    view.showDropDown();
//                    mShowDown = false;
//                } else {
//                    mShowDown = true;
//                }
//            }
//        });
//
//        auto.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (s.length() == 0) {
//                    return;
//                }
//                mSuggestionSearch.requestSuggestion(new SuggestionSearchOption().
//                        keyword(mAutoCompleteTextView.getText().toString()).city("武汉"));
//            }
//        });
//    }
}




//public class MainActivity extends Activity {
//    LocationClient mLocationClient;
//    BDLocationListener myListener = new MyLocationListener();
//    MapView mapView;
//    Vibrator mVibrator01; //should add the relevant uses-permission
//    NotifyListener mNotifyListener = new NotifyListener();
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        SDKInitializer.initialize(getApplicationContext());
//        setContentView(R.layout.activity_main);
//        mapView = (MapView)findViewById(R.id.bmapView);
//        mapView.showZoomControls(false);
//        mapView.showScaleControl(false);
//        BaiduMap mBaiduMap = mapView.getMap();//for controlling something of the map
//        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
//        mLocationClient = new LocationClient(getApplicationContext());
//        mLocationClient.registerLocationListener(myListener);
//        initLocation();
////        mNotifyListener.SetNotifyLocation(30.515701,114.413415,30,"bd09ll");
//        mNotifyListener.SetNotifyLocation(30.515701,100,30,"bd09ll");
//        mLocationClient.registerNotify(mNotifyListener);
//        mLocationClient.start();
//
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mapView.onDestroy();
//        mLocationClient.unRegisterLocationListener(myListener);
//        mLocationClient.removeNotifyEvent(mNotifyListener);
//        mLocationClient.stop();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        mapView.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        mapView.onPause();
//    }
//
//    private void initLocation() {
//        LocationClientOption option = new LocationClientOption();
//        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
//        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
//        int span=1000;
//        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
//        option.setIsNeedAddress(true);//可选，返回的定位结果是否包含地址信息
//        option.setNeedDeviceDirect(true);
//        option.setOpenGps(true);//可选，默认false,设置是否使用gps
//        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
//        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
//        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
//        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
//        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
//        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
//        mLocationClient.setLocOption(option);
//    }
//
//    class NotifyListener extends BDNotifyListener {
//        @Override
//        public void onNotify(BDLocation bdLocation, float distance) {
//            super.onNotify(bdLocation,distance);
//            Toast.makeText(MainActivity.this,"像我这么屌的还有75个",Toast.LENGTH_SHORT).show();
//            mVibrator01 = (Vibrator)getApplication().getSystemService(Service.VIBRATOR_SERVICE);
//            mVibrator01.vibrate(1000);
//
//        }
//    }
//}
//
//class MyLocationListener implements BDLocationListener {
//
//    @Override
//    public void onReceiveLocation(BDLocation location) {
//        //Receive Location
//        StringBuilder sb = new StringBuilder(256);
//        sb.append("time : ");
//        sb.append(location.getTime());
//        sb.append("\nerror code : ");
//        sb.append(location.getLocType());
//        sb.append("\nlatitude : ");
//        sb.append(location.getLatitude());
//        sb.append("\nlontitude : ");
//        sb.append(location.getLongitude());
//        sb.append("\nradius : ");
//        sb.append(location.getRadius());
//        if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
//            sb.append("\nspeed : ");
//            sb.append(location.getSpeed());// 单位：公里每小时
//            sb.append("\nsatellite : ");
//            sb.append(location.getSatelliteNumber());
//            sb.append("\nheight : ");
//            sb.append(location.getAltitude());// 单位：米
//            sb.append("\ndirection : ");
//            sb.append(location.getDirection());// 单位度
//            sb.append("\naddr : ");
//            sb.append(location.getAddrStr());
//            sb.append("\ndescribe : ");
//            sb.append("gps定位成功");
//
//        } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
//            sb.append("\naddr : ");
//            sb.append(location.getAddrStr());
//            //运营商信息
//            sb.append("\noperationers : ");
//            sb.append(location.getOperators());
//            sb.append("\ndescribe : ");
//            sb.append("网络定位成功");
//        } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
//            sb.append("\ndescribe : ");
//            sb.append("离线定位成功，离线定位结果也是有效的");
//        } else if (location.getLocType() == BDLocation.TypeServerError) {
//            sb.append("\ndescribe : ");
//            sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
//        } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
//            sb.append("\ndescribe : ");
//            sb.append("网络不同导致定位失败，请检查网络是否通畅");
//        } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
//            sb.append("\ndescribe : ");
//            sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
//        }
//        sb.append("\nlocation describe : ");
//        sb.append(location.getLocationDescribe());// 位置语义化信息
//        List<Poi> list = location.getPoiList();// POI数据
//        if (list != null) {
//            sb.append("\npoilist size = : ");
//            sb.append(list.size());
//            for (Poi p : list) {
//                sb.append("\npoi= : ");
//                sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
//            }
//        }
//        Log.i("BaiduLocationApiDem", sb.toString());
//    }
//}
//

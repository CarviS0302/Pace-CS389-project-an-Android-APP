package com.example.covid_onetool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.infoactivity.InfoActivity;
import android.libraryactivity.LibraryActivity;
import android.newsroom.NewsroomActivity;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.selftestactivity.SelfTestActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;

public class MainActivity extends AppCompatActivity implements OnMyLocationButtonClickListener,OnMyLocationClickListener,GoogleMap.InfoWindowAdapter, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener,OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback{

    private GoogleMap mMap;
    private List<CovidData> list = new ArrayList<>();
    private boolean mMarkerLoaded = false;
    private Marker clickMarker;
    private GetAddressTask mGetAddTack = null;
    private View mInfoWindowContent = null;
    private TextView tvHeal,tvDied,tvConfirm,tvCurConfirm;
    private EditText etSearch;
    private String baiduMapKey = "B5j2lNjl4TtDwa73vpqeF9aSX3edmam6";
    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean permissionDenied = false;

    //加载菜单栏
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();

        setContentView(R.layout.activity_main);

        etSearch = findViewById(R.id.etLocation);
        findViewById(R.id.searchBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toSearch();
            }
        });

        tvCurConfirm = findViewById(R.id.tvCurConfirm);
        tvConfirm = findViewById(R.id.tvConfirm);
        tvHeal = findViewById(R.id.tvHeal);
        tvDied = findViewById(R.id.tvDied);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    //菜单栏功能：页面跳转
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_Statistics:{
                Intent goStatistics = new Intent(MainActivity.this, MainActivity.class);
                startActivity(goStatistics);
                break;
            }
            case R.id.menu_MyDocument:{
                Intent goMyDocument = new Intent(MainActivity.this, LibraryActivity.class);
                startActivity(goMyDocument);
                break;
            }
            case R.id.menu_SelfTest:{
                Intent goSelfTest = new Intent(MainActivity.this, SelfTestActivity.class);
                startActivity(goSelfTest);
                break;
            }
            case R.id.menu_Newsroom:{
                Intent goNewsroom = new Intent(MainActivity.this, NewsroomActivity.class);
                startActivity(goNewsroom);
                break;
            }
            case R.id.menu_MoreInfo:{
                Intent goMoreInfo = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(goMoreInfo);
                break;
            }

        }
        return true;
    }

    private void initData(){
        //data: https://www.wapi.cn/pneumonia.html
        String appid="12571";
        String secret="4173b99a74d74a4fed17e0216fa88510";
        //data API key above
        String str="appid"+appid+"formatjson"+secret;
        String md5str=md5(str);
        String url="https://oyen.api.storeapi.net/api/94/220?appid="+appid+"&format=json&sign="+md5str;
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Message msg = new Message();
                msg.what = ERROR;
                msg.obj = "网络请求失败";
                mHandler.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String info = response.body().string();//获取服务器返回的json格式数据
                    JSONObject jsonObject = JSON.parseObject(info);//获得一个JsonObject的对象
                    if(jsonObject.getInteger("codeid")==10000){
                        JSONArray dataArr = jsonObject.getJSONArray("retdata");
                        Message msg = new Message();
                        msg.what = SUCCESS;
                        msg.obj=dataArr;
                        mHandler.sendMessage(msg);
                    }
                    else{
                        Message msg = new Message();
                        msg.what = ERROR;
                        msg.obj="get data fail";
                        mHandler.sendMessage(msg);
                    }
                }
            }
        });
    }

    @NonNull
    private static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result.append(temp);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static int SUCCESS=0x123;
    private static int ERROR=0x124;

    //Handler方式实现子线程和主线程间的通信
    Handler mHandler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (SUCCESS == msg.what) {
                JSONArray jsonArray = (JSONArray) msg.obj;
                list.clear();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String xArea = obj.getString("xArea");//地区
                    String confirm = obj.getString("confirm");//确认病例
                    String died=obj.getString("died");
                    String curConfirm = obj.getString("curConfirm");//当前确诊病例
                    String heal = obj.getString("heal");//治愈人数

                    CovidData covidData = new CovidData();
                    covidData.setxArea(xArea);
                    covidData.setCity("");
                    covidData.setConfirm(confirm);
                    covidData.setCurConfirm(curConfirm);
                    covidData.setDied(died);
                    covidData.setHeal(heal);
                    getLocation(xArea,covidData);

                    JSONArray sublist = obj.getJSONArray("subList");
                    //  getLocation(xArea,covidData,sublist);

                    for(int j=0;j<sublist.size();j++){
                        JSONObject subobj = sublist.getJSONObject(j);
                        String city = subobj.getString("city");
                        String sub_confirm = subobj.getString("confirm");
                        String sub_died = subobj.getString("died");
                        String sub_curConfirm=subobj.getString("curConfirm");
                        String sub_heal = subobj.getString("heal");

                        CovidData sub_covidData = new CovidData();
                        sub_covidData.setxArea(xArea);
                        sub_covidData.setCity(city);
                        sub_covidData.setConfirm(sub_confirm);
                        sub_covidData.setCurConfirm(sub_curConfirm);
                        sub_covidData.setDied(sub_died);
                        sub_covidData.setHeal(sub_heal);
                        sub_covidData.setLatitude(0.0);
                        sub_covidData.setLongitude(0.0);
                        list.add(sub_covidData);
                        // getLocation(city,sub_covidData);
                    }
                }
                System.out.println("-----");
            } else if (ERROR == msg.what) {
                String info = (String) msg.obj;
                Toast.makeText(MainActivity.this, info, Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Sets the map type to be "hybrid"
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if(list.size()==0){
            // Add a marker in Sydney and move the camera
            LatLng adelaide = new LatLng(37.34, 126.58);
            // mMap.addMarker(new MarkerOptions().position(adelaide).title("Marker in Adelaide"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(adelaide));
            tvDied.setText("Died : --");
            tvConfirm.setText("Confirm : --");
            tvCurConfirm.setText("Curconfirm : --");
            tvHeal.setText("Heal : --");
        }else{
            CovidData covidData = list.get(0);
            LatLng latLng = new LatLng(covidData.getLatitude(),covidData.getLongitude());
            MarkerOptions mMarkOption = new MarkerOptions();
            mMarkOption.draggable(true);
            mMarkOption.position(latLng);
            if(covidData.getCity().equals("")) {
                mMarkOption.title(covidData.getxArea());
            }else{
                mMarkOption.title(covidData.getCity());
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            tvDied.setText("Died : "+covidData.getDied());
            tvConfirm.setText("Confirm : "+covidData.getConfirm());
            tvCurConfirm.setText("Curconfirm : "+covidData.getCurConfirm());
            tvHeal.setText("Heal : "+covidData.getHeal());
        }
        mMap.setInfoWindowAdapter(this);
        //Register click event listener
        mMap.setOnMarkerClickListener(this);
        //Register drag event listener
        mMap.setOnMarkerDragListener(this);
    }

    //The returned view will be used to construct the display content of the info window and retain the original window background and frame
    @Override
    public View getInfoContents(Marker marker) {
        //Fill the layout of a view
        LayoutInflater mInflater = LayoutInflater.from(this);
        if(mInfoWindowContent == null){
            mInfoWindowContent = mInflater.inflate(R.layout.map_info, null);
        }

        //Settings Icon
        ImageView infoImage = (ImageView)mInfoWindowContent.findViewById(R.id.map_info_image);
        infoImage.setImageResource(R.drawable.map);
        //setting title
        TextView infoTitle = (TextView)mInfoWindowContent.findViewById(R.id.map_info_title);
        infoTitle.setText(marker.getTitle());
        //setting snippet
        TextView infoSnippet = (TextView)mInfoWindowContent.findViewById(R.id.map_info_snippet);
        infoSnippet.setText(marker.getSnippet());
        return mInfoWindowContent;
    }

    //The returned view will be used to construct the window of the entire info window
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    /* OnMarkerDragListener start */
    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        if(marker.isInfoWindowShown())
            marker.hideInfoWindow();
        mMarkerLoaded = false;
    }
    /* OnMarkerDragListener  end */

    /* OnMarkerClickListener start */
    @Override
    public boolean onMarkerClick(Marker marker) {
        clickMarker = marker;
        if(mMarkerLoaded == false)
            getAddressOfMarker();
        return false;
    }
    /* OnMarkerClickListener end */

    private void getAddressOfMarker(){
        if(mGetAddTack != null){
            mGetAddTack.cancel(true);
        }
        mGetAddTack = new GetAddressTask(this);
        mGetAddTack.execute(clickMarker.getPosition());
    }

    private class GetAddressTask extends AsyncTask<LatLng, Void, String[]> {
        Context mContext;

        public GetAddressTask(Context context) {
            super();
            mContext = context;
        }

        @Override
        protected void onPreExecute(){
            clickMarker.setTitle("Marker");
            clickMarker.setSnippet(" ");
            if(clickMarker.isInfoWindowShown())
                clickMarker.showInfoWindow();
        }

        @Override
        protected void onPostExecute(String[] result){
            if(result == null)
                return;
            if(clickMarker != null){
                if((result[1] != null) && (result[0] != null)){
                    clickMarker.setTitle(result[0]);
                    clickMarker.setSnippet(result[1]);
                    if(clickMarker.isInfoWindowShown()) {
                        clickMarker.showInfoWindow();
                    }else{
                        clickMarker.setTitle("Marker");
                        clickMarker.setSnippet("");
                        if(clickMarker.isInfoWindowShown())
                            clickMarker.showInfoWindow();
                    }
                }
                mMarkerLoaded = true;
            }
        }

        @Override
        protected String[] doInBackground(LatLng... params) {
            LatLng latlng = params[0];
            String[] result = new String[2];
            //To send a request using the get method, you need to add the parameter after the URL, and use? Connection, parameters separated by &
            String urlString = "http://maps.google.com/maps/api/geocode/xml?language=zh-CN&sensor=true&latlng=";//31.1601,121.3962";
            //Generate request object
            HttpGet httpGet = new HttpGet(urlString + latlng.latitude + "," + latlng.longitude);
            HttpClient httpClient = new DefaultHttpClient();
            InputStream inputStream = null;
            HttpResponse mHttpResponse = null;
            HttpEntity mHttpEntity = null;
            try{
                mHttpResponse = httpClient.execute(httpGet);
                mHttpEntity = mHttpResponse.getEntity();
                inputStream = mHttpEntity.getContent();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                String startTag = "<formatted_address>";
                String endTag = "</formatted_address>";
                while (null != (line = bufferedReader.readLine())){
                    if(isCancelled())
                        break;
                    line = line.trim();
                    String low = line.toLowerCase(Locale.getDefault());
                    if(low.startsWith(startTag)){
                        int endIndex = low.indexOf(endTag);
                        String addr = line.substring(startTag.length(), endIndex);
                        if((addr != null) && (addr.length() >0)){
                            result[1] = addr;
                            result[0] = "Marker";
                            break;
                        }
                    }
                }
            }
            catch (Exception e){
                System.out.println("Exception in GetAddressTask doInBackground():" + e);
            }
            finally{
                try{
                    if(inputStream != null)
                        inputStream.close();
                }
                catch (IOException e){
                    System.out.println("IOException in GetAddressTask doInBackground():" + e);
                }
            }
            return result;
        }
    }

    /**
     * 根据地名返回一个有经纬度location,如果查询不到经纬度  则默认经纬度是0
     * @param address
     * @return
     */
    //public void getLocation(String address,CovidData covidData,JSONArray sublist) {
    public void getLocation(String address,CovidData covidData) {

        try {
            address = java.net.URLEncoder.encode(address, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String url = String.format("http://api.map.baidu.com/geocoder?address=%s&output=json&key=%s&city=", address, baiduMapKey, "");
        new Thread(new Runnable(){
            @Override
            public void run() {

                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .get()
                            .build();
                    Call call = client.newCall(request);
                    Response response = call.execute();
                    if (response.isSuccessful()) {
                        String info = response.body().string();
                        JSONObject jsonObject = JSON.parseObject(info);

                        String status=jsonObject.getString("status");
                        if(status.equals("OK")){
                            Object object = jsonObject.get("result");
                            if (object instanceof JSONObject) {
                                JSONObject resultObject = jsonObject.getJSONObject("result");
                                JSONObject locationObj = resultObject.getJSONObject("location");
                                covidData.setLongitude(locationObj.getFloatValue("lng"));
                                covidData.setLatitude(locationObj.getFloatValue("lat"));
                                list.add(covidData);
//                                for(int j=0;j<sublist.size();j++){
//                                    JSONObject subobj = sublist.getJSONObject(j);
//                                    String city = subobj.getString("city");
//                                    String sub_confirm = subobj.getString("confirm");
//                                    String sub_died = subobj.getString("died");
//                                    String sub_curConfirm=subobj.getString("curConfirm");
//                                    String sub_heal = subobj.getString("heal");
//
//                                    CovidData sub_covidData = new CovidData();
//                                    sub_covidData.setxArea(covidData.getxArea());
//                                    sub_covidData.setCity(city);
//                                    sub_covidData.setConfirm(sub_confirm);
//                                    sub_covidData.setCurConfirm(sub_curConfirm);
//                                    sub_covidData.setDied(sub_died);
//                                    sub_covidData.setHeal(sub_heal);
//                                    sub_covidData.setLatitude(locationObj.getFloatValue("lng"));
//                                    sub_covidData.setLongitude(locationObj.getFloatValue("lat"));
//                                    list.add(sub_covidData);
//                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void toSearch(){
        String location = etSearch.getText().toString();
        if(location.equals("")){
            Toast.makeText(MainActivity.this,"input country or city",Toast.LENGTH_SHORT).show();
        }else{
            boolean flag=true;
            for(int i=0;i<list.size();i++) {
                CovidData covidData = list.get(i);
                if(covidData.getxArea().contains(location)){
                    LatLng latLng = new LatLng(covidData.getLatitude(),covidData.getLongitude());
                    MarkerOptions mMarkOption = new MarkerOptions();
                    mMarkOption.draggable(true);
                    mMarkOption.position(latLng);
                    mMarkOption.title(covidData.getxArea());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    tvDied.setText("Died : "+covidData.getDied());
                    tvConfirm.setText("Confirm : "+covidData.getConfirm());
                    tvCurConfirm.setText("Curconfirm : "+covidData.getCurConfirm());
                    tvHeal.setText("Heal : "+covidData.getHeal());
                    flag=false;
                    break;
                }
            }
            if(flag){
                Toast.makeText(MainActivity.this,"no find",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

}
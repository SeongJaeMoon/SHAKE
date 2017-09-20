package app.cap.shake;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.net.NetworkInfo;
import android.net.ConnectivityManager;

import com.sackcentury.shinebuttonlib.ShineButton;

public class MainActivity extends AppCompatActivity {
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 777;
    private static final int MY_TTS_CHECK = 0;
    private LocationManager locationManager;
    private ShineButton uni_station, station_uni, dongeoul, seongnam, Incheon, seocho;
    private String choose = "";
    private boolean changed = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (isServiceRunning(FloatingViewService.class)){
            Toast.makeText(getApplicationContext(), getString(R.string.already_start), Toast.LENGTH_SHORT).show();
            finish();
        }
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        station_uni = (ShineButton)findViewById(R.id.station_uni);
        uni_station = (ShineButton)findViewById(R.id.uni_station);
        dongeoul = (ShineButton)findViewById(R.id.uni_dongseoul);
        seongnam = (ShineButton)findViewById(R.id.uni_seongnam);
        Incheon = (ShineButton)findViewById(R.id.uni_incheon);
        seocho = (ShineButton)findViewById(R.id.uni_seocho);

        dongeoul.init(MainActivity.this);
        seongnam.init(MainActivity.this);
        Incheon.init(MainActivity.this);
        seocho.init(MainActivity.this);
        station_uni.init(MainActivity.this);
        uni_station.init(MainActivity.this);

        //View 그리기 권한
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        } else {
            initializeView();
        }
        //위치 권한
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED&&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)!=PackageManager.PERMISSION_GRANTED){
                int PERMISSION_ALL = 1;
                String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CALENDAR};
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            }
        }
        //TTS 엔진 확인
        final Intent checkTTS = new Intent();
        checkTTS.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTS, MY_TTS_CHECK);

        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        //역->학교
        station_uni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uni_station.setBtnColor(Color.GRAY);
                dongeoul.setBtnColor(Color.GRAY);
                seongnam.setBtnColor(Color.GRAY);
                seocho.setBtnColor(Color.GRAY);
                Incheon.setBtnColor(Color.GRAY);
                choose = "station_uni";
                Log.w("setBtn: ", choose);
            }
        });
        //힉교->역
        uni_station.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                station_uni.setBtnColor(Color.GRAY);
                dongeoul.setBtnColor(Color.GRAY);
                seongnam.setBtnColor(Color.GRAY);
                seocho.setBtnColor(Color.GRAY);
                Incheon.setBtnColor(Color.GRAY);
                choose = "uni_station";
                Log.w("setBtn: ", choose);
            }
        });
        //학교->동서울
        dongeoul.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                station_uni.setBtnColor(Color.GRAY);
                uni_station.setBtnColor(Color.GRAY);
                seongnam.setBtnColor(Color.GRAY);
                seocho.setBtnColor(Color.GRAY);
                Incheon.setBtnColor(Color.GRAY);
                choose = "dongseoul";
                Log.w("setBtn: ", choose);
            }
        });
        //학교->성남
        seongnam.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                station_uni.setBtnColor(Color.GRAY);
                uni_station.setBtnColor(Color.GRAY);
                dongeoul.setBtnColor(Color.GRAY);
                seocho.setBtnColor(Color.GRAY);
                Incheon.setBtnColor(Color.GRAY);
                choose = "seongnam";
                Log.w("setBtn: ", choose);
            }
        });
        //학교->서초
        seocho.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                station_uni.setBtnColor(Color.GRAY);
                uni_station.setBtnColor(Color.GRAY);
                dongeoul.setBtnColor(Color.GRAY);
                seongnam.setBtnColor(Color.GRAY);
                Incheon.setBtnColor(Color.GRAY);
                choose = "seocho";
                Log.w("setBtn: ", choose);
            }
        });
        //학교->인천
        Incheon.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                station_uni.setBtnColor(Color.GRAY);
                uni_station.setBtnColor(Color.GRAY);
                dongeoul.setBtnColor(Color.GRAY);
                seocho.setBtnColor(Color.GRAY);
                seongnam.setBtnColor(Color.GRAY);
                choose = "Incheon";
                Log.w("setBtn: ", choose);
            }
        });
    }

    //시작 버튼 클릭
    public void initializeView(){
        findViewById(R.id.notify_me).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    showGpsDisabledDialog();
                }else if (choose==null||choose.length()<0||choose.equals("")){
                    Toast.makeText(getApplicationContext(), getString(R.string.choose_not_set), Toast.LENGTH_SHORT).show();
                }
                else if(!isServiceRunning(FloatingViewService.class)){
                    Intent intent = new Intent(MainActivity.this, FloatingViewService.class);
                    intent.putExtra("type", choose);
                    startService(intent);
                    Toast.makeText(getApplicationContext(), getString(R.string.selected)+" "+choose, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    //서비스 실행 중인지 판단
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //메뉴
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    //메뉴 이벤트
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case R.id.all_view :
                break;
            case R.id.help_view:
                break;
        }
        return true;
    }

    //권한, TTS 체크
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            //다른 앱 위에 그려지기 권한
            if (resultCode == RESULT_OK) {
                initializeView();
            } else{ //권한 실패
                Toast.makeText(getApplicationContext(), getString(R.string.view_per), Toast.LENGTH_SHORT).show();
                finish();
            }
        }else if(requestCode == MY_TTS_CHECK){
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
                Toast.makeText(getApplicationContext(),getString(R.string.tts_setup),Toast.LENGTH_SHORT).show();
            }else{
                Intent installTTS = new Intent();
                installTTS.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTS);
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    //위치 정보 사용 권한
    public void showGpsDisabledDialog(){
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle(getString(R.string.setting));
        alertDialog.setMessage(getString(R.string.location_set));
        alertDialog.setPositiveButton(getString(R.string.setting), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }
}

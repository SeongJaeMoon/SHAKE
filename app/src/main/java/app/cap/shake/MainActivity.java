package app.cap.shake;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.sackcentury.shinebuttonlib.ShineButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 777;
    private static final int MY_TTS_CHECK = 0;
    private static final int MY_ALL_CHECK = 1;
    private static final int MY_CALENDER_CHEK = 3;
    private LocationManager locationManager;
    private ShineButton
            uni_station, station_uni,yeoungduengpo,
            cheolsan, anyang, ilsan,
            incehon_juan, incheon_bupyeong,
            bucheon, suwon, seongnam_moran, dongtan, ansan;
    private String choose = "";
    public static TextToSpeech tts;
    private BackHandler backHandler;
    private static final String uni_station_ = "uni_station"; // 학교->역(셔틀장)
    private static final String station_uni_ = "station_uni"; // 역->학교 (성환역)
    private SimpleDateFormat sdf = new SimpleDateFormat("HHmm", Locale.KOREA);//시간 차를 계산하기 위한 포멧
    private Calendar c1, c2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (isServiceRunning(FloatingViewService.class)){
            Toast.makeText(getApplicationContext(), getString(R.string.already_start), Toast.LENGTH_SHORT).show();
            finish();
        }
        final Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        station_uni = (ShineButton)findViewById(R.id.station_uni);
        uni_station = (ShineButton)findViewById(R.id.uni_station);
        yeoungduengpo = (ShineButton)findViewById(R.id.uni_yeongdeungpo);
        cheolsan = (ShineButton)findViewById(R.id.uni_cheolsan);
        anyang = (ShineButton)findViewById(R.id.uni_anyang);
        ilsan = (ShineButton)findViewById(R.id.uni_ilsan);
        incehon_juan = (ShineButton)findViewById(R.id.uni_incheon_juan);
        incheon_bupyeong = (ShineButton)findViewById(R.id.uni_bupyeong);
        bucheon = (ShineButton)findViewById(R.id.uni_bucheon);
        suwon = (ShineButton)findViewById(R.id.uni_suwon);
        seongnam_moran = (ShineButton)findViewById(R.id.uni_seongnam_moran);
        dongtan = (ShineButton)findViewById(R.id.uni_dongtan);
        ansan = (ShineButton)findViewById(R.id.uni_ansan);

        station_uni.init(MainActivity.this);
        uni_station.init(MainActivity.this);
        yeoungduengpo.init(MainActivity.this);
        cheolsan.init(MainActivity.this);
        anyang.init(MainActivity.this);
        ilsan.init(MainActivity.this);
        incehon_juan.init(MainActivity.this);
        incheon_bupyeong.init(MainActivity.this);
        bucheon.init(MainActivity.this);
        suwon.init(MainActivity.this);
        seongnam_moran.init(MainActivity.this);
        dongtan.init(MainActivity.this);
        ansan.init(MainActivity.this);

        //View 그리기 권한
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        }
        else {
        initializeView();
        }
        //위치 권한&캘랜더 권한
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M&&ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED&&
             ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED&&
             ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)!=PackageManager.PERMISSION_GRANTED){
             String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CALENDAR};
             ActivityCompat.requestPermissions(this, PERMISSIONS, MY_ALL_CHECK);
            }

        //BACK버튼
        backHandler = new BackHandler(this);
        //TTS 엔진 확인
        final Intent checkTTS = new Intent();
        checkTTS.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTS, MY_TTS_CHECK);
        tts = new TextToSpeech(this, this);
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE); //위치 켜져 있는지 확인용
        SharedPreferences mPref = getSharedPreferences("isFirst", MODE_PRIVATE);
        Boolean bfirst = mPref.getBoolean("isFirst", true);
        //도움말
        if (bfirst){
            SharedPreferences.Editor editor = mPref.edit();
            editor.putBoolean("isFirst", false).apply();
            showGuide();
        }

        //역->학교
        station_uni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choose = "station_uni";
                Log.w("setBtn: ", choose);
            }
        });
        //힉교->역
        uni_station.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choose = "uni_station";
                Log.w("setBtn: ", choose);
            }
        });
        /*여기서 부터 모두 하행*/

        //학교->영등포
        yeoungduengpo.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
                    if (checked){
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED){
                            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CALENDAR)) {
                                Toast.makeText(getApplicationContext(), getString(R.string.calender_per), Toast.LENGTH_LONG).show();
                            }else{
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CALENDAR},MY_CALENDER_CHEK);
                            }
                        } else {
                            if (day_ofthe_week().equals("금") || day_ofthe_week().equals("토") || day_ofthe_week().equals("일")) {
                                Toast.makeText(getApplicationContext(), getString(R.string.fry_weekend_over), Toast.LENGTH_SHORT).show();
                                speech(getString(R.string.fry_weekend_over));
                            } else {
                                if ((100 < what_time_isit() && what_time_isit() < 1200) || (1810 < what_time_isit() && what_time_isit() < 2460)) {//오전1시~오전12시 && 오후6시10~오후12시는 계산시간에서 제외
                                    Toast.makeText(getApplicationContext(), getString(R.string.time_over), Toast.LENGTH_SHORT).show();
                                    speech(getString(R.string.time_over));
                                } else {
                                    int result = 1810 - what_time_isit();
                                    c1 = Calendar.getInstance();
                                    c2 = Calendar.getInstance();
                                    try {
                                        c1.setTime(sdf.parse(String.valueOf(what_time_isit())));
                                        c2.setTime(sdf.parse("1810"));
                                        long b = (c2.getTimeInMillis() - c1.getTimeInMillis())/1000;
                                        long a = b/(60*60); //남은시간
                                        long hourToMin = a *60;//분으로 변환
                                        long min = (b/60)-hourToMin;//차를 분으로 변환한 것에 구해진 시간을 분으로 변환한 것을 뺌
                                        if (result > 100) {
                                            Toast.makeText(getApplicationContext(), "셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                            speech("셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.");
                                        } else {
                                            Toast.makeText(getApplicationContext(),  String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                            speech("셔틀 출발까지 " + String.valueOf(min) + "분 남았습니다.");
                                        }
                                    }catch (ParseException e){
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }
                    }
                });
        //학교->광명(철산역)
        cheolsan.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
                if (checked) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED){
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CALENDAR)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.calender_per), Toast.LENGTH_LONG).show();
                        }else{
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CALENDAR},MY_CALENDER_CHEK);
                        }
                    }else {
                        if (day_ofthe_week().equals("금") || day_ofthe_week().equals("토") || day_ofthe_week().equals("일")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.fry_weekend_over), Toast.LENGTH_SHORT).show();
                            speech(getString(R.string.fry_weekend_over));
                        } else {
                            if ((100 < what_time_isit() && what_time_isit() < 1200) || (1700 < what_time_isit() && what_time_isit() < 2460)) {//오전1시~오전12시 && 오후5시~오후12시는 계산시간에서 제외
                                Toast.makeText(getApplicationContext(), getString(R.string.time_over), Toast.LENGTH_SHORT).show();
                                speech(getString(R.string.time_over));
                            } else {
                                int result = 1700 - what_time_isit();
                                c1 = Calendar.getInstance();
                                c2 = Calendar.getInstance();
                                try {
                                    c1.setTime(sdf.parse(String.valueOf(what_time_isit())));
                                    c2.setTime(sdf.parse("1700"));
                                    long b = (c2.getTimeInMillis() - c1.getTimeInMillis())/1000;
                                    long a = b/(60*60); //남은시간
                                    long hourToMin = a *60;//분으로 변환
                                    long min = (b/60)-hourToMin;//차를 분으로 변환한 것에 구해진 시간을 분으로 변환한 것을 뺌
                                    if (result > 100) {
                                        Toast.makeText(getApplicationContext(), "셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                        speech("셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.");
                                    } else {
                                        Toast.makeText(getApplicationContext(),  String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                        speech("셔틀 출발까지 " + String.valueOf(min) + "분 남았습니다.");
                                    }
                                }catch (ParseException e){
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
            }
        });
        //학교->안양
            anyang.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
                if (checked) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED){
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CALENDAR)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.calender_per), Toast.LENGTH_LONG).show();
                        }else{
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CALENDAR},MY_CALENDER_CHEK);
                        }
                    }else {
                        if (day_ofthe_week().equals("금") || day_ofthe_week().equals("토") || day_ofthe_week().equals("일")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.fry_weekend_over), Toast.LENGTH_SHORT).show();
                            speech(getString(R.string.fry_weekend_over));
                        } else {
                            if ((100 < what_time_isit() && what_time_isit() < 1200) || (1700 < what_time_isit() && what_time_isit() < 2460)) {//오전1시~오전12시 && 오후5시~오후12시는 계산시간에서 제외
                                Toast.makeText(getApplicationContext(), getString(R.string.time_over), Toast.LENGTH_SHORT).show();
                                speech(getString(R.string.time_over));
                            } else {
                                int result = 1700 - what_time_isit();
                                c1 = Calendar.getInstance();
                                c2 = Calendar.getInstance();
                                try {
                                    c1.setTime(sdf.parse(String.valueOf(what_time_isit())));
                                    c2.setTime(sdf.parse("1700"));
                                    long b = (c2.getTimeInMillis() - c1.getTimeInMillis())/1000;
                                    long a = b/(60*60); //남은시간
                                    long hourToMin = a *60;//분으로 변환
                                    long min = (b/60)-hourToMin;//차를 분으로 변환한 것에 구해진 시간을 분으로 변환한 것을 뺌
                                    if (result > 100) {
                                        Toast.makeText(getApplicationContext(), "셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                        speech("셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.");
                                    } else {
                                        Toast.makeText(getApplicationContext(),  String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                        speech("셔틀 출발까지 " + String.valueOf(min) + "분 남았습니다.");
                                    }
                                }catch (ParseException e){
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
            }
        });
        //학교->일산
        ilsan.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(View view, boolean checked) {
                if (checked) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED){
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CALENDAR)){
                            Toast.makeText(getApplicationContext(), getString(R.string.calender_per), Toast.LENGTH_LONG).show();
                        }else{
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CALENDAR},MY_CALENDER_CHEK);
                        }
                    }else {
                        if (day_ofthe_week().equals("금") || day_ofthe_week().equals("토") || day_ofthe_week().equals("일")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.fry_weekend_over), Toast.LENGTH_SHORT).show();
                            speech(getString(R.string.fry_weekend_over));
                        } else {
                            if ((100 < what_time_isit() && what_time_isit() < 1200) || (1700 < what_time_isit() && what_time_isit() < 2460)) {//오전1시~오전12시 && 오후5시~오후12시는 계산시간에서 제외
                                Toast.makeText(getApplicationContext(), getString(R.string.time_over), Toast.LENGTH_SHORT).show();
                                speech(getString(R.string.time_over));
                            } else {
                                int result = 1700 - what_time_isit();
                                c1 = Calendar.getInstance();
                                c2 = Calendar.getInstance();
                                try {
                                    c1.setTime(sdf.parse(String.valueOf(what_time_isit())));
                                    c2.setTime(sdf.parse("1700"));
                                    long b = (c2.getTimeInMillis() - c1.getTimeInMillis())/1000;
                                    long a = b/(60*60); //남은시간
                                    long hourToMin = a *60;//분으로 변환
                                    long min = (b/60)-hourToMin;//차를 분으로 변환한 것에 구해진 시간을 분으로 변환한 것을 뺌
                                    if (result > 100) {
                                        Toast.makeText(getApplicationContext(), "셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                        speech("셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.");
                                    } else {
                                        Toast.makeText(getApplicationContext(),  String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                        speech("셔틀 출발까지 " + String.valueOf(min) + "분 남았습니다.");
                                    }
                                }catch (ParseException e){
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
            }
        });
        //학교->인천(주안역)
        incehon_juan.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
                if (checked) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED){
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CALENDAR)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.calender_per), Toast.LENGTH_LONG).show();
                        }else{
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CALENDAR},MY_CALENDER_CHEK);
                        }
                    }else {
                        if (day_ofthe_week().equals("금") || day_ofthe_week().equals("토") || day_ofthe_week().equals("일")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.fry_weekend_over), Toast.LENGTH_SHORT).show();
                            speech(getString(R.string.fry_weekend_over));
                        } else {
                            if ((100 < what_time_isit() && what_time_isit() < 1200) || (1630 < what_time_isit() && what_time_isit() < 2460)) {//오전1시~오전12시 && 오후4시 반~오후12시는 계산시간에서 제외
                                Toast.makeText(getApplicationContext(), getString(R.string.time_over), Toast.LENGTH_SHORT).show();
                                speech(getString(R.string.time_over));
                            } else {
                                int result = 1630 - what_time_isit();
                                c1 = Calendar.getInstance();
                                c2 = Calendar.getInstance();
                                try {
                                    c1.setTime(sdf.parse(String.valueOf(what_time_isit())));
                                    c2.setTime(sdf.parse("1630"));
                                    long b = (c2.getTimeInMillis() - c1.getTimeInMillis())/1000;
                                    long a = b/(60*60); //남은시간
                                    long hourToMin = a *60;//분으로 변환
                                    long min = (b/60)-hourToMin;//차를 분으로 변환한 것에 구해진 시간을 분으로 변환한 것을 뺌
                                    if (result > 100) {
                                        Toast.makeText(getApplicationContext(), "셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                        speech("셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.");
                                    } else {
                                        Toast.makeText(getApplicationContext(),  String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                        speech("셔틀 출발까지 " + String.valueOf(min) + "분 남았습니다.");
                                    }
                                }catch (ParseException e){
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
            }
        });
        //학교->인천(부평역)
        incheon_bupyeong.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(View view, boolean checked) {
            if (checked) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED){
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CALENDAR)) {
                        Toast.makeText(getApplicationContext(), getString(R.string.calender_per), Toast.LENGTH_LONG).show();
                    }else{
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CALENDAR},MY_CALENDER_CHEK);
                    }
                }else {
                    if (day_ofthe_week().equals("토") || day_ofthe_week().equals("일")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.weekend_over), Toast.LENGTH_SHORT).show();
                        speech(getString(R.string.weekend_over));
                    } else {
                        if ((100 < what_time_isit() && what_time_isit() < 1200) || (1630 < what_time_isit() && what_time_isit() < 2460)) {//오전1시~오전12시 && 오후4시 반~오후12시는 계산시간에서 제외
                            Toast.makeText(getApplicationContext(), getString(R.string.time_over), Toast.LENGTH_SHORT).show();
                            speech(getString(R.string.time_over));
                        } else {
                            int result = 1630 - what_time_isit();
                            c1 = Calendar.getInstance();
                            c2 = Calendar.getInstance();
                            try {
                                c1.setTime(sdf.parse(String.valueOf(what_time_isit())));
                                c2.setTime(sdf.parse("1630"));
                                long b = (c2.getTimeInMillis() - c1.getTimeInMillis())/1000;
                                long a = b/(60*60); //남은시간
                                long hourToMin = a *60;//분으로 변환
                                long min = (b/60)-hourToMin;//차를 분으로 변환한 것에 구해진 시간을 분으로 변환한 것을 뺌
                                if (result > 100) {
                                    Toast.makeText(getApplicationContext(), "셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                    speech("셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.");
                                } else {
                                    Toast.makeText(getApplicationContext(),  String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                    speech("셔틀 출발까지 " + String.valueOf(min) + "분 남았습니다.");
                                }
                            }catch (ParseException e){
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        }
    });
    //학교->부천(송내역)
        bucheon.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
                if (checked) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED){
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CALENDAR)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.calender_per), Toast.LENGTH_LONG).show();
                        }else{
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CALENDAR},MY_CALENDER_CHEK);
                        }
                    }else {
                        if (day_ofthe_week().equals("토") || day_ofthe_week().equals("일")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.weekend_over), Toast.LENGTH_SHORT).show();
                            speech(getString(R.string.weekend_over));
                        } else {
                            if ((100 < what_time_isit() && what_time_isit() < 1200) || (1710 < what_time_isit() && what_time_isit() < 2460)) {//오전1시~오전12시 && 오후4시 반, 5시 10분~오후12시는 계산시간에서 제외
                                Toast.makeText(getApplicationContext(), getString(R.string.time_over), Toast.LENGTH_SHORT).show();
                                speech(getString(R.string.time_over));
                            } else {
                                c1 = Calendar.getInstance();
                                c2 = Calendar.getInstance();
                                if (what_time_isit() < 1630) {
                                    int result = 1630 - what_time_isit();
                                    try {
                                        c1.setTime(sdf.parse(String.valueOf(what_time_isit())));
                                        c2.setTime(sdf.parse("1630"));
                                        long b = (c2.getTimeInMillis() - c1.getTimeInMillis())/1000;
                                        long a = b/(60*60); //남은시간
                                        long hourToMin = a *60;//분으로 변환
                                        long min = (b/60)-hourToMin;//차를 분으로 변환한 것에 구해진 시간을 분으로 변환한 것을 뺌
                                        if (result > 100) {
                                            Toast.makeText(getApplicationContext(), "셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                            speech("셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.");
                                        } else {
                                            Toast.makeText(getApplicationContext(),  String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                            speech("셔틀 출발까지 " + String.valueOf(min) + "분 남았습니다.");
                                        }
                                    }catch (ParseException e){
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    try {
                                        int result = 1710 - what_time_isit();
                                        c1.setTime(sdf.parse(String.valueOf(what_time_isit())));
                                        c2.setTime(sdf.parse("1710"));
                                        long b = (c2.getTimeInMillis() - c1.getTimeInMillis())/1000;
                                        long a = b/(60*60); //남은시간
                                        long hourToMin = a *60;//분으로 변환
                                        long min = (b/60)-hourToMin;//차를 분으로 변환한 것에 구해진 시간을 분으로 변환한 것을 뺌
                                        if (result > 100) {
                                            Toast.makeText(getApplicationContext(), "셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                            speech("셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.");
                                        } else {
                                            Toast.makeText(getApplicationContext(),  String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                            speech("셔틀 출발까지 " + String.valueOf(min) + "분 남았습니다.");
                                        }
                                    }catch (ParseException e){
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        //학교->수원
        suwon.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
                if (checked) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED){
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CALENDAR)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.calender_per), Toast.LENGTH_LONG).show();
                        }else{
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CALENDAR},MY_CALENDER_CHEK);
                        }
                    }else {
                        if (day_ofthe_week().equals("금") || day_ofthe_week().equals("토") || day_ofthe_week().equals("일")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.fry_weekend_over), Toast.LENGTH_SHORT).show();
                            speech(getString(R.string.fry_weekend_over));
                        } else {
                            if ((100 < what_time_isit() && what_time_isit() < 1200) || (1700 < what_time_isit() && what_time_isit() < 2460)) {//오전1시~오전12시 && 오후5시~오후12시는 계산시간에서 제외
                                Toast.makeText(getApplicationContext(), getString(R.string.time_over), Toast.LENGTH_SHORT).show();
                                speech(getString(R.string.time_over));
                            } else {
                                c1 = Calendar.getInstance();
                                c2 = Calendar.getInstance();
                                int result = 1700 - what_time_isit();
                                try {
                                    c1.setTime(sdf.parse(String.valueOf(what_time_isit())));
                                    c2.setTime(sdf.parse("1700"));
                                    long b = (c2.getTimeInMillis() - c1.getTimeInMillis())/1000;
                                    long a = b/(60*60); //남은시간
                                    long hourToMin = a *60;//분으로 변환
                                    long min = (b/60)-hourToMin;//차를 분으로 변환한 것에 구해진 시간을 분으로 변환한 것을 뺌
                                    if (result > 100) {
                                        Toast.makeText(getApplicationContext(), "셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                        speech("셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.");
                                    } else {
                                        Toast.makeText(getApplicationContext(),  String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                        speech("셔틀 출발까지 " + String.valueOf(min) + "분 남았습니다.");
                                    }
                                }catch (ParseException e){
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
            }
        });
        //학교->성남모란
        seongnam_moran.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
                if (checked) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED){
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CALENDAR)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.calender_per), Toast.LENGTH_LONG).show();
                        }else{
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CALENDAR},MY_CALENDER_CHEK);
                        }
                    }else {
                        if (day_ofthe_week().equals("토") || day_ofthe_week().equals("일")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.weekend_over), Toast.LENGTH_SHORT).show();
                            speech(getString(R.string.weekend_over));
                        } else {
                            if ((100 < what_time_isit() && what_time_isit() < 1200) || (2010 < what_time_isit() && what_time_isit() < 2460)) {//오전1시~오전12시 && 오후5시 20분, 8시 10분~오후12시는 계산시간에서 제외
                                Toast.makeText(getApplicationContext(), getString(R.string.time_over), Toast.LENGTH_SHORT).show();
                                speech(getString(R.string.time_over));
                            } else {
                                c1 = Calendar.getInstance();
                                c2 = Calendar.getInstance();
                                if (what_time_isit() < 1720) {
                                    int result = 1720 - what_time_isit();
                                    try {
                                        c1.setTime(sdf.parse(String.valueOf(what_time_isit())));
                                        c2.setTime(sdf.parse("1720"));
                                        long b = (c2.getTimeInMillis() - c1.getTimeInMillis())/1000;
                                        long a = b/(60*60); //남은시간
                                        long hourToMin = a *60;//분으로 변환
                                        long min = (b/60)-hourToMin;//차를 분으로 변환한 것에 구해진 시간을 분으로 변환한 것을 뺌
                                        if (result > 100) {
                                            Toast.makeText(getApplicationContext(), "셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                            speech("셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.");
                                        } else {
                                            Toast.makeText(getApplicationContext(),  String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                            speech("셔틀 출발까지 " + String.valueOf(min) + "분 남았습니다.");
                                        }
                                    }catch (ParseException e){
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    try {
                                        int result = 2010 - what_time_isit();
                                        c1.setTime(sdf.parse(String.valueOf(what_time_isit())));
                                        c2.setTime(sdf.parse("2010"));
                                        long b = (c2.getTimeInMillis() - c1.getTimeInMillis())/1000;
                                        long a = b/(60*60); //남은시간
                                        long hourToMin = a *60;//분으로 변환
                                        long min = (b/60)-hourToMin;//차를 분으로 변환한 것에 구해진 시간을 분으로 변환한 것을 뺌
                                        if (result > 100) {
                                            Toast.makeText(getApplicationContext(), "셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                            speech("셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.");
                                        } else {
                                            Toast.makeText(getApplicationContext(),  String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                            speech("셔틀 출발까지 " + String.valueOf(min) + "분 남았습니다.");
                                        }
                                    }catch (ParseException e){
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        //학교->동탄
        dongtan.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
                if (checked) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED){
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CALENDAR)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.calender_per), Toast.LENGTH_LONG).show();
                        }else{
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CALENDAR},MY_CALENDER_CHEK);
                        }
                    }else {
                        if (day_ofthe_week().equals("금") || day_ofthe_week().equals("토") || day_ofthe_week().equals("일")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.fry_weekend_over), Toast.LENGTH_SHORT).show();
                            speech(getString(R.string.fry_weekend_over));
                        } else {
                            if ((100 < what_time_isit() && what_time_isit() < 1200) || (1700 < what_time_isit() && what_time_isit() < 2460)) {//오전1시~오전12시 && 오후5시~오후12시는 계산시간에서 제외
                                Toast.makeText(getApplicationContext(), getString(R.string.time_over), Toast.LENGTH_SHORT).show();
                                speech(getString(R.string.time_over));
                            } else {
                                c1 = Calendar.getInstance();
                                c2 = Calendar.getInstance();
                                int result = 1700 - what_time_isit();
                                try {
                                    c1.setTime(sdf.parse(String.valueOf(what_time_isit())));
                                    c2.setTime(sdf.parse("1700"));
                                    long b = (c2.getTimeInMillis() - c1.getTimeInMillis())/1000;
                                    long a = b/(60*60); //남은시간
                                    long hourToMin = a *60;//분으로 변환
                                    long min = (b/60)-hourToMin;//차를 분으로 변환한 것에 구해진 시간을 분으로 변환한 것을 뺌
                                    if (result > 100) {
                                        Toast.makeText(getApplicationContext(), "셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                        speech("셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.");
                                    } else {
                                        Toast.makeText(getApplicationContext(),  String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                        speech("셔틀 출발까지 " + String.valueOf(min) + "분 남았습니다.");
                                    }
                                }catch (ParseException e){
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
            }
        });
        //학교->안산
        ansan.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
                if (checked) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CALENDAR)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.calender_per), Toast.LENGTH_LONG).show();
                        } else {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CALENDAR}, MY_CALENDER_CHEK);
                        }
                    } else {
                        if (day_ofthe_week().equals("토") || day_ofthe_week().equals("일")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.weekend_over), Toast.LENGTH_SHORT).show();
                            speech(getString(R.string.weekend_over));
                        } else {
                            if ((100 < what_time_isit() && what_time_isit() < 1200) || (1730 < what_time_isit() && what_time_isit() < 2460)) {//오전1시~오전12시 && 오후4시 반~오후12시는 계산시간에서 제외
                                Toast.makeText(getApplicationContext(), getString(R.string.time_over), Toast.LENGTH_SHORT).show();
                                speech(getString(R.string.time_over));
                            } else {
                                c1 = Calendar.getInstance();
                                c2 = Calendar.getInstance();
                                int result = 1730 - what_time_isit();
                                try {
                                    c1.setTime(sdf.parse(String.valueOf(what_time_isit())));
                                    c2.setTime(sdf.parse("1730"));
                                    long b = (c2.getTimeInMillis() - c1.getTimeInMillis())/1000;
                                    long a = b/(60*60); //남은시간
                                    long hourToMin = a *60;//분으로 변환
                                    long min = (b/60)-hourToMin;//차를 분으로 변환한 것에 구해진 시간을 분으로 변환한 것을 뺌
                                    if (result > 100) {
                                        Toast.makeText(getApplicationContext(), "셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                        speech("셔틀 출발까지 " + String.valueOf(a) + "시간 " + String.valueOf(min) + "분 남았습니다.");
                                    } else {
                                        Toast.makeText(getApplicationContext(),  String.valueOf(min) + "분 남았습니다.", Toast.LENGTH_LONG).show();
                                        speech("셔틀 출발까지 " + String.valueOf(min) + "분 남았습니다.");
                                    }
                                }catch (ParseException e){
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
            }
        });
  }

    //시작 버튼 클릭
    public void initializeView(){
        findViewById(R.id.notify_me).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CALENDAR)) {
                        Toast.makeText(getApplicationContext(), getString(R.string.calender_per)+"\n"+getString(R.string.location_per), Toast.LENGTH_LONG).show();
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CALENDAR}, MY_ALL_CHECK);
                    }
                } else {
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        showGpsDisabledDialog();
                    } else if (choose == null || choose.length() < 0 || choose.equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.choose_not_set), Toast.LENGTH_SHORT).show();
                    } else if (!isServiceRunning(FloatingViewService.class)) {
                        Intent intent = new Intent(MainActivity.this, FloatingViewService.class);
                        intent.putExtra("type", choose);
                        startService(intent);
                        switch (choose) {
                            case uni_station_:
                                Toast.makeText(getApplicationContext(), getString(R.string.uni_station), Toast.LENGTH_SHORT).show();
                                break;
                            case station_uni_:
                                Toast.makeText(getApplicationContext(), getString(R.string.station_uni), Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                                break;
                        }
                        finish();
                    }
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
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
        }
        return true;
    }

    //View권한, TTS 체크
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION){
            //다른 앱 위에 그려지기 권한
            if(resultCode == RESULT_OK){
                initializeView();
            }else{
                Toast.makeText(getApplicationContext(), getString(R.string.view_per), Toast.LENGTH_SHORT).show();
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
    //도움말 가기
    public void showGuide(){
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle(getString(R.string.help_view));
        alertDialog.setMessage(getString(R.string.first_time));
        alertDialog.setPositiveButton(getString(R.string.open), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
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

    //speech
    public void speech(String s) {
        tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (tts!=null){
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.KOREA);
            if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA) {
                Toast.makeText(getApplicationContext(), getString(R.string.tts_not_setup), Toast.LENGTH_LONG).show();
            } else if (tts.isLanguageAvailable(Locale.KOREA) == TextToSpeech.LANG_AVAILABLE) {
                tts.setLanguage(Locale.KOREA);
            }
        } else if (status == TextToSpeech.ERROR) {
            Toast.makeText(getApplicationContext(), getString(R.string.tts_not_setup), Toast.LENGTH_LONG).show();
        }
    }

    //요일 구하기
    public String day_ofthe_week() {
        String[] weekDay = {"일", "월", "화", "수", "목", "금", "토"};
        Calendar cal = Calendar.getInstance();
        int num = cal.get(Calendar.DAY_OF_WEEK) - 1;
        String today = null;
        today = weekDay[num];
        return today;
    }

    //현재 시간 시, 분 구하기
    public int what_time_isit() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HHmm", Locale.KOREA);
        String now = null;
        now = sdf.format(date);
        return Integer.parseInt(now);
    }

    @Override
    public void onBackPressed(){
        backHandler.onBackPressed();
    }
    @Override
    protected void onStart() {
        super.onStart();

    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_ALL_CHECK: {
                //요청이 취소되면 결과 배열이 비어있음
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){//권한부여
                 Toast.makeText(getApplicationContext(), getString(R.string.permission_ok),Toast.LENGTH_SHORT).show();
                }
                else{
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CALENDAR}, MY_ALL_CHECK);
                }
                return;
            }
        }
    }
}

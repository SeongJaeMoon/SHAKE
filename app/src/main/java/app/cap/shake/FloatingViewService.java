package app.cap.shake;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import com.github.tbouron.shakedetector.library.ShakeDetector;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FloatingViewService extends Service implements ShakeDetector.OnShakeListener, TextToSpeech.OnInitListener, LocationListener{

    private WindowManager mWindowManager;
    private View mFloatingView;
    LocationManager locationManager;
    public static TextToSpeech tts;
    PendingIntent contentIntent;
    //셔틀장 경위도
    public static final double Lat = 36.910371;
    public static final double Lng = 127.142214;
    //역 경위도
    public static final double lat= 36.916708;
    public static final double lng = 127.126918;

    Location lastlocation = new Location("last");
    double currentLon = 0;
    double currentLat = 0;
    double distance = 0;
    private String set = "";
    private final String Date_pattren = "HH:mm:ss";
    //학교->역
    private static final String start_time_shuttle = "07:45:00";
    //역->학교
    private static final String start_time_station="07:00:00";
    //역->학교 금요일
    private static final String start_time_station_f="08:00:00";
    //역<->학교
    private static final String end_time = "";
    //학교->서초
    private static final String start_seocho="";
    private static final String end_seocho="";
    //학교-평택-동서울
    private static final String start_donseoul="";
    private static final String end_dongseoul="";
    //학교->성남<주말, 공휴일 제외>
    private static final String start_seongnam="";
    private static final String getEnd_seongname="";
    //학교->인천<주말, 공휴일 제외>
    private static final String start_Incheon="";
    private static final String end_Incheon="";


    public FloatingViewService() {

    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId){
        if (intent!=null) {
            String type = intent.getStringExtra("type");
            set = type;
            //uni_station, station_uni, dongseoul, seongnam, Incheon, seocho 중 하나!
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        update_shuttle(false);
        update_station(false);
        //위치 권한 설정
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)&&ActivityCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_DENIED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 5, this);
        }else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)&&locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)&&
                ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_DENIED){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 5, this);
        }
        //View 객체 생성
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);
        //흔들림 감지 객체
        ShakeDetector.create(this, this);
        ShakeDetector.updateConfiguration(10f, 3);
        //tts 객체
        tts = new TextToSpeech(this, this);

        //화면에 그려질 View 생성
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        //View 위치 지정
        params.gravity = Gravity.TOP | Gravity.LEFT; //왼쪽 상단에 View 추가
        params.x = 0;
        params.y = 100;

        //View 화면에 추가
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);

        //close 버튼 객체 생성
        ImageView closeButtonCollapsed = (ImageView) mFloatingView.findViewById(R.id.close_btn);
        closeButtonCollapsed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //close 버튼 클릭시 서비스도 종료
                stopSelf();
                ShakeDetector.destroy();
            }
        });

        //사용자의 View 터치 동작을 인식하여 View 그리기
        mFloatingView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        //포지션 초기화
                        initialX = params.x;
                        initialY = params.y;

                        //터치 위치 위에 그리기
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        //View의 X,Y 계산
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);

                        //X,Y 값 설정
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    //흔들림 감지
    @Override
    public void OnShake(){

        Toast.makeText(getApplicationContext(), day_ofthe_week()+what_time_isit(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location){
        currentLat = location.getLatitude();
        currentLon = location.getLongitude();
         //역->학교
        if (set.equals("station_uni")){
            lastlocation.setLatitude(lat);
            lastlocation.setLongitude(lng);
        }else {//나머지
            lastlocation.setLatitude(Lat);
            lastlocation.setLongitude(Lng);
        }
        distance = lastlocation.distanceTo(location);
        Log.w("Distance:", String.valueOf(distance));
        if (set.equals("station_uni")){
            update_station(true);
        }else{
            update_shuttle(true);
        }
    }
    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager!=null) {
            locationManager.removeUpdates(this);
        }
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        ShakeDetector.destroy();
    }

    //tts 초기화
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS){
            int result = tts.setLanguage(Locale.KOREA);
            if (result == TextToSpeech.LANG_NOT_SUPPORTED||result==TextToSpeech.LANG_MISSING_DATA){
                Toast.makeText(getApplicationContext(), getString(R.string.tts_not_setup),Toast.LENGTH_LONG).show();
            }
            else if(tts.isLanguageAvailable(Locale.KOREA) == TextToSpeech.LANG_AVAILABLE){
                tts.setLanguage(Locale.KOREA);
            }
        }
        else if (status == TextToSpeech.ERROR)
        {
            Toast.makeText(getApplicationContext(), getString(R.string.tts_not_setup), Toast.LENGTH_LONG).show();
        }
    }

    //speech
    public void speech(String s){tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, null);}

    //학교->셔틀
    public void update_shuttle(boolean asData) {
        Notification.Builder builder = new Notification.Builder(getBaseContext())
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.drawable.shakeit3)
                .setContentIntent(contentIntent);
        if (distance>1000f){
            builder.setContentText(String.format(getString(R.string.so_far)));
        }
        else if(asData&&distance<1000f){
            builder.setContentText(String.format(getString(R.string.distance_shuttle), String.valueOf(distance)));
        }else{
            builder.setContentText(String.format(getString(R.string.distance_shuttle), '-'));
        }
        Notification notification = builder.build();
        startForeground(R.string.app_name, notification);
    }
    //역->학교
    public void update_station(boolean asData) {
        Notification.Builder builder = new Notification.Builder(getBaseContext())
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.drawable.shakeit3)
                .setContentIntent(contentIntent);
        if (distance>1000f){
            builder.setContentText(String.format(getString(R.string.so_far)));
        }
        else if (asData){
            builder.setContentText(String.format(getString(R.string.distance_station),String.valueOf(distance)));
        } else{
            builder.setContentText(String.format(getString(R.string.distance_station), '-'));
        }
        Notification notification = builder.build();
        startForeground(R.string.app_name, notification);
    }
    //요일 구하기
    public String day_ofthe_week(){
        String[] weekDay = {"일", "월", "화", "수", "목", "금", "토"};
        Calendar cal = Calendar.getInstance();
        int num = cal.get(Calendar.DAY_OF_WEEK)-1;
        String today= null;
        today = weekDay[num];
        return today;
    }
    //현재 시간 구하기
    public String what_time_isit(){
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss",Locale.KOREA);
        String now = null;
        now = sdf.format(date);
        return now;
    }
    //7시
    //8시
    //9시
    //10시
    //11시
    //12시
    //13시
    //14시
    //15시
    //16시
    //17시
    //18시
    //19시
    //20시
    //21시
    //22시
}


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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FloatingViewService extends Service implements ShakeDetector.OnShakeListener, TextToSpeech.OnInitListener, LocationListener {

    private WindowManager mWindowManager;
    private View mFloatingView;
    LocationManager locationManager;
    public static TextToSpeech tts;
    PendingIntent contentIntent;
    //버튼 종류 변수(상수) -> Main에서 Intent로 넘어오는 값
    private static final String uni_station = "uni_station"; // 학교->역(셔틀장)
    private static final String station_uni = "station_uni"; // 역->학교 (성환역)

    //셔틀장 경위도
    public static final double Lat = 36.910371;
    public static final double Lng = 127.142214;
    //역 경위도
    public static final double lat = 36.916134;
    public static final double lng = 127.127917;
    //거리 변수
    Location lastlocation = new Location("last");
    double currentLon = 0;
    double currentLat = 0;
    double distance = 0;
    private String set = "";
    //학교->역
    private static final int start_time_shuttle = 745;
    private static final int end_time_shuttle = 2215;
    //역->학교
    private static final int start_time_station = 700;
    private static final int end_time_station = 2205;
    //역->학교 금요일
    private static final int start_time_station_f = 800;
    private static final int end_time_f = 2200;
    private static final String isOK = "ok";
    private static final String isNo = "no";
    //학교->역
    private static final int[] min_uni_station =
            new int[]{745, 750, 755, 800, 805, 810, 815, 820, 825, 826, 828, 830, 840, 832, 840, 845, 847, 850,
                    852, 855, 857, 900, 903, 905, 907, 910, 913, 915, 917, 920, 922, 925, 926, 927, 928, 929, 930,
                    932, 935, 940, 945, 950, 1000, 1002, 1005, 1007, 1010, 1012, 1015, 1020, 1025, 1030, 1037, 1045,
                    1050, 1055, 1057, 1100, 1110, 1120, 1130, 1140, 1150, 1205, 1215, 1225, 1235, 1245, 1255, 1305,
                    1315, 1320, 1330, 1340, 1350, 1400, 1410, 1420, 1430, 1435, 1445, 1450, 1455, 1500, 1510, 1520,
                    1530, 1540, 1600, 1605, 1610, 1612, 1615, 1618, 1620, 1622, 1625, 1627, 1630, 1632, 1635, 1637,
                    1640, 1642, 1645, 1647, 1650, 1655, 1700, 1705, 1710, 1712, 1715, 1717, 1720, 1722, 1725, 1727,
                    1730, 1732, 1735, 1740, 1742, 1745, 1750, 1755, 1800, 1805, 1810, 1815, 1820, 1825, 1830, 1835,
                    1840, 1845, 1850, 1855, 1900, 1905, 1910, 1915, 1920, 1925, 1930, 1935, 1940, 1946, 1950, 1955,
                    2005, 2015, 2025, 2035, 2045, 2100, 2125, 2150, 2215};
    //금요일 학교->역
    private static final int[] min_uni_station_f =
            new int[]{745, 750, 755, 800, 805, 810, 815, 820, 830, 840, 845, 850, 855, 900, 903, 905, 907,
                    910, 913, 915, 917, 920, 925, 930, 935, 940, 945, 950, 955, 1000, 1005, 1010, 1015, 1020,
                    1025, 1030, 1040, 1050, 1100, 1110, 1120, 1130, 1140, 1155, 1205, 1215, 1225, 1235, 1245, 1255,
                    1305, 1315, 1325, 1345, 1355, 1405, 1415, 1425, 1435, 1445, 1450, 1455, 1500, 1505, 1510, 1515,
                    1520, 1525, 1530, 1535, 1540, 1545, 1555, 1600, 1605, 1610, 1615, 1620, 1625, 1630, 1635, 1645,
                    1655, 1705, 1710, 1715, 1720, 1725, 1730, 1735, 1740, 1750, 1755, 1800, 1810, 1820, 1830, 1840,
                    1850, 1905, 1915, 1925, 1935, 1945, 2010, 2035, 2100, 2025, 2050, 2215};
    //역->학교
    private static final int[] min_station_uni =
            new int[]{700, 705, 710, 715, 720, 725, 730, 735, 740, 741, 743, 745, 747, 748, 750, 753, 755,
                    800, 802, 805, 807, 810, 812, 815, 818, 820, 822, 825, 828, 830, 832, 835, 840, 841, 842,
                    843, 844, 845, 900, 902, 905, 907, 910, 912, 915, 918, 920, 922, 925, 928, 935, 940, 941,
                    942, 943, 944, 945, 1000, 1005, 1007, 1010, 1012, 1015, 1017, 1020, 1022, 1025, 1027, 1030,
                    1035, 1040, 1045, 1050, 1055, 1100, 1105, 1110, 1115, 1125, 1130, 1135, 1145, 1155, 1205,
                    1220, 1230, 1240, 1250, 1305, 1310, 1320, 1330, 1335, 1345, 1355, 1405, 1415, 1425, 1435,
                    1445, 1450, 1455, 1500, 1505, 1510, 1515, 1525, 1535, 1545, 1555, 1600, 1605, 1615, 1620,
                    1625, 1627, 1630, 1633, 1635, 1637, 1640, 1642, 1645, 1647, 1650, 1655, 1657, 1800, 1702,
                    1705, 1710, 1715, 1720, 1725, 1727, 1730, 1732, 1735, 1737, 1740, 1742, 1745, 1747, 1750,
                    1755, 1757, 1800, 1805, 1810, 1815, 1820, 1825, 1830, 1835, 1840, 1845, 1850, 1855, 1900,
                    1905, 1910, 1915, 1920, 1925, 1930, 1935, 1940, 1945, 1950, 1955, 2000, 2005, 2010, 2020,
                    2030, 2040, 2050, 2100, 2115, 2140, 2205};

    //역->학교 금요일
    private static final int[] min_station_uni_f =
            new int[]{800, 805, 810, 815, 820, 825, 830, 835, 840, 845, 850, 855, 900, 905, 910,
                    915, 920, 925, 930, 935, 940, 945, 950, 955, 1000, 1005, 1010, 1015, 1020, 1025,
                    1030, 1035, 1040, 1045, 1050, 1055, 1105, 1115, 1125, 1135, 1145, 1155, 1205,
                    1220, 1230, 1240, 1250, 1310, 1320, 1330, 1340, 1350, 1400, 1410, 1420, 1430, 1440, 1450,
                    1500, 1505, 1510, 1015, 1520, 1525, 1530, 1535, 1540, 1545, 1550, 1555, 1600, 1610, 1615,
                    1620, 1625, 1630, 1640, 1645, 1650, 1655, 1700, 1710, 1720, 1725, 1730, 1740, 1745, 1750, 1755,
                    1805, 1810, 1815, 1825, 1835, 1845, 1855, 1905, 1920, 1930, 1940, 1950, 2000, 2015, 2040, 2100,
                    2115, 2140, 2200};

    //계산 순서 : 거리?>휴일(토,일)?>버튼종류?>요일?>운행시간?>>남은 시간 최소값(시, 분, 초)?> 남은 시간, 거리 Return.
    public FloatingViewService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        if (intent != null) {
            String type = intent.getStringExtra("type"); //type 받아오기
            set = type;
            Log.w("TYPE:", set);
            //uni_station, station_uni
        }
        return START_STICKY;
    }

    //값 초기화
    @Override
    public void onCreate() {
        super.onCreate();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Log.w("WhatTimeIsIT:", String.valueOf(what_time_isit()));
        update_shuttle(false, set);

        //위치 권한 설정&&위치 업데이트 설정
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && ActivityCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_DENIED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 5, this);
        } else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_DENIED) {
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
        params.gravity = Gravity.TOP | Gravity.START; //왼쪽 상단에 View 추가
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
    public void OnShake() {
        String isChecked = null;
        String isFinished = null;
        //위치 동기화
        if (distance<0||distance==0||!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(getApplicationContext(), getString(R.string.watting_location), Toast.LENGTH_SHORT).show();
        }else {//거리?
        if (distance > 2000f) {
            Toast.makeText(getApplicationContext(), getString(R.string.so_far), Toast.LENGTH_SHORT).show();
        }
        else {
        if (day_ofthe_week().equals("토") || day_ofthe_week().equals("일")) {//주말?
            Toast.makeText(getApplicationContext(), getString(R.string.weekend_over), Toast.LENGTH_SHORT).show();
        } else {
            if (set != null) {//버튼?
                isChecked = time_period(set, day_ofthe_week()); //운행 시간 계산
                if (isChecked == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                } else {
                        if (isChecked.equals(isNo)) { //운행시간?
                            Toast.makeText(getApplicationContext(), getString(R.string.time_over), Toast.LENGTH_SHORT).show();
                            speech(getString(R.string.time_over));
                        } else {
                                isFinished = getMinimum(what_time_isit(), day_ofthe_week(), set);
                                Toast.makeText(getApplicationContext(), isFinished+String.valueOf(distance)+" 미터입니다.", Toast.LENGTH_LONG).show();
                                speech(isFinished+String.valueOf(distance)+"미터입니다.");
                            }
                        }
                    }
                }
            }
        }
}
/*테스트 종료시 주석 다 풀기.*/

    @Override
    public void onLocationChanged(Location location) {
        currentLat = location.getLatitude();
        currentLon = location.getLongitude();
        //역->학교 (성환역)
        if (set.equals(station_uni)) {//역->학교
            lastlocation.setLatitude(lat);
            lastlocation.setLongitude(lng);
        } else {//나머지 (셔틀장)
            lastlocation.setLatitude(Lat);
            lastlocation.setLongitude(Lng);
        }
        distance = lastlocation.distanceTo(location);
        update_shuttle(true, set);


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
        if (locationManager != null) {
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

    //speech
    public void speech(String s) {
        tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    //@param uni_station, station_uni
    //학교->역, 역->학교

    //운행시간 구하기
    public String time_period(String name, String day) {
        String result = null;
        int start, end, start_f, end_f = 0;
        if (name != null && day != null) {
            switch (name) {
                case uni_station://학교->역 운행시간
                    start = start_time_shuttle;//운행 시작시간
                    end = end_time_shuttle;//운행 종료시간
                    if (start < what_time_isit() && what_time_isit() < end) {
                        result = isOK;
                    } else {
                        result = isNo;
                    }
                    break;
                case station_uni://역->학교 운행시간
                    start = start_time_station;// 운행 시작시간 평일
                    end = end_time_station;//운행 종료시간 평일
                    start_f = start_time_station_f;//운행 시작시간 금요일
                    end_f = end_time_f;//운행 종료시간 금요일
                    if (day.equals("금")) {
                        if (start_f < what_time_isit() && what_time_isit() < end_f) {
                            result = isOK;
                        } else {
                            result = isNo;
                        }
                    } else {
                        if (start < what_time_isit() && what_time_isit() < end) {
                            result = isOK;
                        } else {
                            result = isNo;
                        }
                    }
                    break;
            }
            return result;
        } else {
            return null;
        }
    }

    //Foreground Service.
    public void update_shuttle(boolean asData, String type) {
        Notification.Builder builder = new Notification.Builder(getBaseContext())
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(contentIntent);
        if (distance > 2000f) {
            builder.setContentText(String.format(getString(R.string.so_far)));
        } else if (asData && type.equals(station_uni)) {
            builder.setContentText(String.format(getString(R.string.distance_station), String.valueOf(Math.ceil(distance))));
        } else if (asData && !type.equals(station_uni)) {
            builder.setContentText(String.format(getString(R.string.distance_shuttle), String.valueOf(Math.ceil(distance))));
        } else {
            builder.setContentText(getString(R.string.watting_location));
        }
        Notification notification = builder.build();
        startForeground(R.string.app_name, notification);
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

    /*남은 시간 최소 값 리턴. 현재 시간, 요일(학교<->역, 월~목과 금요일 구분하기 위해서)*/
    //학교->역/역->학교
    public String getMinimum(int now, String day, String type) {
        String min_string = null;
        int result = 0;
        int min = Integer.MAX_VALUE;
        if (!day.equals("금") && type.equals(uni_station)) {//학교->역 금요일 아닐 경우.
            for (int i = 0; i < min_uni_station.length; i++) {
                int a = Math.abs(min_uni_station[i] - now);//비교 대상 - 현재 시간
                if (min > a && min_uni_station[i] > now) { // 최대 값 > abs(비교 대상 - 현재 시간) && 비교대상이 현재시간보다 나중 시간
                    min = a; //abs(비교 대상 - 현재시간)>> 최소 값을 구해나가기 위해서 모든 비교 대상의 값을 하나하나 비교
                    result = min_uni_station[i];//결과 = 비교 대상
                }
            }
             if (result>1000){//10:00시 이후
                 min_string = "가장 가까운 셔틀의 출발시간은 "+String.valueOf(result/1000%10)+String.valueOf(result/100%10)+"시 "+String.valueOf(result /10%10)+String.valueOf(result%10)+"분 차이며, 셔틀장까지 거리는 약 ";
            }else{ //10:00시 이전
                min_string = "가장 가까운 셔틀의 출발시간은 "+String.valueOf(result/100%10)+"시 "+String.valueOf(result /10%10)+String.valueOf(result%10)+"분 차이며, 셔틀장까지 거리는 약 ";
            }
        } else if (!day.equals("금") && type.equals(station_uni)) {//역->학교 금요일이 아닐경우
            for (int i = 0; i < min_station_uni.length; i++) {
                int a = Math.abs(min_station_uni[i] - now);
                if (min > a && min_station_uni[i] > now) {
                    min = a;
                    result = min_station_uni[i];
                }
            }
            if (result>1000){//10:00시 이후
                min_string = "가장 가까운 셔틀의 출발시간은 "+String.valueOf(result/1000%10)+String.valueOf(result/100%10)+"시 "+String.valueOf(result /10%10)+String.valueOf(result%10)+"분 차이며, 역 셔틀장까지 거리는 약 ";
            }else{ //10:00시 이전
                min_string = "가장 가까운 셔틀의 출발시간은 "+String.valueOf(result/100%10)+"시 "+String.valueOf(result /10%10)+String.valueOf(result%10)+"분 차이며, 역 셔틀장까지 거리는 약 ";
            }
        } else if (day.equals("금") && type.equals(uni_station)) {//학교->역 금요일
            for (int i = 0; i < min_uni_station_f.length; i++) {
                int a = Math.abs(min_uni_station_f[i] - now);
                if (min > a && min_uni_station_f[i] > now) {
                    min = a;
                    result = min_uni_station_f[i];
                }
            }
            if (result>1000){//10:00시 이후
                min_string = "가장 가까운 셔틀의 출발시간은 "+String.valueOf(result/1000%10)+String.valueOf(result/100%10)+"시 "+String.valueOf(result /10%10)+String.valueOf(result%10)+"분 차이며, 셔틀장까지 거리는 약";
            }else{ //10:00시 이전
                min_string = "가장 가까운 셔틀의 출발시간은 "+String.valueOf(result/100%10)+"시 "+String.valueOf(result /10%10)+String.valueOf(result%10)+"분 차이며, 셔틀장까지 거리는 약";
            }
        } else {//역->학교 금요일
            for (int i = 0; i < min_station_uni_f.length; i++) {
                int a = Math.abs(min_station_uni_f[i] - now);
                if (min > a && min_station_uni_f[i] > now) {
                    min = a;
                    result = min_station_uni_f[i];
                }
            }
            if (result>1000){//10:00시 이후
                min_string = "가장 가까운 셔틀의 출발시간은 "+String.valueOf(result/1000%10)+String.valueOf(result/100%10)+"시 "+String.valueOf(result /10%10)+String.valueOf(result%10)+"분 차이며, 역 셔틀장까지 거리는 약";
            }else{ //10:00시 이전
                min_string = "가장 가까운 셔틀의 출발시간은 "+String.valueOf(result/100%10)+"시 "+String.valueOf(result /10%10)+String.valueOf(result%10)+"분 차이며, 역 셔틀장까지 거리는 약";
            }
        }
        return min_string;
    }
}


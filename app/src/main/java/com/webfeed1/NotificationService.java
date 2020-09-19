
        package com.webfeed1;

        import android.app.Notification;
        import android.app.PendingIntent;
        import android.app.Service;
        import android.content.Intent;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.os.Handler;
        import android.os.IBinder;
        import android.os.Looper;
        import android.util.Log;
        import android.webkit.JavascriptInterface;
        import android.webkit.WebChromeClient;
        import android.webkit.WebView;
        import android.webkit.WebViewClient;

        import androidx.annotation.Nullable;
        import androidx.core.app.NotificationCompat;
        import androidx.core.app.NotificationManagerCompat;

        import java.util.ArrayList;
        import java.util.Iterator;
        import java.util.regex.Matcher;
        import java.util.regex.Pattern;

        import static com.webfeed1.App.CHANNEL_ID;
/*
public class NotificationService extends Service {

    private PowerManager.WakeLock wakeLock;

    public NotificationService() {
        super("NotificationService");
        setIntentRedelivery(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "WebAlert:Wakelock");
        wakeLock.acquire();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Web Alert IntentService")
                .setContentText("Running...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
        startForeground(1, notification);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wakeLock.release();
    }
}
*/


public class NotificationService extends Service {

    public boolean isStopThread() {
        return stopThread;
    }

    public void setStopThread(boolean stopThread) {
        this.stopThread = stopThread;
    }

    private boolean stopThread;
    private ArrayList<RecordItem> recordItemList=new ArrayList<>();
    private DBHelper mDBHelper;
    private WebView currentWebView;
    private String currentUrl;
    //    private int currentKeywordNumber;
    private Handler mHandler;
    private int webNumber, count;
    private RecordItem r;
    private SQLiteDatabase db;
    private keywordCheckThread runnable;
    private Thread thread;
    private int currentKeywordNumber[];
    private ArrayList<String> list, temp_list;
    private ArrayList<String>[] arr_list, arr_temp_list;
    private String[] currentKeyword;
    private Pattern pattern;
    private Matcher matcher;
    private boolean currentIsServiceStart;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("NR", "OnCreate 호출");

        Log.d("태그2", "서비스 호출");

        mDBHelper = new DBHelper(this);

//        currentWebView=new WebView(this);
//        mHandler=new Handler();
//        mHandler=new Handler();
//        runnable = new keywordCheckThread();
//        thread=new Thread(runnable);
    }

    //    서비스가 시작될 때 호출
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
        Log.d("keyword개수2", "OnStartCommand 호출");

        recordItemList.clear();
        currentIsServiceStart=intent.getBooleanExtra("isServiceStart", true);
        Log.d("currentIsStartService", currentIsServiceStart+" in onStartCommand");

//        setStopThread(false);

        list=new ArrayList<>();
        temp_list=new ArrayList<>();
//        arr_list= new ArrayList[100];
//        arr_temp_list=new ArrayList[100];
//        currentKeyword=new String[100];
//        currentKeywordNumber=new int[100];

        Cursor cursor = mDBHelper.loadSQLiteDBKeywordCursor();
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                addItem(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4));
                Log.d("keyword개수2", cursor.getLong(0)+" / "+cursor.getString(1)+" / "+cursor.getString(2)+" / "+cursor.getString(3)+" / "+cursor.getInt(4));
                cursor.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

//        String currentUrl=intent.getStringExtra("URL");

//        thread=new Thread(runnable);
//        thread.setDaemon(true);
        startThread();

        Intent notificationIntent=new Intent(this, MainActivity.class);
        PendingIntent pendingIntent= PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent broadcastIntent = new Intent(this, NotificationReceiver.class);
        broadcastIntent.putExtra("isServiceStart", false);
        PendingIntent actionIntent = PendingIntent.getBroadcast(this,
                0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification=new NotificationCompat.Builder(NotificationService.this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Web Feed 서비스 실행중")
                .setSmallIcon(R.drawable.web_alert_icon_1)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "중지", actionIntent)
                .build();
        startForeground(1, notification);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("currentIsStartService", "onDestroy 호출");
        currentIsServiceStart=false;
        super.onDestroy();
    }



    //    바운드 서비스를 사용하는 데 이용
//    앞뒤로 통신
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void startThread() {
        Log.d("Notification Service", "startThread 호출");
        setStopThread(false);
        runnable = new keywordCheckThread();
        thread=new Thread(runnable);
//        thread.setDaemon(true);
        thread.start();
    }

    public void stopThread(){

        if(thread!=null){
//            thread.interrupt();
            Log.d("keyword개수2", "stopThread 호출");
            setStopThread(true);
            Log.d("stopThread_Tag", "stopThread="+isStopThread());
        }
    }

    class keywordCheckThread implements Runnable{

        @Override
        public void run() {
            Log.d("태그3", "keywordcheckthread 시작");
            Log.d("태그3", "stopThread: "+stopThread);
            while(!stopThread){
                try{
//                    SharedPreferences sharedPreferences = getSharedPreferences("isServiceStart", MODE_PRIVATE);
                    setStopThread(!currentIsServiceStart);
                    Log.d("currentIsStartService", currentIsServiceStart+" in Thread");

                    Log.d("stopThread_Tag", "while 내부 stopThread="+isStopThread());

                    mHandler=new Handler(Looper.getMainLooper());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("keyword개수", "run 호출");

                            Iterator<RecordItem> iterator = recordItemList.iterator();
                            webNumber=0;
                            while (iterator.hasNext()) {
                                webNumber++;
                                Log.d("keyword개수3", "while 내부 호출"+webNumber);
                                r = iterator.next();
                                currentWebView=new WebView(NotificationService.this);
//                                              Log.d("태그3", "RecordItem: " + r.getTitle());
//                                              currentKeywordNumber = r.getKeywordNumber();
//                                currentKeyword[webNumber-1] = r.getKeyword();
                                currentUrl = r.getAddress();
//                                              Log.d("태그3", "loadUrl:" + currentUrl);
                                currentWebView.setWebViewClient(new WebViewClient(){
                                    @Override
                                    public void onPageFinished(WebView view, String url) {
                                        Log.d("keyword개수", "onPageFinished 호출");
                                        super.onPageFinished(view, url);
                                        view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);"); //<html></html> 사이에 있는 모든 html을 넘겨준다.
                                    }
                                });
                                currentWebView.getSettings().setJavaScriptEnabled(true);
                                currentWebView.addJavascriptInterface(new MyJavascriptInterface(r), "Android");
                                currentWebView.setWebChromeClient(new WebChromeClient());
                                currentWebView.loadUrl(currentUrl);
/*
                                currentWebView.setFindListener(new WebView.FindListener() {
                                    @Override
                                    public void onFindResultReceived(int i, int i1, boolean b) {
//                                        Log.d("keyword개수3", r.getTitle()+"의 키워드("+r.getKeyword()+")의 개수: "+i1);
                                        if(i1>0){
                                            //키워드 증가
                                            Log.d("keyword개수3", r.getTitle()+"의 키워드("+r.getKeyword()+")의 개수: "+i1);
                                            if(i1>r.getKeywordNumber()){
//                                                Log.d("keyword개수3", r.getTitle()+"의 키워드("+r.getKeyword()+")의 개수: "+i1);
                                                notifyChange(r.getTitle());
                                                mDBHelper.updatetoChange(r.getId());
                                            }
                                            r.setKeywordNumber(i1);
                                        }
                                    }
                                });*/

                                currentWebView.findAllAsync(r.getKeyword());
//                                try {
//                                    Thread.sleep(5000);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                                Log.d("keyword개수", r.getTitle()+":"+currentKeywordNumber[webNumber-1]);
//                                if(currentKeywordNumber[webNumber-1]>r.getKeywordNumber()){
//                                    //상태변화
//                                    notifyChange(r.getTitle());
//                                    mDBHelper.updatetoChange(r.getId());
//                                }else if(currentKeywordNumber[webNumber-1]==r.getKeywordNumber()){
//                                    for(int i=0;i<list.size();i++){
//                                        if(!arr_list[webNumber-1].get(i).equals(arr_temp_list[webNumber-1].get(i))){
//                                            //상태변화
//                                            notifyChange(r.getTitle());
//                                            mDBHelper.updatetoChange(r.getId());
//                                        }
//                                    }
//                                }
//                                if(arr_list[webNumber-1]!=null){
//                                    arr_list[webNumber-1].clear();
//                                    for(String s:arr_temp_list[webNumber-1]){
//                                        arr_list[webNumber-1].add(s);
//                                    }
//                                }
//                                r.setKeywordNumber(currentKeywordNumber[webNumber-1]);
                            }
                        }
                    });
                    Thread.sleep(60*60*1000);
                }catch(Exception e){
                    e.printStackTrace();
                }
//                thread.setDaemon(true);
            }
        }
    }

    public class MyJavascriptInterface{

        RecordItem r;

        MyJavascriptInterface(RecordItem r){
            this.r=r;
        }

        @JavascriptInterface
        public void getHtml(String html) { //위 자바스크립트가 호출되면 여기로 html이 반환됨
//            Log.d("keyword개수", html);
//            Log.d("html_order", count+"->"+html);
//            SharedPreferences sharedPreferences = getSharedPreferences("isServiceStart", MODE_PRIVATE);
            setStopThread(!currentIsServiceStart);
            Log.d("currentIsStartService", currentIsServiceStart+" in JavaInterface");

            Log.d("stopThread_Tag", "javascriptInterface stopThread="+isStopThread());

            if(!stopThread){
//                Log.d("keyword개수2", "MyJavascriptInterface 호출");
                int cnt=0;
//            String str=html;
//            if(arr_temp_list[count%mDBHelper.getDBCount()]!=null){
//                arr_temp_list[count%mDBHelper.getDBCount()].clear();
//            }
                pattern= Pattern.compile(r.getKeyword());
                matcher=pattern.matcher(html);
                while(matcher.find()){
                    cnt++;
                    int temp=matcher.end();
//                Log.d("keyword2", "keyword 다음 문자 위치: "+temp);
                    if(temp_list!=null)   temp_list.add(html.substring(temp, temp+10));
                }
                Log.d("keyword개수2", r.getTitle()+"의 키워드("+r.getKeyword()+"):"+cnt);

//            키워드 증가
                if(cnt>r.getKeywordNumber()){
                    Log.d("keyword개수", "keyword증가감지1");
                    notifyChange(r.getTitle());
                    mDBHelper.updatetoChange(r.getId());
                }
                else if(cnt==r.getKeywordNumber()&&cnt>0){
                    Log.d("keyword개수", "keyword증가감지2");
                    if(r.getList()!=null){
                        boolean temp_flag=true;
                        for(int i=0;i<temp_list.size();i++){
                            if(!r.getList().get(i).equals(temp_list.get(i))) temp_flag=false;
                        }
//                    키워드 증가
                        if(!temp_flag){
                            notifyChange(r.getTitle());
                            mDBHelper.updatetoChange(r.getId());
                        }
                    }
                }
                r.setKeywordNumber(cnt);
                r.setList(temp_list);
//            Log.d("keyword개수", count+"번째 키워드 개수:"+currentKeywordNumber[count]);
//            if(arr_temp_list[count%mDBHelper.getDBCount()]!=null)   Log.d("keyword2", "list.size="+arr_temp_list[count%mDBHelper.getDBCount()].size());
                count++;
            }
        }
    }

    public void addItem(Long id, String address, String title, String keyword, int isChange) {
        RecordItem item = new RecordItem();
        item.setId(id);
        item.setAddress(address);
        item.setTitle(title);
        item.setKeyword(keyword);
        item.setChangeDetection(isChange);
        item.setKeywordNumber(0);
        recordItemList.add(item);
    }

    //    없앨 수 있는 notification으로 바꾸기
    private void notifyChange(String title) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////            CharSequence name = getString(R.string.channel_name);
//            String description = title+"에서 키워드가 감지되었습니다.";
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "keyword_change", importance);
//            channel.setDescription(description);
//            // Register the channel with the system; you can't change the importance
//            // or other notification behaviors after this
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent2 = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.web_alert_icon_1)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(title+"에서 키워드가 감지되었습니다.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent2)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, builder.build());
    }
}

        package com.webalert;

        import android.app.IntentService;
        import android.app.Notification;
        import android.app.PendingIntent;
        import android.app.Service;
        import android.content.Intent;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.os.Handler;
        import android.os.IBinder;
        import android.os.Looper;
        import android.os.PowerManager;
        import android.util.Log;
        import android.view.View;
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

        import static com.webalert.App.CHANNEL_ID;
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

    private volatile boolean stopThread = false;
    private ArrayList<RecordItem> recordItemList=new ArrayList<>();
    private DBHelper mDBHelper;
    private WebView currentWebView;
    private String currentKeyword, currentUrl;
    //    private int currentKeywordNumber;
    private Handler mHandler;
    private int webNumber, count;
    private RecordItem r;
    private SQLiteDatabase db;
    private keywordCheckThread runnable;
    private Thread thread;
    private static int currentKeywordNumber=0;
    private ArrayList<String> list, temp_list;
    private ArrayList<String>[] arr_list, arr_temp_list;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("NR", "OnCreate 호출");

        Log.d("태그2", "서비스 호출");

        mDBHelper = new DBHelper(this);
        list=new ArrayList<>();
        temp_list=new ArrayList<>();
        arr_list= new ArrayList[mDBHelper.getDBCount()];
        arr_temp_list=new ArrayList[mDBHelper.getDBCount()];
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
        Log.d("NR", "OnStartCommand 호출");

        Cursor cursor = mDBHelper.loadSQLiteDBKeywordCursor();
        try {
            Log.d("태그2", "loadSQLiteDBKeywordCursor 호출");

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                addItem(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4));
                Log.d("태그2", cursor.getLong(0)+", "+cursor.getString(1)+", "+cursor.getString(2)+", "+cursor.getString(3)+", "+cursor.getInt(4));
                cursor.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("태그2", "Exception 호출\n"+e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        String currentUrl=intent.getStringExtra("URL");

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
                .setContentTitle("Web Alert")
                .setContentText("Web Alert 서비스 실행중")
                .setSmallIcon(R.drawable.web_alert_icon_1)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "중지", actionIntent)
                .build();
        startForeground(1, notification);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("NR", "OnDestroy 호출");
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
        stopThread = false;
        runnable = new keywordCheckThread();
        thread=new Thread(runnable);
//        thread.setDaemon(true);
        thread.start();
    }

    public void stopThread(){
        if(thread!=null){
            thread.interrupt();
            stopThread=true;
        }
    }

    class keywordCheckThread implements Runnable{

        @Override
        public void run() {
            Log.d("태그3", "keywordcheckthread 시작");
            Log.d("태그3", "stopThread: "+stopThread);
            while(!Thread.currentThread().interrupted()){
                try{
                    mHandler=new Handler(Looper.getMainLooper());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("keyword개수", "run 호출");
                            Iterator<RecordItem> iterator = recordItemList.iterator();
                            webNumber=0;
                            while (iterator.hasNext()) {
                                webNumber++;
                                Log.d("keyword개수", "while 내부 호출");
                                r = iterator.next();
                                currentWebView=new WebView(NotificationService.this);
//                                              Log.d("태그3", "RecordItem: " + r.getTitle());
//                                              currentKeywordNumber = r.getKeywordNumber();
                                currentKeyword = r.getKeyword();
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
                                currentWebView.addJavascriptInterface(new MyJavascriptInterface(), "Android");
                                currentWebView.setWebChromeClient(new WebChromeClient());
                                currentWebView.loadUrl(currentUrl);
                                Log.d("keyword개수", r.getTitle()+":"+currentKeywordNumber);
                                if(currentKeywordNumber>r.getKeywordNumber()){
                                    //상태변화
                                    notifyChange(r.getTitle());
                                    mDBHelper.updatetoChange(r.getId());
                                }else if(currentKeywordNumber==r.getKeywordNumber()){
                                    for(int i=0;i<list.size();i++){
                                        if(!list.get(i).equals(temp_list.get(i))){
                                            //상태변화
                                            notifyChange(r.getTitle());
                                            mDBHelper.updatetoChange(r.getId());
                                        }
                                    }
                                }
                                list.clear();
                                for(String s:temp_list){
                                    list.add(s);
                                }
                                r.setKeywordNumber(currentKeywordNumber);
//                                              currentWebView.findAllAsync(currentKeyword);
//                                              currentWebView.reload();
//                                              currentWebView.findAll(currentKeyword);
//                                try {
//                                    Thread.sleep(60*1000);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
                                           /*   currentWebView.setFindListener(new WebView.FindListener() {
                                                  @Override
                                                  public void onFindResultReceived(int i, int i1, boolean b) {
//                                        temp=i1;
//                                        currentKeywordNumber=Math.max(currentKeywordNumber, temp);
//                                        Log.d("태그3", currentKeyword+"키워드 개수: "+currentKeywordNumber);
//                                                      if(b) {
//                                                          currentKeywordNumber=i1;
//                                                          Log.d("태그3", "주소"+currentUrl+"\n"+currentKeyword+" 키워드 개수: "+currentKeywordNumber);
                                                      Log.d("태그3", "onFindResultReceived 호출");
                                                      if (i1 != 0) {
                                                          currentKeywordNumber = i1;
                                                          Log.d("태그3", currentKeyword+"의 개수: "+currentKeywordNumber);
                                                      }
                                                      if (b) {
//                                                          if (i1 != 0) {
//                                                              currentKeywordNumber = i1;
//                                                              Log.d("태그3", currentKeyword+"의 개수: "+currentKeywordNumber);
//                                                          }
//                                                          currentKeywordNumber = i1;
//                                                          Log.d("태그3", currentKeyword+"의 개수: "+currentKeywordNumber);

                                                          //키워드 변화 감지한 경우
                                                          if (mDBHelper.getKeywordNumber(r.getId()) != currentKeywordNumber) {
                                                              Log.d("태그3", "if\nmDBHelper.getKeywordNumber(r.getId()):" + mDBHelper.getKeywordNumber(r.getId()) + "\ncurrentKeywordNumber" + currentKeywordNumber);
//                                                              Log.d("태그3", "현재 아이디: " + r.getId());
                                                              Intent notificationIntent = new Intent(NotificationService.this, MainActivity.class);
                                                              PendingIntent pendingIntent = PendingIntent.getActivity(NotificationService.this, 0, notificationIntent, 0);
                                                              notifyChange(r.getTitle(), pendingIntent);
                                                              mDBHelper.updatetoChange(r.getId());
//                                                Intent notificationIntent=new Intent(NotificationService.this, ShowInfoActivity.class);
//                                                notificationIntent.putExtra("totalId", r.getId());
//                                                PendingIntent pendingIntent2= PendingIntent.getActivity(NotificationService.this, 0, notificationIntent, 0);
                                                          } else {
                                                              Log.d("태그3", "키워드 변화 x");
                                                              mDBHelper.updatetoUnchange(r.getId());
                                                          }
                                                          r.setKeywordNumber(currentKeywordNumber);
                                                          mDBHelper.updateKeywordNumber(r.getId(), currentKeywordNumber);
//                                                          Log.d("태그3", "현재 아이디2: " + r.getId());
//                                                          db = mDBHelper.getWritableDatabase();

                                                          Log.d("태그3", r.getTitle() + "의 최종 키워드 개수: " + r.getKeywordNumber());
                                                          currentWebView.reload();
                                                      }
                                                  }
                                              });*/
//                                try {
//                                    Thread.sleep(60*1000);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
                            }
                        }
                    });
                    Thread.sleep(60*60*1000);

//                    if(++i>10) break;
//                    for(RecordItem r:recordItemList){
//                        currentKeywordNumber=r.getKeywordNumber();
//                        Log.d("태그2", i+"번 호출");
//                        currentKeyword=r.getKeyword();
//                        final String currentUrl=r.getAddress();
//                        final int finalI = i;
//                        mHandler.post(new Runnable(){
//                            @Override
//                            public void run() {
//                                Log.d("태그2", "웹뷰 쓰레드 "+ finalI +"번 호출");
//                                currentWebView.loadUrl(currentUrl);
//                                currentWebView.findAllAsync(currentKeyword);
//                                currentWebView.setFindListener(new WebView.FindListener() {
//                                    @Override
//                                    public void onFindResultReceived(int i, int i1, boolean b) {
//                                        Log.d("태그2", "setFindListener 호출\n 키워드 개수: "+i1);
//                                        currentKeywordNumber=Math.max(currentKeywordNumber, i1);
//                                    }
//                                });
//                            }
//                        });
//                        r.setKeywordNumber(currentKeywordNumber);
//                        Log.d("태그2", r.getTitle()+"의 최종 키워드 개수: "+r.getKeywordNumber());
//                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
//                thread.setDaemon(true);
            }
        }
    }

    public class MyJavascriptInterface{
        @JavascriptInterface
        public void getHtml(String html) { //위 자바스크립트가 호출되면 여기로 html이 반환됨
//            Log.d("keyword개수", html);
            Log.d("keyword개수", "MyJavascriptInterface 호출");
            int cnt=0;
//            String str=html;
            arr_temp_list[].clear();
            Pattern pattern= Pattern.compile(currentKeyword);
            Matcher matcher=pattern.matcher(html);
            while(matcher.find()){
                cnt++;
                int temp=matcher.end();
//                Log.d("keyword2", "keyword 다음 문자 위치: "+temp);
                temp_list.add(html.substring(temp, temp+10));
            }
            currentKeywordNumber=cnt;
            Log.d("keyword개수", "키워드 개수:"+currentKeywordNumber);
            Log.d("keyword2", "list.size="+temp_list.size());
//            for(int i=0;i<temp_list.size();i++){
//                Log.d("keyword개수", currentKeyword+"키워드 다음 문자 "+(i+1)+" : "+temp_list.get(i));
//            }

//            int cnt=0;
//            String s[]=html.split(" ");
//            for(int i=0;i<s.length;i++){
//                if(s[i].equals("JAVA")) cnt++;
//            }
//            Toast.makeText(ShowInfoActivity.this, "키워드의 개수는 "+cnt+ " 개입니다.", Toast.LENGTH_SHORT).show();
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
                .setContentTitle("Web Alert")
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
package com.webfeed1;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ShowInfoActivity extends AppCompatActivity {

    private WebView WebView_show_webview;
    private DBHelper mDBHelper;
    private String currentUrl, currentTitle, currentKeyword;
    private long currentId;
    private EditText EditText_save_title, EditText_save_keyword;
    private Button Button_save_title, Button_save_keyword;
    private int currentKeywordNumber=0;
    SQLiteDatabase db;
    private long currentTimeInMillis;
    private int temp;
    private NotificationService notificationService;

//    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_info);

        notificationService=new NotificationService();
        WebView_show_webview=findViewById(R.id.WebView_show_webview);
        mDBHelper = new DBHelper(this);
        db = mDBHelper.getReadableDatabase();

        Intent gintent=getIntent();
        currentId=gintent.getExtras().getLong("totalId", -1);
        Log.d("currentId", String.valueOf(currentId));

        Cursor cursor=mDBHelper.getCursorInfo(db, currentId);
        try{
            currentUrl=cursor.getString(0);
            currentTitle=cursor.getString(1);
            currentKeyword=cursor.getString(2);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if (cursor != null) {
                cursor.close();
            }
        }

        ActionBar ab = getSupportActionBar() ;
        ab.setTitle(currentTitle) ;

        WebView_show_webview.getSettings().setJavaScriptEnabled(true);
        WebView_show_webview.setWebChromeClient(new WebChromeClient());

        WebView_show_webview.loadUrl(currentUrl);

//        Log.d("키워드 찾기", "키워드 개수: "+findKeywordNumber(currentKeyword));
        WebView_show_webview.findAllAsync(currentKeyword);
    }

//    class MyJavaScriptInterface {
//
//        private Context ctx;
//
//        MyJavaScriptInterface(Context ctx) {
//            this.ctx = ctx;
//        }
//
//        public void showHTML(String html) {
//            new AlertDialog.Builder(ctx).setTitle("HTML").setMessage(html)
//                    .setPositiveButton(android.R.string.ok, null).setCancelable(false).create().show();
//        }
//
//    }
//    public class MyJavascriptInterface{
//        @JavascriptInterface
//        public void getHtml(String html) { //위 자바스크립트가 호출되면 여기로 html이 반환됨
//            Log.d("html 불러오기", html);
//            int cnt=0;
//            String str=html;
//            Pattern pattern= Pattern.compile("java");
//            Matcher matcher=pattern.matcher(html);
//            while(matcher.find()){
//                cnt++;
//            }
////            int cnt=0;
////            String s[]=html.split(" ");
////            for(int i=0;i<s.length;i++){
////                if(s[i].equals("JAVA")) cnt++;
////            }
//            Toast.makeText(ShowInfoActivity.this, "키워드의 개수는 "+cnt+ " 개입니다.", Toast.LENGTH_SHORT).show();
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_show_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.action_show_info_modify){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater factory = LayoutInflater.from(ShowInfoActivity.this);
            final View dialog_view = factory.inflate(R.layout.dialog_save, null);

            EditText_save_title=dialog_view.findViewById(R.id.EditText_save_title);
            EditText_save_keyword=dialog_view.findViewById(R.id.EditText_save_keyword);
            Button_save_title=dialog_view.findViewById(R.id.Button_save_title);
            Button_save_keyword=dialog_view.findViewById(R.id.Button_save_keyword);

            EditText_save_title.setText(currentTitle);
            EditText_save_keyword.setText(currentKeyword);

            Button_save_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText_save_keyword.requestFocus();
                }
            });

            Button_save_keyword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            builder.setTitle("수정").setView( dialog_view)
                    .setPositiveButton("저장", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            currentTitle=EditText_save_title.getText().toString();
                            currentKeyword=EditText_save_keyword.getText().toString();
                            if(currentTitle.equals("")) Toast.makeText(ShowInfoActivity.this, "제목이 입력되지 않았습니다.\n저장에 실패하였습니다.", Toast.LENGTH_LONG).show();
                            else if(currentKeyword.equals(""))  Toast.makeText(ShowInfoActivity.this, "키워드가 입력되지 않았습니다.\n저장에 실패하였습니다.", Toast.LENGTH_LONG).show();
                            else {
                                SQLiteDatabase db2=mDBHelper.getWritableDatabase();
                                mDBHelper.modify_keyword_title(db2, currentId, currentTitle, currentKeyword);

                                Intent serviceIntent=new Intent(ShowInfoActivity.this, NotificationService.class);
                                stopService(serviceIntent);
                                startService(serviceIntent);

                                notificationService.stopThread();
                                notificationService.startThread();

                                Intent intent = new Intent(ShowInfoActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return true;
        }
        else if(item.getItemId()==R.id.action_show_info_delete){
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setMessage("삭제하시겠습니까?")
                    .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SQLiteDatabase db2=mDBHelper.getWritableDatabase();
                            mDBHelper.dbDelete(db2, currentId);
                            Toast.makeText(ShowInfoActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ShowInfoActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            AlertDialog alertDialog=builder.create();
            alertDialog.show();
   /*
            //            시작->중지
            if(mDBHelper.getIsStart(currentId)==1){
                Log.d("태그1", "시작->중지");
//                Intent serviceIntent=new Intent(this, NotificationService.class);
//                stopService(serviceIntent);
                mDBHelper.modify_isStart2(currentId, 0);
            }
//            중지->시작
            else if(mDBHelper.getIsStart(currentId)==0){
                Log.d("태그1", "중지->시작");
//                Intent serviceIntent=new Intent(this, NotificationService.class);
//                serviceIntent.putExtra("URL", mDBHelper.getUrl());
//                ContextCompat.startForegroundService(this, serviceIntent);
                mDBHelper.modify_isStart2(currentId, 1);
            }
            Log.d("currentId", String.valueOf(currentId));
//            mDBHelper.modify_isStart2(currentId);
*/
            return true;
        }
        else    return super.onOptionsItemSelected(item) ;
    }

//    public int findKeywordNumber(String currentKeyword){
//        final int[] result = {0};
//        WebView_show_webview.findAllAsync(currentKeyword);
//        WebView_show_webview.setFindListener(new WebView.FindListener() {
//            @Override
//            public void onFindResultReceived(int i, int i1, boolean b) {
//                Log.d("키워드 찾기", "setFindListener 호출"+i);
//                if(b) {
//                    result[0] =i1;
//                    Log.d("키워드 찾기", "키워드 개수: "+currentKeywordNumber);
//                }
//            }
//        });
//        return result[0];
//    }
}






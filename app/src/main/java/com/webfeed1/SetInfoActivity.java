package com.webfeed1;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SetInfoActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private EditText EditText_set_search, EditText_save_title, EditText_save_keyword;
    private WebView WebView_set_webview;
    private ImageButton ImageButton_set_search;
    private Button Button_save_title, Button_save_keyword;
    private DBHelper mDBHelper;
    private String webUrl="";
    private SharedPreferences sharedPreferences;
    private NotificationService notificationService;

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_info);

        notificationService=new NotificationService();
        EditText_set_search=findViewById(R.id.EditText_set_search);
        WebView_set_webview=findViewById(R.id.WebView_set_webview);
        ImageButton_set_search=findViewById(R.id.ImageButton_set_search);

        WebView_set_webview.getSettings().setJavaScriptEnabled(true);
        WebView_set_webview.setWebChromeClient(new WebChromeClient());
        WebView_set_webview.setWebViewClient(new WebViewClientClass());

        mDBHelper=new DBHelper(this);
        db=mDBHelper.getWritableDatabase();

        sharedPreferences = getSharedPreferences("isServiceStart", MODE_PRIVATE);

//        TextView tv = new TextView(getApplicationContext());
//        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
//        tv.setLayoutParams(lp);
//        tv.setText("Welcome!");
//        tv.setTextSize(20);
//        tv.setTextColor(Color.parseColor("#FFFFFF"));
//        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/bccardb");
//        tv.setTypeface(tf);
//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        getSupportActionBar().setCustomView(tv);

        ActionBar ab = getSupportActionBar() ;
        ab.setTitle("웹 페이지 정보 설정") ;
        ab.setDisplayHomeAsUpEnabled(true);
//        ab.setHomeAsUpIndicator(R.drawable.left_arrow_white);

        ImageButton_set_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webUrl=EditText_set_search.getText().toString();
                if(webUrl.startsWith("http")!=true) webUrl="https://"+webUrl;
//                String http=webUrl.substring(0, 7);
//                if(!http.equals("https://")) webUrl="https://"+webUrl;
                Log.d("webUrl", "webUrl="+webUrl);
                WebView_set_webview.loadUrl(webUrl);
            }
        });

        EditText_set_search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i==KeyEvent.KEYCODE_ENTER){
                    String currentUrl=EditText_set_search.getText().toString();
                    if(currentUrl.startsWith("http")!=true) currentUrl="https://"+currentUrl;
                    WebView_set_webview.loadUrl(currentUrl);
                }
                return false;
            }
        });

//        WebView_set_webview.loadUrl("https://www.google.co.kr/");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//뒤로가기 버튼 이벤트
        if ((keyCode == KeyEvent.KEYCODE_BACK) && WebView_set_webview.canGoBack()) {//웹뷰에서 뒤로가기 버튼을 누르면 뒤로가짐
            WebView_set_webview.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class WebViewClientClass extends WebViewClient {//페이지 이동
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("check URL",url);
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            String currentUrl=WebView_set_webview.getUrl();
            EditText_set_search.setText(currentUrl);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_set_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                Log.d("TAG", "홈 버튼 클릭하기");
                SetInfoActivity.super.onBackPressed();
                return true ;
            case R.id.action_set_save:
//                  저장 후 다이얼로그 창 띄우기
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater factory = LayoutInflater.from(SetInfoActivity.this);
                final View dialog_view = factory.inflate(R.layout.dialog_save, null);

                EditText_save_title=dialog_view.findViewById(R.id.EditText_save_title);
                EditText_save_keyword=dialog_view.findViewById(R.id.EditText_save_keyword);
                Button_save_title=dialog_view.findViewById(R.id.Button_save_title);
                Button_save_keyword=dialog_view.findViewById(R.id.Button_save_keyword);

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

                builder.setTitle("저장")
                        .setView(dialog_view)
                        .setPositiveButton("저장", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d("웹 사이트 주소", EditText_set_search.getText().toString());
                                if(EditText_set_search.getText().toString().equals("")) Toast.makeText(SetInfoActivity.this, "웹 사이트 주소가 입력되어있지 않습니다.", Toast.LENGTH_LONG).show();
                                else if(EditText_save_title.getText().toString().equals(""))    Toast.makeText(SetInfoActivity.this, "제목이 입력되어있지 않습니다.", Toast.LENGTH_LONG).show();
                                else if(EditText_save_keyword.getText().toString().equals(""))  Toast.makeText(SetInfoActivity.this, "키워드가 입력되어있지 않습니다.", Toast.LENGTH_LONG).show();
                                else{
                                    try{
                                        mDBHelper.insertData(db, EditText_set_search.getText().toString(), EditText_save_title.getText().toString(), EditText_save_keyword.getText().toString(), 0, 1, 0);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    Toast.makeText(SetInfoActivity.this, "성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show();
                                    if(sharedPreferences.getBoolean("isServiceStart", true)){
                                        Intent serviceIntent=new Intent(SetInfoActivity.this, NotificationService.class);
                                        stopService(serviceIntent);
                                    }
//                                notificationService.stopThread();
                                    Intent serviceIntent=new Intent(SetInfoActivity.this, NotificationService.class);
                                    serviceIntent.putExtra("URL", mDBHelper.getUrl());
                                    ContextCompat.startForegroundService(SetInfoActivity.this, serviceIntent);
                                    Intent intent = new Intent(SetInfoActivity.this, MainActivity.class);
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
            case R.id.action_set_help:
//          도움말 띄우기
                Intent intent=new Intent(this, HelpActivity.class);
                startActivity(intent);
          return true;
            default :
                return super.onOptionsItemSelected(item) ;
        }
    }
}




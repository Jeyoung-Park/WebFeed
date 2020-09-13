package com.webalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<RecordItem> recordItems = new ArrayList<>();
    private ImageButton ImageButton_main_add;
    private DBHelper mDBHelper = new DBHelper(this);
    private SQLiteDatabase db;
    private SharedPreferences sharedPreferences;
    private MenuItem action_main_stop_all;
    private NotificationService notificationService;
    private long backKeyPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.RecyclerView_main);
        ImageButton_main_add=findViewById(R.id.ImageButton_main_add);
        notificationService=new NotificationService();

        recordItems.clear();
        mDBHelper = new DBHelper(this);
        db = mDBHelper.getReadableDatabase();

        sharedPreferences = getSharedPreferences("isServiceStart", MODE_PRIVATE);

        ActionBar ab = getSupportActionBar() ;
        ab.setTitle(getString(R.string.app_name)) ;

        Cursor cursor = mDBHelper.loadSQLiteDBCursor();
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                addItem(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4));
                cursor.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // 리사이클러뷰 사이즈를 고정
        recyclerView.setHasFixedSize(true);

        // linear layout 사용
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        // Adapter와 엽동

        mAdapter = new MyAdapter(recordItems, this);
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                Intent intent=new Intent(MainActivity.this, ShowInfoActivity.class);
                long currentId=recordItems.get(pos).getId();
                if(recordItems.get(pos).getChangeDetection()==1){
                    mDBHelper.updatetoUnchange(currentId);
                    recordItems.get(pos).setChangeDetection(0);
                    mAdapter.notifyDataSetChanged();
                }
                intent.putExtra("totalId", currentId);
                startActivity(intent);
            }
        });

        ImageButton_main_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, SetInfoActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        action_main_stop_all=menu.findItem(R.id.action_main_stop_all);
        if(mDBHelper.getDBCount()>0){
            if(sharedPreferences.getBoolean("isServiceStart", true)){
                action_main_stop_all.setTitle("전체 중지");
            }
            else{
                action_main_stop_all.setTitle("전체 시작");
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_main_delete_all:
//                전체 삭제하는 메서드 작성
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("삭제").setMessage("모든 항목을 삭제하시겠습니까?")
                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(mDBHelper.getDBCount()<=0) Toast.makeText(MainActivity.this, "삭제할 항목이 없습니다.", Toast.LENGTH_SHORT).show();
                                else{
                                    mDBHelper.deleteAll();
                                    mAdapter.notifyDataSetChanged(); // data 변경
                                    SharedPreferences.Editor editor=sharedPreferences.edit();
                                    Intent serviceIntent=new Intent(MainActivity.this, NotificationService.class);
                                    stopService(serviceIntent);
                                    editor.putBoolean("isServiceStart", false);
                                    Intent intent=new Intent(MainActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtra("isServiceStart", false);
                                    startActivity(intent);
                                    overridePendingTransition(0, 0);
                                    notificationService.stopThread();
//                                    Log.d("StopThread", "stopThread:"+notificationService.stopThread);
                                    editor.commit();
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
                return true ;
            case R.id.action_main_stop_all:
                //            시작->중지
                SharedPreferences.Editor editor=sharedPreferences.edit();
                if(mAdapter.getItemCount()>0) {
                    if(sharedPreferences.getBoolean("isServiceStart", true)){
                        Log.d("currentIsStartService", "시작->중지 호출");
                        Intent serviceIntent=new Intent(this, NotificationService.class);
                        serviceIntent.putExtra("isServiceStart", false);
                        stopService(serviceIntent);
                        editor.putBoolean("isServiceStart", false);
                        item.setTitle("전체 시작");
                        notificationService.stopThread();
                    }
//            중지->시작
                    else{
                        Log.d("currentIsStartService", "중지->시작 호출");

                        Intent serviceIntent=new Intent(this, NotificationService.class);
                        serviceIntent.putExtra("URL", mDBHelper.getUrl());
                        serviceIntent.putExtra("isServiceStart", true);
                        ContextCompat.startForegroundService(this, serviceIntent);
                        editor.putBoolean("isServiceStart", true);
                        item.setTitle("전체 중지");
                        notificationService.startThread();
                    }
                    editor.commit();
//            mDBHelper.modify_isStart2(currentId);
                }
                return true;
            default :
                return super.onOptionsItemSelected(item) ;
        }
    }

    public void addItem(Long id, String address, String title, String keyword, int isChange) {
        RecordItem item = new RecordItem();
        item.setId(id);
        item.setAddress(address);
        item.setTitle(title);
        item.setKeyword(keyword);
        item.setChangeDetection(isChange);
        recordItems.add(item);
    }

    @Override
    public void onBackPressed() {
        Toast toast=Toast.makeText(MainActivity.this, "한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
        if(System.currentTimeMillis() > backKeyPressedTime + 2000){
            backKeyPressedTime = System.currentTimeMillis();
            toast.show();
            return;
        }
        if(System.currentTimeMillis() <= backKeyPressedTime + 2000){
            finish();
            toast.cancel();
        }
    }
}
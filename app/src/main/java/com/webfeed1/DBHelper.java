package com.webfeed1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME="WebInfo.db";
    public static final int DATABASE_VERSION=1;
    public static final String TABLE_NAME="Table_WebInfo";
    public static final String ID="id";
    public static final String ADDRESS="address";
    public static final String TITLE="title";
    public static final String KEYWORD="keyword";
    public static final String ISCHANGE="isChange";
    public static final String ISSTART="isStart";
    public static final String KEYWORD_NUMBER="keyword_number";

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public String createQuery="CREATE TABLE IF NOT EXISTS "+TABLE_NAME
            +"("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
            +ADDRESS+ " TEXT NOT NULL, "
            +TITLE+ " TEXT, "
            +KEYWORD+ " TEXT NOT NULL, "
            +ISCHANGE+" INTEGER, "
            +ISSTART+" INTEGER, "
            +KEYWORD_NUMBER+ " INTEGER)";

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("db_tag", createQuery);
        sqLiteDatabase.execSQL(createQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public Cursor loadSQLiteDBCursor(){
        SQLiteDatabase db=this.getReadableDatabase();
        db.beginTransaction();
        String selectQuery="SELECT "+ID+", "+ADDRESS+", "+TITLE+", "+KEYWORD+", "+ISCHANGE+", "+ISSTART+", "+KEYWORD_NUMBER+" FROM "+TABLE_NAME;
        Cursor cursor=null;
        try{
            cursor=db.rawQuery(selectQuery, null);
            db.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            db.endTransaction();
        }
        return cursor;
    }

    public Cursor loadSQLiteDBKeywordCursor(){
        Log.d("태그2", "loadSQLiteDBKeywordCursor 호출2");
        SQLiteDatabase db=this.getReadableDatabase();
        db.beginTransaction();
        String selectQuery="SELECT "+ID+", "+ADDRESS+", "+TITLE+", "+KEYWORD+", "+ISCHANGE+", "+ISSTART+", "+KEYWORD_NUMBER+" FROM "+TABLE_NAME+" WHERE "+ISSTART+"=1";
        Cursor cursor=null;
        try{
            cursor=db.rawQuery(selectQuery, null);
            db.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            db.endTransaction();
        }
        return cursor;
    }


    public void insertData(SQLiteDatabase db, String address, String title, String keyword, int isChange, int isStart, int keyword_number){
        ContentValues contentValues=new ContentValues();
        contentValues.put(ADDRESS, address);
        contentValues.put(TITLE, title);
        contentValues.put(KEYWORD, keyword);
        contentValues.put(ISCHANGE, isChange);
        contentValues.put(ISSTART, isStart);
        contentValues.put(KEYWORD_NUMBER, keyword_number);
        db.insert(TABLE_NAME, null, contentValues);
    }

    public int getDBCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = (int)DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        db.close();
        return count;
    }

    public void deleteAll(){
        SQLiteDatabase db=getWritableDatabase();
        String deleteQuery="DELETE FROM "+TABLE_NAME;
        db.execSQL(deleteQuery);
    }

    public void dbDelete(SQLiteDatabase db, Long dbId){
        db.execSQL("DELETE FROM "+TABLE_NAME+" WHERE "+ID+"="+dbId);
    }

    public Cursor getCursorInfo(SQLiteDatabase db, long id){
        String getUrlQuery="SELECT "+ADDRESS+", "+TITLE+", "+KEYWORD+" FROM "+TABLE_NAME+" WHERE "+ID+"="+id;
        Cursor cursor=null;
        db.beginTransaction();
        try{
            cursor=db.rawQuery(getUrlQuery, null);
            Log.d("getUrlQuery", getUrlQuery);
            db.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
            Log.d("Exception, getUrlQuery", getUrlQuery);
        }finally{
            db.endTransaction();
        }
        cursor.moveToFirst();
        return cursor;
    }

    public void modify_keyword_title(SQLiteDatabase db, long mid, String mtitle, String mkeyword){
        ContentValues contentValues=new ContentValues();
        contentValues.put(TITLE, mtitle);
        contentValues.put(KEYWORD, mkeyword);
        db.update(TABLE_NAME, contentValues, ID+"="+mid, null);
    }
/*
    public void modify_isStart(long mid){
        SQLiteDatabase db=getReadableDatabase();
        SQLiteDatabase db2=getWritableDatabase();
        Cursor cursor=null;
        db.beginTransaction();
        String isStartQuery="SELECT "+ISSTART+" FROM "+TABLE_NAME;
        try{
            cursor=db.rawQuery(isStartQuery, null);
            db.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            db.endTransaction();
        }
        db2.beginTransaction();
        ContentValues contentValues=new ContentValues();
        try{
            if(cursor.getInt(0)==1){
                contentValues.put(ISSTART, 0);
            }
            else if(cursor.getInt(0)==0){
                contentValues.put(ISSTART, 1);
            }
            db2.update(TABLE_NAME, contentValues, ID+"="+mid, null);
            db2.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            db2.endTransaction();
        }
    }*/

    public void modify_isStart2(long mid, int num){
        Log.d("태그1", "modify_isStart2 호출");
        SQLiteDatabase db=getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(ISSTART, num);
        db.update(TABLE_NAME, contentValues, ID+"="+mid, null);
    }

    public int getIsStart(long mid){
        SQLiteDatabase db=getReadableDatabase();
        Cursor cursor=null;
        db.beginTransaction();
        String isStartQuery="SELECT "+ISSTART+" FROM "+TABLE_NAME+" WHERE "+ID+"="+mid;
        try{
            cursor=db.rawQuery(isStartQuery, null);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            db.endTransaction();
        }
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public String getUrl(){
        SQLiteDatabase db=getReadableDatabase();
        Cursor cursor=null;
        db.beginTransaction();
        String addressQuery="SELECT "+ADDRESS+" FROM "+TABLE_NAME;
        try{
            cursor=db.rawQuery(addressQuery, null);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            db.endTransaction();
        }
        cursor.moveToFirst();
        return cursor.getString(0);
    }

    public int getKeywordNumber(long mid){
        SQLiteDatabase db=getReadableDatabase();
        Cursor cursor=null;
        db.beginTransaction();
        String keywordNumQuery="SELECT "+KEYWORD_NUMBER+" FROM "+TABLE_NAME+" WHERE "+ID+"="+mid;
        try{
            cursor=db.rawQuery(keywordNumQuery, null);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            db.endTransaction();
        }
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public void updatetoChange(long mid){
        SQLiteDatabase db=getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(ISCHANGE, 1);
        db.update(TABLE_NAME, contentValues, ID+"="+mid, null);
    }

    public void updatetoUnchange(long mid){
        SQLiteDatabase db=getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(ISCHANGE, 0);
        db.update(TABLE_NAME, contentValues, ID+"="+mid, null);
    }

    public void updateKeywordNumber(long mid, int num){
        Log.d("태그3", "updateKeywordNumber 호출");
        SQLiteDatabase db=getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(KEYWORD_NUMBER, num);
        db.update(TABLE_NAME, contentValues, ID+"="+mid, null);
    }


}

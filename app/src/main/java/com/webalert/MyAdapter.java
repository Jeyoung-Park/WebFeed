package com.webalert;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private ArrayList<RecordItem> rvList;
    private DBHelper mDBHelper;
    private Context mContext;

    private static OnItemClickListener mListener=null;

    public void setOnClickListener(OnItemClickListener listener){
        this.mListener= listener;
    }

    public interface OnItemClickListener{
        void onItemClick(View v, int pos);
    }

    // 뷰홀더(View Holder): 화면에 표시될 아이템뷰를 저장
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView TextView_title, TextView_address, TextView_ChangeDetection, TextView_recyclerview_delete_item;
        public MyViewHolder(View itemView) {
            super(itemView);
            TextView_address=itemView.findViewById(R.id.TextView_rvitem_address);
            TextView_title=itemView.findViewById(R.id.TextView_rvitem_title);
            TextView_ChangeDetection=itemView.findViewById(R.id.TextView_rvitem_ChangeDetection);
            TextView_recyclerview_delete_item=itemView.findViewById(R.id.TextView_recyclerview_delete_item);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position=getAdapterPosition();
                    if(position!=RecyclerView.NO_POSITION){
                        if(mListener!=null){
                            mListener.onItemClick(view, position);
                        }
                    }
                }
            });
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(ArrayList<RecordItem> myDataset, Context context) {
        rvList = myDataset;
        mContext=context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // 5번에서 생성한 아이템 뷰와 연동
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // view에 아이템 값을 할당
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.TextView_title.setText(rvList.get(position).getTitle());
        holder.TextView_address.setText(rvList.get(position).getAddress());
        if(rvList.get(position).getChangeDetection()==0)  holder.TextView_ChangeDetection.setText("변화 없음");
        else if(rvList.get(position).getChangeDetection()==1)  holder.TextView_ChangeDetection.setText("키워드 감지");
        holder.TextView_recyclerview_delete_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup=new PopupMenu(mContext, holder.TextView_recyclerview_delete_item);
                popup.inflate(R.menu.recyclerview_item_menu);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch(item.getItemId()){
                            case R.id.recyclerview_menu_delete:
//                                삭제 버튼을 누르면 item 삭제
                                mDBHelper=new DBHelper(mContext);
                                SQLiteDatabase db=mDBHelper.getWritableDatabase();
//                                Cursor cursor = mDBHelper.LoadSQLiteDBCursor();
                                mDBHelper.dbDelete(db, rvList.get(position).getId());
                                rvList.remove(position);
//                                삭제된 아이템 반영
                                notifyDataSetChanged();
//                                notifyItemChanged(position);
//                                notifyItemRangeChanged(position, rvList.size());
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });
    }

    // 데이터 열(row)의 개수 리턴
    @Override
    public int getItemCount() {
        return rvList.size();
    }
}


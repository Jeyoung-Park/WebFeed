package com.webfeed1;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class HelpActivity extends AppCompatActivity {

    private SliderAdapter sliderAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        viewPager=findViewById(R.id.ViewPager_help);
        sliderAdapter=new SliderAdapter(HelpActivity.this);
        viewPager.setAdapter(sliderAdapter);
    }
}

package com.hogan.eatsharing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Bundle;

import com.blankj.utilcode.util.AdaptScreenUtils;

public class BaseActivity extends AppCompatActivity {

    @Override
    public Resources getResources() {
        return AdaptScreenUtils.adaptWidth(super.getResources(),138);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}

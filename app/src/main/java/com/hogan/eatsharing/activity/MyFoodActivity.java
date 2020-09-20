package com.hogan.eatsharing.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hogan.eatsharing.BaseActivity;
import com.hogan.eatsharing.MyApplication;
import com.hogan.eatsharing.R;
import com.hogan.eatsharing.adapter.MyFoodAdapter;
import com.hogan.eatsharing.config.Food;
import com.hogan.eatsharing.config.FoodList;
import com.hogan.eatsharing.utils.CommonUtils;
import com.hogan.eatsharing.utils.ReqManager;
import com.hogan.eatsharing.utils.ToastUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import java.util.ArrayList;
import java.util.List;

public class MyFoodActivity extends BaseActivity {

    private static final int RESULT = 1;
    private static final int FAIL = 0;
    private static final int DELETE = 2;

    private Context mContext;
    private ReqManager reqManager;

    private List<Food> foods;
    private int choosePtn;

    private SmartRefreshLayout refreshLayout;
    private MyFoodAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_food);

        mContext = getApplicationContext();
        reqManager = ReqManager.getInstance();
        foods = new ArrayList<>();

        TextView title = findViewById(R.id.toolbar_title);
        title.setText("我的动态");
        View mStateBarFixer = findViewById(R.id.status_bar_fix);
        //设置状态栏高度填充
        mStateBarFixer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                CommonUtils.getStatusBarHeight(mContext)));

        refreshLayout = findViewById(R.id.my_food_refresh);
        refreshLayout.autoRefresh();
        RecyclerView recyclerView = findViewById(R.id.my_food_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyFoodAdapter(mContext,foods);
        recyclerView.setAdapter(adapter);

        //下拉刷新监听
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.resetNoMoreData();
                initData(MyApplication.getInstances().getUsername());
            }
        });

        adapter.setOnItemClickListener(position -> {
            Food food = foods.get(position);
            Intent intent = new Intent(this, ShowFoodActivity.class);
            intent.putExtra("food",food);
            startActivity(intent);
        });

        adapter.setOnUserClickListener(position -> {
            choosePtn = position;
            showMineDialog(foods.get(position));
        });
    }

    private Handler mUIHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case RESULT:
                    FoodList foodList =(FoodList) msg.obj;
                    if(foodList!=null){
                        foods.clear();
                        foods.addAll(foodList.body);
                        adapter.notifyDataSetChanged();
                        refreshLayout.finishRefresh(true);
                        refreshLayout.finishLoadMore(true);
                    }else {
                        refreshLayout.finishLoadMoreWithNoMoreData();
                    }
                    break;
                case FAIL:
                    refreshLayout.finishRefresh(false);
                    break;
                case DELETE:
                    String result = (String)msg.obj;
                    if(result.equals("1")){
                        ToastUtil.showShort(mContext,"删除成功");
                        foods.remove(choosePtn);
                        adapter.notifyDataSetChanged();
                    }
                    break;
            }
            return false;
        }
    });

    private void initData(String username) {
        reqManager.getMyFoods(username ,(success, foodList) -> {
            Message msg;
            if(success){
                msg = mUIHandler.obtainMessage(RESULT);
                msg.obj = foodList;
            }else {
                msg = mUIHandler.obtainMessage(FAIL);
            }
            msg.sendToTarget();
        });
    }

    private void deleteFood(String fid){
        reqManager.deleteFood(fid,(success, message) -> {
            Message msg;
            if(success){
                msg = mUIHandler.obtainMessage(DELETE);
                msg.obj = message;
            }else {
                msg = mUIHandler.obtainMessage(FAIL);
            }
            msg.sendToTarget();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLayout.autoRefresh();
    }

    public void showMineDialog(Food food){
        final Dialog dialog = new Dialog(this,R.style.recharge_dialog);
        View view = View.inflate(this,R.layout.dialog_mine,null);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        dialog.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFood(food.fid);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyFoodActivity.this,FoodActivity.class);
                intent.putExtra("type","modify");
                intent.putExtra("food",food);
                startActivity(intent);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
}

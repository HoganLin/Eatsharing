package com.hogan.eatsharing.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.hogan.eatsharing.R;
import com.hogan.eatsharing.adapter.UserAdapter;
import com.hogan.eatsharing.config.User;
import com.hogan.eatsharing.config.UserList;
import com.hogan.eatsharing.utils.CommonUtils;
import com.hogan.eatsharing.utils.ReqManager;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import java.util.ArrayList;
import java.util.List;

public class FocusUserActivity extends AppCompatActivity {

    private static final String mHost = "http://192.168.137.1:8080/EatSharing/";
    private static final int RESULT = 1;    //上传成功
    private static final int FAIL = 0;      //上传失败

    private Context context;
    private ReqManager reqManager;
    private List<User> users;
    private SmartRefreshLayout refreshLayout;
    private UserAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_user);

        context = getApplicationContext();
        reqManager = ReqManager.getInstance();
        users = new ArrayList<>();

        SharedPreferences user = context.getSharedPreferences("user_info",0);
        String uid = user.getString("uid", "");

        View mStateBarFixer = findViewById(R.id.status_bar_fix);
        //设置状态栏高度填充
        mStateBarFixer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                CommonUtils.getStatusBarHeight(context)));
        TextView barTitle = findViewById(R.id.toolbar_title);
        barTitle.setText("我的关注");

        RecyclerView recyclerView = findViewById(R.id.focus_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new UserAdapter(context,users);
        recyclerView.setAdapter(adapter);

        refreshLayout = findViewById(R.id.user_list_refresh);
        refreshLayout.autoRefresh();

        //下拉刷新监听
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.resetNoMoreData();
                initData(uid);
            }
        });
    }

    private Handler mUIHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case RESULT:
                    UserList userList =(UserList) msg.obj;
                    if(userList!=null){
                        users.clear();
                        users.addAll(userList.body);
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
            }
            return false;
        }
    });

    private void initData(String uid) {
        reqManager.getFocusUser(uid,(success, recipeList) -> {
            Message msg;
            if(success){
                msg = mUIHandler.obtainMessage(RESULT);
                msg.obj = recipeList;
            }else {
                msg = mUIHandler.obtainMessage(FAIL);
            }
            msg.sendToTarget();
        });
    }
}

package com.hogan.eatsharing.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.hogan.eatsharing.BaseActivity;
import com.hogan.eatsharing.GlideApp;
import com.hogan.eatsharing.MyApplication;
import com.hogan.eatsharing.R;
import com.hogan.eatsharing.config.Food;
import com.hogan.eatsharing.utils.CommonUtils;
import com.hogan.eatsharing.utils.ReqManager;
import com.hogan.eatsharing.utils.ToastUtil;

public class ShowFoodActivity extends BaseActivity {

    private static final int RESULT = 1;
    private static final int FAIL = 0;
    private static final int FOLLOW_RESULT = 2;
    private static final int COLLECTED = 3;
    private static final int FOLLOWED = 4;

    private Context mContext;
    private ReqManager reqManager;

    private ImageView collectBtn;
    private Button focusBtn;
    private EditText commentEdit;
    private LinearLayout commentLayout;
    private Bitmap iconBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_food);

        mContext = getApplicationContext();
        reqManager = ReqManager.getInstance();

        View mStateBarFixer = findViewById(R.id.status_bar_fix);
        //设置状态栏高度填充
        mStateBarFixer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                CommonUtils.getStatusBarHeight(mContext)));

        Intent intent = getIntent();
        Food food = (Food)intent.getSerializableExtra("food");

        ImageView coverImg = findViewById(R.id.food_image);
        TextView title = findViewById(R.id.food_title);
        TextView content = findViewById(R.id.msg_text);
        ImageView myPhoto = findViewById(R.id.my_hphoto);
        TextView username = findViewById(R.id.user_name);
        ImageView hPhotoView = findViewById(R.id.hphoto);
        collectBtn = findViewById(R.id.collect_btn);
        focusBtn = findViewById(R.id.focus_btn);

        //设置数据
        username.setText(food.username);
        GlideApp.with(mContext)
                .load(reqManager.getHost()+food.hphoto)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(hPhotoView);

        GlideApp.with(mContext)
                .load(reqManager.getHost()+food.fphoto)
                .into(coverImg);
        title.setText(food.title);
        content.setText(food.message);

        iconBitmap = MyApplication.getInstances().readHeadImage();
        GlideApp.with(this)
                .load(iconBitmap)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(myPhoto);

        SharedPreferences user = mContext.getSharedPreferences("user_info",0);
        String myUid = user.getString("uid","");

        collectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCollect(myUid,food.fid);
            }
        });

        checkCollect(myUid,food.fid);
        checkFollow(myUid,food.uid);

        focusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFollow(myUid,food.uid);
            }
        });
    }

    private Handler mUIHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case RESULT:
                    String result = (String)msg.obj;
                    if(result.equals("3")){
                        ToastUtil.showShort(mContext,"取消收藏成功");
                        collectBtn.setImageResource(R.drawable.collect);
                    }else if(result.equals("1")){
                        ToastUtil.showShort(mContext,"收藏成功");
                        collectBtn.setImageResource(R.drawable.collect_click);
                    }
                    break;
                case COLLECTED:
                    String collect = (String)msg.obj;
                    if(collect.equals("0")){
                        collectBtn.setImageResource(R.drawable.collect);
                    }else if(collect.equals("1")){
                        collectBtn.setImageResource(R.drawable.collect_click);
                    }
                    break;
                case FOLLOW_RESULT:
                    String fo_re = (String)msg.obj;
                    if(fo_re.equals("3")){
                        focusBtn.setText("关注");
                        focusBtn.setTextColor(getResources().getColor(R.color.black));
                    }else if(fo_re.equals("1")){
                        focusBtn.setText("已关注");
                        focusBtn.setTextColor(getResources().getColor(R.color.gray));
                    }
                    break;
                case FOLLOWED:
                    String fo = (String)msg.obj;
                    if(fo.equals("0")){
                        focusBtn.setText("关注");
                        focusBtn.setTextColor(getResources().getColor(R.color.black));
                    }else if(fo.equals("1")){
                        focusBtn.setText("已关注");
                        focusBtn.setTextColor(getResources().getColor(R.color.gray));
                    }
                    break;
            }
            return false;
        }
    });

    private void checkCollect(String uid,String cid) {
        reqManager.checkCollect(uid,cid ,(success,message) -> {
            Message msg;
            if(success){
                msg = mUIHandler.obtainMessage(COLLECTED);
                msg.obj = message;
            }else {
                msg = mUIHandler.obtainMessage(FAIL);
            }
            msg.sendToTarget();
        });
    }

    private void setCollect(String uid,String cid){
        reqManager.setCollect(uid,"2",cid ,(success,message) -> {
            Message msg;
            if(success){
                msg = mUIHandler.obtainMessage(RESULT);
                msg.obj = message;
            }else {
                msg = mUIHandler.obtainMessage(FAIL);
            }
            msg.sendToTarget();
        });
    }

    private void checkFollow(String uid,String uid_ed) {
        reqManager.checkFollow(uid,uid_ed ,(success,message) -> {
            Message msg;
            if(success){
                msg = mUIHandler.obtainMessage(FOLLOWED);
                msg.obj = message;
            }else {
                msg = mUIHandler.obtainMessage(FAIL);
            }
            msg.sendToTarget();
        });
    }

    private void setFollow(String uid,String uid_ed){
        reqManager.setFollow(uid,uid_ed,(success,message) -> {
            Message msg;
            if(success){
                msg = mUIHandler.obtainMessage(FOLLOW_RESULT);
                msg.obj = message;
            }else {
                msg = mUIHandler.obtainMessage(FAIL);
            }
            msg.sendToTarget();
        });
    }
}

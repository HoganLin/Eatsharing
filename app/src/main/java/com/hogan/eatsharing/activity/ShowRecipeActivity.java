package com.hogan.eatsharing.activity;

import androidx.annotation.NonNull;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hogan.eatsharing.BaseActivity;
import com.hogan.eatsharing.GlideApp;
import com.hogan.eatsharing.MyApplication;
import com.hogan.eatsharing.R;
import com.hogan.eatsharing.config.Comment;
import com.hogan.eatsharing.config.CommentList;
import com.hogan.eatsharing.config.Recipe;
import com.hogan.eatsharing.utils.CommonUtils;
import com.hogan.eatsharing.utils.ReqManager;
import com.hogan.eatsharing.utils.ToastUtil;
import java.util.List;
import java.util.Map;

public class ShowRecipeActivity extends BaseActivity {

    private static final String mHost = "http://192.168.137.1:8080/EatSharing/";
    private static final int RESULT = 1;
    private static final int FAIL = 0;
    private static final int FOLLOW_RESULT = 2;
    private static final int COLLECTED = 3;
    private static final int FOLLOWED = 4;
    private static final int COMMENT_RESULT = 5;
    private static final int COMMENTS = 6;

    private Context mContext;
    private ReqManager reqManager;
    private Bitmap iconBitmap;
    private String content;

    private ImageView collectBtn;
    private Button focusBtn;
    private EditText commentEdit;
    private LinearLayout commentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_recipe);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        Intent intent = ShowRecipeActivity.this.getIntent();
        Recipe recipe = (Recipe)intent.getSerializableExtra("recipe");

        reqManager = ReqManager.getInstance();
        mContext = getApplicationContext();
        SharedPreferences user = mContext.getSharedPreferences("user_info",0);
        Gson gson = new Gson();

        //view
        View mStateBarFixer = findViewById(R.id.status_bar_fix);
        //设置状态栏高度填充
        mStateBarFixer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                CommonUtils.getStatusBarHeight(mContext)));
        TextView barTitle = findViewById(R.id.toolbar_title);
        barTitle.setText("食谱详情");

        ImageView imageView = findViewById(R.id.cover_image);
        ImageView hPhotoView = findViewById(R.id.hphoto);
        ImageView myPhoto = findViewById(R.id.my_hphoto);
        collectBtn = findViewById(R.id.collect_btn);
        focusBtn = findViewById(R.id.focus_btn);

        TextView titleText = findViewById(R.id.title_text);
        LinearLayout ingreLayout = findViewById(R.id.ingre_layout);
        LinearLayout stepLayout = findViewById(R.id.step_layout);
        TextView tipText = findViewById(R.id.tip_text);
        TextView userName = findViewById(R.id.user_name);
        commentLayout = findViewById(R.id.comment_layout);

        commentEdit = findViewById(R.id.comment_edit);
        ImageButton sendMsg = findViewById(R.id.send_msg);

        //添加数据
        GlideApp.with(mContext)
                .load(mHost+recipe.cover)
                .centerCrop()
                .into(imageView);

        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content = commentEdit.getText().toString();
                if(!content.equals("")){
                    addComment(MyApplication.getInstances().getUsername(),content,recipe.rid);
                }else {
                    ToastUtil.showShort(mContext,"评论不能为空");
                }
            }
        });

        Map<String,String> ingreMap = gson.fromJson(recipe.ingredients,new TypeToken<Map<String,String>>() {}.getType());
        for(String ingre : ingreMap.keySet()){
            View newIngre = LayoutInflater.from(mContext).inflate(R.layout.item_show_ingre,null);
            TextView ingreText = newIngre.findViewById(R.id.ingre_text);
            TextView amountText = newIngre.findViewById(R.id.amount_text);
            ingreText.setText(ingre);
            amountText.setText(ingreMap.get(ingre));
            ingreLayout.addView(newIngre);
        }

        List<String> stepList = gson.fromJson(recipe.step,new TypeToken<List<String>>(){}.getType());
        List<String> imgList = gson.fromJson(recipe.stepImg,new TypeToken<List<String>>(){}.getType());

        int i = 0;
        for(String img : imgList){
            View newStep = LayoutInflater.from(mContext).inflate(R.layout.item_show_step,null);
            TextView stepNum = newStep.findViewById(R.id.step_num);
            TextView stepText = newStep.findViewById(R.id.step_text);
            ImageView stepImg = newStep.findViewById(R.id.step_img);
            String step = stepList.get(i);
            stepNum.setText(String.valueOf(i+1));
            i++;
            stepText.setText(step);
            GlideApp.with(mContext).load(mHost+img)
                    .centerCrop()
                    .into(stepImg);
            stepLayout.addView(newStep);

        }

        tipText.setText(recipe.tips);
        userName.setText(recipe.username);

        GlideApp.with(mContext).load(mHost+recipe.hphoto)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(hPhotoView);

        titleText.setText(recipe.title);
        iconBitmap = MyApplication.getInstances().readHeadImage();
        GlideApp.with(this)
                .load(iconBitmap)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(myPhoto);

        String myUid = user.getString("uid","");

        myPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCollect(myUid,recipe.rid);
            }
        });

        collectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCollect(myUid,recipe.rid);
            }
        });

        checkCollect(myUid,recipe.rid);
        checkFollow(myUid,recipe.uid);
        getComments(recipe.rid);

        focusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFollow(myUid,recipe.uid);
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
                case COMMENT_RESULT:
                    String co = (String)msg.obj;
                    if(co.equals("1")){
                        ToastUtil.showShort(mContext,"评论成功");
                        closeInputMethod();
                        commentEdit.setText("");
                        commentEdit.clearFocus();
                        View view = LayoutInflater.from(mContext).inflate(R.layout.item_comment,null);
                        ImageView icon = view.findViewById(R.id.comment_icon);
                        TextView username = view.findViewById(R.id.comment_user);
                        TextView contentText = view.findViewById(R.id.comment_content);
                        GlideApp.with(mContext)
                                .load(iconBitmap)
                                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                .into(icon);
                        username.setText(MyApplication.getInstances().getUsername());
                        contentText.setText(content);
                        commentLayout.addView(view,0);
                    }
                    break;
                case COMMENTS:
                    CommentList commentList = (CommentList) msg.obj;
                    if(commentList!=null){
                        for (Comment comment: commentList.body){
                            View view = LayoutInflater.from(mContext).inflate(R.layout.item_comment,null);
                            ImageView icon = view.findViewById(R.id.comment_icon);
                            TextView username = view.findViewById(R.id.comment_user);
                            TextView contentText = view.findViewById(R.id.comment_content);
                            GlideApp.with(mContext)
                                    .load(mHost+comment.hphoto)
                                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                    .into(icon);
                            username.setText(comment.username);
                            contentText.setText(comment.content);
                            commentLayout.addView(view,0);
                        }
                    }
            }
            return false;
        }
    });

    //直接关闭键盘输入法
    private void closeInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();
        if (isOpen) {
            // imm.toggleSoftInput(0,
            // InputMethodManager.HIDE_NOT_ALWAYS);//没有显示则显示
            imm.hideSoftInputFromWindow(commentEdit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

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

    private void setCollect(String uid,String cid){
        reqManager.setCollect(uid,"1",cid ,(success,message) -> {
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

    private void addComment(String username,String content,String rid){
        reqManager.addComment(username,content,"1",rid,(success, message) -> {
            Message msg;
            if(success){
                msg = mUIHandler.obtainMessage(COMMENT_RESULT);
                msg.obj = message;
            }else {
                msg = mUIHandler.obtainMessage(FAIL);
            }
            msg.sendToTarget();
        });
    }

    private void getComments(String main_id){
        reqManager.getComments(main_id,(success, commentList) -> {
            Message msg;
            if(success){
                msg = mUIHandler.obtainMessage(COMMENTS);
                msg.obj = commentList;
            }else {
                msg = mUIHandler.obtainMessage(FAIL);
            }
            msg.sendToTarget();
        });
    }
}

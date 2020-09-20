package com.hogan.eatsharing.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.hogan.eatsharing.GlideApp;
import com.hogan.eatsharing.MyApplication;
import com.hogan.eatsharing.R;
import com.hogan.eatsharing.activity.EditMsgActivity;
import com.hogan.eatsharing.activity.FocusUserActivity;
import com.hogan.eatsharing.activity.MyFoodActivity;
import com.hogan.eatsharing.activity.MyRecipeActivity;
import com.hogan.eatsharing.config.LoginClass;
import com.hogan.eatsharing.utils.CommonUtils;
import com.hogan.eatsharing.utils.ReqManager;
import com.hogan.eatsharing.utils.ToastUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.wildma.pictureselector.PictureBean;
import com.wildma.pictureselector.PictureSelector;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MineFragment extends Fragment {

    private static final int UPLOAD_RESULT = 1;    //上传成功
    private static final int UPLOAD_FAIL = 0;      //上传失败
    private static final int LOGIN_RESULT =3;
    private static final int FOLLOW_COUNT = 2;
    private static final int SIGN_RESULT =5;
    private static final int RELOAD_USER = 6;

    private Toolbar toolbar;
    private View layoutView;
    private ImageView headView;
    private TextView nameText;
    private TextView signText;
    private LinearLayout loginLayout;
    private LinearLayout userLayout;
    private Button loginButton;
    private Button signButton;
    private EditText nameEdit;
    private EditText psdEdit;
    private TextView introText;
    private TextView focusNum;

    private Context context;
    private ReqManager reqManager;
    private PictureBean pictureBean;

    private Boolean loginNow;

    public MineFragment() {
    }


    public static MineFragment newInstance() {
        MineFragment fragment = new MineFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @SuppressLint("CheckResult")
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(layoutView!=null ){
            return layoutView;
        }
        layoutView = inflater.inflate(R.layout.fragment_mine, container, false);
        //页面
        loginLayout = layoutView.findViewById(R.id.login_layout);
        userLayout = layoutView.findViewById(R.id.user_layout);
        headView = layoutView.findViewById(R.id.head_image);
        nameText = layoutView.findViewById(R.id.user_text);
        toolbar = layoutView.findViewById(R.id.toolbar);
        focusNum = layoutView.findViewById(R.id.focus_num);

        View mStateBarFixer = layoutView.findViewById(R.id.status_bar_fix);
        //设置状态栏高度填充
        mStateBarFixer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                CommonUtils.getStatusBarHeight(context)));

        nameEdit = layoutView.findViewById(R.id.user_edit);
        psdEdit = layoutView.findViewById(R.id.psd_edit);

        ImageButton addButton = layoutView.findViewById(R.id.add_button);
        Button editMsgBtn = layoutView.findViewById(R.id.edit_btn);
        introText = layoutView.findViewById(R.id.intro_text);
        loginButton = layoutView.findViewById(R.id.login_button);
        signButton = layoutView.findViewById(R.id.sign_button);
        signButton.setVisibility(View.GONE);

        signText = layoutView.findViewById(R.id.sign_text);

        LinearLayout login_out = layoutView.findViewById(R.id.login_out);
        LinearLayout my_food = layoutView.findViewById(R.id.food_mine);
        LinearLayout my_recipe = layoutView.findViewById(R.id.recipe_mine);

        //功能
        reqManager = ReqManager.getInstance();
        RxPermissions rxPermissions = new RxPermissions(this);
        loginNow = true;

        if(MyApplication.getInstances().getUsername()!=null){
            loadUser();
        }else {
            loginLayout.setVisibility(View.VISIBLE);
            userLayout.setVisibility(View.INVISIBLE);
        }

        focusNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FocusUserActivity.class);
                startActivity(intent);
            }
        });

        editMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditMsgActivity.class);
                startActivity(intent);
            }
        });

        my_food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MyFoodActivity.class);
                startActivity(intent);
            }
        });

        my_recipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MyRecipeActivity.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = nameEdit.getText().toString();
                String password = psdEdit.getText().toString();
                login(username,password);
            }
        });

        signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = nameEdit.getText().toString();
                String password = psdEdit.getText().toString();
                if(password.length()>3){
                    signUp(username,password);
                }else {
                    ToastUtil.showShort(context,"密码太短");
                }

            }
        });

        login_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginOut();
            }
        });

        signText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnToLogin();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.getInstances().showBottomDialog(getActivity());
            }
        });

        headView.setOnClickListener(view -> {
            rxPermissions.request(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ,Manifest.permission.READ_EXTERNAL_STORAGE).subscribe(granted ->{
                if (granted) {
                    PictureSelector
                            .create(MineFragment.this,PictureSelector.SELECT_REQUEST_CODE)
                            .selectPicture(true,400,400,1,1);
                }else {
                    ToastUtil.showShort(context,"请打开相机和文件存储权限");
                }
            });
        });

        return layoutView;
    }

    private Handler mUIHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case UPLOAD_RESULT:
                    GlideApp.with(context)
                        .load((pictureBean.isCut() ? pictureBean.getPath() : pictureBean.getUri()).toString())
                            .apply(RequestOptions.bitmapTransform(new CircleCrop())).into(headView);
                    try {
                        FileInputStream fis = new FileInputStream(pictureBean.getPath());
                        Bitmap bitmap = BitmapFactory.decodeStream(fis);
                        MyApplication.getInstances().writeHeadImage(bitmap);
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                    ToastUtil.showShort(context,"上传成功");
                    break;
                case UPLOAD_FAIL:
                    ToastUtil.showShort(context,"网络错误");
                    break;
                case LOGIN_RESULT:
                    LoginClass loginClass = (LoginClass)msg.obj;
                    if(loginClass!=null){
                        if (loginClass.success.equals("true")){
                            String url = reqManager.getHost()+loginClass.body.get(0).hphoto;
                            String nul = null;
                            if(loginClass.body.get(0).hphoto!=null){
                                GlideApp.with(context)
                                        .asBitmap()
                                        .load(url)
                                        .listener(new RequestListener<Bitmap>() {
                                            @Override
                                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                                return false;
                                            }
                                            @Override
                                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                                MyApplication.getInstances().writeSharedPreferences(loginClass.body.get(0).uid,loginClass.body.get(0).username,loginClass.body.get(0).password,
                                                        loginClass.body.get(0).sex,loginClass.body.get(0).birthday,loginClass.body.get(0).profession,loginClass.body.get(0).city
                                                        ,loginClass.body.get(0).introduction,resource);
                                                MyApplication.getInstances().setUsername(loginClass.body.get(0).username);
                                                mUIHandler.sendEmptyMessage(RELOAD_USER);
                                                return false;
                                            }
                                        }).submit();
                            }else {
                                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.head_holder);
                                MyApplication.getInstances().writeSharedPreferences(loginClass.body.get(0).uid,loginClass.body.get(0).username,loginClass.body.get(0).password,
                                        loginClass.body.get(0).sex,loginClass.body.get(0).birthday,loginClass.body.get(0).profession,loginClass.body.get(0).city
                                        ,loginClass.body.get(0).introduction,bitmap);
                                MyApplication.getInstances().setUsername(loginClass.body.get(0).username);
                                mUIHandler.sendEmptyMessage(RELOAD_USER);
                            }

                        }else {
                            ToastUtil.showShort(context,"用户名或密码错误");
                        }

                    }
                    break;
                case RELOAD_USER:
                    loadUser();
                    break;
                case SIGN_RESULT:
                    String result = (String)msg.obj;
                    if(result.equals("-1")){
                        ToastUtil.showShort(context,"该用户名已经存在");
                    }else if(result.equals("1")){
                        login(nameEdit.getText().toString(),psdEdit.getText().toString());
                        ToastUtil.showShort(context,"注册成功,已为您登录");
                        closeInputMethod(psdEdit,context);
                    }
                    break;
                case FOLLOW_COUNT:
                    String count = (String)msg.obj;
                    focusNum.setText(count);
                    break;
            }
            return false;
        }
    });

    private void loadUser(){
        loginLayout.setVisibility(View.INVISIBLE);
        userLayout.setVisibility(View.VISIBLE);
        SharedPreferences user = context.getSharedPreferences("user_info",0);
        String introString=user.getString("intro", "");
        if(!introString.equals("")){
            introText.setText(introString);
        }else {
            introText.setText("这个人没有简介");
        }
        followCount(MyApplication.getInstances().getUsername());
        nameText.setText(MyApplication.getInstances().getUsername());
        Bitmap bitmap = MyApplication.getInstances().readHeadImage();
        GlideApp.with(this)
                .load(bitmap)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(headView);
        closeInputMethod(psdEdit,context);
    }

    @Override
    public void onResume() {
        super.onResume();
        followCount(MyApplication.getInstances().getUsername());
    }

    private void loginOut(){
        loginLayout.setVisibility(View.VISIBLE);
        userLayout.setVisibility(View.INVISIBLE);
        MyApplication.getInstances().setLoginOut();
        nameText.setText(null);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.head_holder);
        MyApplication.getInstances().writeHeadImage(bitmap);
        loginNow = false;
        turnToLogin();
    }

    private void turnToLogin(){
        if(loginNow){
            signButton.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
            signText.setText("登录");
            loginNow = false;
        }else {
            signButton.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
            signText.setText("注册");
            loginNow = true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PictureSelector.SELECT_REQUEST_CODE){
            if(data != null){
                pictureBean = data.getParcelableExtra(PictureSelector.PICTURE_RESULT);
                File mFile = new File(pictureBean.getPath());
                reqManager.upLoadHead(mFile, MyApplication.getInstances().getUsername(),(success, message) -> {
                    Message msg;
                    if(success){
                        msg = mUIHandler.obtainMessage(UPLOAD_RESULT);
                    }else {
                        msg = mUIHandler.obtainMessage(UPLOAD_FAIL);
                    }
                    msg.sendToTarget();
                });

            }
        }
    }

    //直接关闭键盘输入法
    private void closeInputMethod(EditText editText,Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();
        if (isOpen) {
            // imm.toggleSoftInput(0,
            // InputMethodManager.HIDE_NOT_ALWAYS);//没有显示则显示
            imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void login(String username,String password){
        reqManager.login(username,password,(success, message) -> {
            Message msg;
            if(success){
                msg = mUIHandler.obtainMessage(LOGIN_RESULT);
                msg.obj = message;
            }else {
                msg = mUIHandler.obtainMessage(UPLOAD_FAIL);
            }
            msg.sendToTarget();
        });
    }

    private void signUp(String username,String password){
        reqManager.signUp(username,password,(success, message) -> {
            Message msg;
            if(success){
                msg = mUIHandler.obtainMessage(SIGN_RESULT);
                msg.obj = message;
            }else {
                msg = mUIHandler.obtainMessage(UPLOAD_FAIL);
            }
            msg.sendToTarget();
        });
    }

    private void followCount(String username) {
        if (username!=null){
            reqManager.followCount(username,(success,message) -> {
                Message msg;
                if(success){
                    msg = mUIHandler.obtainMessage(FOLLOW_COUNT);
                    msg.obj = message;
                }else {
                    msg = mUIHandler.obtainMessage(UPLOAD_FAIL);
                }
                msg.sendToTarget();
            });
        }
    }
}

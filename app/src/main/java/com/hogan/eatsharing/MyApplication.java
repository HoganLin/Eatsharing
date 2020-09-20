package com.hogan.eatsharing;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import androidx.fragment.app.FragmentActivity;
import com.hogan.eatsharing.activity.FoodActivity;
import com.hogan.eatsharing.activity.RecipeActivity;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreator;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreator;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import org.jetbrains.annotations.NotNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class MyApplication extends Application {

    static {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @NotNull
            @Override
            public RefreshHeader createRefreshHeader(@NotNull Context context, @NotNull RefreshLayout layout) {
                layout.setEnableHeaderTranslationContent(false);
                return new MaterialHeader(context).setColorSchemeResources(R.color.colorRed);
                //.setTimeFormat(new DynamicTimeFormat("更新于 %s"));
                // 指定为经典Header，默认是 贝塞尔雷达Header
            }
        });
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @NotNull
            @Override
            public RefreshFooter createRefreshFooter(@NotNull Context context, @NotNull RefreshLayout layout) {
                //指定为经典Footer，默认是 BallPulseFooter
                return new ClassicsFooter(context).setDrawableSize(20);
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    public static MyApplication instances;

    @SuppressLint("StaticFieldLeak")
    public static Context mContext;

    private String username;

    @Override
    public void onCreate() {
        super.onCreate();

        instances = this;
        mContext = this.getApplicationContext();

        SharedPreferences user = mContext.getSharedPreferences("user_info",0);
        username = user.getString("username",null);

    }

    public static MyApplication getInstances() {
        return instances;
    }

    public String getUsername(){return username;}

    public void setUsername(String name){
        username = name;
    }

    public void writeSharedPreferences(String uid,String username, String password, String sex,String birthday,String profession,String city,String intro,Bitmap bitmap){
        SharedPreferences user = mContext.getSharedPreferences("user_info",0);
        //第一步:将Bitmap压缩至字节数组输出流ByteArrayOutputStream
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        //第二步:利用Base64将字节数组输出流中的数据转换成字符串String
        byte[] byteArray=byteArrayOutputStream.toByteArray();
        String imageString = Base64.encodeToString(byteArray, Base64.DEFAULT);
        //第三步:将String保持至SharedPreferences
        user.edit().putString("uid",uid).apply();
        user.edit().putString("image",imageString).apply();
        user.edit().putString("username",username).apply();
        user.edit().putString("password",password).apply();
        user.edit().putString("sex",sex).apply();
        user.edit().putString("birthday",birthday).apply();
        user.edit().putString("profession",profession).apply();
        user.edit().putString("city",city).apply();
        user.edit().putString("intro",intro).apply();
    }

    public void setLoginOut(){
        SharedPreferences user = mContext.getSharedPreferences("user_info",0);
        user.edit().putString("username",null).apply();
    }

    public Bitmap readHeadImage(){
        SharedPreferences user = mContext.getSharedPreferences("user_info",0);
        //第一步:取出字符串形式的Bitmap
        String imageString=user.getString("image", "");
        //第二步:利用Base64将字符串转换为ByteArrayInputStream
        byte[] byteArray= Base64.decode(imageString, Base64.DEFAULT);
        ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(byteArray);
        //第三步:利用ByteArrayInputStream生成Bitmap
        return BitmapFactory.decodeStream(byteArrayInputStream);
    }

    public void writeHeadImage(Bitmap bitmap){
        SharedPreferences user = mContext.getSharedPreferences("user_info",0);
        //第一步:将Bitmap压缩至字节数组输出流ByteArrayOutputStream
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        //第二步:利用Base64将字节数组输出流中的数据转换成字符串String
        byte[] byteArray=byteArrayOutputStream.toByteArray();
        String imageString = Base64.encodeToString(byteArray, Base64.DEFAULT);
        //第三步:将String保持至SharedPreferences
        user.edit().putString("image",imageString).apply();
    }

    /**
     * 弹窗
     */
    public void showBottomDialog(FragmentActivity activity){
        final Dialog dialog = new Dialog(activity,R.style.recharge_dialog);
        View view = View.inflate(activity,R.layout.dialog_add,null);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        dialog.findViewById(R.id.add_recipe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, RecipeActivity.class);

                activity.startActivity(intent);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.add_food).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, FoodActivity.class);
                activity.startActivity(intent);
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

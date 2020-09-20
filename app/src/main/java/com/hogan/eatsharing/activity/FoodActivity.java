package com.hogan.eatsharing.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hogan.eatsharing.BaseActivity;
import com.hogan.eatsharing.GlideApp;
import com.hogan.eatsharing.MyApplication;
import com.hogan.eatsharing.R;
import com.hogan.eatsharing.config.Food;
import com.hogan.eatsharing.utils.CommonUtils;
import com.hogan.eatsharing.utils.ReqManager;
import com.hogan.eatsharing.utils.ToastUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.wildma.pictureselector.PictureBean;
import com.wildma.pictureselector.PictureSelector;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

public class FoodActivity extends BaseActivity {

    private static final int UPLOAD_RESULT = 1;    //上传成功
    private static final int UPLOAD_FAIL = 0;      //上传失败
    private static final int UPDATE_RESULT = 2;

    private Context context;
    private ReqManager reqManager;
    private PictureBean pictureBean;

    private ImageView foodImage;
    private File foodFile;
    private Food food;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        context = getApplicationContext();
        reqManager = ReqManager.getInstance();
        RxPermissions rxPermissions = new RxPermissions(this);

        View mStateBarFixer = findViewById(R.id.status_bar_fix);
        //设置状态栏高度填充
        mStateBarFixer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                CommonUtils.getStatusBarHeight(context)));
        TextView barTitle = findViewById(R.id.toolbar_title);
        barTitle.setText("发布动态");

        foodImage = findViewById(R.id.food_image);
        EditText titleEdit = findViewById(R.id.title_edit);
        EditText contentEdit = findViewById(R.id.content_edit);
        EditText tagEdit = findViewById(R.id.tag_edit);
        Button releaseBtn = findViewById(R.id.release_btn);

        Intent intent = this.getIntent();
        String type = intent.getStringExtra("type");

        if (type!=null&&type.equals("modify")) {
            food = (Food)intent.getSerializableExtra("food");
            if(food!=null){
                GlideApp.with(context)
                        .asBitmap()
                        .load(reqManager.getHost()+food.fphoto)
                        .listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }
                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                foodFile = getFile(resource);
                                foodImage.setImageBitmap(resource);
                                return false;
                            }
                        }).submit();
                titleEdit.setText(food.title);
                contentEdit.setText(food.message);
                tagEdit.setText(food.tag);
            }
        }

        foodImage.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CheckResult")
            @Override
            public void onClick(View v) {
                rxPermissions.request(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ,Manifest.permission.READ_EXTERNAL_STORAGE).subscribe(granted ->{
                    if (granted) {
                        PictureSelector
                                .create(FoodActivity.this,PictureSelector.SELECT_REQUEST_CODE)
                                .selectPicture(true,900,600,4,3);
                    }else {
                        ToastUtil.showShort(context,"请打开相机和文件存储权限");
                    }
                });
            }
        });

        releaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ok = 0;
                String username = MyApplication.getInstances().getUsername();
                String titleStr = titleEdit.getText().toString();
                String contentStr = contentEdit.getText().toString();
                String tagStr = tagEdit.getText().toString();
                if(type!=null&&type.equals("modify")) {
                    update(foodFile,titleStr,contentStr,food.fid,tagStr);
                }else {
                    release(foodFile,titleStr,contentStr,username,tagStr);
                }

            }
        });

    }

    private Handler mUIHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case UPLOAD_RESULT:
                    String meg = (String)msg.obj;
                    if(meg.equals("1")){
                        ToastUtil.showShort(context,"发布成功");
                        finish();
                    }else {
                        ToastUtil.showShort(context,"错误");
                    }
                    break;
                case UPLOAD_FAIL:
                    ToastUtil.showShort(context,"网络错误");
                    break;
                case UPDATE_RESULT:
                    String meg2 = (String)msg.obj;
                    if(meg2.equals("1")){
                        ToastUtil.showShort(context,"修改成功");
                        finish();
                    }else {
                        ToastUtil.showShort(context,"错误");
                    }
                    break;
            }
            return false;
        }
    });

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PictureSelector.SELECT_REQUEST_CODE) {
            if (data != null) {
                pictureBean = data.getParcelableExtra(PictureSelector.PICTURE_RESULT);

                foodFile = new File(pictureBean.getPath());

                GlideApp.with(context)
                        .load(pictureBean.getPath())
                        .into(foodImage);
            }
        }
    }

    private void release(File file,String title,String content,String username,String tag){
        reqManager.foodRelease(file,title,content,tag,username,((success, message) -> {
            Message msg;
            if(success){
                msg = mUIHandler.obtainMessage(UPLOAD_RESULT);
                msg.obj = message;
            }else {
                msg = mUIHandler.obtainMessage(UPLOAD_FAIL);
            }
            msg.sendToTarget();
        }));
    }

    private void update(File file,String title,String content,String fid,String tag){
        reqManager.updateFood(file,title,content,tag,fid,((success, message) -> {
            Message msg;
            if(success){
                msg = mUIHandler.obtainMessage(UPDATE_RESULT);
                msg.obj = message;
            }else {
                msg = mUIHandler.obtainMessage(UPLOAD_FAIL);
            }
            msg.sendToTarget();
        }));
    }

    public File getFile(Bitmap bitmap) {
        Random random = new Random();
        int i = random.nextInt(999999);
        String local_file = Environment.getExternalStorageDirectory().getAbsolutePath() +"/upload/";
        File f = new File(local_file);
        if(!f.exists()){
            f.mkdirs();
        }
        String filePath = f.getAbsolutePath()+"/"+i+".jpg";
        File file = new File(filePath);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

}

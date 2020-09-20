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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hogan.eatsharing.BaseActivity;
import com.hogan.eatsharing.GlideApp;
import com.hogan.eatsharing.MyApplication;
import com.hogan.eatsharing.R;
import com.hogan.eatsharing.config.Recipe;
import com.hogan.eatsharing.config.StepImg;
import com.hogan.eatsharing.utils.CommonUtils;
import com.hogan.eatsharing.utils.ReqManager;
import com.hogan.eatsharing.utils.ToastUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.wildma.pictureselector.PictureBean;
import com.wildma.pictureselector.PictureSelector;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RecipeActivity extends BaseActivity {

    private static final int UPLOAD_RESULT = 1;    //上传成功
    private static final int UPLOAD_FAIL = 0;      //上传失败
    private static final int UPDATE_RESULT = 2;

    private List<Integer> ingreIds;
    private List<Integer> stepIds;

    private int stepNum;
    private Context context;
    private ReqManager reqManager;
    private PictureBean pictureBean;

    private File coverImg;
    private List<File> stepList;
    private List<String> steps;
    private LinearLayout stepLayout;
    private LinearLayout ingreLayout;

    private ImageView coverImage;
    private ImageView step1;
    private EditText stepEdit;
    private Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        Gson gson = new Gson();
        context = getApplicationContext();
        reqManager = ReqManager.getInstance();
        RxPermissions rxPermissions = new RxPermissions(this);

        ingreIds = new ArrayList<>();
        stepIds = new ArrayList<>();
        stepList = new ArrayList<>();
        steps = new ArrayList<>();

        ingreIds.add(R.id.ingre1);
        stepIds.add(R.id.step1);

        stepNum = 1;

        ingreLayout = findViewById(R.id.ingre_layout);
        stepLayout = findViewById(R.id.step_layout);
        step1 = findViewById(R.id.step_image);
        stepEdit = findViewById(R.id.step_edit);
        coverImage = findViewById(R.id.cover_image);
        TextView titleView = findViewById(R.id.title_edit);
        View mStateBarFixer = findViewById(R.id.status_bar_fix);
        //设置状态栏高度填充
        mStateBarFixer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                CommonUtils.getStatusBarHeight(context)));
        TextView barTitle = findViewById(R.id.toolbar_title);
        barTitle.setText("发布食谱");

        Button releaseBtn = findViewById(R.id.release_btn);

        Intent intent = this.getIntent();
        String type = intent.getStringExtra("type");

        if (type!=null&&type.equals("modify")){
            recipe = (Recipe)intent.getSerializableExtra("recipe");
            if (recipe!=null){
                GlideApp.with(context)
                        .asBitmap()
                        .load(reqManager.getHost()+recipe.cover)
                        .listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                coverImg = getFile(resource);
                                coverImage.setImageBitmap(resource);
                                return false;
                            }
                        }).submit();
                titleView.setText(recipe.title);
                Map<String,String> ingreMap = gson.fromJson(recipe.ingredients,new TypeToken<Map<String,String>>() {}.getType());
                int ingreNum = 0;
                for(String ingre : ingreMap.keySet()){
                    ingreNum++;
                    if (ingreNum==1){
                        EditText ingre1 = findViewById(R.id.ingre_edit);
                        EditText amount1 = findViewById(R.id.amount_edit);
                        ingre1.setText(ingre);
                        amount1.setText(ingreMap.get(ingre));
                    }else {
                        View newIngre = LayoutInflater.from(context).inflate(R.layout.item_ingre,null);
                        int id = View.generateViewId();
                        newIngre.setId(id);
                        EditText ingre2 = newIngre.findViewById(R.id.ingre_edit);
                        EditText amount2 = newIngre.findViewById(R.id.amount_edit);
                        ingre2.setText(ingre);
                        amount2.setText(ingreMap.get(ingre));
                        ingreIds.add(id);
                        ingreLayout.addView(newIngre);
                    }
                }
                List<String> stepList1 = gson.fromJson(recipe.step,new TypeToken<List<String>>(){}.getType());
                List<String> imgList = gson.fromJson(recipe.stepImg,new TypeToken<List<String>>(){}.getType());
                for(String img : imgList){
                    if (stepNum==1){
                        GlideApp.with(context)
                                .asBitmap()
                                .load(reqManager.getHost()+img)
                                .listener(new RequestListener<Bitmap>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                        File imgFile = getFile(resource);
                                        stepList.add(0,imgFile);
                                        Bitmap bitmap = resource;
                                        Message msg = mUIHandler.obtainMessage(0x15123,bitmap);
                                        msg.sendToTarget();
                                        return false;
                                    }
                                }).submit();
                        stepEdit.setText(stepList1.get(stepNum-1));
                        stepNum++;
                    }else {
                        View newStep = LayoutInflater.from(context).inflate(R.layout.item_step,null);
                        TextView num = newStep.findViewById(R.id.step_num);
                        num.setText(String.valueOf(stepNum));
                        int id = View.generateViewId();

                        ImageView stepImg = newStep.findViewById(R.id.step_image);
                        EditText stepEt = newStep.findViewById(R.id.step_edit);
                        stepImg.setOnClickListener(new View.OnClickListener() {
                            @SuppressLint("CheckResult")
                            @Override
                            public void onClick(View v) {

                                rxPermissions.request(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        ,Manifest.permission.READ_EXTERNAL_STORAGE).subscribe(granted ->{
                                    if (granted) {
                                        PictureSelector
                                                .create(RecipeActivity.this,id)
                                                .selectPicture(true,800,600,4,3);
                                    }else {
                                        ToastUtil.showShort(context,"请打开相机和文件存储权限");
                                    }
                                });

                            }
                        });
                        stepEt.setText(stepList1.get(stepNum-1));
                        newStep.setId(id);
                        stepIds.add(id);
                        stepLayout.addView(newStep);
                        GlideApp.with(context)
                                .asBitmap()
                                .load(reqManager.getHost()+img)
                                .listener(new RequestListener<Bitmap>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                        File imgFile = getFile(resource);
                                        stepList.add(imgFile);
                                        StepImg img1 = new StepImg();
                                        img1.id = id;
                                        img1.bitmap = resource;
                                        Message msg = mUIHandler.obtainMessage(0x11,img1);
                                        msg.sendToTarget();
                                        return false;
                                    }
                                }).submit();
                        stepNum++;
                    }

                }
            }
        }

        releaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ok = 0;

                steps.clear();
                String title = titleView.getText().toString();

                Map<String,String> ingres = new HashMap<>();
                for(int id:ingreIds){
                    RelativeLayout layout = ingreLayout.findViewById(id);
                    EditText ingre = layout.findViewById(R.id.ingre_edit);
                    EditText amount = layout.findViewById(R.id.amount_edit);
                    String ingreStr = ingre.getText().toString();
                    String amountStr = amount.getText().toString();
                    if(ingreStr.equals("")||amountStr.equals("")){
                        ok = 2;
                    }
                    ingres.put(ingreStr,amountStr);

                }
                String ingreStr = gson.toJson(ingres);

                for (int id:stepIds){
                    RelativeLayout layout = stepLayout.findViewById(id);
                    EditText step = layout.findViewById(R.id.step_edit);
                    String stepStr = step.getText().toString();
                    if(!stepStr.equals("")){
                        steps.add(stepStr);
                    }
                }
                if (steps.size()!=stepList.size()){
                    Log.d("TAG", "onClick: "+steps.size()+stepList.size());
                    ok = 3;
                }
                if (title.equals("")){
                    ok=1;
                }
                String stepStr = gson.toJson(steps);
                if(coverImg==null){
                    ok = 4;
                }

                EditText tip = findViewById(R.id.tip_edit);
                String tipStr = tip.getText().toString();
                EditText sort = findViewById(R.id.sort_edit);
                String sortStr = sort.getText().toString();

                String username = MyApplication.getInstances().getUsername();
                switch (ok){
                    case 0:
                        if (type!=null&&type.equals("modify")) {
                            update(coverImg,title,ingreStr,stepStr,stepList,tipStr,sortStr,recipe.rid);
                        }else {
                            release(coverImg, title, ingreStr, stepStr, stepList, tipStr, sortStr, username);
                        }
                        break;
                    case 1:
                        ToastUtil.showShort(context,"标题不能为空");
                        break;
                    case 2:
                        ToastUtil.showShort(context,"食材或用量不能为空");
                        break;
                    case 3:
                        ToastUtil.showShort(context,"步骤不能为空");
                        break;
                    case 4:
                        ToastUtil.showShort(context,"请添加封面");
                        break;
                }

            }
        });

        step1.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CheckResult")
            @Override
            public void onClick(View v) {
                rxPermissions.request(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ,Manifest.permission.READ_EXTERNAL_STORAGE).subscribe(granted ->{
                    if (granted) {
                        PictureSelector
                                .create(RecipeActivity.this,0x18)
                                .selectPicture(true,800,600,4,3);
                    }else {
                        ToastUtil.showShort(context,"请打开相机和文件存储权限");
                    }
                });
            }
        });

        coverImage.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CheckResult")
            @Override
            public void onClick(View v) {
                rxPermissions.request(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ,Manifest.permission.READ_EXTERNAL_STORAGE).subscribe(granted ->{
                    if (granted) {
                        PictureSelector
                                .create(RecipeActivity.this,PictureSelector.SELECT_REQUEST_CODE)
                                .selectPicture(true,800,600,4,3);
                    }else {
                        ToastUtil.showShort(context,"请打开相机和文件存储权限");
                    }
                });
            }
        });

        Button addIngre = findViewById(R.id.add_ingre);
        Button addStep = findViewById(R.id.add_step);

        addIngre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View newIngre = LayoutInflater.from(context).inflate(R.layout.item_ingre,null);
                int id = View.generateViewId();
                newIngre.setId(id);
                ingreIds.add(id);
                ingreLayout.addView(newIngre);
            }
        });

        addStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View newStep = LayoutInflater.from(context).inflate(R.layout.item_step,null);
                TextView num = newStep.findViewById(R.id.step_num);
                stepNum = stepNum+1;
                num.setText(String.valueOf(stepNum));
                int id = View.generateViewId();

                ImageView stepImg = newStep.findViewById(R.id.step_image);
                stepImg.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("CheckResult")
                    @Override
                    public void onClick(View v) {

                        rxPermissions.request(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ,Manifest.permission.READ_EXTERNAL_STORAGE).subscribe(granted ->{
                            if (granted) {
                                PictureSelector
                                        .create(RecipeActivity.this,id)
                                        .selectPicture(true,800,600,4,3);
                            }else {
                                ToastUtil.showShort(context,"请打开相机和文件存储权限");
                            }
                        });

                    }
                });

                newStep.setId(id);
                stepIds.add(id);
                stepLayout.addView(newStep);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PictureSelector.SELECT_REQUEST_CODE){
            if(data != null){
                pictureBean = data.getParcelableExtra(PictureSelector.PICTURE_RESULT);

                coverImg = new File(pictureBean.getPath());

                GlideApp.with(context)
                        .load(pictureBean.getPath())
                        .into(coverImage);
            }
        } else if (requestCode == 0x18) {
            if(data != null){
                pictureBean = data.getParcelableExtra(PictureSelector.PICTURE_RESULT);

                File file = new File(pictureBean.getPath());
                if (stepList.size()>=1){
                    stepList.remove(0);
                    stepList.add(0,file);
                }else {
                    stepList.add(file);
                }


                GlideApp.with(context)
                        .load(pictureBean.getPath())
                        .into(step1);
            }

        } else {
            for (int id : stepIds) {
                if (requestCode == id) {
                    if (data != null) {
                        int i = stepIds.indexOf(id);
                        pictureBean = data.getParcelableExtra(PictureSelector.PICTURE_RESULT);
                        RelativeLayout layout = stepLayout.findViewById(id);
                        ImageView view = layout.findViewById(R.id.step_image);
                        File file = new File(pictureBean.getPath());
                        if(stepList.size()<i+1){
                            stepList.add(file);
                        }else {
                            stepList.remove(i);
                            stepList.add(i,file);
                        }
                        GlideApp.with(context)
                                .load(pictureBean.getPath())
                                .into(view);
                    }
                }
            }
        }
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
                case 0x15123:
                    Bitmap bitmap = (Bitmap)msg.obj;
                    GlideApp.with(context)
                            .load(bitmap)
                            .into(step1);
                    break;
                case 0x11:
                    StepImg img = (StepImg) msg.obj;
                    RelativeLayout layout = stepLayout.findViewById(img.id);
                    ImageView view = layout.findViewById(R.id.step_image);
                    GlideApp.with(context)
                            .load(img.bitmap)
                            .into(view);
            }
            return false;
        }
    });

    private void release(File coverFile,String title,String ingre,String step,List<File> images,String tip,String sort,String username){
        reqManager.recipeRelease(coverFile,title,ingre,step,images,tip,sort,username,(success, message) -> {
            Message msg;
            if(success){
                msg = mUIHandler.obtainMessage(UPLOAD_RESULT);
                msg.obj = message;
            }else {
                msg = mUIHandler.obtainMessage(UPLOAD_FAIL);
            }
            msg.sendToTarget();
        });
    }

    private void update(File coverFile,String title,String ingre,String step,List<File> images,String tip,String sort,String rid){
        reqManager.updateRecipe(coverFile,title,ingre,step,images,tip,sort,rid,(success, message) -> {
            Message msg;
            if(success){
                msg = mUIHandler.obtainMessage(UPDATE_RESULT);
                msg.obj = message;
            }else {
                msg = mUIHandler.obtainMessage(UPLOAD_FAIL);
            }
            msg.sendToTarget();
        });
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

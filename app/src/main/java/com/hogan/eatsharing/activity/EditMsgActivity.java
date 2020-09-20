package com.hogan.eatsharing.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.hogan.eatsharing.MyApplication;
import com.hogan.eatsharing.R;
import com.hogan.eatsharing.utils.ReqManager;
import com.hogan.eatsharing.utils.ToastUtil;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditMsgActivity extends AppCompatActivity {

    private static final int RESULT = 1;
    private static final int FAIL = 0;
    private Context mContext;
    private ReqManager reqManager;
    private String birthday = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_msg);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        reqManager = ReqManager.getInstance();
        mContext = getApplicationContext();

        TextView bid = findViewById(R.id.birtext);
        EditText sexEdit = findViewById(R.id.sex_edit);
        EditText proEdit = findViewById(R.id.pro_edit);
        EditText cityEdit = findViewById(R.id.city_edit);
        EditText introEdit = findViewById(R.id.intro_edit);
        SharedPreferences user = mContext.getSharedPreferences("user_info",0);
        String bir = user.getString("birthday","");
        if(!bir.equals("")){
            long time = Long.parseLong(bir);
            Date date = new Date(time);
            bid.setText(simpleDateFormat.format(date));
            birthday = simpleDateFormat.format(date);
        }
        sexEdit.setText(user.getString("sex",""));
        proEdit.setText(user.getString("profession",""));
        cityEdit.setText(user.getString("city",""));
        introEdit.setText(user.getString("intro",""));

        Button saveBtn = findViewById(R.id.save_msg_btn);

        TimePickerView timePicker = new TimePickerBuilder(EditMsgActivity.this, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                birthday = simpleDateFormat.format(date);
                bid.setText(simpleDateFormat.format(date));
                bid.setTextColor(getResources().getColor(R.color.black));
            }
        }).build();

        bid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePicker.show();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = MyApplication.getInstances().getUsername();
                String sex = sexEdit.getText().toString();
                String pro = proEdit.getText().toString();
                String city = cityEdit.getText().toString();
                String intro = introEdit.getText().toString();
                Log.d("msg", "onClick: "+sex+pro+city+intro);
                updateMsg(username,sex,pro,birthday,city,intro);
            }
        });
    }

    private Handler mUIHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case RESULT:
                    String result = (String)msg.obj;
                    if(result.equals("1")){
                        ToastUtil.showShort(mContext,"保存成功");
                        finish();
                    }else {
                        ToastUtil.showShort(mContext,"错误");
                    }
                    break;
                case FAIL:
                    ToastUtil.showShort(mContext,"网络错误");
                    break;
            }
            return false;
        }
    });

    private void updateMsg(String username,String sex,String pro,String birthday,String city,String intro) {
        reqManager.updateMsg(username,sex,pro,birthday,city,intro ,(success, message) -> {
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
}

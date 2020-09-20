package com.hogan.eatsharing;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.hogan.eatsharing.fragment.CollectFragment;
import com.hogan.eatsharing.fragment.MainFragment;
import com.hogan.eatsharing.fragment.MineFragment;

public class MainActivity extends BaseActivity {

    public FragmentManager manager;

    private String[] tabs = { "首页","收藏", "我的" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {

        RadioGroup mRadioGroup = findViewById(R.id.nav_group);
        manager = getSupportFragmentManager();

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButton1:
                        // 切换到"首页"界面
                        switchFragmentSupport(R.id.content, tabs[0]);
                        break;
                    case R.id.radioButton2:
                        // 切换到"收藏"界面
                        switchFragmentSupport(R.id.content, tabs[1]);
                        break;
                    case R.id.radioButton3:
                        // 切换到"我的"界面
                        switchFragmentSupport(R.id.content, tabs[2]);
                        break;
                }
            }
        });
        // 默认选中最左边的RadioButton
        RadioButton btn = (RadioButton) mRadioGroup.getChildAt(0);
        btn.toggle();
    }

    /**
     * 动态切换组件中显示的界面
     * @param containerId  待切换界面的组件
     */
    public void switchFragmentSupport(int containerId, String tag) {
        // 根据tag标签名查找是否存在Fragment对象
        Fragment destFragment = manager.findFragmentByTag(tag);

        // 如果tag标签对应的Fragment对象不存在，则初始化它
        if (destFragment == null) {
            if (tag.equals(tabs[0])){
                destFragment = MainFragment.newInstance();
            }
            if (tag.equals(tabs[1])){
                destFragment = CollectFragment.newInstance();
            }
            if (tag.equals(tabs[2])){
                destFragment = MineFragment.newInstance();
            }
        }

        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(containerId, destFragment, tag);
        ft.addToBackStack(tag);
        ft.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}

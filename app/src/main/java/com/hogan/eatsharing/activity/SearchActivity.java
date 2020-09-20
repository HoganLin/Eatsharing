package com.hogan.eatsharing.activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.hogan.eatsharing.BaseActivity;
import com.hogan.eatsharing.R;
import com.hogan.eatsharing.adapter.RecipeCollectAdapter;
import com.hogan.eatsharing.config.Recipe;
import com.hogan.eatsharing.config.RecipeList;
import com.hogan.eatsharing.utils.CommonUtils;
import com.hogan.eatsharing.utils.ReqManager;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity {

    private static final int RESULT = 1;
    private static final int FAIL = 0;

    private Context context;
    private ReqManager reqManager;
    private List<Recipe> recipes;
    private RecipeCollectAdapter adapter;

    private EditText searchEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        context = getApplicationContext();
        reqManager = ReqManager.getInstance();
        recipes = new ArrayList<>();

        View mStateBarFixer = findViewById(R.id.status_bar_fix);
        //设置状态栏高度填充
        mStateBarFixer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                CommonUtils.getStatusBarHeight(context)));

        searchEdit = findViewById(R.id.search_edit);
        ImageButton searchBtn = findViewById(R.id.search_btn);

        RecyclerView recyclerView = findViewById(R.id.list_search);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new RecipeCollectAdapter(context, recipes);
        recyclerView.setAdapter(adapter);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = searchEdit.getText().toString();
                if (!key.equals("")){
                    closeInputMethod();
                    search(key);
                }
            }
        });
    }

    private Handler mUIHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case RESULT:
                    RecipeList recipeList = (RecipeList) msg.obj;
                    if (recipeList != null) {
                        recipes.clear();
                        recipes.addAll(recipeList.body);
                        adapter.notifyDataSetChanged();
                    } else {
                        recipes.clear();
                        adapter.notifyDataSetChanged();
                    }
                    break;
                case FAIL:
                    break;
            }
            return false;
        }
    });

    private void search(String key) {
        reqManager.searchRecipe(key,(success, recipeList) -> {
            Message msg;
            if (success) {
                msg = mUIHandler.obtainMessage(RESULT);
                msg.obj = recipeList;
            } else {
                msg = mUIHandler.obtainMessage(FAIL);
            }
            msg.sendToTarget();
        });
    }

    //直接关闭键盘输入法
    private void closeInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();
        if (isOpen) {
            // imm.toggleSoftInput(0,
            // InputMethodManager.HIDE_NOT_ALWAYS);//没有显示则显示
            imm.hideSoftInputFromWindow(searchEdit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}

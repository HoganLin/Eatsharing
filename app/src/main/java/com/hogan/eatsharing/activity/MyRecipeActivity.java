package com.hogan.eatsharing.activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.hogan.eatsharing.BaseActivity;
import com.hogan.eatsharing.MyApplication;
import com.hogan.eatsharing.R;
import com.hogan.eatsharing.adapter.MyRecipeAdapter;
import com.hogan.eatsharing.config.Recipe;
import com.hogan.eatsharing.config.RecipeList;
import com.hogan.eatsharing.utils.CommonUtils;
import com.hogan.eatsharing.utils.ReqManager;
import com.hogan.eatsharing.utils.ToastUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import java.util.ArrayList;
import java.util.List;

public class MyRecipeActivity extends BaseActivity {

    private static final int RESULT = 1;
    private static final int FAIL = 0;
    private static final int DELETE = 2;

    private Context context;
    private ReqManager reqManager;

    private List<Recipe> recipes;
    private int choosePtn;

    private SmartRefreshLayout refreshLayout;
    private MyRecipeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recipe);

        context = getApplicationContext();
        reqManager = ReqManager.getInstance();
        recipes = new ArrayList<>();

        TextView title = findViewById(R.id.toolbar_title);
        title.setText("我的食谱");

        View mStateBarFixer = findViewById(R.id.status_bar_fix);
        //设置状态栏高度填充
        mStateBarFixer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                CommonUtils.getStatusBarHeight(context)));

        refreshLayout = findViewById(R.id.my_recipe_refresh);
        refreshLayout.autoRefresh();
        RecyclerView recyclerView = findViewById(R.id.my_recipe_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MyRecipeAdapter(context,recipes);
        recyclerView.setAdapter(adapter);

        //下拉刷新监听
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.resetNoMoreData();
                initData(MyApplication.getInstances().getUsername());
            }
        });

        adapter.setOnItemClickListener(position -> {
            Recipe recipe = recipes.get(position);
            Intent intent = new Intent(MyRecipeActivity.this, ShowRecipeActivity.class);
            intent.putExtra("recipe",recipe);
            //intent.putExtra("type","normal");
            startActivity(intent);
        });

        adapter.setOnUserClickListener(position -> {
            choosePtn = position;
            showMineDialog(recipes.get(position));
        });
    }

    private Handler mUIHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case RESULT:
                    RecipeList recipeList =(RecipeList)msg.obj;
                    if(recipeList!=null){
                        recipes.clear();
                        recipes.addAll(recipeList.body);
                        adapter.notifyDataSetChanged();
                        Log.d("123", "handleMessage: "+recipeList.body.get(0).cover);
                        refreshLayout.finishRefresh(true);
                        refreshLayout.finishLoadMore(true);
                    }else {
                        refreshLayout.finishLoadMoreWithNoMoreData();
                    }
                    break;
                case FAIL:
                    refreshLayout.finishRefresh(false);
                    break;
                case DELETE:
                    String result = (String)msg.obj;
                    if(result.equals("1")){
                        ToastUtil.showShort(context,"删除成功");
                        recipes.remove(choosePtn);
                        adapter.notifyDataSetChanged();
                    }
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onResume() {
        super.onResume();
        refreshLayout.autoRefresh();
    }

    private void initData(String username) {
        reqManager.getMyRecipes(username ,(success, recipeList) -> {
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

    private void deleteRecipe(String rid){
        reqManager.deleteRecipe(rid,(success, message) -> {
            Message msg;
            if(success){
                msg = mUIHandler.obtainMessage(DELETE);
                msg.obj = message;
            }else {
                msg = mUIHandler.obtainMessage(FAIL);
            }
            msg.sendToTarget();
        });
    }

    public void showMineDialog(Recipe recipe){
        final Dialog dialog = new Dialog(this,R.style.recharge_dialog);
        View view = View.inflate(this,R.layout.dialog_mine,null);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        dialog.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteRecipe(recipe.rid);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyRecipeActivity.this,RecipeActivity.class);
                intent.putExtra("type","modify");
                intent.putExtra("recipe",recipe);
                startActivity(intent);
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

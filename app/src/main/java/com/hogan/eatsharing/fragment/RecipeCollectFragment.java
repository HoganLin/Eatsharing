package com.hogan.eatsharing.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hogan.eatsharing.MyApplication;
import com.hogan.eatsharing.R;
import com.hogan.eatsharing.activity.ShowRecipeActivity;
import com.hogan.eatsharing.adapter.RecipeCollectAdapter;
import com.hogan.eatsharing.config.Recipe;
import com.hogan.eatsharing.config.RecipeList;
import com.hogan.eatsharing.utils.ReqManager;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import java.util.ArrayList;
import java.util.List;

public class RecipeCollectFragment extends Fragment {

    private static final int RESULT = 1;
    private static final int FAIL = 0;

    private View layoutView;
    private Context context;
    private ReqManager reqManager;
    private List<Recipe> recipes;

    private SmartRefreshLayout refreshLayout;
    private RecipeCollectAdapter adapter;

    public RecipeCollectFragment() {

    }

    public static RecipeCollectFragment newInstance() {
        RecipeCollectFragment fragment = new RecipeCollectFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (layoutView != null) {
            return layoutView;
        }
        layoutView = inflater.inflate(R.layout.fragment_collect_recipe, container, false);
        reqManager = ReqManager.getInstance();
        recipes = new ArrayList<>();

        RecyclerView recyclerView = layoutView.findViewById(R.id.list_recipe);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new RecipeCollectAdapter(context, recipes);
        recyclerView.setAdapter(adapter);

        refreshLayout = layoutView.findViewById(R.id.recipe_list_refresh);
        refreshLayout.autoRefresh();

        //下拉刷新监听
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.resetNoMoreData();
                String username = MyApplication.getInstances().getUsername();
                initData(username);
            }
        });

        adapter.setOnItemClickListener(position -> {
            Recipe recipe = recipes.get(position);
            Intent intent = new Intent(getActivity(), ShowRecipeActivity.class);
            intent.putExtra("recipe", recipe);
            intent.putExtra("type", "normal");
            startActivity(intent);
        });

        return layoutView;
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
                        refreshLayout.finishRefresh(true);
                        refreshLayout.finishLoadMore(true);
                    } else {
                        recipes.clear();
                        adapter.notifyDataSetChanged();
                        refreshLayout.finishLoadMoreWithNoMoreData();
                    }
                    break;
                case FAIL:
                    refreshLayout.finishRefresh(false);
                    break;
            }
            return false;
        }
    });

    private void initData(String username) {
        reqManager.getCollectRecipe(username,(success, recipeList) -> {
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
}

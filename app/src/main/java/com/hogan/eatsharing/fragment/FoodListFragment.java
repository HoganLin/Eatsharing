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
import com.hogan.eatsharing.R;
import com.hogan.eatsharing.activity.ShowFoodActivity;
import com.hogan.eatsharing.adapter.FoodRecommAdapter;
import com.hogan.eatsharing.config.Food;
import com.hogan.eatsharing.config.FoodList;
import com.hogan.eatsharing.utils.ReqManager;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import java.util.ArrayList;
import java.util.List;

public class FoodListFragment extends Fragment {

    private static final int RESULT = 1;
    private static final int FAIL = 0;

    private View layoutView;
    private Context context;
    private ReqManager reqManager;
    private List<Food> foods;

    private SmartRefreshLayout refreshLayout;
    private FoodRecommAdapter adapter;

    public FoodListFragment(){

    }

    public  static FoodListFragment newInstance(){
        FoodListFragment fragment = new FoodListFragment();
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
        if(layoutView!=null){
            return layoutView;
        }
        layoutView = inflater.inflate(R.layout.fragment_food,container,false);

        reqManager = ReqManager.getInstance();
        foods = new ArrayList<>();

        RecyclerView recyclerView = layoutView.findViewById(R.id.list_food);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new FoodRecommAdapter(context,foods);
        recyclerView.setAdapter(adapter);

        refreshLayout = layoutView.findViewById(R.id.food_list_refresh);
        refreshLayout.autoRefresh();

        //下拉刷新监听
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.resetNoMoreData();
                initData();
            }
        });

        adapter.setOnItemClickListener(position -> {
            Food food = foods.get(position);
            Intent intent = new Intent(getActivity(), ShowFoodActivity.class);
            intent.putExtra("food",food);
            startActivity(intent);
        });

        return layoutView;
    }

    private Handler mUIHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case RESULT:
                    FoodList recipeList =(FoodList) msg.obj;
                    if(recipeList!=null){
                        foods.clear();
                        foods.addAll(recipeList.body);
                        adapter.notifyDataSetChanged();
                        refreshLayout.finishRefresh(true);
                        refreshLayout.finishLoadMore(true);
                    }else {
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

    private void initData() {
        reqManager.getFoods((success, foodList) -> {
            Message msg;
            if(success){
                msg = mUIHandler.obtainMessage(RESULT);
                msg.obj = foodList;
            }else {
                msg = mUIHandler.obtainMessage(FAIL);
            }
            msg.sendToTarget();
        });
    }
}

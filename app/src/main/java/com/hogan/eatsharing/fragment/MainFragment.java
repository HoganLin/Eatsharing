package com.hogan.eatsharing.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.tabs.TabLayout;
import com.hogan.eatsharing.MyApplication;
import com.hogan.eatsharing.R;
import com.hogan.eatsharing.activity.SearchActivity;
import com.hogan.eatsharing.adapter.ViewPagerAdapter;
import com.hogan.eatsharing.utils.CommonUtils;
import com.hogan.eatsharing.utils.ReqManager;
import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private View layoutView;
    private Toolbar toolbar;
    private Context context;
    private ReqManager reqManager;

    private String[] strings = new String[]{"推荐","动态"};

    public MainFragment() {
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(layoutView!=null ){
            return layoutView;
        }

        layoutView = inflater.inflate(R.layout.fragment_main, container, false);
        toolbar = layoutView.findViewById(R.id.toolbar);
        View mStateBarFixer = layoutView.findViewById(R.id.status_bar_fix);
        //设置状态栏高度填充
        mStateBarFixer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                CommonUtils.getStatusBarHeight(context)));

        ViewPager viewPager = layoutView.findViewById(R.id.main_vp);
        TabLayout tabLayout = layoutView.findViewById(R.id.tab_layout);

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new RecipeListFragment());
        fragments.add(new FoodListFragment());
        ViewPagerAdapter adapter = new ViewPagerAdapter(getParentFragmentManager(),fragments,strings);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        ImageButton addBtn = layoutView.findViewById(R.id.add_button);
        TextView search = layoutView.findViewById(R.id.toolbar_search);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.getInstances().showBottomDialog(getActivity());
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        return layoutView;
    }
}

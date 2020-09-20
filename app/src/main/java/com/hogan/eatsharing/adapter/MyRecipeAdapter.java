package com.hogan.eatsharing.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hogan.eatsharing.GlideApp;
import com.hogan.eatsharing.R;
import com.hogan.eatsharing.config.Recipe;
import java.util.List;

public class MyRecipeAdapter extends RecyclerView.Adapter<MyRecipeAdapter.ViewHolder> {

    private List<Recipe> recipes;
    private Context mContext;
    private String mHost = "http://192.168.137.1:8080/EatSharing/";

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView iconImage;
        TextView iconName;
        ImageButton moreBtn;

        public ViewHolder(View view){
            super(view);
            iconImage = view.findViewById(R.id.my_recipe_view);
            iconName = view.findViewById(R.id.my_recipe_title);
            moreBtn = view.findViewById(R.id.more_button);
        }
    }

    public MyRecipeAdapter(Context context,List<Recipe> recipes){
        this.recipes = recipes;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_recipe,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.iconName.setText(recipe.title);
        GlideApp.with(mContext)
                .load(mHost+recipe.cover)
                .centerCrop()
                .into(holder.iconImage);

        holder.iconImage.setOnClickListener(v -> mOnItemClickListener.onItemClick(position));
        holder.moreBtn.setOnClickListener(v -> mOnUserClickListener.onUserClick(position));
    }

    public interface onItemClickListener{
        void onItemClick(int position);
    }

    public interface onUserClickListener{
        void onUserClick(int position);
    }

    private onItemClickListener mOnItemClickListener;
    private onUserClickListener mOnUserClickListener;

    public void setOnItemClickListener(onItemClickListener mOnItemClickListener){
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void setOnUserClickListener(onUserClickListener mOnUserClickListener){
        this.mOnUserClickListener = mOnUserClickListener;
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }
}

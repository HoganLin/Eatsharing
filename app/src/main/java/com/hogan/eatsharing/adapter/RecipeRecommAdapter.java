package com.hogan.eatsharing.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.hogan.eatsharing.GlideApp;
import com.hogan.eatsharing.R;
import com.hogan.eatsharing.config.Recipe;
import java.util.List;

public class RecipeRecommAdapter extends RecyclerView.Adapter<RecipeRecommAdapter.ViewHolder> {

    private List<Recipe> recipes;
    private Context mContext;
    private String mHost = "http://192.168.137.1:8080/EatSharing/";

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView iconImage;
        TextView iconName;
        ImageView hImage;
        LinearLayout userLayout;

        public ViewHolder(View view){
            super(view);
            iconImage = view.findViewById(R.id.recipe_cover);
            iconName = view.findViewById(R.id.recipe_title);
            hImage = view.findViewById(R.id.author_img);
            userLayout = view.findViewById(R.id.user_layout);
        }
    }

    public RecipeRecommAdapter(Context context,List<Recipe> recipes){
        this.recipes = recipes;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_view,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.iconName.setText(recipe.title);
        GlideApp.with(mContext)
                .load(mHost+recipe.cover)
                .placeholder(R.drawable.image_holder)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(20)))
                .into(holder.iconImage);
        if(recipe.hphoto!=null){
            GlideApp.with(mContext)
                    .load(mHost+recipe.hphoto)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(holder.hImage);
        }else {
            GlideApp.with(mContext)
                    .load(R.drawable.head_holder)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(holder.hImage);
        }

        holder.iconImage.setOnClickListener(v -> mOnItemClickListener.onItemClick(position));
        holder.userLayout.setOnClickListener(v -> mOnUserClickListener.onUserClick(position));
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

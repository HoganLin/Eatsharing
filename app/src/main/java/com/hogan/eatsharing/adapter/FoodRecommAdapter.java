package com.hogan.eatsharing.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.hogan.eatsharing.GlideApp;
import com.hogan.eatsharing.R;
import com.hogan.eatsharing.config.Food;
import java.util.List;

public class FoodRecommAdapter extends RecyclerView.Adapter<FoodRecommAdapter.ViewHolder> {

private List<Food> foods;
private Context mContext;
private String mHost = "http://192.168.137.1:8080/EatSharing/";

static class ViewHolder extends RecyclerView.ViewHolder{
    ImageView iconImage;
    TextView iconName;
    TextView userName;
    ImageView hImage;
    TextView msgText;

    private ViewHolder(View view){
        super(view);
        userName = view.findViewById(R.id.user_name);
        iconImage = view.findViewById(R.id.food_image);
        iconName = view.findViewById(R.id.food_title);
        hImage = view.findViewById(R.id.author_img);
        msgText = view.findViewById(R.id.msg_text);
    }
}

    public FoodRecommAdapter(Context context,List<Food> foods){
        this.foods = foods;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_view,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Food food = foods.get(position);
        holder.iconName.setText(food.title);
        GlideApp.with(mContext)
                .load(mHost+food.fphoto)
                .centerCrop()
                .into(holder.iconImage);
        if(food.hphoto!=null){
            GlideApp.with(mContext)
                    .load(mHost+food.hphoto)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(holder.hImage);
        }else {
            GlideApp.with(mContext)
                    .load(R.drawable.head_holder)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(holder.hImage);
        }
        holder.iconImage.setOnClickListener(v -> {mOnItemClickListener.onItemClick(position);});
        holder.msgText.setText(food.message);
        holder.userName.setText(food.username);
    }

    public interface onItemClickListener{
        void onItemClick(int position);
    }

    private onItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(onItemClickListener mOnItemClickListener){
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }
}

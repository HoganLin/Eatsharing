package com.hogan.eatsharing.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.hogan.eatsharing.GlideApp;
import com.hogan.eatsharing.R;
import com.hogan.eatsharing.config.User;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<User> users;
    private Context mContext;
    private String mHost = "http://192.168.137.1:8080/EatSharing/";

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView userIcon;
        TextView userName;
        Button focusBtn;

        public ViewHolder(View view){
            super(view);
            userIcon = view.findViewById(R.id.hphoto);
            userName = view.findViewById(R.id.user_name);
            focusBtn = view.findViewById(R.id.focus_btn);
        }
    }

    public UserAdapter(Context context,List<User> users){
        this.users = users;
        Log.d("userNum", "UserAdapter: "+users.size());
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.userName.setText(user.username);
        if(user.hphoto!=null){
            GlideApp.with(mContext)
                    .load(mHost+user.hphoto)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(holder.userIcon);
        }else {
            GlideApp.with(mContext)
                    .load(R.drawable.head_holder)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(holder.userIcon);
        }

    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}

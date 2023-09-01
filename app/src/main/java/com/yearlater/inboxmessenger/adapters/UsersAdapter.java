package com.yearlater.inboxmessenger.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.yearlater.inboxmessenger.R;
import com.yearlater.inboxmessenger.activities.main.messaging.ChatActivity;
import com.yearlater.inboxmessenger.model.realms.User;
import com.yearlater.inboxmessenger.utils.AdapterHelper;

import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by yearlater on 03/08/2017.
 */

//show the groupUsers from phonebook who have installed this app
public class UsersAdapter extends RealmRecyclerViewAdapter<User, RecyclerView.ViewHolder> {
    Context context;
    List<User> userList;
    private OnItemClickListener onItemClickListener;

    public UsersAdapter(@Nullable OrderedRealmCollection<User> data, boolean autoUpdate, Context context) {
        super(data, autoUpdate);
        this.userList = data;
        this.context = context;
        onItemClickListener = (OnItemClickListener) context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_user, parent, false);
        return new UserHolder(row);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final User user = userList.get(position);
        UserHolder mHolder = (UserHolder) holder;
        mHolder.tvName.setText(user.getProperUserName());
        mHolder.tvStatus.setText(user.getStatus());

        mHolder.rlltBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null)
                    onItemClickListener.onItemClick(user);
            }
        });


        mHolder.userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null)
                    onItemClickListener.onUserPhotoClick(user);
            }
        });


        loadUserPhoto(user, mHolder.userPhoto);

    }

    private void loadUserPhoto(final User user, final ImageView imageView) {
        if (user == null) return;
        if (user.getUid() == null) return;

        if (user.getThumbImg() != null) {
//            byte[] bytes = BitmapUtils.encodeImageAsBytes(user.getThumbImg());
           // GlideApp.with(context).load(user.getThumbImg()).into(imageView);
            float size = context.getResources().getDimension(R.dimen.profile_pic_size);
            Glide.with(context)
                    .asBitmap()
                    .override((int) size, (int) size)
                    .load(user.getThumbImg())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            AdapterHelper.drawProfileImage(resource, imageView, context);
                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                        @Override
                        public void onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                        }
                    });
        }


    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class UserHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout rlltBody;
        private ImageView userPhoto;
        private TextView tvName, tvStatus;



        public UserHolder(View itemView) {
            super(itemView);
            rlltBody =  itemView.findViewById(R.id.container_layout);
            userPhoto = (ImageView) itemView.findViewById(R.id.user_photo);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvStatus = (TextView) itemView.findViewById(R.id.tv_status);


        }
    }

    public interface OnItemClickListener {
        void onItemClick(User user);

        void onUserPhotoClick(User user);

    }

}

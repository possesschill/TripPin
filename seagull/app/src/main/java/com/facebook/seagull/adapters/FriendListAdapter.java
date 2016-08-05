package com.facebook.seagull.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.seagull.R;
import com.facebook.seagull.models.FriendCheck;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by satchinc on 7/22/16.
 */
public class FriendListAdapter extends ArrayAdapter<FriendCheck> {

    public List<FriendCheck> getFriendChecklist() {
        return friendChecklist;
    }

    private ArrayList<FriendCheck> friendChecklist;
    public FriendListAdapter(Context context, List<FriendCheck> objects) {
        super(context, R.layout.friend_item, objects);
        this.friendChecklist = new ArrayList<>();
        this.friendChecklist.addAll(objects);

    }

    @Override
    public void add(FriendCheck object) {
        super.add(object);
        friendChecklist.add(object);
        Log.d("NoWay", Integer.toString(this.friendChecklist.size()));
    }

    @Override
    public void addAll(Collection<? extends FriendCheck> collection) {
        super.addAll(collection);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FriendCheck friendCheck = this.getItem(position);
        ParseUser user = friendCheck.getUser();
        ViewHolder viewHolder;

        if(convertView==null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.friend_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);

        }
        else{
            viewHolder=(ViewHolder) convertView.getTag();
        }

        viewHolder.cbInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                FriendCheck cbFriend = (FriendCheck) v.getTag();
                Log.d("Checkbox", "Parse User Following Selected: " + cbFriend.getUser().getString("first_name"));
                cbFriend.setSelected(cb.isChecked());
                Log.d("Checkbox", String.valueOf(cb.isChecked()));
            }
        });

        String name = user.getString("first_name") + " " + user.getString("last_name");
        String profile = user.getString("picture");
        if(!TextUtils.isEmpty(profile)) {
            Picasso.with(getContext()).load(profile).into(viewHolder.ivProfile);
        }
        viewHolder.tvName.setText(name);
        viewHolder.cbInvite.setChecked(friendCheck.getSelected());
        viewHolder.cbInvite.setTag(friendCheck);

        return convertView;
    }
    static class ViewHolder{
        @BindView(R.id.ivProfile)
        ImageView ivProfile;
        @BindView(R.id.tvName)
        TextView  tvName;
        @BindView(R.id.cbInvite)
        CheckBox cbInvite;
        public ViewHolder(View view){
            ButterKnife.bind(this, view);
        }
    }
}

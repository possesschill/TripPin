package com.facebook.seagull.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.seagull.R;
import com.facebook.seagull.activities.HomeActivity;
import com.facebook.seagull.activities.ProfileActivity;
import com.facebook.seagull.activities.RouteDetailActivity;
import com.facebook.seagull.decorators.ItemClickSupport;
import com.facebook.seagull.models.Route;
import com.facebook.seagull.models.SectionRouteModel;

import java.util.ArrayList;

/**
 * Created by klimjinx on 7/26/16.
 */

public class RecyclerViewDataAdapter extends RecyclerView.Adapter<RecyclerViewDataAdapter.ItemRowHolder> {

    private ArrayList<SectionRouteModel> dataList;
    private Context mContext;

    public RecyclerViewDataAdapter(Context context, ArrayList<SectionRouteModel> dataList) {
        this.dataList = dataList;
        this.mContext = context;
    }

    @Override
    public ItemRowHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, null);
        ItemRowHolder mh = new ItemRowHolder(v);
        return mh;
    }

    @Override
    public void onBindViewHolder(ItemRowHolder itemRowHolder, int i) {

        final String sectionName = dataList.get(i).getHeaderTitle();

        final ArrayList singleSectionItems = dataList.get(i).getAllItemsInSection();

        itemRowHolder.tvTitle.setText(sectionName);

        RoutesArrayAdapter routeAdapter = new RoutesArrayAdapter(mContext, singleSectionItems, true);

        if (mContext instanceof ProfileActivity) {
            routeAdapter.setOnUserClickListener((ProfileActivity) mContext);
        } else {
            routeAdapter.setOnUserClickListener((HomeActivity) mContext);
        }

        ItemClickSupport.addTo(itemRowHolder.rvList).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Intent i = new Intent( mContext, RouteDetailActivity.class);
                        i.putExtra("route_id", ( (Route) singleSectionItems.get(position)).getObjectId().toString() );
                        mContext.startActivity(i);
                    }
                }
        );

//        itemRowHolder.rvList.setHasFixedSize(true);
        itemRowHolder.rvList.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

        itemRowHolder.rvList.setNestedScrollingEnabled(false);
        itemRowHolder.rvList.setAdapter(routeAdapter);

       /* Glide.with(mContext)
                .load(feedItem.getImageURL())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .error(R.drawable.bg)
                .into(feedListRowHolder.thumbView);*/
    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    public class ItemRowHolder extends RecyclerView.ViewHolder {

        protected TextView tvTitle;
        protected RecyclerView rvList;

        public ItemRowHolder(View view) {
            super(view);

            this.tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            this.rvList = (RecyclerView) view.findViewById(R.id.rvList);

        }
    }

}
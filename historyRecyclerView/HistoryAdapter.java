package com.example.mazharali.projectfyp.historyRecyclerView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mazharali.projectfyp.R;

import java.util.List;

/**
 * Created by Mazhar Ali on 03/10/2019.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolders> {

    private List<HistoryObject> listitem;
    private Context context;

    public HistoryAdapter (List<HistoryObject> listitem , Context context){
        this.listitem = listitem;
        this.context = context;
    }

    @Override
    public HistoryViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, null, false);
        RecyclerView.LayoutParams layoutParams =new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);
        HistoryViewHolders hvh= new HistoryViewHolders(layoutView);
        return hvh;
    }

    @Override
    public void onBindViewHolder(HistoryViewHolders holder, int position) {
        holder.serviceId.setText(listitem.get(position).getServiceId());

    }

    @Override
    public int getItemCount() {
        return listitem.size();
    }
}

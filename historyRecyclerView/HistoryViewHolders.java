package com.example.mazharali.projectfyp.historyRecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.BundleCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.mazharali.projectfyp.HistorySignleActivity;
import com.example.mazharali.projectfyp.R;


/**
 * Created by Mazhar Ali on 03/10/2019.
 */

public class HistoryViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView serviceId;
    public HistoryViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        serviceId = (TextView) itemView.findViewById(R.id.service_Id);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext() , HistorySignleActivity.class );
        Bundle bundle = new Bundle();
        bundle.getString("serviceId" , serviceId.getText().toString());
        intent.putExtras(bundle);
        view.getContext().startActivity(intent);

    }
}

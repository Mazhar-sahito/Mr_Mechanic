package com.example.mazharali.projectfyp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.mazharali.projectfyp.historyRecyclerView.HistoryAdapter;
import com.example.mazharali.projectfyp.historyRecyclerView.HistoryObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private String customerOrMechanic , userId;

    private RecyclerView historyRecyView;
    private RecyclerView.Adapter historyAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyRecyView = (RecyclerView)findViewById(R.id.historyRecyclerView);
        historyRecyView.setNestedScrollingEnabled(false);
        historyRecyView.setHasFixedSize(true);

        layoutManager= new LinearLayoutManager(HistoryActivity.this);
        historyRecyView.setLayoutManager(layoutManager);
        historyAdapter = new HistoryAdapter(getDataSetHistory() , HistoryActivity.this);
        historyRecyView.setAdapter(historyAdapter);

//        for (int i=0; i<100; i++) {
//            HistoryObject obj = new HistoryObject(Integer.toString(i));
//            resultHistory.add(obj);
//        }
//        historyAdapter.notifyDataSetChanged();

        customerOrMechanic = getIntent().getExtras().getString("customerOrMechanic");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistoryIds();

    }

    private void getUserHistoryIds() {
        DatabaseReference customerHistotyDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(customerOrMechanic)
                .child(userId).child("history");
        customerHistotyDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot history : dataSnapshot.getChildren()){
                        fetchServiceInfo(history.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchServiceInfo(String servicekey) {
        DatabaseReference histotyDatabaseRef = FirebaseDatabase.getInstance().getReference().child("history").child(servicekey);
        histotyDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String serviceId= dataSnapshot.getKey();
                    HistoryObject obj= new HistoryObject(serviceId);
                    resultHistory.add(obj);
                    historyAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private ArrayList resultHistory = new ArrayList<HistoryObject>();

    private ArrayList<HistoryObject> getDataSetHistory() {
        return resultHistory;
    }
}

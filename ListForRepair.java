package com.example.mazharali.projectfyp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ListForRepair extends AppCompatActivity {

    ListView listView;
    String [] listOfRepair= new String[]{"Tyre Puncture:  300 to 400 Rs", "Braking: 150 to 200 Rs", "Chain Break: 600 to 800 Rs",
            "Broken Cleat: 150 to 200 Rs", "Engine halting: ...", "Jerking at Speed: 300 to 400 Rs"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_for_repair);

        listView = (ListView)findViewById(R.id.list_item_rates);

        ArrayAdapter<String> arrayAdapter= new ArrayAdapter (this , R.layout.listofrepairing ,listOfRepair);
        listView.setAdapter(arrayAdapter);
    }
}

package com.rwizen.arqr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Adapter;

import java.io.File;

public class Models_List extends AppCompatActivity {
    Context context;
    public Boolean isScanned = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_models__list);

        RecyclerView models_list=findViewById(R.id.available_models_list);
        models_list.setLayoutManager(new LinearLayoutManager(this));


        /////////////////////////////////////////////////////////////////////DATA LIST //////////////////////////////////////////////////////////////////////////////////////////
        String[] pathnames;
        // Creates a new File instance by converting the given pathname string
        // into an abstract pathname
        File f = new File(Environment.getExternalStorageDirectory()+"/EduCore/Asset3D");
        // Populates the array with names of files and directories
        pathnames = f.list();
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        models_list.setAdapter(new Model_list_adapter(pathnames));
    }


}

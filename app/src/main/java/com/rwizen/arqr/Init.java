package com.rwizen.arqr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;

public class Init extends AppCompatActivity {
    Button start_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        RunPermissionCheck();
        CreateRootFileSystem();
        start_btn=findViewById(R.id.start_btn);

        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(getApplicationContext(),Scanner.class));
            }
        });
    }

    /*
        Function : RunPermissionCheck
        ReturnType : Void
        this function will check for the required permissions for the app. App will not function
        without these permissions. these permissions include.

        1 Internet Permissions
        2 Storage Permissions[Read and Write]
        3 Camera Permission
    */
    private void RunPermissionCheck() {
        String[] Permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };
        ActivityCompat.requestPermissions(this,Permissions , 1);
    }

    /*
        Function : CreateRootFileSystem
        ReturnType : Void
        UseCase : Creates directory for storing files
        Note : Given Project is build on API Level 29, File System Methods can change with other
        APIs, Hence, need to be checked while building for other API levels
    */
    private void CreateRootFileSystem(){
        String rootPath=this.getExternalFilesDir(null)+"/EduCore/Asset3D";
        File file=new File(rootPath);
        if(!file.exists()){
            file.mkdirs();
            Log.d("Init RFS","created");
        }
        Log.d("Init RFS","already present");
    }
}

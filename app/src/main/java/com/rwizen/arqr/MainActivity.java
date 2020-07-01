package com.rwizen.arqr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    Button start_btn;
    Button check_available_models_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /////////////////////////////////////////////PERMISSION STATUS////////////////////////////////////////////////////////////////////////////////////////////










        /////////////////////////////////////////////CREATING DIRECTORY STRUCTURE/////////////////////////////////////////////////////////////////////////////////

        String rootPath=Environment.getExternalStorageDirectory()+"/EduCore/Asset3D";
        File file=new File(rootPath);
        if(!file.exists()){
            file.mkdirs();
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        setContentView(R.layout.activity_main);
        start_btn=findViewById(R.id.start_btn);
        start_btn.setOnClickListener(new View.OnClickListener()
        { @Override
        public void onClick(View view)
        {
            startActivity(new Intent(getApplicationContext(),Scanner.class));
        }
        });

        check_available_models_btn=findViewById(R.id.available_models_btn);
        check_available_models_btn.setOnClickListener(new View.OnClickListener()
        { @Override
        public void onClick(View view)
        {
            startActivity(new Intent(getApplicationContext(),Models_List.class));
        }
        });



    }
}

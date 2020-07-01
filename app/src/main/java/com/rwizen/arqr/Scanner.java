package com.rwizen.arqr;

import androidx.appcompat.app.AppCompatActivity;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import android.content.Intent;
import android.os.Bundle;
import com.google.zxing.Result;

public class Scanner extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    ZXingScannerView ScannerView;
    String resultString;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScannerView=new ZXingScannerView( this);
        setContentView(ScannerView);

    }

    @Override
    public void handleResult(Result result) {
        //MainActivity.result_text.setText(result.getText());
        //onBackPressed();

        resultString=result.getText();
        Intent i = new Intent(this, AR_Fragment.class);
        i.putExtra("qr_result", resultString);
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ScannerView.setResultHandler(this);
        ScannerView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ScannerView.stopCamera();
    }
}


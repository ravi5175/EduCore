package com.rwizen.arqr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.Result;

import java.io.File;

public class Scanner extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView; // qr scanner
    private String resultString; // data extracted by qr scanner from code
    private FirebaseStorage storageRef; // personal firebase storage bucket reference
    private String firebaseStorageUri = "gs://educore-1046d.appspot.com"; // personal firebase storage bucket url
    private StorageReference eduCoreAssets; // personal firebase storage bucket asset directory reference
    private  String downloadDir; // app model storage path

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        scannerView = findViewById(R.id.qr_scanner);
        storageRef = FirebaseStorage.getInstance(firebaseStorageUri);
        eduCoreAssets = storageRef.getReference().child("EduCoreAssets");
        downloadDir = this.getExternalFilesDir(null).getAbsolutePath()+"/EduCore/Asset3D";
    }

    @Override
    public void handleResult(Result result) {
        resultString=result.getText();
        validateQR(resultString);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    /*
    * Function : validateQR(string qrCode)
    * ReturnType : Void
    * Parameters :
    * -> qrCode - string value extracted by qr scanner from code
    * UseCase : checks whether the qrCode is for educore or not, it searches the data over firebase
    *   cloud storage bucket.
    * */
    private void validateQR(String qrCode){
        final String qr = firebaseStorageUri+"/EduCoreAssets/"+qrCode;
        Log.d("storage","qr " + qr );
        eduCoreAssets.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>(){
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference prefix : listResult.getPrefixes()) {
                    if(qr.equals(prefix.toString())){
                        Log.d("storage","equal value found");
                        checkLocalReference(qrCode);
                    }
                }
                Log.d("storage","model not available on firebase storage");
                Toast.makeText(getApplicationContext(),"QR not belongs to EduCore",Toast.LENGTH_SHORT).show();
                onResume();

            }
        }).addOnFailureListener(new OnFailureListener(){
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("storage","something went wrong "+ e.toString());
            }
        });
    }

    /*
    * Function : checkLocalReference(String modelName)
    * ReturnType : Void
    * Parameters :
    * -> qrCode - string value extracted by qr scanner from code
    * UseCase : checks whether model needs to be downloaded or already available in local storage
    * */

    private void checkLocalReference(String qrCode){
        Log.d("storage",downloadDir+"");
        File modelDirectory=new File(downloadDir+"/"+qrCode);
        if(!modelDirectory.exists()){
            Log.d("storage","download required");
            arIntent(qrCode,true);
        }else{
            Log.d("storage","download available");
            arIntent(qrCode,false);
        }
    }

    /*
    * Function : arIntent(String qrCode,Boolean downloadRequired)
    * ReturnTYpe : Void
    * Parameters :
    *  -> qrCode - String value extracted by qr scanner from code
    *  -> downloadRequired - Boolean value, true if model is not available in default directory, False
    *       if model is already downloaded and stored in default directory
    * UseCase : Starts AR activity
    * */

    private void arIntent(String qrCode,Boolean downloadRequired){
        Intent i = new Intent(this, AR.class);
        i.putExtra("qr_code", qrCode);
        i.putExtra("download_required", downloadRequired);
        startActivity(i);
        finish();
    }
}


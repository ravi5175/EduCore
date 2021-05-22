package com.rwizen.arqr;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class AR extends AppCompatActivity {

    private ArFragment arFragment; // variable pointing ARFragment of Sceneform
    public String qrString; // string extracted from QRCode

    private  String localRef; // variable pointing to default location of 3d assets in phone physical storage

    public ImageView reScan_btn; // button to go back to scanning fragment
    public ProgressBar progress; // variable pointing to model processing progress bar

    public TextView modelName; // variable pointing to onscreen active model name
    public Boolean downloadRequired; // determines if model is available in local storage or needs download

    public String requiredAsset;// variable pointing to .gltf model file in physical memory for rendering

    private FirebaseStorage storageRef; // firebase storage bucket reference
    private StorageReference eduCoreAsset;// firebase storage bucket educore asset reference
    private String storageBucketUrl = "gs://educore-1046d.appspot.com"; // storage bucket public url

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        progress=findViewById(R.id.progress);
        modelName = findViewById(R.id.model_name);
        reScan_btn=findViewById(R.id.rescan);

        localRef = this.getExternalFilesDir(null)+"/EduCore/Asset3D/";

        qrString = getIntent().getStringExtra("qr_code");
        downloadRequired = getIntent().getBooleanExtra("download_required",false);

        storageRef = FirebaseStorage.getInstance(storageBucketUrl);
        eduCoreAsset = storageRef.getReference().child("EduCoreAssets/"+qrString);

        arFragment=(ArFragment)getSupportFragmentManager().findFragmentById(R.id.arFragment);
        arFragment.setOnTapArPlaneListener((hitResult,plane,motionEvent)->
        {
            progress.setVisibility(ProgressBar.VISIBLE);
            modelName.setVisibility(TextView.VISIBLE);

            if (downloadRequired) {
                Log.d("storageAR","download required");
                modelName.setText("downloading");
                downloadModel(qrString);
            }

            requiredAsset = localRef+qrString+"/"+qrString+".gltf";
            placeModel(hitResult.createAnchor(),requiredAsset);
            modelName.setText(qrString);
        });

        reScan_btn.setOnClickListener(
                view -> startActivity(new Intent(getApplicationContext(),Scanner.class)));

    }

    /* Function : placeMOde(Anchor anchor,String assetLocation)
    *  ReturnType : Void
    *  Parameters :
    *   -> anchor - reference to anchor in physical space
    *   -> assetLocation - reference to .gltf asset in local storage
    *  Use Case : Used to Place 3D Asset in physical World
    * */
    private void placeModel(Anchor anchor, String assetLocation)
    {
        Log.d("storageAR","placemodel is called");
        ModelRenderable
                .builder()
                .setSource(this,
                        RenderableSource
                        .builder()
                        .setSource(this, Uri.parse(assetLocation), RenderableSource.SourceType.GLTF2)
                        .setScale(0.5f)
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build()
                )
                .setRegistryId(localRef+qrString+"/")
                .build()
                .thenAccept(modelRenderable -> addModelToScene(anchor,modelRenderable))
                .exceptionally(throwable -> {
                    AlertDialog.Builder builder= new AlertDialog.Builder(this);
                    builder.setMessage(throwable.getMessage()).show();
                    return null;
                });
    }

    /* Function : placeMOde(Anchor anchor,String assetLocation)
     *  ReturnType : Void
     *  Parameters :
     *   -> anchor - reference to anchor in physical space
     *   -> assetLocation - reference to .gltf asset in physical memory
     *  Use Case : Used to Place 3D Asset in physical World
     * */
    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.setParent(anchorNode);
        transformableNode.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        transformableNode.select();
        progress.setVisibility(ProgressBar.INVISIBLE);
    }

    private void downloadModel(String qrCode){
        Log.d("storageAR",eduCoreAsset.toString());
        final long ONE_MEGABYTE = 1024 * 1024;
        File modelDirectory = new File(localRef+qrCode);
        if (!modelDirectory.exists()) modelDirectory.mkdirs();

        eduCoreAsset.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>(){
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference prefix : listResult.getItems()) {
                    String[] modelFile = prefix.toString().split("//")[1].split("/");
                    Log.d("storageAR", modelFile[3]);
                    File localFile = new File(modelDirectory, modelFile[3]);
                    if(!localFile.isFile()){
                        Log.d("storageAR","file not exists, creating one");
                        try {
                            if(localFile.createNewFile()){
                                Log.d("storageAR","file created");
                            }
                            Log.d("storageAR","file already present");
                        } catch (IOException e) {
                            Log.d("storageAR","file not created");
                        }
                    }else{
                        Log.d("storageAR","local file already present");
                    }

                    prefix.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Log.d("storageAR","file loaded in memory "+modelFile[3]);
                            try {
                                FileOutputStream out = new FileOutputStream(localFile,true);
                                out.write(bytes);
                                out.close();
                            } catch (IOException e) {
                                Log.d("storageAR","file out error" + e.toString());
                                e.printStackTrace();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener(){
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("storageAR","something went wrong "+ e.toString());
            }
        });


    }
}

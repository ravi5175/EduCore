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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.File;
import java.io.IOException;


public class AR extends AppCompatActivity {

    private ArFragment arFragment;
    private String ASSET = "http://http://daf6527506f5.ngrok.io/3DAssets";
    private String JSOUP_ASSET=ASSET;
    public String qrString;
    private int i = 0;
    private  String downlaodDir;

    ImageView reScan_btn;
    ProgressBar progress;
    public DownloadManager downloadManager;

    public TextView modelName;
    public Boolean downloadRequired;

    public String requiredAsset;

    private FirebaseStorage storageRef;
    private StorageReference eduCoreAsset;
    private String storageBucketUrl = "gs://educore-1046d.appspot.com";;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        progress=findViewById(R.id.progress);
        modelName = findViewById(R.id.model_name);
        reScan_btn=findViewById(R.id.rescan);

        downlaodDir = this.getExternalFilesDir(null).getAbsolutePath();

        qrString = getIntent().getStringExtra("qr_code");
        downloadRequired = getIntent().getBooleanExtra("download_required",false);

        ASSET= ASSET+"/"+qrString+"/"+qrString+".fbx";
        JSOUP_ASSET=JSOUP_ASSET+"/"+qrString+"/";

        storageRef = FirebaseStorage.getInstance(storageBucketUrl);
        eduCoreAsset = storageRef.getReference().child("EduCoreAssets/"+qrString);

        progress.setVisibility(ProgressBar.GONE);
        modelName.setVisibility(TextView.GONE);

        arFragment=(ArFragment)getSupportFragmentManager().findFragmentById(R.id.arFragment);

        arFragment.setOnTapArPlaneListener((hitResult,plane,motionEvent)->
        {
            progress.setVisibility(ProgressBar.VISIBLE);
            modelName.setVisibility(TextView.VISIBLE);

            if (downloadRequired) {
                modelName.setText("downloading");
                downloadModel(qrString);
                //new Jsoup_Scrap().execute();                                                                         //separate_thread
            }
            else {
                requiredAsset = downlaodDir+"/EduCore/Asset3D/"+qrString+"/"+qrString+".gltf";
            }
            //placeModel(hitResult.createAnchor());
            //modelName.setText(qrString);
        });


        reScan_btn.setOnClickListener(
                view -> startActivity(new Intent(getApplicationContext(),Scanner.class)));

    }

    private void placeModel(Anchor anchor)
    {
        ModelRenderable
                .builder()
                .setSource(this,
                        RenderableSource
                        .builder()
                        .setSource(this, Uri.parse(downlaodDir+"Asset3D/"+qrString+"/"+qrString+".gltf"), RenderableSource.SourceType.GLTF2)
                        .setScale(0.5f)
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build()
                )
                .setRegistryId(downlaodDir+"Asset3D/"+qrString+"/")
                .build()
                .thenAccept(modelRenderable -> addModelToScene(anchor,modelRenderable))
                .exceptionally(throwable -> {
                    AlertDialog.Builder builder= new AlertDialog.Builder(this);
                    builder.setMessage(throwable.getMessage()).show();
                    return null;
                });
    }

    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.setParent(anchorNode);
        transformableNode.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        //animateModel(modelRenderable);                                                                         // will be added in future
        transformableNode.select();
        progress.setVisibility(ProgressBar.INVISIBLE);
    }

    /*private void animateModel(ModelRenderable modelRenderable)
    {
        if (modelAnimator != null && modelAnimator.isRunning())
            modelAnimator.end();
        int animationCount = modelRenderable.getAnimationDataCount();
        if (i==animationCount)
            i=0;
        AnimationData animationData = modelRenderable.getAnimationData(i);
        modelAnimator=new ModelAnimator(animationData,modelRenderable);
        modelAnimator.start();
        i++;
    }*/

    public class Jsoup_Scrap extends AsyncTask<String,String,String>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
        @Override
        protected String doInBackground(String... strings)
        {
            String rootPath=Environment.getExternalStorageDirectory()+"/EduCore/Asset3D/"+qrString;
            File file=new File(rootPath);
            if(!file.exists()){
                file.mkdirs();
            }
            try
            {
                Document doc = Jsoup.connect(JSOUP_ASSET).get();
                Elements ref = doc.getElementsByTag("A");
                for (Element i : ref)
                {
                    String j = JSOUP_ASSET + i.attr("href");
                    String nameOfFile = URLUtil.guessFileName(j,null,MimeTypeMap.getFileExtensionFromUrl(j));
                    downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(j));
                    request.setDestinationUri(Uri.fromFile(new File(rootPath+"/",i.attr("href"))));
                    downloadManager.enqueue(request);
                }
            }
            catch (IOException e) { }
            return null;
        }
    }

    private void downloadModel(String qrCode){
        Log.d("storageAR",eduCoreAsset.toString());
        File modelDirectory = new File(downlaodDir+"/"+qrCode);
        eduCoreAsset.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>(){
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference prefix : listResult.getItems()) {
                    Log.d("storageAR",prefix.toString());
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

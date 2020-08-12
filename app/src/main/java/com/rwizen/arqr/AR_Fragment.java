package com.rwizen.arqr;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.animation.ModelAnimator;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.AnimationData;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.File;
import java.io.IOException;


public class AR_Fragment extends AppCompatActivity {
    private ArFragment arFragment;
    private String ASSET="http://http://daf6527506f5.ngrok.io/3DAssets";
    private String JSOUP_ASSET=ASSET;
    private ModelAnimator modelAnimator;
    public String qrString;
    private int i = 0;
    private  String downlaodDir = Environment.getExternalStorageDirectory()+"/EduCore/";
    ImageButton reScan_btn;
    ProgressBar progress;
    public DownloadManager downloadManager;
    public TextView modelName;
    public Boolean isScanned;
    public String scanned_model_name;
    public String Final_ASSET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        qrString = getIntent().getStringExtra("qr_result");
        isScanned = getIntent().getBooleanExtra("is_scanned",false);
        ASSET= ASSET+"/"+qrString+"/"+qrString+".fbx";
        JSOUP_ASSET=JSOUP_ASSET+"/"+qrString+"/";
        setContentView(R.layout.activity_ar__fragment);
        progress=findViewById(R.id.progress);
        progress.setVisibility(ProgressBar.GONE);
        modelName = findViewById(R.id.model_name);
        modelName.setVisibility(TextView.GONE);
        arFragment=(ArFragment)getSupportFragmentManager().findFragmentById(R.id.arFragment);
        arFragment.setOnTapArPlaneListener((hitResult,plane,motionEvent)->
        {
            progress.setVisibility(ProgressBar.VISIBLE);
            modelName.setVisibility(TextView.VISIBLE);
            if (isScanned == false) {
                new Jsoup_Scrap().execute();                                                                         //separate_thread
            }
            else
            {
                scanned_model_name=getIntent().getStringExtra("scanned_model_name");
                Final_ASSET=downlaodDir+"Asset3D/"+qrString+"/"+scanned_model_name+".gltf";
            }
            placeModel(hitResult.createAnchor());
            modelName.setText(qrString);
        });

        /////////////////////////////////////////////////////////////RESCAN BUTTON////////////////////////////////////////////////////////////////////////
        reScan_btn=findViewById(R.id.rescan);
        reScan_btn.setOnClickListener(new View.OnClickListener()
        { @Override
        public void onClick(View view)
        {
            startActivity(new Intent(getApplicationContext(),Scanner.class));

        }});
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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

    private void animateModel(ModelRenderable modelRenderable)
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
    }


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
            catch (IOException e)
            {

            }
            return null;
        }
    }
}

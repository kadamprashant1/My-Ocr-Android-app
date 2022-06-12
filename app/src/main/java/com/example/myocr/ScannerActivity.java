package com.example.myocr;


import static android.Manifest.permission.CAMERA;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class ScannerActivity extends AppCompatActivity {
   private ImageView captureIV;
   private TextView resultTV;
   private Button snapBtn, detectBtn;
   private Bitmap imageBitmap;
   static final int REQUEST_IMAGE_CAPTURE = 1;
    @SuppressLint({"CutPasteId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        captureIV = findViewById(R.id.idIVCaptureImage);
        resultTV = findViewById(R.id.idTVDetectedText);
        snapBtn = findViewById(R.id.idBtnSnap);
        detectBtn = findViewById(R.id.idBtnDetect);

        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectText();
            }
        });
        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              if(checkPermissions()){//if permission graneted the it will call to captureImage function...
                  captureImage();
              }else{
                  requestPermission();//other wise request for permission...
              }
            }
        });

    }
   private boolean checkPermissions(){
        int camerPermision = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        return  camerPermision == PackageManager.PERMISSION_GRANTED;// return the camera permission is granted
    }
    private void requestPermission(){
      int PERMISSION_CODE = 200;
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, PERMISSION_CODE);
    }
    private void captureImage(){
         Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         if(takePictureIntent.resolveActivity(getPackageManager())!=null){
             startActivityForResult( takePictureIntent, REQUEST_IMAGE_CAPTURE);
         }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if(cameraPermission){
                Toast.makeText(this, "Permission Granted...", Toast.LENGTH_SHORT).show();
                captureImage();
            }else{
                Toast.makeText(this, "Permission Denied...", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE  && resultCode == RESULT_OK){
            assert data != null;//added later.............!
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            captureIV.setImageBitmap(imageBitmap);
        }
    }
    private void detectText() {// detecting text code...
        InputImage image = InputImage.fromBitmap(imageBitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result;
        result = recognizer.process(image).addOnSuccessListener( new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                StringBuilder result = new StringBuilder();
                for(Text.TextBlock block: text.getTextBlocks()){
                    String blockText =  block.getText();
                    Point[] blockCornerPoint = block.getCornerPoints();
                    Rect  blockFrame = block.getBoundingBox();
                    for(Text.Line line :  block.getLines()){
                        String LineText = line.getText();
                        Point[] lineCornerPoint = line.getCornerPoints();
                        Rect linRect = line.getBoundingBox();
                         for (Text.Element element: line.getElements()){
                             String elementText = element.getText();
                             result.append(elementText);
                         }
                         resultTV.setText(blockText);
                    }

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {   //if fails to detect exception...
                Toast.makeText(ScannerActivity.this, "Fail to Detect Text From Image..."+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } );
    }
}

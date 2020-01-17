package com.example.ejemplo;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.TextAnnotation;


import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    public Vision vision;
    private static String APP_DIRECTORY = "MyPictureApp/";
    private static String MEDIA_DIRECTORY = APP_DIRECTORY + "PictureApp";

    private final int MY_PERMISSIONS = 100;
    private final int PHOTO_CODE = 200;
    private final int SELECT_PICTURE = 300;

    private ImageView mSetImage;
    private Button mOptionButton;
    private RelativeLayout mRlView;

    private String mPath;

    private static final int RQS_LOADIMAGE = 1;
    private Button btnLoad, btnDetFace;
    private ImageView imgView;
    private Bitmap myBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSetImage = (ImageView) findViewById(R.id.set_picture);
        mOptionButton = (Button) findViewById(R.id.show_cargar);
        mRlView = (RelativeLayout) findViewById(R.id.rl_view);


        if(mayRequestStoragePermission())
            mOptionButton.setEnabled(true);
        else
            mOptionButton.setEnabled(false);


        mOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptions();
            }
        });

        Vision.Builder visionBuilder = new Vision.Builder(new NetHttpTransport(),
                new AndroidJsonFactory(), null);
        visionBuilder.setVisionRequestInitializer(new VisionRequestInitializer("AIzaSyCRixVuaHhDZ6HEnH1XuwX1dMs6LjIpSOM"));
        vision = visionBuilder.build();

    }

    public void botonClick(View view){

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ImageView imagen=(ImageView) findViewById(R.id.set_picture);
                BitmapDrawable drawable = (BitmapDrawable) imagen.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                ImageView myImageView = (ImageView) findViewById(R.id.set_picture);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable=true;


                bitmap = scaleBitmapDown(bitmap,1200 );
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                byte[] imageInByte = stream.toByteArray();


                //paso 1
                Image inputImage = new Image();
                inputImage.encodeContent(imageInByte);

                //paso 2 Feature

                Feature desiredFeature = new Feature();
                desiredFeature.setType("TEXT_DETECTION");


                // paso 3 arma la sulicitud(es)
                AnnotateImageRequest request = new AnnotateImageRequest();
                request.setImage(inputImage);
                request.setFeatures(Arrays.asList(desiredFeature));
                BatchAnnotateImagesRequest batchRequest = new
                        BatchAnnotateImagesRequest();
                batchRequest.setRequests(Arrays.asList(request));

                //paso 4 asignamos al control visionbuilder la solicitud

                try {
                    Vision.Images.Annotate  annotateRequest =
                            vision.images().annotate(batchRequest);

                    //paso 5 enviamos la solicitud
                    annotateRequest.setDisableGZipContent(true);
                    BatchAnnotateImagesResponse batchResponse =
                            annotateRequest.execute();



                    //paso 6 obtener la respuesta
                    TextAnnotation text = batchResponse.getResponses().get(0).getFullTextAnnotation();
                    final  String result =text.getText();



                    //paso 7 asignar la UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView imageDetail = (TextView)findViewById(R.id.textView2);
                            imageDetail.setText(result);
                        }
                    });
                    //return text.getText();

                } catch (IOException e) {
                    e.printStackTrace();
                }




            }
        });
    }
    public void botonClickDetetion(View view){

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {


                ImageView imagen=(ImageView) findViewById(R.id.set_picture);
                BitmapDrawable drawable = (BitmapDrawable) imagen.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                ImageView myImageView = (ImageView) findViewById(R.id.set_picture);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable=true;


                bitmap = scaleBitmapDown(bitmap,1200 );
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                byte[] imageInByte = stream.toByteArray();


                //paso 1
                Image inputImage = new Image();
                inputImage.encodeContent(imageInByte);

                //paso 2 Feature


                Feature desiredFaces = new Feature();
                desiredFaces.setType("LABEL_DETECTION");


                // paso 3 arma la sulicitud(es)


                AnnotateImageRequest request = new AnnotateImageRequest();
                request.setImage(inputImage);
                request.setFeatures(Arrays.asList(desiredFaces));
                BatchAnnotateImagesRequest batchRequest = new
                        BatchAnnotateImagesRequest();
                batchRequest.setRequests(Arrays.asList(request));

                //paso 4 asignamos al control visionbuilder la solicitud

                try {
                    Vision.Images.Annotate  annotateRequest =
                            vision.images().annotate(batchRequest);

                    //paso 5 enviamos la solicitud
                    annotateRequest.setDisableGZipContent(true);
                    BatchAnnotateImagesResponse batchResponse =
                            annotateRequest.execute();


                    //paso 6 obtener la respuesta


                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    AnnotateImageResponse imageResponses = response.getResponses().get(0);
                    List<EntityAnnotation> entityAnnotations;
                    entityAnnotations = imageResponses.getLabelAnnotations();
                    String message=null;
                    if (entityAnnotations != null) {
                        for (EntityAnnotation entity : entityAnnotations) {
                            message = message + "    " + entity.getDescription() + " " + entity.getScore();
                            message += "\n";
                        }
                    } else {
                        message = "Nothing Found";
                    }
                    TextView imageDetail = (TextView)findViewById(R.id.textView2);
                    imageDetail.setText(message);


                    //paso 7 asignar la UI

                    //return text.getText();

                } catch (IOException e) {
                    e.printStackTrace();
                }




            }
        });
    }
    public void botonClickFaces(View view){

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                ImageView imagen=(ImageView) findViewById(R.id.set_picture);
                BitmapDrawable drawable = (BitmapDrawable) imagen.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                ImageView myImageView = (ImageView) findViewById(R.id.set_picture);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable=true;


                bitmap = scaleBitmapDown(bitmap,1200 );
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                byte[] imageInByte = stream.toByteArray();


                //paso 1
                Image inputImage = new Image();
                inputImage.encodeContent(imageInByte);

                //paso 2 Feature


                Feature desiredFaces = new Feature();
                desiredFaces.setType("FACE_DETECTION");


                // paso 3 arma la sulicitud(es)

                AnnotateImageRequest request = new AnnotateImageRequest();
                request.setImage(inputImage);
                request.setFeatures(Arrays.asList(desiredFaces));

                BatchAnnotateImagesRequest batchRequest = new
                        BatchAnnotateImagesRequest();
                batchRequest.setRequests(Arrays.asList(request));


                //paso 4 asignamos al control visionbuilder la solicitud

                try {
                    BatchAnnotateImagesResponse batchResponse =
                            vision.images().annotate(batchRequest).execute();

                    //paso 5 enviamos la solicitud
                    List<FaceAnnotation> faces = batchResponse.getResponses()
                            .get(0).getFaceAnnotations();


                    //paso 6 obtener la respuesta


                    // Count faces
                    int numberOfFaces = faces.size();

// Get joy likelihood for each face
                    String likelihoods = "";
                    for(int i=0; i<numberOfFaces; i++) {
                        likelihoods += "\n It is " +
                                faces.get(i).getJoyLikelihood() +
                                " that face " + i + " is happy";
                    }

// Concatenate everything
                    final String message =
                            "This photo has " + numberOfFaces + " faces" + likelihoods;

// Display toast on UI thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView imageDetail = (TextView)findViewById(R.id.textView2);
                            imageDetail.setText(message);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;
        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }


    private boolean mayRequestStoragePermission() {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        if((checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED))
            return true;

        if((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) || (shouldShowRequestPermissionRationale(CAMERA))){
            Snackbar.make(mRlView, "Los permisos son necesarios para poder usar la aplicación",
                    Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);
                }
            });
        }else{
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);
        }

        return false;
    }

    private void showOptions() {
        final CharSequence[] option = {"Tomar foto", "Elegir de galeria", "Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Eleige una opción");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(option[which] == "Tomar foto"){
                    openCamera();
                }else if(option[which] == "Elegir de galeria"){
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent, "Selecciona app de imagen"), SELECT_PICTURE);
                }else {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }

    private void openCamera() {
        File file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
        boolean isDirectoryCreated = file.exists();

        if(!isDirectoryCreated)
            isDirectoryCreated = file.mkdirs();

        if(isDirectoryCreated){
            Long timestamp = System.currentTimeMillis() / 1000;
            String imageName = timestamp.toString() + ".jpg";

            mPath = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY
                    + File.separator + imageName;

            File newFile = new File(mPath);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
            startActivityForResult(intent, PHOTO_CODE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("file_path", mPath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mPath = savedInstanceState.getString("file_path");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            switch (requestCode){
                case PHOTO_CODE:
                    MediaScannerConnection.scanFile(this,
                            new String[]{mPath}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ExternalStorage", "Scanned " + path + ":");
                                    Log.i("ExternalStorage", "-> Uri = " + uri);
                                }
                            });


                    Bitmap bitmap = BitmapFactory.decodeFile(mPath);
                    mSetImage.setImageBitmap(bitmap);
                    break;
                case SELECT_PICTURE:
                    Uri path = data.getData();
                    mSetImage.setImageURI(path);
                    break;

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == MY_PERMISSIONS){
            if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(MainActivity.this, "Permisos aceptados", Toast.LENGTH_SHORT).show();
                mOptionButton.setEnabled(true);
            }
        }else{
            showExplanation();
        }
    }

    private void showExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Permisos denegados");
        builder.setMessage("Para usar las funciones de la app necesitas aceptar los permisos");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        builder.show();
    }
}


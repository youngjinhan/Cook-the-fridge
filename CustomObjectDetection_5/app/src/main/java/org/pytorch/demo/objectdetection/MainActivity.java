// Copyright (c) 2020 Facebook, Inc. and its affiliates.
// All rights reserved.
//
// This source code is licensed under the BSD-style license found in the
// LICENSE file in the root directory of this source tree.

package org.pytorch.demo.objectdetection;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.PyTorchAndroid;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Runnable {
    private int mImageIndex = 0;
    private String[] mTestImages = {"test1.jpg", "test2.jpg", "test3.jpg"};

    private ImageView mImageView;
    private ResultView mResultView;
    private Button mButtonDetect;
    private TextView tv_class;
    private Button btn_email;
    private Button btn_info;
    private Button btn_recipe;
    private Button btn_user;
    private Button btn_next;

    private long backBtnTime = 0;
    //private int ingredient;

    private ProgressBar mProgressBar;
    private Bitmap mBitmap = null;
    private Module mModule = null;
    private float mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY;
    private DrawerLayout drawerLayout;
    private View drawerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }

        setContentView(R.layout.activity_main);
        try {
            mBitmap = BitmapFactory.decodeStream(getAssets().open(mTestImages[mImageIndex]));
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }


        //네비게이션 메뉴
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerView = (View)findViewById(R.id.drawer);

        ImageView btn_menu = (ImageView)findViewById(R.id.btn_open);
        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawerView);
            }
        });

        Button btn_close = (Button)findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
            }
        });



        // 이용자 정보 버튼을 눌렀을 때 toast massage
        btn_user = findViewById(R.id.btn_user);
        btn_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "아직 제공되지 않는 기능입니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 네비게이션 - 레시피 버튼을 눌렀을 때 화면전환
        btn_recipe = findViewById(R.id.btn_recipe);
        btn_recipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SubActivity3.class);
                startActivity(intent); //액티비티 이동
            }
        });

        //네비게이션 - 이메일 버튼을 눌렀을때 화면전환
        btn_email = findViewById(R.id.btn_email);
        btn_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SubActivity1.class);
                startActivity(intent); //액티비티 이동
            }
        });

        // 네비게이션 - 공지사항 버튼을 눌렀을 때 화면전환
        btn_info = findViewById(R.id.btn_info);
        btn_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SubActivity2.class);
                startActivity(intent);
            }
        });

        drawerLayout.setDrawerListener(listener);
        drawerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        try {
            mBitmap = BitmapFactory.decodeStream(getAssets().open(mTestImages[mImageIndex]));
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }

        mImageView = findViewById(R.id.imageView);
        mImageView.setImageBitmap(mBitmap);
        mResultView = findViewById(R.id.resultView);
        mResultView.setVisibility(View.INVISIBLE);

        final Button buttonTest = findViewById(R.id.testButton);
        buttonTest.setText(("Test Image 1/3"));
        buttonTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mResultView.setVisibility(View.INVISIBLE);
                mImageIndex = (mImageIndex + 1) % mTestImages.length;
                buttonTest.setText(String.format("Text Image %d/%d", mImageIndex + 1, mTestImages.length));

                try {
                    mBitmap = BitmapFactory.decodeStream(getAssets().open(mTestImages[mImageIndex]));
                    mImageView.setImageBitmap(mBitmap);
                } catch (IOException e) {
                    Log.e("Object Detection", "Error reading assets", e);
                    finish();
                }
            }
        });


        final Button buttonSelect = findViewById(R.id.selectButton);
        buttonSelect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mResultView.setVisibility(View.INVISIBLE);

                final CharSequence[] options = { "사진촬영", "사진첩에서 선택", "취소" };
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("이미지 선택");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("사진촬영")) {
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 0);
                        }
                        else if (options[item].equals("사진첩에서 선택")) {
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto , 1);
                        }
                        else if (options[item].equals("취소")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        final Button buttonLive = findViewById(R.id.liveButton);
        buttonLive.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              final Intent intent = new Intent(MainActivity.this, ObjectDetectionActivity.class);
              startActivity(intent);
            }
        });

        tv_class = findViewById(R.id.tv_class);
        btn_next = findViewById(R.id.btn_next);
        mButtonDetect = findViewById(R.id.detectButton);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mButtonDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_class.setText("");

                mButtonDetect.setEnabled(false);
                mProgressBar.setVisibility(ProgressBar.VISIBLE);
                mButtonDetect.setText(getString(R.string.run_model));

                mImgScaleX = (float)mBitmap.getWidth() / PrePostProcessor.mInputWidth;
                mImgScaleY = (float)mBitmap.getHeight() / PrePostProcessor.mInputHeight;

                mIvScaleX = (mBitmap.getWidth() > mBitmap.getHeight() ? (float)mImageView.getWidth() / mBitmap.getWidth() : (float)mImageView.getHeight() / mBitmap.getHeight());
                mIvScaleY  = (mBitmap.getHeight() > mBitmap.getWidth() ? (float)mImageView.getHeight() / mBitmap.getHeight() : (float)mImageView.getWidth() / mBitmap.getWidth());

                mStartX = (mImageView.getWidth() - mIvScaleX * mBitmap.getWidth())/2;
                mStartY = (mImageView.getHeight() -  mIvScaleY * mBitmap.getHeight())/2;

                Thread thread = new Thread(MainActivity.this);
                thread.start();
                btn_next.setVisibility(View.VISIBLE);

            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("class", tv_class.getText().toString());
                startActivity(intent);
                tv_class.setText("");
                btn_next.setVisibility(View.GONE);
            }
        });

        try {
            mModule = PyTorchAndroid.loadModuleFromAsset(getAssets(), "last.torchscript.pt");
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("classes.txt")));
            String line;
            List<String> classes = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                classes.add(line);
            }
            PrePostProcessor.mClasses = new String[classes.size()];
            classes.toArray(PrePostProcessor.mClasses);
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawers();
        }
        else{
            long curTime = System.currentTimeMillis();
            long gapTime = curTime - backBtnTime;

            if(0 <= gapTime && 2000 >= gapTime){
                super.onBackPressed();
            } else{
                backBtnTime = curTime;
                Toast.makeText(this, "뒤로가기를 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
            }

        }
    }

    DrawerLayout.DrawerListener listener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        mBitmap = (Bitmap) data.getExtras().get("data");
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90.0f);
                        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
                        mImageView.setImageBitmap(mBitmap);
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                mBitmap = BitmapFactory.decodeFile(picturePath);
                                Matrix matrix = new Matrix();
                                matrix.postRotate(90.0f);
                                mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
                                mImageView.setImageBitmap(mBitmap);
                                cursor.close();
                            }
                        }
                    }
                    break;
            }
        }
    }



    @Override
    public void run() {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(mBitmap, PrePostProcessor.mInputWidth, PrePostProcessor.mInputHeight, true);
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap, PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);
        IValue[] outputTuple = mModule.forward(IValue.from(inputTensor)).toTuple();
        final Tensor outputTensor = outputTuple[0].toTensor();
        final float[] outputs = outputTensor.getDataAsFloatArray();
        final ArrayList<Result> results =  PrePostProcessor.outputsToNMSPredictions(outputs, mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY);

        //새로 작성한 코드 : 라벨을 불러오는 부분
        //ingredient = PrePostProcessor.a;
        tv_class.setText(PrePostProcessor.mClasses[PrePostProcessor.a]);

        runOnUiThread(() -> {
            mButtonDetect.setEnabled(true);
            mButtonDetect.setText(getString(R.string.detect));
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            mResultView.setResults(results);
            mResultView.invalidate();
            mResultView.setVisibility(View.VISIBLE);

        });
    }
}

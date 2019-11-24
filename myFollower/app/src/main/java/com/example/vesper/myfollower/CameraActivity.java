package com.example.vesper.myfollower;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;


public class CameraActivity extends AppCompatActivity implements StepSenorBase.StepCallBack, OrientSensor.OrientCallBack {

    private SurfaceView surfaceView;
    private android.hardware.Camera camera;
   protected boolean isPreview = false; //摄像区域是否准备良好
    private SurfaceHolder surfaceHolder;
    private Button photoButton;  //拍照按钮
    ///////////////////////////////////////////////////////
    protected static TextView mSteptogo;;//显示服务器返回的步数
    ///////////////////////////////////////////////////////
    private String tag ="MaHaochen_______CameraActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        initCamera();
        initViews();
        ////////////////////////////////////////////////////////////
        mStepText = (TextView) findViewById(R.id.step_text);
        mOrientText = (TextView) findViewById(R.id.orient_text);
        mSteptogo=(TextView)findViewById(R.id.stepstogo);
        // 注册计步监听
//        mStepSensor = new StepSensorPedometer(this, this);
//        if (!mStepSensor.registerStep()) {
        mStepSensor = new StepSensorAcceleration(this, this);
        if (!mStepSensor.registerStep()) {
            Toast.makeText(this, "计步功能不可用！", Toast.LENGTH_SHORT).show();
        }
//        }
        // 注册方向监听
        mOrientSensor = new OrientSensor(this, this);
        if (!mOrientSensor.registerOrient()) {
            Toast.makeText(this, "方向功能不可用！", Toast.LENGTH_SHORT).show();
        }
        ////////////////////////////////////////////////////////////
    }
    //初始化摄像头
    private void initCamera(){
        surfaceView=(SurfaceView)findViewById(R.id.camera_surfaceview);
        SurfaceHolder cameraSurfaceHolder=surfaceView.getHolder();
        cameraSurfaceHolder.addCallback(new SurfaceHolder.Callback(){
            public void surfaceCreated(SurfaceHolder holder){
                try{
                    //启动相机
                    camera= android.hardware.Camera.open();
                    camera.setDisplayOrientation(90);
                    //设置parames
                    android.hardware.Camera.Parameters parameters=camera.getParameters();
                    //parameters.setPreviewFrameRate(5);//每秒5帧
                    parameters.setPictureFormat(ImageFormat.JPEG);//设置照片的输出格式
                    parameters.set("jpeg-quality",15);//照片质量
                    camera.setParameters(parameters);
                    camera.setPreviewDisplay(holder);
                    isPreview=true;
                    camera.startPreview();
                }catch (IOException e){
                    e.printStackTrace();
                }
                surfaceHolder=holder;
            }

            public void surfaceChanged(SurfaceHolder holder,int format,int width,int height){
                surfaceHolder=holder;
            }

            public void surfaceDestroyed(SurfaceHolder holder){
                if(camera!=null){
                    if(isPreview){
                        camera.stopPreview();
                        isPreview=false;
                    }
                    camera.release();
                    camera=null;//记得释放Camera
                }
                surfaceView=null;
                surfaceHolder=null;
            }
        });
        //开发时建议设置
        //This method was deprecated in API level 11. this is ignored, this value is set automatically when needed.
       // cameraSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void initViews(){
        photoButton = (Button) findViewById(R.id.camera_photo);
        ButtonOnClickListener onClickListener=new ButtonOnClickListener();
        photoButton.setOnClickListener(onClickListener);

    }

    class ButtonOnClickListener implements View.OnClickListener{
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.camera_photo:
                    try {
                        camera = Camera.open();
                        Camera.Parameters parameters = camera.getParameters();
                        camera.setDisplayOrientation(90);
//						parameters.setPreviewFrameRate(5); // 每秒5帧
                        parameters.setPictureFormat(ImageFormat.JPEG);// 设置照片的输出格式
                        parameters.set("jpeg-quality", 15);// 照片质量
                        camera.setParameters(parameters);
                        camera.setPreviewDisplay(surfaceHolder);
                        camera.startPreview();
                        isPreview = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
            if (camera != null) {
                camera.autoFocus(null);
                camera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        new SavePictureTask().execute(data);
                        camera.startPreview();
                        Log.e(tag, "=====拍照成功=====");

                    }
                }); // 拍照
            }

        }
        };
    class SavePictureTask extends AsyncTask<byte[], String, String> {
        @Override
        protected String doInBackground(byte[]... params) {
            String path = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/mahc/image";
            File out = new File(path);
            if (!out.exists()) {
                out.mkdirs();
            }
            File picture = new File(path+"/"+"1.jpg");
            try {
                FileOutputStream fos = new FileOutputStream(picture.getPath());
                fos.write(params[0]);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e(tag, "=====照片保存完成=====");
            /////////////////////////////////////////////////////////保存完了再上传
            File f=new File(Environment.getExternalStorageDirectory().getPath()+"/mahc/image/1.jpg");
            try {
                ImageUpload.run(f);
                //System.out.println("@@@@@@@@@@@@@@@@@");
            } catch (Exception e) {
                e.printStackTrace();
            }
           // mSteptogo.setText("togo:"+ImageUpload.ssss);
            //CameraActivity.this.finish();
            return null;
        }
    }
  ///////////////////////////////////////////////////////////////////////////////////////
    private TextView mStepText;
    private TextView mOrientText;
    private StepSenorBase mStepSensor; // 计步传感器
    private OrientSensor mOrientSensor; // 方向传感器
    public void Step(int stepNum) {

        //  计步回调
        mStepText.setText("步数:" + stepNum);
    }

    public void Orient(int orient) {
        // 方向回调
        mOrientText.setText("方向:" + orient);
//        获取手机转动停止后的方向
//        orient = SensorUtil.getInstance().getRotateEndOrient(orient);
    }

    protected static Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 111:
                    //在这里可以进行UI操作
                    mSteptogo.setText("togo:"+ImageUpload.ssss);
                    break;
                default:
                    break;
            }
        }
    };

    }
    /////////////////////////////////////////////////////////////////////////////////












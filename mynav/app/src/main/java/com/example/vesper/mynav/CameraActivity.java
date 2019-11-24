package com.example.vesper.mynav;

import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;


public class CameraActivity extends AppCompatActivity implements StepSenorBase.StepCallBack, OrientSensor.OrientCallBack {


private File mRecVideoPath;
private SurfaceView surfaceView;
private android.hardware.Camera camera;
protected boolean isPreview = false; //摄像区域是否准备良好
    private boolean isRecording = true; // true表示没有录像，点击开始；false表示正在录像，点击暂停
    private SurfaceHolder surfaceHolder;
    private MediaRecorder mediaRecorder;
    private TextView timeTextView;
    private Button videoButton;
    private int hour = 0;
    private int minute = 0;  //计时专用
    private int second = 0;//改成公用  在计步里prestep里用
    /////////////////////////////////////////////////////////
    private int secondcopy=second;
    private int timexiabiao=0;//时间下标
    private int[] timesstep=new int[20];
    ///////////////////////////////////////////////////////
    private boolean bool;//???//？？？？？
    private String tag ="MaHaochen_______CameraActivity";
    private File mRecAudioFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        initCamera();
        initViews();
        ////////////////////////////////////////////////////////////
        mStepText = (TextView) findViewById(R.id.step_text);
        mOrientText = (TextView) findViewById(R.id.orient_text);
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
        //创建存储路径
        mRecVideoPath=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/mahc/video/temp/");
        //如果路径存在，创建文件夹
        if(!mRecVideoPath.exists()){
            mRecVideoPath.mkdirs();
        }
        //应该是视频输出屏幕
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
                    parameters.setPreviewFrameRate(5);//每秒5帧
                    parameters.setPictureFormat(ImageFormat.JPEG);//设置照片的输出格式
                    parameters.set("jpeg-quality",85);//照片质量
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
                mediaRecorder=null;
            }
        });
        //开发时建议设置
        //This method was deprecated in API level 11. this is ignored, this value is set automatically when needed.
        cameraSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void initViews(){
        timeTextView=(TextView)findViewById(R.id.camera_time);
        timeTextView.setVisibility(View.GONE);
        videoButton=findViewById(R.id.camera_video);
        ButtonOnClickListener onClickListener=new ButtonOnClickListener();
        videoButton.setOnClickListener(onClickListener);
    }

    class ButtonOnClickListener implements View.OnClickListener{
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.camera_video:
                    //点击开始录像
                    if (isRecording) {
                        if (isPreview) {
                            camera.stopPreview();
                            camera.release();
                            camera = null;
                        }
                        second = 0;
                        minute = 0;
                        hour = 0;
                        bool = true;
                        //////////////////////////////////////////
                        if (null == mediaRecorder) {
                            mediaRecorder = new MediaRecorder();
                        } else {
                            mediaRecorder.reset();
                        }
                        /////////////////////////////////////////
                       // camera.setDisplayOrientation(90);

                        android.hardware.Camera c = android.hardware.Camera.open();
                        c.setDisplayOrientation(90);
                        c.unlock();
                        mediaRecorder.setCamera(c);
                        mediaRecorder.setOrientationHint(90);
                        //start实现录像静音
                       // mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                        //表面设置显示记录媒体（视频）的预览
                        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
                        //开始捕捉和编码数据到setOutputFile（指定的文件）
                        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                        //设置用于录制的音源
                        mediaRecorder.setVideoEncodingBitRate(5*1024*1024);
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        //设置在录制过程中产生的输出文件的格式
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        //设置视频编码器，用于录制
                        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                        //设置audio的编码格式
                        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        //设置要捕获的视频的宽度和高度
                        mediaRecorder.setVideoSize(320, 240);
                        // 设置要捕获的视频帧速率
                        mediaRecorder.setVideoFrameRate(15);
                        try {
                            mRecAudioFile = File.createTempFile("Video", ".3gp", mRecVideoPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mediaRecorder.setOutputFile(mRecAudioFile.getAbsolutePath());
                        try {
                            mediaRecorder.prepare();
                            timeTextView.setVisibility(View.VISIBLE);
                            handler.postDelayed(task, 1000);
                            mediaRecorder.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        isRecording = !isRecording;
                        Log.e(tag, "=====开始录制视频=====");
                    } else {
                        //点击停止录像
                        bool = false;
                        mediaRecorder.stop();
                        timeTextView.setText(FormatUtil.format(hour) + ":" + FormatUtil.format(minute) + ":" + FormatUtil.format(second));
                        mediaRecorder.release();
                        mediaRecorder = null;
                        FormatUtil.videoRename(mRecAudioFile);
                        Log.e(tag, "=====录制完成，已保存=====");
                        Log.e(tag, "存了几个"+timexiabiao);
//                        for(int i=0;i<=timexiabiao;i++)
//                        {
//                            System.out.println(timesstep[timexiabiao]);
//                        }
                        //////////////////////////////////////////////
                        File file =new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/mahc/video/temp/2.txt");
                        Writer out = null;
                        try {
                            out = new FileWriter(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            for(int j=0;j<timexiabiao;j++){
                                out.write(timesstep[j]+"\t");
                            }
                            out.write("\r\n");
                            out.write("=======================");
                            out.write(secondcopy);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ////////////////////////////////////////////////////
                        isRecording = !isRecording;
//                        try {
//                            camera = android.hardware.Camera.open();
//                            android.hardware.Camera.Parameters parameters = camera.getParameters();
////      parameters.setPreviewFrameRate(5); // 每秒5帧
//                            parameters.setPictureFormat(ImageFormat.JPEG);// 设置照片的输出格式
//                            parameters.set("jpeg-quality", 85);// 照片质量
//                            camera.setParameters(parameters);
//                            camera.setPreviewDisplay(surfaceHolder);
//                            camera.startPreview();
//                            isPreview = true;
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                    }
                    break;
            }
        };

        /*
         * 定时器设置，实现计时
         */
        private Handler handler = new Handler();
        private Runnable task = new Runnable() {
            public void run() {
                if (bool) {
                    handler.postDelayed(this, 1000);
                    second++;
                    secondcopy=second;
                    if(secondcopy%2==0)
                    {
                        timesstep[timexiabiao]=StepSenorBase.CURRENT_SETP;
                        timexiabiao++;
                    }
                    if (second >= 60) {
                        minute++;
                        second = second % 60;
                    }
                    if (minute >= 60) {
                        hour++;
                        minute = minute % 60;
                    }
                    timeTextView.setText(FormatUtil.format(hour) + ":" + FormatUtil.format(minute) + ":"
                            + FormatUtil.format(second));
                }
            }
        };

    }
    /////////////////////////////////////////////////////////////////////////////////

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





}


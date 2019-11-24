package com.example.vesper.myfollower;

import android.os.Environment;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.vesper.myfollower.CameraActivity.mSteptogo;

public class ImageUpload{
    private static final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");
    private static final OkHttpClient client = new OkHttpClient();
    /////////////////////////////////////////////
    protected static String ssss;
    //////////////////////////////////////////
    public static void run(File f) throws Exception {
        final File file=f;
        new Thread() {
            @Override
            public void run() {
                //子线程需要做的工作
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", Environment.getExternalStorageDirectory().getPath()+"/mahc/image/1.jpg",
                                RequestBody.create(MEDIA_TYPE_JPG, file))
                        .build();
                //设置为自己的ip地址
                Request request = new Request.Builder()
                        .url("http://192.168.166.105:5000/upload")
                        .post(requestBody)
                        .build();
                try(Response response = client.newCall(request).execute()){
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    ssss=response.body().string();
                    System.out.println(ssss);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ///////////////////////////////////////////
                Message message=new Message();
                message.what=111;
                //然后将消息发送出去
                CameraActivity.handler.sendMessage(message);
            }

        }.start();


    }
}


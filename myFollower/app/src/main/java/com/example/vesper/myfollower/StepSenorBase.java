package com.example.vesper.myfollower;
import android.content.Context;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public abstract class StepSenorBase  implements SensorEventListener {
    private Context context;
    protected StepCallBack stepCallBack;
    protected SensorManager sensorManager;
    protected static int CURRENT_SETP = 0;
    protected boolean isAvailable = false;

    public StepSenorBase(Context context, StepCallBack stepCallBack) {
        this.context = context;
        this.stepCallBack = stepCallBack;
    }

    public interface StepCallBack {
        /**
         * 计步回调
         */
        void Step(int stepNum);
    }

    /**
     * 开启计步
     */
    public boolean registerStep() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            sensorManager = null;
        }
//        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorManager = SensorUtil.getInstance().getSensorManager(context);
        registerStepListener();
        return isAvailable;
    }

    /**
     * 注册计步监听器
     */
    protected abstract void registerStepListener();

    /**
     * 注销计步监听器
     */
    public abstract void unregisterStep();
}


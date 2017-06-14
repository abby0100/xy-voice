package com.baidu.android.voicedemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.recognizerdemo.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ActivityWakeUp extends Activity {
    private static final String TAG = "ActivityWakeUp";
    private TextView txtResult;
    private TextView txtLog;

    private final String DESC_TEXT = "" +
            "唤醒已经启动(首次使用需要联网授权)\n" +
            "如果无法正常使用请检查:\n" +
            " 1. 是否在AndroidManifest.xml配置了APP_ID\n" +
            " 2. 是否在开放平台对应应用绑定了包名\n" +
            "\n";

    private EventManager mWpEventManager;
	private WakeLock wakeLock;
	private PowerManager pm;
	private WakeLock wl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sdk2_api);

        txtResult = (TextView) findViewById(R.id.txtResult);
        txtLog = (TextView) findViewById(R.id.txtLog);
        findViewById(R.id.btn).setVisibility(View.GONE);
        findViewById(R.id.setting).setVisibility(View.GONE);

        txtResult.setText("请说唤醒词:  小度你好 或 百度一下");
        
        setUpWakeUp();
    }

    private void setUpWakeUp() {
		// TODO Auto-generated method stub
    	// 唤醒功能打开步骤
        // 1) 创建唤醒事件管理器
        mWpEventManager = EventManagerFactory.create(ActivityWakeUp.this, "wp");

        // 2) 注册唤醒事件监听器
        mWpEventManager.registerListener(new EventListener() {
            @Override
            public void onEvent(String name, String params, byte[] data, int offset, int length) {
                Log.d(TAG, String.format("event: name=%s, params=%s", name, params));
                try {
                    JSONObject json = new JSONObject(params);
                    if ("wp.data".equals(name)) { // 每次唤醒成功, 将会回调name=wp.data的时间, 被激活的唤醒词在params的word字段
                        String word = json.getString("word");
                        txtLog.append("唤醒成功, 唤醒词: " + word + "\r\n");
                        doWakeUp();
                    } else if ("wp.exit".equals(name)) {
                        txtLog.append("唤醒已经停止: " + params + "\r\n");
                    }
                } catch (JSONException e) {
                    throw new AndroidRuntimeException(e);
                }
            }
        });

        // 3) 通知唤醒管理器, 启动唤醒功能
        HashMap params = new HashMap();
        params.put("kws-file", "assets:///WakeUp.bin"); // 设置唤醒资源, 唤醒资源请到 http://yuyin.baidu.com/wake#m4 来评估和导出
        mWpEventManager.send("wp.start", new JSONObject(params).toString(), null, 0, 0);

        txtLog.setText(DESC_TEXT);
	}

	@Override
    protected void onResume() {
        super.onResume();

        
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 停止唤醒监听
        //mWpEventManager.send("wp.stop", null, null, 0, 0);
    }
    
	private void doWakeUp() {
		// TODO Auto-generated method stub
		Log.d(TAG, "doWakeUp");
		wakeUpScreenIfNeed();
	}

	public void wakeUpScreenIfNeed() {
		pm =(PowerManager) getSystemService(Context.POWER_SERVICE);
		if (pm.isScreenOn()) {
			Log.d(TAG, "screen is already on");
			return;
		}
		if (null == wl) {
			wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
		}
		Log.d(TAG, "going to acquire wake lock");
	  	wl.acquire();
	  	
	  	wl.release();
	  	//turnOffScreen();
	}
	
	
	private void turnOffScreen() {
	  if (!pm.isScreenOn()) {
	      Log.d(TAG, "screen is already off");
	      return;
	  }
	  pm.goToSleep(3000 / 1000);
	  if (null == wl) {
		  wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
	  }
	  wl.release();
	}

//    /** 
//     * 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行 
//     */  
//    private void acquireWakeLock() {  
//    	Log.d(TAG, "acquireWakeLock");
//    	
//        wakeLock = null;
//		if (null == wakeLock) {  
//            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);  
//            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK  
//                    | PowerManager.ON_AFTER_RELEASE, getClass()  
//                    .getCanonicalName());  
//            if (null != wakeLock) {  
//                Log.i(TAG, "call acquireWakeLock");  
//                wakeLock.acquire();  
//            }  
//        }  
//    }  
//  
//    // 释放设备电源锁  
//    private void releaseWakeLock() {  
//        if (null != wakeLock && wakeLock.isHeld()) {  
//            Log.i(TAG, "call releaseWakeLock");  
//            wakeLock.release();  
//            wakeLock = null;  
//        }  
//    }
}

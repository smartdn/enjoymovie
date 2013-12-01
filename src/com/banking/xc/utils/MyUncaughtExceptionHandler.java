package com.banking.xc.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import skytv_com.banking.enjoymovie.ErrorActivity;

//import com.banking.xc.service.MessagePullService;


import android.content.Context;
import android.content.Intent;
import android.os.Process;

 /**
  * 未捕获异常接口 处理实现类
  *
  */
public class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	private Context context;
	private Thread.UncaughtExceptionHandler mOldUncaughtExceptionHandler;

	//收集异常信息
	private static StringBuffer errorDataBuffer = new StringBuffer();
	
	public static void resetErrorInfo(String content){
		errorDataBuffer.setLength(0);
		errorDataBuffer.append(content);
	}
	
	public static void appendErrorInfo(String content){
		errorDataBuffer.append(content);
	}
	
	public MyUncaughtExceptionHandler(Context c) {
		context = c;
		mOldUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
	}

	@Override
	public void uncaughtException(final Thread thread, final Throwable ex) {
		ex.printStackTrace();
		if (!myUncaughtException(thread, ex) && mOldUncaughtExceptionHandler != null) {
			mOldUncaughtExceptionHandler.uncaughtException(thread, ex);
		} else {
//			try {
//				Thread.sleep(500);
//			} catch (Exception e2) {
//				e2.printStackTrace();
//			}
			android.os.Process.killProcess(Process.myTid());
			System.exit(0);
		}
	}

	private boolean myUncaughtException(Thread thread, final Throwable ex) {
		// if(ex.toString().indexOf("VerifyError")!=-1)
		// return true;
		// new Thread() {
		// public void run() {
		// Looper.prepare();
		// Toast.makeText(MyApplication.this, ex.toString(), 1).show();
		// Notification notification = new Notification();
		// String text = "意外错误!";
		// notification.icon = R.drawable.jd_buy_icon;// 设置icon图片
		// notification.tickerText = text;// 设置text
		// notification.defaults = Notification.DEFAULT_SOUND;// 设置通知声音
		// notification.setLatestEventInfo(MyApplication.this, getString(R.string.app_name), text, PendingIntent.getActivity(MyApplication.this, 0, i, 0));// 设置留言条参数
		// manager.notify(0, notification);
		Intent i = new Intent(context, ErrorActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			String error = errorDataBuffer.toString() + "||" + ResolveException.resolve(ex);
			i.putExtra("error", error);
			context.startActivity(i);
//			context.stopService(new Intent(context,MessagePullService.class));//停止Service服务
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		// Looper.loop();
		// };
		// }.start();
		return true;
	}

}

package com.moko.beacon;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.moko.beacon.utils.IOUtils;
import com.moko.support.MokoSupport;
import com.moko.support.log.LogModule;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.BaseApplication
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MokoSupport.getInstance().init(getApplicationContext());
        // 启动蓝牙服务
        Thread.setDefaultUncaughtExceptionHandler(new BTUncaughtExceptionHandler());
    }

    public class BTUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        private static final String LOGTAG = "BTUncaughtExceptionHandler";

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            // 读取stacktrace信息
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            ex.printStackTrace(printWriter);
            StringBuffer errorReport = new StringBuffer();
            // 获取packagemanager的实例
            PackageManager packageManager = getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = null;
            try {
                packInfo = packageManager.getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (packInfo != null) {
                String version = packInfo.versionName;
                errorReport.append(version);
                errorReport.append("\r\n");
            }
            errorReport.append(result.toString());
            IOUtils.setCrashLog(errorReport.toString());
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
}

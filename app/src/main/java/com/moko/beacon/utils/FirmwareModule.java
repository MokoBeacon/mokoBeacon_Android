package com.moko.beacon.utils;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FirmwareModule {

    private static volatile FirmwareModule INSTANCE;
    private Context context;
    private static final String FOLDER_NAME = "firmware";
    private static final String BASE = "base.zip";
    private static final String BASE_SENSOR = "base_sensor.zip";

    private FirmwareModule(Context context) {
        this.context = context;
    }


    public static FirmwareModule getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (FirmwareModule.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FirmwareModule(context);
                }
            }
        }
        return INSTANCE;
    }

    public void copyAssets2SD() {
        copyAssets(context, FOLDER_NAME, context.getExternalCacheDir() + File.separator + FOLDER_NAME);
    }

    /**
     * @Date 2018/3/30
     * @Author wenzheng.liu
     * @Description 拷贝assets到sd卡上
     */
    private void copyAssets(Context context, String assetDir, String dir) {
        String[] files;
        try {
            files = context.getResources().getAssets().list(assetDir);
        } catch (IOException e1) {
            return;
        }
        File sdDir = new File(dir);
        if (!sdDir.exists()) {
            sdDir.mkdirs();
        }
        for (int i = 0; i < files.length; i++) {
            try {
                String fileName = files[i];
                File outFile = new File(dir, fileName);
                if (outFile.exists()) {
                    continue;
                } else {
                    outFile.createNewFile();
                }
                InputStream in = context.getAssets().open(assetDir + File.separator + fileName);
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @Date 2018/3/30
     * @Author wenzheng.liu
     * @Description 获取固件路径
     */
    public String getFilePath(boolean isSupportSensor) {
        String firmwareFilePath;
        if (isSupportSensor) {
            firmwareFilePath = context.getExternalCacheDir() + File.separator + FOLDER_NAME + File.separator + BASE_SENSOR;
        } else {
            firmwareFilePath = context.getExternalCacheDir() + File.separator + FOLDER_NAME + File.separator + BASE;
        }
        return firmwareFilePath;
    }
}

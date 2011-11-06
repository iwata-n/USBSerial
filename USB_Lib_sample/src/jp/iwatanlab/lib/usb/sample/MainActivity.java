
package jp.iwatanlab.lib.usb.sample;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import jp.iwatanlab.lib.usb.UsbSerial;
import jp.iwatanlab.lib.usb.UsbSerialManager;
import android.app.Activity;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity implements Runnable {

    public static final String TAG = "usb test";

    Thread thread = new Thread(this);
    boolean runFlag = false;

    /** USBシリアル通信デバイス */
    UsbSerial serial;

    /** USBシリアル通信のマネージャー */
    UsbSerialManager manager;

    /** USBシリアル通信デバイスを使用するパーミッションを取得した際のコールバックリスナー */
    UsbSerialManager.OnGetPermissionListener listener = new UsbSerialManager.OnGetPermissionListener() {
        @Override
        public void onGetPermission(UsbSerial device) {
            Log.d(TAG, "onGetPermission");
            UsbDevice dev = device.getDevice();
            Log.d(TAG, "DeviceName=" + dev.getDeviceName());
            Log.d(TAG, "id=" + String.valueOf(dev.getDeviceId()));
            serial = device;

            try {
                serial.open();
            } catch (IOException e) {
                e.printStackTrace();
            }
            threadStart();
        }

    };

    private void threadStart() {
        Log.d(TAG, "threadStart");
        runFlag = true;
        thread = new Thread(MainActivity.this);
        thread.start();
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.d(TAG, "onCreate");
    }

    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        manager = new UsbSerialManager(this);
        if (serial == null) {
            List<UsbDevice> list = manager.getList();
            if (list.size() > 0) {
                Log.d(TAG, "Device count=" + String.valueOf(list.size()));
                UsbDevice dev = list.get(0);
                manager.requestPermission(dev, listener);
            } else {
                Log.e(TAG, "USB Serial Device not found");
            }
        } else {
            Log.e(TAG, "USB Serial Device has already got instance.");
        }

    }

    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        try {
            if (runFlag) {
                runFlag = false;
                thread.join();
                serial.close();
            }
        } catch (InterruptedException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }

        thread = null;
    }

    @Override
    public void run() {

        int count = 0;
        byte[] readBuf = new byte[3];
        byte[] writeBuf = new byte[3];
        // for (int i = 0; i < writeBuf.length - 1; i++) {
        // writeBuf[i] = (byte) ('a' + i);
        // }
        writeBuf[0] = '@';
        writeBuf[1] = 0;
        writeBuf[2] = 1;

        InputStream in = serial.getInputStream();
        OutputStream out = serial.getOutputStream();

        /* シリアルに1秒間隔で出力する */
        while (runFlag) {

            try {
                // writeBuf[9] = (byte) ('0' + count);
                Log.d(TAG, "write:" + new String(writeBuf));
                out.write(writeBuf);
                serial.read(readBuf);
                Log.d(TAG, "read :" + new String(readBuf));
            } catch (IOException e1) {
                e1.printStackTrace();

            }
            count++;

            count = count % 3;
            // Log.d(TAG, String.valueOf(count));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}

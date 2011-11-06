
package jp.iwatanlab.lib.usb.sample2;

import java.io.IOException;
import java.util.List;

import jp.iwatanlab.lib.usb.UsbSerial;
import jp.iwatanlab.lib.usb.UsbSerialManager;
import android.app.Activity;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity implements Runnable {

    static final int MSG_READ = 1;

    EditText editInput, textRead;

    /** USBシリアル通信デバイス */
    UsbSerial serial;

    /** USBシリアル通信のマネージャー */
    UsbSerialManager manager;

    /** USBシリアル通信デバイスを使用するパーミッションを取得した際のコールバックリスナー */
    UsbSerialManager.OnGetPermissionListener listener = new UsbSerialManager.OnGetPermissionListener() {
        @Override
        public void onGetPermission(UsbSerial device) {
            serial = device;

            try {
                serial.open();
                thread = new Thread(MainActivity.this);
                runFlag = true;
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    boolean runFlag = false;
    Thread thread;

    Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_READ:
                    SpannableStringBuilder sb = (SpannableStringBuilder) textRead.getText();
                    String str = sb.toString() + (String) msg.obj;

                    textRead.setText(str);
                    break;
                default:
                    break;
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        editInput = (EditText) findViewById(R.id.edit_input);
        textRead = (EditText) findViewById(R.id.text_read);
    }

    public void onStart() {
        super.onStart();
        manager = new UsbSerialManager(this);
        if (serial == null) {
            List<UsbDevice> list = manager.getList();
            if (list.size() > 0) {
                UsbDevice dev = list.get(0);
                manager.requestPermission(dev, listener);
            }
        }
    }

    public void onStop() {
        super.onStop();
        try {
            serial.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClickSend(View v) {
        SpannableStringBuilder sb = (SpannableStringBuilder) editInput.getText();
        String str = sb.toString();
        try {
            Log.d("send", str);
            serial.write(str.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        final byte[] buffer = new byte[1024];
        while (runFlag) {
            try {
                int len = serial.read(buffer);
                Log.d("len", String.valueOf(len));
                if (len > 0) {
                    Log.d("read", new String(buffer));
                    Message msg = handle.obtainMessage(MSG_READ, new String(buffer, 0, len));
                    handle.sendMessage(msg);
                }
                Thread.sleep(10);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}

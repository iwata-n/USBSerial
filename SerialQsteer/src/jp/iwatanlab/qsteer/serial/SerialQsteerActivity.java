
package jp.iwatanlab.qsteer.serial;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.iwatanlab.lib.usb.UsbSerial;
import jp.iwatanlab.lib.usb.UsbSerialManager;
import android.app.Activity;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SerialQsteerActivity extends Activity implements OnTouchListener {

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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private class CtrlData {
        public byte normal = 0;
        public byte turbo = 0;

        public CtrlData(byte normal, byte turbo) {
            this.normal = normal;
            this.turbo = turbo;
        }
    }

    private static final byte CTRL_STOP = 0;
    private static final byte CTRL_FORWARD = 1;
    private static final byte CTRL_BACK = 2;
    private static final byte CTRL_LEFT = 3;
    private static final byte CTRL_RIGHT = 4;
    private static final byte CTRL_TURBO_FORWARD = 5;
    private static final byte CTRL_FORWARD_LEFT = 6;
    private static final byte CTRL_FORWARD_RIGHT = 7;
    private static final byte CTRL_TURBO_FORWARD_LEFT = 8;
    private static final byte CTRL_TURBO_FORWARD_RIGHT = 9;
    private static final byte CTRL_LEFT_BACK = 10;
    private static final byte CTRL_RIGHT_BACK = 11;
    private static final byte CTRL_TURBO_BACK = 12;
    private static final byte CTRL_TURBO_LEFT_BACK = 13;
    private static final byte CTRL_TURBO_RIGHT_BACK = 14;

    private List<CtrlData> ctrlData = new ArrayList<CtrlData>();

    private ToggleButton tglTurbo;

    private byte mBand = 0;

    /**
     * チョロQにコマンドを送信する
     * 
     * @param id コマンド
     */
    private void sendCommand(int id) {
        CtrlData ctrl = ctrlData.get(id);
        byte command = ctrl.normal;
        if (tglTurbo.isChecked()) {
            command = ctrl.turbo;
        }
        byte[] data = {
                '@', mBand, command
        };

        if (serial == null) {
            Toast.makeText(this, "Device not connect", Toast.LENGTH_SHORT).show();
        }

        try {
            serial.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Viewにタッチイベントを登録する
     * 
     * @param id ViewのID
     */
    private void setTouchEvent(int id) {
        View v = findViewById(id);
        v.setOnTouchListener(this);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tglTurbo = (ToggleButton) findViewById(R.id.tgl_turbo);

        ctrlData.add(new CtrlData(CTRL_STOP, CTRL_STOP));
        ctrlData.add(new CtrlData(CTRL_FORWARD, CTRL_TURBO_FORWARD));
        ctrlData.add(new CtrlData(CTRL_FORWARD_RIGHT, CTRL_TURBO_FORWARD_RIGHT));
        ctrlData.add(new CtrlData(CTRL_RIGHT, CTRL_RIGHT));
        ctrlData.add(new CtrlData(CTRL_RIGHT_BACK, CTRL_TURBO_RIGHT_BACK));
        ctrlData.add(new CtrlData(CTRL_BACK, CTRL_TURBO_BACK));
        ctrlData.add(new CtrlData(CTRL_LEFT_BACK, CTRL_TURBO_LEFT_BACK));
        ctrlData.add(new CtrlData(CTRL_LEFT, CTRL_LEFT));
        ctrlData.add(new CtrlData(CTRL_FORWARD_LEFT, CTRL_TURBO_FORWARD_LEFT));

        setTouchEvent(R.id.btn_forward);
        setTouchEvent(R.id.btn_right_forward);
        setTouchEvent(R.id.btn_right);
        setTouchEvent(R.id.btn_right_back);
        setTouchEvent(R.id.btn_back);
        setTouchEvent(R.id.btn_left_back);
        setTouchEvent(R.id.btn_left);
        setTouchEvent(R.id.btn_left_forward);
        setTouchEvent(R.id.btn_stop);
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

    /**
     * タッチ時の動作
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int id = Integer.parseInt((String) v.getTag());

        if (event.getAction() == MotionEvent.ACTION_UP) {
            sendCommand(0);
        } else {
            sendCommand(id);
        }

        return false;
    }
}

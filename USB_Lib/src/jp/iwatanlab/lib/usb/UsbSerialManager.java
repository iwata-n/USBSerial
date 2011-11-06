/**
 * 
 */

package jp.iwatanlab.lib.usb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jp.iwatanlab.lib.usb.ftdi.FtdiUsbSerial;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

/**
 * @author naoki
 */
public class UsbSerialManager {

    /**
     * パーミッションを取得した際のコールバックリスナー
     * 
     * @author naoki
     */
    public interface OnGetPermissionListener {

        /**
         * パーミッションを取得した際のコールバック
         * 
         * @param device パーミッションを取得したUSBデバイス
         */
        void onGetPermission(UsbSerial device);
    }

    private static final String TAG = UsbSerialManager.class.getSimpleName();

    public static final String ACTION_USB_PERMISSION = "jp.iwatan.lib.usb.action.USB_PERMISSON";

    UsbManager manager;

    List<UsbDevice> devices = new ArrayList<UsbDevice>();

    Context mContext;

    OnGetPermissionListener listener;

    BroadcastReceiver mPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    // Log.e(TAG, "Permission not granted");
                } else {
                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null) {

                        if (device.getVendorId() == FtdiUsbSerial.VENDOR_ID
                                && device.getProductId() == FtdiUsbSerial.PRODUCT_ID) {
                            Log.d(TAG, "device open");
                            UsbSerial dev = new FtdiUsbSerial(manager, device);
                            if (listener != null) {
                                listener.onGetPermission(dev);
                            } else {
                                Log.e(TAG, "OnGetPermissionListener is null");
                            }
                        }
                    } else {
                        Log.e(TAG, "Device is null");
                    }
                }
                /* TODO エラー回避の苦肉の策 複数のデバイスを同時に使った際に動作が怪しくなる気がする */
                mContext.unregisterReceiver(mPermissionReceiver);
            }
        }
    };

    /**
     * コンストラクタ
     * 
     * @param context コンテキスト
     */
    public UsbSerialManager(Context context) {
        mContext = context;
        manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
    }

    /**
     * USBシリアル通信が行えるデバイスリストを取得
     * 
     * @return USBシリアル通信が行えるUSBデバイスのリスト
     */
    public List<UsbDevice> getList() {
        HashMap<String, UsbDevice> list = manager.getDeviceList();
        Iterator<UsbDevice> ite = list.values().iterator();

        while (ite.hasNext()) {
            UsbDevice device = ite.next();
            Log.d(TAG, "DeviceName=" + device.getDeviceName());
            Log.d(TAG, "VendorID=" + String.valueOf(device.getVendorId()));
            Log.d(TAG, "ProductID=" + String.valueOf(device.getProductId()));
            if (device.getVendorId() == FtdiUsbSerial.VENDOR_ID
                    && device.getProductId() == FtdiUsbSerial.PRODUCT_ID) {
                devices.add(device);
            }
        }
        return devices;
    }

    /**
     * USBデバイスのパーミッション確認
     * 
     * @param device パーミッションを確認するUSBデバイス
     * @return == true パーミッションあり == false パーミッションなし
     */
    public boolean hasPermission(UsbDevice device) {
        return manager.hasPermission(device);
    }

    /**
     * USBデバイスのパーミッション確認
     * 
     * @param device パーミッションを確認するUSBデバイス
     * @return == true パーミッションあり == false パーミッションなし
     */
    public boolean hasPermission(UsbSerial device) {
        return hasPermission(device.getDevice());
    }

    /**
     * パーミッションを要求する
     * 
     * @param device パーミッションを要求するUSBデバイス
     * @param listener パーミッションを取得できた際のコールバックリスナー
     */
    public void requestPermission(UsbDevice device, OnGetPermissionListener listener) {

        this.listener = listener;

        PendingIntent pendigIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(
                ACTION_USB_PERMISSION), 0);

        mContext.registerReceiver(mPermissionReceiver, new IntentFilter(
                ACTION_USB_PERMISSION));
        if (!manager.hasPermission(device)) {
            manager.requestPermission(device, pendigIntent);
        } else {
            UsbSerial dev = new FtdiUsbSerial(manager, device);
            listener.onGetPermission(dev);
        }
    }

    /**
     * パーミッションを要求する
     * 
     * @param device パーミッションを要求するUSBデバイス
     * @param listener パーミッションを取得できた際のコールバックリスナー
     */
    public void requestPermission(UsbSerial device, OnGetPermissionListener listener) {
        requestPermission(device.getDevice(), listener);
    }
}

/**
 * 
 */

package jp.iwatanlab.lib.usb;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

/**
 * @author naoki
 */
public abstract class UsbSerialImpl implements UsbSerial {

    protected UsbDevice mDevice;

    protected UsbManager mManager;

    protected UsbDeviceConnection conn;

    public UsbSerialImpl(UsbManager manager, UsbDevice device) {
        mDevice = device;
        mManager = manager;

        conn = manager.openDevice(device);
    }

    public UsbDevice getDevice() {
        return mDevice;
    }
}

/**
 * 
 */

package jp.iwatanlab.lib.usb.ftdi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jp.iwatanlab.lib.usb.UsbSerialImpl;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

/**
 * @author naoki
 */
public class FtdiUsbSerial extends UsbSerialImpl {

    private static final String TAG = FtdiUsbSerial.class.getSimpleName();

    public static final String VENDOR_ID_HEX = "0403";
    public static final String PRODUCT_ID_HEX = "6001";
    public static final int VENDOR_ID = 1027;
    public static final int PRODUCT_ID = 24577;
    public static final int DEFAULT_BAUDRATE = 9600;

    static final int RESET_REQUEST = 0;
    static final int RESET_SIO = 0;
    static final int RESET_PURGE_RX = 1;
    static final int RESET_PURGE_TX = 2;
    static final int SET_BAUDRATE_REQUEST = 3;

    /** 必ず頭に２バイトおまけが付いているから無視する */
    private static final int FTDI_DATA_LENGTH = 2;

    UsbEndpoint epIn, epOut;

    class UsbSerialOutputStream extends OutputStream {

        @Override
        public void write(int oneByte) throws IOException {
            byte[] buf = {
                    (byte) oneByte
            };
            conn.bulkTransfer(epOut, buf, 1, 100);
        }

    }

    class UsbSerialInputStream extends InputStream {

        @Override
        public int read() throws IOException {
            byte[] buf = new byte[4096];
            int len = conn.bulkTransfer(epIn, buf, buf.length, 100);
            Log.d(TAG, "ReadSize:" + String.valueOf(len));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < len; i++) {
                sb.append(0xFF & buf[i]);
                sb.append(" ");
            }
            Log.d(TAG, sb.toString());

            return buf[2];
        }

    }

    UsbSerialOutputStream out = new UsbSerialOutputStream();
    UsbSerialInputStream in = new UsbSerialInputStream();

    public FtdiUsbSerial(UsbManager manager, UsbDevice device) {
        super(manager, device);
    }

    /*
     * (non-Javadoc)
     * @see jp.iwatan.lib.usb.USBSerial#close()
     */
    @Override
    public void close() throws IOException {
        out.close();
        in.close();
    }

    /*
     * (non-Javadoc)
     * @see jp.iwatan.lib.usb.USBSerial#getInputStream()
     */
    @Override
    public InputStream getInputStream() {
        return in;
    }

    /*
     * (non-Javadoc)
     * @see jp.iwatan.lib.usb.USBSerial#getOutputStream()
     */
    @Override
    public OutputStream getOutputStream() {
        return out;
    }

    /*
     * (non-Javadoc)
     * @see jp.iwatan.lib.usb.USBSerial#open()
     */
    @Override
    public void open() throws IOException {
        open(DEFAULT_BAUDRATE);
    }

    /*
     * (non-Javadoc)
     * @see jp.iwatan.lib.usb.USBSerial#open(int)
     */
    @Override
    public void open(int baudrate) throws IOException {

        conn.controlTransfer(UsbConstants.USB_TYPE_VENDOR, RESET_REQUEST, RESET_SIO, 0, null, 0, 0);
        conn.controlTransfer(UsbConstants.USB_TYPE_VENDOR, RESET_REQUEST, RESET_PURGE_RX, 0, null,
                0, 0);
        conn.controlTransfer(UsbConstants.USB_TYPE_VENDOR, RESET_REQUEST, RESET_PURGE_TX, 0, null,
                0, 0);

        conn.controlTransfer(0x40, 0x02, 0x0000, 0, null, 0, 0);// flow control
                                                                // none
        setBaudRate(baudrate);
        conn.controlTransfer(0x40, 0x04, 0x0008, 0, null, 0, 0);// data bit 8,
                                                                // parity none,
                                                                // stop bit 1,
                                                                // tx off

        UsbInterface usbIf = mDevice.getInterface(0);
        for (int i = 0; i < usbIf.getEndpointCount(); i++) {
            if (usbIf.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (usbIf.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN) {
                    epIn = usbIf.getEndpoint(i);
                } else {
                    epOut = usbIf.getEndpoint(i);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see jp.iwatan.lib.usb.USBSerial#setBaudRate(int)
     */
    @Override
    public void setBaudRate(int baudrate) throws IOException {

        // TODO ボーレートの計算を実装する
        conn.controlTransfer(UsbConstants.USB_TYPE_VENDOR, SET_BAUDRATE_REQUEST, 0x4138, 0, null,
                0, 0);
    }

    /*
     * (non-Javadoc)
     * @see jp.iwatan.lib.usb.USBSerial#getBaudRate()
     */
    @Override
    public int getBaudRate() throws IOException {
        return 0;
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        out.write(buffer);
    }

    @Override
    public synchronized int read(byte[] buffer) throws IOException {
        byte[] buf = new byte[4096];

        int offset = 0;
        int ret = 0;

        do {
            ret = conn.bulkTransfer(epIn, buf, buf.length, 100);

            /* for debug */
            // StringBuilder sb = new StringBuilder();
            // for (int i = 0; i < ret; i++) {
            // sb.append(0xFF & buf[i]);
            // sb.append(" ");
            // }
            // Log.d(TAG, "buf=" + sb.toString());
            // Log.d(TAG, "ret=" + String.valueOf(ret));

            if (ret > 2) {

                int cpy_size = ret - FTDI_DATA_LENGTH;
                // Log.d(TAG, "cpy_size1=" + String.valueOf(cpy_size));

                if (cpy_size > buffer.length) {
                    cpy_size = buffer.length;
                }
                // Log.d(TAG, "cpy_size2=" + String.valueOf(cpy_size));

                System.arraycopy(buf, FTDI_DATA_LENGTH, buffer, offset, cpy_size);
                offset += cpy_size;
                // Log.d(TAG, "offset  =" + String.valueOf(offset));
            }
        } while (ret > 0 && offset < buffer.length);
        // Log.d(TAG, "offset=" + offset);
        // Log.d(TAG, "length=" + buffer.length);

        return 0;

    }
}

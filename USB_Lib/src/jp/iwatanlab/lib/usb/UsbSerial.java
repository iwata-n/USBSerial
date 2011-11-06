/**
 * 
 */

package jp.iwatanlab.lib.usb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.hardware.usb.UsbDevice;

/**
 * @author naoki
 */
public interface UsbSerial {

    /**
     * USBシリアルポートを開く
     * 
     * @throws IOException
     */
    void open() throws IOException;

    /**
     * USBシリアルポートを開く
     * 
     * @param baudrate
     * @throws IOException
     */
    void open(int baudrate) throws IOException;

    /**
     * @param baudrate
     * @throws IOException
     */
    void setBaudRate(int baudrate) throws IOException;

    /**
     * @return
     * @throws IOException
     */
    int getBaudRate() throws IOException;

    /**
     * @throws IOException
     */
    void close() throws IOException;

    void write(byte[] buffer) throws IOException;

    int read(byte[] buffer) throws IOException;

    /**
     * @return
     * @throws IOException
     */
    OutputStream getOutputStream();

    /**
     * @return
     * @throws IOException
     */
    InputStream getInputStream();

    UsbDevice getDevice();

}

package org.droidplanner.services.android.communication.connection.usb;

import java.io.IOException;
import java.util.List;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

class UsbCDCConnection extends UsbConnection.UsbConnectionImpl {
	private static final String TAG = UsbCDCConnection.class.getSimpleName();
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private static final IntentFilter intentFilter = new IntentFilter(ACTION_USB_PERMISSION);

	private static UsbSerialDriver sDriver = null;

    private final PendingIntent usbPermissionIntent;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(ACTION_USB_PERMISSION.equals(action)){
                synchronized(this){
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            //call method to set up device communication
                            try {
                                openUsbDevice(device);
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage(), e);
                            }
                        }
                    }
                    else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };

	protected UsbCDCConnection(Context context, int baudRate) {
		super(context, baudRate);
        this.usbPermissionIntent = PendingIntent.getBroadcast(context, 0,
                new Intent(ACTION_USB_PERMISSION), 0);
	}

    private void registerUsbPermissionBroadcastReceiver(){
//        mContext.registerReceiver(broadcastReceiver, intentFilter);
    }

    private void unregisterUsbPermissionBroadcastReceiver(){
//        mContext.unregisterReceiver(broadcastReceiver);
    }

	@Override
	protected void openUsbConnection() throws IOException {
        registerUsbPermissionBroadcastReceiver();

		// Get UsbManager from Android.
		UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        //Get the list of available devices
        List<UsbDevice> availableDevices = UsbSerialProber.getAvailableSupportedDevices(manager);
        if(availableDevices.isEmpty()){
            Log.d("USB", "No Devices found");
            throw new IOException("No Devices found");
        }

        //Pick the first device
        UsbDevice device = availableDevices.get(0);
        if(manager.hasPermission(device)){
            openUsbDevice(device);
        }
        else{
            //TODO: complete implementation
//            manager.requestPermission(device, usbPermissionIntent);
            throw new IOException("No permission to access usb device " + device.getDeviceName());
        }
	}

    private void openUsbDevice(UsbDevice device) throws IOException {
        // Get UsbManager from Android.
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        // Find the first available driver.
        sDriver = UsbSerialProber.openUsbDevice(manager, device);

        if (sDriver == null) {
            Log.d("USB", "No Devices found");
            throw new IOException("No Devices found");
        } else {
            Log.d("USB", "Opening using Baud rate " + mBaudRate);
            try {
                sDriver.open();
                sDriver.setParameters(mBaudRate, 8, UsbSerialDriver.STOPBITS_1,
                        UsbSerialDriver.PARITY_NONE);
            } catch (IOException e) {
                Log.e("USB", "Error setting up device: " + e.getMessage(), e);
                try {
                    sDriver.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                sDriver = null;
            }
        }
    }

	@Override
	protected int readDataBlock(byte[] readData) throws IOException {
		// Read data from driver. This call will return upto readData.length
		// bytes.
		// If no data is received it will timeout after 200ms (as set by
		// parameter 2)
		int iavailable = 0;
		try {
			iavailable = sDriver.read(readData, 200);
		} catch (NullPointerException e) {
			final String errorMsg = "Error Reading: " + e.getMessage()
					+ "\nAssuming inaccessible USB device.  Closing connection.";
			Log.e(TAG, errorMsg, e);
			throw new IOException(errorMsg, e);
		}

		if (iavailable == 0)
			iavailable = -1;
		return iavailable;
	}

	@Override
	protected void sendBuffer(byte[] buffer) {
		// Write data to driver. This call should write buffer.length bytes
		// if data cant be sent , then it will timeout in 500ms (as set by
		// parameter 2)
		if (sDriver != null) {
			try {
				sDriver.write(buffer, 500);
			} catch (IOException e) {
				Log.e("USB", "Error Sending: " + e.getMessage(), e);
			}
		}
	}

	@Override
	protected void closeUsbConnection() throws IOException {
        unregisterUsbPermissionBroadcastReceiver();

		if (sDriver != null) {
			try {
				sDriver.close();
			} catch (IOException e) {
				// Ignore.
			}
			sDriver = null;
		}
	}

	@Override
	public String toString() {
		return TAG;
	}
}

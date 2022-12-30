package com.ingenico;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.usdk.apiservice.aidl.constants.RFDeviceName;
import com.usdk.apiservice.aidl.pinpad.DeviceName;
import com.usdk.apiservice.aidl.printer.AlignMode;
import com.usdk.apiservice.aidl.printer.ECLevel;
import com.usdk.apiservice.aidl.printer.FactorMode;
import com.usdk.apiservice.aidl.printer.OnPrintListener;
import com.usdk.apiservice.aidl.printer.PrintFormat;
import com.usdk.apiservice.aidl.printer.PrinterError;
import com.usdk.apiservice.aidl.printer.UPrinter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ReactOneCustomMethod extends ReactContextBaseJavaModule {
    private static ReactApplicationContext reactContext;

    private static final String DURATION_SHORT_KEY = "SHORT";
    private static final String DURATION_LONG_KEY = "LONG";

    private DeviceHelper serviceConnection;
    private UPrinter printer;

    public static String PINPAD_DEVICE_NAME = DeviceName.IPP;
    public static String RF_DEVICE_NAME = RFDeviceName.INNER;

    ReactOneCustomMethod(ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }

    @NonNull
    @Override
    public String getName() {
        return "ReactOneCustomMethod";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(DURATION_SHORT_KEY, Toast.LENGTH_SHORT);
        constants.put(DURATION_LONG_KEY, Toast.LENGTH_LONG);
        return constants;
    }

    @ReactMethod
    public void getPhoneID(Promise response) {
        try {
            @SuppressLint("HardwareIds") String id = Settings.Secure.getString(reactContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            response.resolve(id);
        } catch (Exception e) {
            response.reject("Error", e);
        }
    }

    @ReactMethod
    public void show(String message, int duration) {
        Toast.makeText(getReactApplicationContext(), message, duration).show();
    }

   

    @ReactMethod
    public void initDefaultConfig() {
        if (Build.MODEL.startsWith("DX8000")) {
            PINPAD_DEVICE_NAME = DeviceName.COM_EPP;
            RF_DEVICE_NAME = RFDeviceName.EXTERNAL;
        } else {
            PINPAD_DEVICE_NAME = DeviceName.IPP;
            RF_DEVICE_NAME = RFDeviceName.INNER;
        }
    }

    @ReactMethod
    public void bind(){
        serviceConnection = DeviceHelper.me();
        serviceConnection.init(getReactApplicationContext());
        serviceConnection.bindService();
    }

    @ReactMethod
    public void register(boolean module) {
        try {
            serviceConnection.register(module);
            printer = serviceConnection.getPrinter();
            getStatus();
        } catch (IllegalStateException e) {

        }
    }

    @ReactMethod
    public void getStatus() {
        try {
            Log.d("Printer", String.valueOf(printer.getStatus()));
            int status = printer.getStatus();
            if (status != PrinterError.SUCCESS) {
                Log.d("Printer","status not normal");
                return;
            }
            Log.d("Printer","status normal");

        } catch (Exception e) {
            Log.d("Printer","status error");
            Log.e("Printer", "exception", e);
        }
    }

    @ReactMethod
    public void startPrint(String printData) {
        try {

//          printer.setPrintFormat();
            Log.d("Printer", String.valueOf(printer.getValidWidth()));
            Log.d("Printer","i start print");
            printer.addText(AlignMode.CENTER, printData);
//
//            printer.addQrCode(AlignMode.CENTER, 240, ECLevel.ECLEVEL_H, "www.landicorp.com");
//            printer.addBarCode(AlignMode.CENTER, 2, 48,  "1234567");
//
//            printer.setPrintFormat(PrintFormat.FORMAT_FONTMODE, PrintFormat.VALUE_FONTMODE_BOLDLEVEL2);
//            printer.addText(AlignMode.LEFT, ">>>>>>> addBmpImage bold2");
            byte[] image = readAssetsFile(getReactApplicationContext(), "jin.png");
            printer.addBmpImage(AlignMode.CENTER, FactorMode.BMP1X1, image);
            printer.startPrint(new OnPrintListener.Stub() {
                @Override
                public void onFinish() throws RemoteException {
                    printer.autoCutPaper();
                }

                @Override
                public void onError(int i) throws RemoteException {

                }
            });
        } catch (Exception e) {

        }
    }

    private static byte[] readAssetsFile(Context ctx, String fileName) {
        InputStream input = null;
        try {
            input = ctx.getAssets().open(fileName);
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
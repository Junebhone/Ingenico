package com.ingenico;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.usdk.apiservice.aidl.printer.PrinterError;
import com.usdk.apiservice.aidl.vectorprinter.Alignment;
import com.usdk.apiservice.aidl.vectorprinter.OnPrintListener;
import com.usdk.apiservice.aidl.vectorprinter.TextSize;
import com.usdk.apiservice.aidl.vectorprinter.UVectorPrinter;
import com.usdk.apiservice.aidl.vectorprinter.VectorPrinterData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReactOneCustomMethod extends ReactContextBaseJavaModule {
    private static ReactApplicationContext reactContext;

    private static final String DURATION_SHORT_KEY = "SHORT";
    private static final String DURATION_LONG_KEY = "LONG";

    private DeviceHelper serviceConnection;
    private UVectorPrinter vectorPrinter;

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
    public void bind(){
        serviceConnection = DeviceHelper.me();
        serviceConnection.init(getReactApplicationContext());
        serviceConnection.bindService();
    }

    @ReactMethod
    public void register(boolean module) {
        try {
            serviceConnection.register(module);
            vectorPrinter = serviceConnection.getVectorPrinter();
            getStatus();
        } catch (IllegalStateException e) {

        }
    }

    public void getStatus() {
        try {
            Log.d("Printer", String.valueOf(vectorPrinter.getStatus()));
            int status = vectorPrinter.getStatus();
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
    public void startPrint(String printData) throws RemoteException, JSONException {

        final long startTime = System.currentTimeMillis();

        JSONObject data = new JSONObject(printData);

        String title = data.get("title").toString();
        // String amount = data.get("amount").toString();   

        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getReactApplicationContext().getAssets().open("uab.png"));
            vectorPrinter.addImage(null, bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        addTitle("uab_Production Testing (1)\n");
        addTitle("Ingenico\n");
        addTitle("Time City, Yangon\n");
        addTitle("Kamaryut Township\n");
        addTitle(title);
        // addTitle("Yangon, Myanmar\n");
        Dash("------------------------------------------------------");

        addContent("TID:40000040","62445******5609",null,null,null);
        addContent("HOST: MPU Host","NII:109",null,null,null);
        addContent("DEC 27,2022","14:33:24",null,null,null);
        addContent("INVOICE NO:","000027",null,null,null);
        Dash("------------------------------------------------------");
        addTitle("SALE\n");
        addContent("MPU-UPI","62445******5609",null,null,null);
        addContent("EXP DATE","**/**",null,null,null);
        addContent("ENTRY TYPE","CHIP",null,null,null);
        addContent("CARD NAME","JN",null,null,null);
        addContent("APP CODE","177351",null,null,null);
        addContent("REF","136331424360",null,null,null);
        addContent("AID","A000004820000001",2,4,null);

        Dash("------------------------------------------------------");

        addContent("AMOUNT","MMK 100\n",null,null,true);

        Dash("------------------------------------------------------");

   
        vectorPrinter.feedPix(250);
        vectorPrinter.startPrint(new OnPrintListener.Stub() {
            @Override
            public void onFinish() throws RemoteException {
                Log.d("Printer", (String.valueOf(System.currentTimeMillis() - startTime)));
                
            }

            @Override
            public void onStart() throws RemoteException {
                Log.d("Printer","I am starting to print");
            }

            @Override
            public void onError(int i, String s) throws RemoteException {

            }
        });
    }

    public void addContent(String col1, String col2, Integer col1Width,Integer col2Width,Boolean bold) throws RemoteException{


      int[] width = col1Width != null ? new int[]{col1Width, col2Width}: new int[]{8,8};
      Boolean Bold = bold != null ? bold : false;

      // int[] weights  = {8,8}
        int[] weights = width;
        int[] aligns = {Alignment.NORMAL, Alignment.OPPOSITE};

        Bundle textContent = new Bundle();
        textContent.putBoolean(VectorPrinterData.BOLD, Bold);
        textContent.putInt(VectorPrinterData.TEXT_SIZE,TextSize.NORMAL);
        textContent.putInt(VectorPrinterData.FONT_SIZE,22);

        vectorPrinter.addTextColumns(textContent,
                new String[]{col1,col2}, weights, aligns);
    }

    public void Dash(String dash) throws RemoteException{
        Bundle DashFormat = new Bundle();
        DashFormat.putInt(VectorPrinterData.ALIGNMENT, Alignment.CENTER);
        DashFormat.putInt(VectorPrinterData.TEXT_SIZE, TextSize.NORMAL);
        vectorPrinter.addText(DashFormat,dash);
    }

    public void addTitle(String text) throws RemoteException{
        Bundle textTitle = new Bundle();
        textTitle.putInt(VectorPrinterData.ALIGNMENT, Alignment.CENTER);
        textTitle.putInt(VectorPrinterData.TEXT_SIZE, TextSize.NORMAL);
        vectorPrinter.addText(textTitle, text);
    }





}
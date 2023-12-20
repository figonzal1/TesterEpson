package cl.ryc.testerepson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.VectorDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.epson.epos2.Epos2CallbackCode;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.epson.eposprint.Print;

import java.util.HashMap;

import cl.ryc.testerepson.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements ReceiveListener {

    private ActivityMainBinding binding = null;
    Button btnConnect, btnDisconnect;
    TextView tvPrintName;
    TextView tvPrintStatus;

    private Printer mPrinter = null;
    private UsbDevice mUsbDevice = null;
    private String mTargetDevice;

    private boolean detected = false;
    private boolean connected = false;

    private static final String ACTION_USB_PERMISSION =
            "cl.ryc.testerepson.USB_PERMISSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        btnConnect = binding.btnConnect;
        btnDisconnect = binding.btnDisconnect;
        tvPrintName = binding.tvPrintName;
        tvPrintStatus = binding.tvPrintStatus;

        String fecha = "2023-08-12";
        String numeroAtencion = "p22";
        String fila = "Ejecutivo Persona";


        //Registrar listener-
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbListener, filter);

        mUsbDevice = getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (mUsbDevice != null) {

            Log.d("DEVICE_USB", mUsbDevice.toString());

            tvPrintName.setText("Impresora detectada: " + mUsbDevice.getProductName());

            Toast.makeText(getApplicationContext(), "Conectado: " + mUsbDevice.getProductName(), Toast.LENGTH_SHORT).show();

            if (mUsbDevice.getProductName() != null && mUsbDevice.getProductName().contains("TM-T88V")) {
                Log.d("PRINTER", "TM-T88V detectada");

                detected = true;
                mTargetDevice = "USB:" + mUsbDevice.getDeviceName();

                if (detected) {
                    btnConnect.setEnabled(true);
                }
            }
        }


        btnConnect.setOnClickListener(v ->
        {
            if (detected) {
                //Conectar impresora
                try {
                    mPrinter = new Printer(Printer.TM_T88, Printer.MODEL_ANK, getApplicationContext());

                    mPrinter.setStatusChangeEventListener((printer, eventType) -> {

                        Log.d("PRINTER_EVENT", printer.toString() + ", Evento: " + eventType);


                        runOnUiThread(() -> {
                            tvPrintStatus.setText(tvPrintStatus.getText().toString().concat("\n" + makeStatusMassage(eventType)));
                            binding.scrollView.fullScroll(View.FOCUS_DOWN);
                        });
                    });

                    mPrinter.connect(mTargetDevice, Printer.PARAM_DEFAULT);
                    connected = true;

                    btnDisconnect.setVisibility(View.VISIBLE);
                    btnDisconnect.setEnabled(true);
                    btnConnect.setEnabled(false);
                    binding.btnPrint.setEnabled(true);


                    mPrinter.startMonitor();


                } catch (Epos2Exception e) {
                    Log.e("PRINTER", "Error al conectar la impresora: " + e.getMessage());
                }
            }
        });

        btnDisconnect.setOnClickListener(v -> {
            if (detected && connected) {
                try {
                    mPrinter.disconnect();

                    mPrinter.setStatusChangeEventListener(null);
                    mPrinter = null;

                    btnConnect.setEnabled(true);

                    btnDisconnect.setVisibility(View.GONE);
                    btnDisconnect.setEnabled(false);

                    tvPrintStatus.setText("");
                } catch (Epos2Exception e) {
                    Log.e("PRINTER", "Error al desconectar la impresora: " + e.getMessage());
                }
            }
        });


        binding.btnPrint.setOnClickListener(v -> {

            Bitmap resizedBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.coopeuch_android);

            if (mPrinter != null) {
                try {
                    mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                    mPrinter.addTextFont(Printer.FONT_A);
                    mPrinter.addTextSmooth(Printer.TRUE);

                    mPrinter.addImage(resizedBitmap, 0, 0,
                            resizedBitmap.getWidth(),
                            resizedBitmap.getHeight(),
                            Printer.COLOR_1,
                            Printer.MODE_MONO,
                            Printer.HALFTONE_DITHER,
                            Printer.PARAM_DEFAULT,
                            Printer.COMPRESS_NONE);

                    //SPACING
                    mPrinter.addFeedLine(1);

                    //Bienvenido
                    mPrinter.addTextStyle(Print.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.TRUE, Printer.PARAM_DEFAULT);

                    mPrinter.addTextSize(2, 2);
                    mPrinter.addText("¡Bienvenido!");

                    //SPACING
                    mPrinter.addFeedLine(3);

                    //Tu numero de atencion
                    mPrinter.addTextSize(1, 1);
                    mPrinter.addText("Tu número de atención");

                    //SPACING
                    mPrinter.addFeedLine(3);

                    //CODIGO NUMERO
                    mPrinter.addTextSize(5, 5);
                    mPrinter.addText("C17");

                    //SPACING
                    mPrinter.addFeedLine(6);

                    mPrinter.addTextSize(1, 1);
                    mPrinter.addText("Caja");
                    mPrinter.addFeedLine(2);
                    mPrinter.addText("Conoce el estado de tu ticket \n escaneando este código");

                    //SPACING
                    mPrinter.addFeedLine(2);

                    mPrinter.addSymbol("http://mipagina:8080", Printer.SYMBOL_QRCODE_MODEL_2, Printer.LEVEL_H, 8, 8, 0);

                    mPrinter.addFeedLine(1);

                    //BIENVENIDO
                    mPrinter.addTextStyle(Print.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.FALSE, Printer.PARAM_DEFAULT);
                    mPrinter.addText("BIENVENIDO A COOPEUCH");
                    mPrinter.addFeedLine(4);

                    //FOOTER
                    mPrinter.addTextStyle(Print.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.TRUE, Printer.PARAM_DEFAULT);
                    mPrinter.addText("18-12-2023 12:24:58");
                    mPrinter.addFeedLine(2);
                    mPrinter.addTextStyle(Print.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.FALSE, Printer.PARAM_DEFAULT);
                    mPrinter.addText("VISÍTANOS EN WWW.COOPEUCH.CL");

                    //FINAL CUT
                    mPrinter.addFeedLine(1);
                    mPrinter.addCut(Printer.CUT_FEED);

                    mPrinter.sendData(Printer.PARAM_DEFAULT);

                    mPrinter.clearCommandBuffer();
                } catch (Epos2Exception e) {
                    e.printStackTrace();
                    Log.e("PRINTER", "Error al imprimir el texto: " + e.getMessage() + ", " + e.getErrorStatus());
                }
            }
        });
    }


    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status,
                             final String printJobId) {
        runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                if (code == Epos2CallbackCode.CODE_SUCCESS) {
//Displays successful print messages
                    Toast.makeText(getApplicationContext(), "Print success", Toast.LENGTH_SHORT).show();
                } else {
//Displays error messages
                    Toast.makeText(getApplicationContext(), "Print failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
//Abort process
            }
        }).start();
    }

    private String makeStatusMassage(int type) {
        String msg = "";

        switch (type) {
            case Printer.EVENT_ONLINE -> msg = "ONLINE";
            case Printer.EVENT_OFFLINE -> msg = "OFFLINE";
            case Printer.EVENT_POWER_OFF -> msg = "POWER_OFF";
            case Printer.EVENT_COVER_CLOSE -> msg = "COVER_CLOSE";
            case Printer.EVENT_COVER_OPEN -> msg = "COVER_OPEN";
            case Printer.EVENT_PAPER_OK -> msg = "PAPER_OK";
            case Printer.EVENT_PAPER_NEAR_END -> msg = "PAPER_NEAR_END";
            case Printer.EVENT_PAPER_EMPTY -> msg = "PAPER_EMPTY";
            case Printer.EVENT_DRAWER_HIGH ->
                //This status depends on the drawer setting.
                    msg = "DRAWER_HIGH(Drawer close)";
            case Printer.EVENT_DRAWER_LOW ->
                //This status depends on the drawer setting.
                    msg = "DRAWER_LOW(Drawer open)";
            case Printer.EVENT_BATTERY_ENOUGH -> msg = "BATTERY_ENOUGH";
            case Printer.EVENT_BATTERY_EMPTY -> msg = "BATTERY_EMPTY";
            case Printer.EVENT_REMOVAL_WAIT_PAPER -> msg = "WAITING_FOR_PAPER_REMOVAL";
            case Printer.EVENT_REMOVAL_WAIT_NONE -> msg = "NOT_WAITING_FOR_PAPER_REMOVAL";
            case Printer.EVENT_REMOVAL_DETECT_PAPER -> msg = "REMOVAL_DETECT_PAPER";
            case Printer.EVENT_REMOVAL_DETECT_PAPER_NONE -> msg = "REMOVAL_DETECT_PAPER_NONE";
            case Printer.EVENT_REMOVAL_DETECT_UNKNOWN -> msg = "REMOVAL_DETECT_UNKNOWN";
            case Printer.EVENT_AUTO_RECOVER_ERROR -> msg = "AUTO_RECOVER_ERROR";
            case Printer.EVENT_AUTO_RECOVER_OK -> msg = "AUTO_RECOVER_OK";
            case Printer.EVENT_UNRECOVERABLE_ERROR -> msg = "UNRECOVERABLE_ERROR";
            default -> {
            }
        }
        return msg;
    }

    BroadcastReceiver usbListener = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {

                    Log.w("DEVICE_USB", device.toString());

                    Toast.makeText(getApplicationContext(), "Desconectado: " + device.getProductName(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    public void onDestroy() {
        if (mPrinter != null) {
            try {
                mPrinter.disconnect();
                mPrinter.clearCommandBuffer();

                mPrinter.setStatusChangeEventListener(null);
                mPrinter = null;
            } catch (Epos2Exception e) {
                throw new RuntimeException(e);
            }
        }

        super.onDestroy();
    }
}
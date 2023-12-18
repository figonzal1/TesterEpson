package cl.ryc.testerepson;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;

import java.util.HashMap;

import cl.ryc.testerepson.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding = null;
    Button btnConnect, btnDisconnect;
    TextView tvPrintName;
    TextView tvPrintStatus;

    private Printer mPrinter = null;
    private UsbDevice mUsbDevice = null;
    private String mTargetDevice;

    private boolean detected = false;
    private boolean connected = false;

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

        HashMap parameters = new HashMap();
        parameters.put("sFecha", fecha);
        parameters.put("sNumeroAtencion", numeroAtencion);
        parameters.put("sFila", fila);


        mUsbDevice = getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (mUsbDevice != null) {

            Log.d("DEVICE_USB", mUsbDevice.toString());

            tvPrintName.setText("Impresora detectada: " + mUsbDevice.getProductName());

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

    public void onDestroy() {
        if (mPrinter != null) {
            try {
                mPrinter.disconnect();

                mPrinter.setStatusChangeEventListener(null);
                mPrinter = null;
            } catch (Epos2Exception e) {
                throw new RuntimeException(e);
            }
        }

        super.onDestroy();
    }
}
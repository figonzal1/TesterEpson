package cl.ryc.testerepson;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.discovery.Discovery;
import com.epson.epos2.discovery.DiscoveryListener;
import com.epson.epos2.discovery.FilterOption;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.StatusChangeListener;

import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    Button btn;
    TextView txtView;

    private String targetDevice;

    private Printer mPrinter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.btn_print);
        txtView = findViewById(R.id.textView);

        String fecha = "2023-08-12";
        String numeroAtencion = "p22";
        String fila = "Ejecutivo Persona";

        HashMap parameters = new HashMap();
        parameters.put("sFecha", fecha);
        parameters.put("sNumeroAtencion", numeroAtencion);
        parameters.put("sFila", fila);

        btn.setOnClickListener(v -> {

            try {
                FilterOption filterOption = new FilterOption();
                filterOption.setPortType(Discovery.PORTTYPE_USB);
                filterOption.setDeviceModel(Discovery.MODEL_ALL);
                filterOption.setDeviceType(Discovery.TYPE_PRINTER);

                //Start discover
                Discovery.start(this, filterOption, mDiscoveryListener);

            } catch (Exception e) {
                Log.e("DISCOVERY", "discovery error");
            }
        });
    }

    private void initializeObject() {
        try {
            mPrinter = new Printer(Printer.TM_T88, Printer.MODEL_ANK, getApplicationContext());
        } catch (Exception e) {
            txtView.setText("Error el instanciar impresora");
            e.printStackTrace();
        }

        mPrinter.setStatusChangeEventListener((printer, eventType) -> {

            runOnUiThread(() -> {
                txtView.setText(makeStatusMassage(eventType));
            });
        });


        try {
            mPrinter.connect(targetDevice, Printer.PARAM_DEFAULT);

            Toast.makeText(getApplicationContext(), "Impresora conectada", Toast.LENGTH_SHORT).show();

            mPrinter.startMonitor();
        } catch (Epos2Exception e) {
            txtView.setText("Error al conectar impresora");
        }

    }

    private String makeStatusMassage(int type) {
        String msg = "";

        switch (type) {
            case Printer.EVENT_ONLINE:
                msg = "ONLINE";
                break;
            case Printer.EVENT_OFFLINE:
                msg = "OFFLINE";
                break;
            case Printer.EVENT_POWER_OFF:
                msg = "POWER_OFF";
                break;
            case Printer.EVENT_COVER_CLOSE:
                msg = "COVER_CLOSE";
                break;
            case Printer.EVENT_COVER_OPEN:
                msg = "COVER_OPEN";
                break;
            case Printer.EVENT_PAPER_OK:
                msg = "PAPER_OK";
                break;
            case Printer.EVENT_PAPER_NEAR_END:
                msg = "PAPER_NEAR_END";
                break;
            case Printer.EVENT_PAPER_EMPTY:
                msg = "PAPER_EMPTY";
                break;
            case Printer.EVENT_DRAWER_HIGH:
                //This status depends on the drawer setting.
                msg = "DRAWER_HIGH(Drawer close)";
                break;
            case Printer.EVENT_DRAWER_LOW:
                //This status depends on the drawer setting.
                msg = "DRAWER_LOW(Drawer open)";
                break;
            case Printer.EVENT_BATTERY_ENOUGH:
                msg = "BATTERY_ENOUGH";
                break;
            case Printer.EVENT_BATTERY_EMPTY:
                msg = "BATTERY_EMPTY";
                break;
            case Printer.EVENT_REMOVAL_WAIT_PAPER:
                msg = "WAITING_FOR_PAPER_REMOVAL";
                break;
            case Printer.EVENT_REMOVAL_WAIT_NONE:
                msg = "NOT_WAITING_FOR_PAPER_REMOVAL";
                break;
            case Printer.EVENT_REMOVAL_DETECT_PAPER:
                msg = "REMOVAL_DETECT_PAPER";
                break;
            case Printer.EVENT_REMOVAL_DETECT_PAPER_NONE:
                msg = "REMOVAL_DETECT_PAPER_NONE";
                break;
            case Printer.EVENT_REMOVAL_DETECT_UNKNOWN:
                msg = "REMOVAL_DETECT_UNKNOWN";
                break;
            case Printer.EVENT_AUTO_RECOVER_ERROR:
                msg = "AUTO_RECOVER_ERROR";
                break;
            case Printer.EVENT_AUTO_RECOVER_OK:
                msg = "AUTO_RECOVER_OK";
                break;
            case Printer.EVENT_UNRECOVERABLE_ERROR:
                msg = "UNRECOVERABLE_ERROR";
                break;
            default:
                break;
        }
        return msg;
    }

    private final DiscoveryListener mDiscoveryListener = deviceInfo -> runOnUiThread(new Runnable() {
        @Override
        public synchronized void run() {
            //Display the detected device in the application software
            txtView.setText("Device info: " + deviceInfo.getDeviceName() + ", " + deviceInfo.getTarget() + ", " + deviceInfo.getDeviceType());

            targetDevice = deviceInfo.getTarget();

            try {
                Discovery.stop();

                initializeObject();
            } catch (Epos2Exception e) {
                Log.e("STOP_DISCOVERY", "error al detener el discovery");
            }
        }
    });

    public void onDestroy() {
        try {
            mPrinter.disconnect();

            mPrinter.setStatusChangeEventListener(null);
            mPrinter = null;
        } catch (Epos2Exception e) {
            throw new RuntimeException(e);
        }
        super.onDestroy();
    }
}
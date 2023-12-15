package cl.ryc.testerepson;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.usb.UsbConnection;
import com.dantsu.escposprinter.connection.usb.UsbPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.discovery.Discovery;
import com.epson.epos2.discovery.DiscoveryListener;
import com.epson.epos2.discovery.FilterOption;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.StatusChangeListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

import cl.ryc.testerepson.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private static final String ACTION_USB_PERMISSION =
            "cl.ryc.testerepson.USB_PERMISSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UsbDevice device = getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);

        if (device != null) {
            Toast.makeText(getApplicationContext(), "Device: " + device.getProductName(), Toast.LENGTH_SHORT).show();
            binding.deviceName.setText("Device: " + device.getProductName());

            UsbConnection usbConnection = UsbPrintersConnections.selectFirstConnected(this);

            binding.btnPrint.setOnClickListener(view -> {
                try {
                    EscPosPrinter printer = new EscPosPrinter(usbConnection, 203, 48f, 32);
                    printer.printFormattedText("[C]<u><font size='big'>ORDER N°045</font></u>");

                } catch (EscPosConnectionException e) {
                    Log.e("PRINTER", "FAILED TO INSTANTIATE ESC/POST PRINTER");
                } catch (EscPosEncodingException | EscPosBarcodeException |
                         EscPosParserException e) {
                    Log.e("PRINTER", "ENCODING EXCEPTION");
                }
            });


        }

    }
}
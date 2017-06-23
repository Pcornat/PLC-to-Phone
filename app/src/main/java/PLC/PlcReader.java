package PLC;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.widget.TextView;

/**
 * It is the reader of the PLC, you can read all types of variable and with any type except LReal.
 * <p>
 * If you want to write without connecting and disconnecting each time, you must put
 * <code>plcWriter.setDisconnect(false)</code> just before the method
 * <code>setFields(varAddress, typeData, data)</code>
 * <p>
 * Example :
 * <pre>
 * {@code
 * try {
 *      plcReader.setSimpleConnect(false);
 *      plcReader.setFields(varAddress, typeData);
 *      plcReader.setTextView(textView);
 *      t = new Thread(plcReader);
 *      t.start();
 *      t.join();
 *      if (plcReader.getMessageErr() != 0)
 *          dealError(plcReader.getMessageErr());
 *      else {
 *          plcReader.setText();
 *      }
 * } catch (Exception e) {
 *      e.printStackTrace();
 *      dealError(e.toString());
 * }}
 * </pre>
 * <p>
 *
 * @author Denef Florent
 * @see <a href="http://snap7.sourceforge.net/snap7_client.html#1200_1500">
 * Settings for S7-1200 and S7-1500</a>
 * @since 18/05/2017
 */
public class PlcReader extends PlcReaderNoThread implements Parcelable {

    public static final Creator<PlcReader> CREATOR = new Creator<PlcReader>() {
        @Override
        public PlcReader createFromParcel(Parcel in) {
            return new PlcReader(in);
        }

        @Override
        public PlcReader[] newArray(int size) {
            return new PlcReader[size];
        }
    };
    private TextView textView;

    public PlcReader(String address, String password, boolean simpleConnect) {
        super(address, password, simpleConnect);
    }

    public PlcReader(@NonNull String address,
                     @NonNull String password,
                     boolean simpleConnect,
                     @NonNull String varAddress,
                     @NonNull String typeData,
                     @NonNull TextView textView) {
        super(address, password, simpleConnect, varAddress, typeData);
        this.textView = textView;
    }

    private PlcReader(Parcel in) {
        super(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    public void setText() {
        textView.setText(text);
    }

    public String getText() {
        return text;
    }

    public void run() {
        try {
            if (!connected) {
                ConnectTo();
                if (getMessageErr() == 0) {
                    connected = !connected;
                    if (!simpleConnect)
                        Read();
                    if (disconnect) {
                        Disconnect();
                        connected = !connected;
                    }
                } else {
                    connected = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
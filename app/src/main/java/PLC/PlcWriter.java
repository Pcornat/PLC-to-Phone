package PLC;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * It is the writer of the PLC. You can write any variable (except Input, of course)
 * of any type except LReal.
 * <p>
 * If you want to write without connecting and disconnecting each time, you must put
 * <code>plcWriter.setDisconnect(false)</code> just before the method
 * <code>setFields(varAddress, typeData, data)</code>
 * <p>
 * Example : (without the / before the @override
 * <pre>
 * {@code
 * try {
 *      new AsyncTask<Object, Object, Object>() {
 *          /@Override
 *          protected Object doInBackground(Object... objects) {
 *              plcWriter.setSimpleConnect(false);
 *              plcWriter.setFields(varAddress, typeData, data);
 *              plcWriter.run();
 *              return plcWriter.getMessageErr() == 0;
 *          }
 *
 *          /@Override
 *          protected void onPostExecute(Object o) {
 *              super.onPostExecute(o);
 *              if (!(boolean) o)
 *                  dealError(plcReader.getMessageErr());
 *              else
 *                  //You can do something here
 *          }
 *      }.execute();
 * } catch (Exception e) {
 *      e.printStackTrace();
 *      dealError(e.toString());
 * }
 * }
 * </pre>
 *
 * @author Denef Florent
 * @see <a href="http://snap7.sourceforge.net/snap7_client.html#1200_1500">
 * Settings for S7-1200 and S7-1500</a>
 * @since 18/05/2017
 */
public class PlcWriter extends PlcWriterNoThread implements Parcelable {

    public static final Creator<PlcWriter> CREATOR = new Creator<PlcWriter>() {
        @Override
        public PlcWriter createFromParcel(Parcel in) {
            return new PlcWriter(in);
        }

        @Override
        public PlcWriter[] newArray(int size) {
            return new PlcWriter[size];
        }
    };

    public PlcWriter(String address, String password, boolean simpleConnect) {
        super(address, password, simpleConnect);
    }

    public PlcWriter(@NonNull String address,
                     @NonNull String password,
                     boolean simpleConnect,
                     @NonNull String varAddress,
                     @NonNull String typeData,
                     @NonNull String data) {
        super(address, password, simpleConnect, varAddress, typeData, data);
    }

    protected PlcWriter(Parcel in) {
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

    public void run() {
        try {
            if (!connected) {
                ConnectTo();
                if (getMessageErr() == 0) {
                    connected = !connected;
                    if (!simpleConnect)
                        Write();
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
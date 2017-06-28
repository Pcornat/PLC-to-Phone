package PLC;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.electrolux.denefflo.api_to_phone.R;

import Moka7.S7;
import Moka7.S7Client;

/**
 * This is the base class of this package, all of the common methods and fields are inside it.
 */
abstract class PLC extends S7Client implements Parcelable {

    /**
     * Field which from the class is created from a Parcel
     */
    public static final Creator<PLC> CREATOR = new Creator<PLC>() {
        @Override
        public PLC createFromParcel(Parcel in) {
            return new PLC(in) {
                /**
                 * Writes inside the PLC
                 *
                 * @see PlcWriterNoThread#Write()
                 */
                @Override
                public void Write() {

                }

                /**
                 * Reads inside the PLC
                 *
                 * @see PlcReaderNoThread#Read()
                 */
                @Override
                public void Read() {

                }
            };
        }

        @Override
        public PLC[] newArray(int size) {
            return new PLC[size];
        }
    };
    protected String varAddress, typeData;
    protected int codeErr = 0, amount = 0, size = 0;
    protected boolean simpleConnect, disconnect = true, connected = false;
    /**
     * The different error code.
     */
    private int[] arCodeErr = {
            errTCPConnectionFailed,
            errTCPConnectionReset,
            errTCPDataSend,
            errTCPDataRecv,
            errTCPDataRecvTout,
            errS7DataRead,
            errS7DataWrite,
            errS7BufferTooSmall
    };
    private int word = 1, messageErr = 0;
    //This array contains all of the id of the corresponding error message.
    /**
     * The different messages of their corresponding error code. Each on of them is at the same index
     * as the error code.
     */
    private int[] arErrMessage = {
            R.string.dialog_error_connection_failed,
            R.string.dialog_error_reset,
            R.string.dialog_error_send_data,
            R.string.dialog_error_receive_data,
            R.string.dialog_error_timeout,
            R.string.dialog_error_s7_data_read,
            R.string.dialog_error_s7_data_write,
            R.string.dialog_error_buffer_too_small
    };
    /**
     * rack and slot of the PLC. In use of S7-1200 and S7-1500, it is {@code rack = 0}
     * and {@code slot = 1}
     */
    private int rack = 0, slot = 1; //For S7-1200 or S7-1500
    private String address, password;

    /**
     * The constructor, its goal is to instantiate the object
     *
     * @param address  the PLC address
     * @param password the password to set to be able to write inside the PLC.
     *                 The {@link Moka7.S7Client#SetSessionPassword(String)} method is not compatible with the S7-1200 and S7-1500
     *                 for this version of {@link Moka7}.
     */
    protected PLC(@NonNull String address, @Nullable String password, boolean simpleConnect) {
        super();
        this.address = address;
        this.password = password;
        this.simpleConnect = simpleConnect;
    }

    protected PLC(@NonNull String address,
                  @NonNull String password,
                  boolean simpleConnect,
                  @NonNull String varAddress,
                  @NonNull String typeData) {
        this.address = address;
        this.password = password;
        this.simpleConnect = simpleConnect;
        this.varAddress = varAddress.toUpperCase();
        this.typeData = typeData.toLowerCase();
    }

    protected PLC(Parcel in) {
        varAddress = in.readString();
        typeData = in.readString();
        codeErr = in.readInt();
        amount = in.readInt();
        size = in.readInt();
        simpleConnect = in.readByte() != 0;
        disconnect = in.readByte() != 0;
        connected = in.readByte() != 0;
        arCodeErr = in.createIntArray();
        word = in.readInt();
        messageErr = in.readInt();
        arErrMessage = in.createIntArray();
        rack = in.readInt();
        slot = in.readInt();
        address = in.readString();
        password = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(varAddress);
        dest.writeString(typeData);
        dest.writeInt(codeErr);
        dest.writeInt(amount);
        dest.writeInt(size);
        dest.writeByte((byte) (simpleConnect ? 1 : 0));
        dest.writeByte((byte) (disconnect ? 1 : 0));
        dest.writeByte((byte) (connected ? 1 : 0));
        dest.writeIntArray(arCodeErr);
        dest.writeInt(word);
        dest.writeInt(messageErr);
        dest.writeIntArray(arErrMessage);
        dest.writeInt(rack);
        dest.writeInt(slot);
        dest.writeString(address);
        dest.writeString(password);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * @return the address of the PLC.
     */
    public final String getAddress() {
        return address;
    }

    /**
     * @return the password of the PLC session.
     */
    public final String getPassword() {
        return password;
    }

    /**
     * @return if the plc object will just connect or connect and do its thing
     */
    public boolean isSimpleConnect() {
        return simpleConnect;
    }

    /**
     * This is used if you just want to connect without reading/writing after.
     *
     * @param simpleConnect the flag to set
     */
    public void setSimpleConnect(boolean simpleConnect) {
        this.simpleConnect = simpleConnect;
    }

    /**
     * This is used if you want to maintain the connection, in case you want to read multiple target
     * following each other
     *
     * @param disconnect the flag to set
     */
    public void setDisconnect(boolean disconnect) {
        this.disconnect = disconnect;
    }

    public final String getVarAddress() {
        return varAddress;
    }//*/

    /**
     * Set the new variable address you want to write/read to
     *
     * @param varAddress new variable address
     */
    public void setVarAddress(@NonNull String varAddress) {
        this.varAddress = varAddress;
    }

    /**
     * @return the type of data you are going to read or write
     */
    public final String getTypeData() {
        return typeData;
    }

    /**
     * Set the new type of data you want to send
     *
     * @param typeData new type of data as a String
     */
    public void setTypeData(@NonNull String typeData) {
        this.typeData = typeData;
    }

    /**
     * Get the error message to display
     *
     * @return the error message id to use
     */
    public final int getMessageErr() {
        return messageErr;
    }

    /**
     * Set the different fields of the object
     *
     * @param varAddress the address you want to set (if you control more than one PLC for example)
     * @param typeData   the type of data you want to read or write, it is very important !
     */
    public void setFields(@NonNull String varAddress, @NonNull String typeData) {
        this.varAddress = varAddress;
        this.typeData = typeData;
    }

    /**
     * Connecting to the S7 and dealing with errors
     * <p>
     * It sets the type of connection wanted (here just {@link S7#S7_BASIC})
     * then it gets the error code returned by the ConnectTo method and checks it.
     * The parameter of the ConnectTo method are the address of the PLC, its rack number and its
     * slot number (it doesn't change, rack is always equal to 0 and slot to 1 or 0 both runs on)
     *
     * @see S7Client#SetConnectionType(short)
     * @see S7Client#ConnectTo(String, int, int)
     */
    protected void ConnectTo() {
        super.SetConnectionType(S7.PG);
        codeErr = super.ConnectTo(address, rack, slot);
        checkError();
    }

    /**
     * Writes inside the PLC
     *
     * @see PlcWriterNoThread#Write()
     */
    public abstract void Write();

    /**
     * Reads inside the PLC
     *
     * @see PlcReaderNoThread#Read()
     */
    public abstract void Read();

    /**
     * Chose the amount of data to use depending of the type of data you are using.
     * The minimum is 2 because of an endian problem (except for the bool type).
     * Word is always equal to one because you can't use Counter and timer with S7-1200
     */
    protected void processData() {
        typeData = typeData.toLowerCase();
        switch (typeData) {
            case "bool":
                amount = 1;
                break;
            case "byte":
            case "sint":
            case "usint":
            case "int":
            case "uint":
            case "word":
                amount = 2;
                break;
            case "dint":
            case "dword":
            case "udint":
            case "real":
                amount = 4;
                break;
            case "lreal":
                amount = 8;
                break;
        }
        size = word * amount;
    }


    /**
     * Check the error with a code
     * <p>
     * It checks the error code at each loop, and if the test is true,
     * it returns the corresponding message for the dialog.
     * <p>
     * The message is set in the field "messageErr" if there's an error or 0 if it's good.
     */
    protected void checkError() {
        for (int i = 0; i < arCodeErr.length; i++) {
            if (codeErr == arCodeErr[i]) {
                messageErr = arErrMessage[i];
                break;
            } else
                messageErr = 0;

        }
    }
}

package PLC;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import Moka7.S7;

/**
 * This class provides all of the methods used to write inside the PLC. It does not include the
 * {@code Runnable} interface because it would have been to heavy in one file.
 *
 * @author DeneF Florent
 * @version 0.1
 * @since 16/05/2017.
 */

class PlcWriterNoThread extends PLC implements Parcelable {
    public static final Creator<PlcWriterNoThread> CREATOR = new Creator<PlcWriterNoThread>() {
        @Override
        public PlcWriterNoThread createFromParcel(Parcel in) {
            return new PlcWriterNoThread(in);
        }

        @Override
        public PlcWriterNoThread[] newArray(int size) {
            return new PlcWriterNoThread[size];
        }
    };
    protected String data;
    /**
     * The {@code data} variable is transformed and the result is stored inside this field.
     * {@code buffer} is sent to the PLC
     */
    private byte[] buffer;

    public PlcWriterNoThread(String address, String password, boolean simpleConnect) {
        super(address, password, simpleConnect);
    }

    protected PlcWriterNoThread(@NonNull String address,
                                @NonNull String password,
                                boolean simpleConnect,
                                @NonNull String varAddress,
                                @NonNull String typeData,
                                @NonNull String data) {
        super(address, password, simpleConnect, varAddress, typeData);
        this.data = data;
    }

    protected PlcWriterNoThread(Parcel in) {
        super(in);
        data = in.readString();
        buffer = in.createByteArray();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(data);
        dest.writeByteArray(buffer);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * You can change the data you want to write into the PLC
     *
     * @param data the new data you want to write
     */
    public void setData(@NonNull String data) {
        this.data = data;
    }

    /**
     * Write inside the PLC. Running in a thread other that the UI thread
     * <p>
     * First, it connects to the PLC by using the address and password given by the constructor
     * <p>
     * Then it formats the variable's address to prevent any mistakes in the treatment.
     * <p>
     * It determines the necessary size for the buffer.
     * <p>
     * After that, it chooses the type of variable it will write in. Then, it writes inside, with
     * the right type of data and the right buffer size.
     *
     * @see #writeBool()
     * @see #writeB()
     * @see #writeW()
     * @see #writeD()
     * @see #writeDB()
     * @see <a href="http://snap7.sourceforge.net/snap7_client.html#1200_1500">
     * Settings for S7-1200 and S7-1500</a>
     */
    @Override
    public void Write() {
        processData();//size = Word * Amount
        //Word = 1 for a Byte type. So 1 for 8 bits. If want to write for 16 bits, then word = 1 and amount = 2
        buffer = new byte[size];
        if (varAddress.charAt(0) == 'D') {
            writeDB();
        } else if (varAddress.charAt(1) != 'D' || varAddress.charAt(1) != 'B' || varAddress.charAt(1) != 'W')
            writeBool();
        else {
            switch (varAddress.charAt(1)) {
                case 'B':
                    writeB();
                    break;
                case 'W':
                    writeW();
                    break;
                case 'D':
                    writeD();
                    break;
            }
        }
    }

    /**
     * Set the different fields of the object
     *
     * @param varAddress the address you want to set (if you control more than one PLC for example)
     * @param typeData   the type of data you want to read or write, it is very important !
     * @param data       the data you want to write inside your PLC, <b>nullable only if you are reading</b>
     */
    public void setFields(@NonNull String varAddress, @NonNull String typeData, @NonNull String data) {
        this.varAddress = varAddress;
        this.typeData = typeData;
        this.data = data;
    }

    /**
     * Read inside the PLC
     * <p>
     * Don't use it.
     */
    @Override
    public void Read() {
    }

    /**
     * Write a boolean value in a merker, an output or a database, depends on the address.
     * <p>
     * First it set the right bit inside the buffer, and make sure the data is correct
     * <p>
     * Then it searches the "." inside the address string to have the entire number address
     *
     * @see #Write()
     */
    private void writeBool() {
        int start = 0, offset = 0;
        if (data.equals("1"))
            data = "true";
        else data = "false";
        S7.SetBitAt(buffer, start, Character.getNumericValue(varAddress.charAt(3)), Boolean.parseBoolean(data));
        for (int i = 0; i < varAddress.length(); i++) {
            if (varAddress.charAt(i) == '.') {
                offset = Integer.parseInt(varAddress.substring(1, i));
                break;//break the for loop
            }
        }
        switch (varAddress.charAt(0)) {
            case 'Q':
                codeErr = WriteArea(S7.S7AreaPA, 0, offset, amount, buffer);
                break;
            case 'M':
                codeErr = WriteArea(S7.S7AreaMK, 0, offset, amount, buffer);
                break;
        }
        checkError();
    }

    /**
     * Write an 8 bit value inside a byte variable : input, output, merker. In the use
     * it will be inside merkers.
     * <p>
     * It determines the type of data the variable is. To do so, it selects the right method :
     * an unsigned byte or a signed byte. Then it chooses between the merker or the output
     * and write it to the right place inside the PLC memory
     *
     * @see #Write()
     */
    private void writeB() {
        int start = 0;
        switch (typeData) {
            case "usint":
            case "byte":
                S7.SetWordAt(buffer, start, Integer.parseInt(data));
                break;
            case "sint":
                S7.SetShortAt(buffer, start, Integer.parseInt(data));
                break;
        }
        start = Integer.parseInt(varAddress.substring(2));
        switch (varAddress.charAt(0)) {
            case 'Q':
                codeErr = WriteArea(S7.S7AreaPA, 0, start, amount, buffer);
                break;
            case 'M':
                codeErr = WriteArea(S7.S7AreaMK, 0, start, amount, buffer);
                break;
        }
        checkError();
    }

    /**
     * Write a word inside an output : it's an ANALOG WRITE. It can write inside a merker too
     * It is a 16 bit value
     * <p>
     * It determines the type of data the variable is. To do so, it selects the right method :
     * an unsigned byte or a signed byte. Then it chooses between the merker or the output
     * and write it to the right place inside the PLC memory
     *
     * @see #Write()
     */
    private void writeW() {
        int start = 0;
        switch (typeData) {
            case "word":
            case "uint":
                S7.SetWordAt(buffer, start, Integer.parseInt(data));
                break;
            case "int":
                S7.SetShortAt(buffer, start, Integer.parseInt(data));
                break;
        }
        S7.SetWordAt(buffer, start, Integer.parseInt(data));
        start = Integer.parseInt(varAddress.substring(2));
        switch (varAddress.charAt(0)) {
            case 'Q':
                codeErr = WriteArea(S7.S7AreaPA, 0, start, amount, buffer);
                break;
            case 'M':
                codeErr = WriteArea(S7.S7AreaMK, 0, start, amount, buffer);
                break;
        }
        checkError();
    }

    /**
     * Writing a DWord, depending of the type of variable it is : a real or a simple DWord
     * <p>
     * First, it checks its type and chooses the right method, then it gets the right
     * address to where to start inside the PLC memory.
     * <p>
     * Finally it chooses between an output or a merker
     *
     * @see #Write()
     */
    private void writeD() {
        int start = 0;
        switch (typeData) {
            case "real":
                S7.SetFloatAt(buffer, start, Float.parseFloat(data));
                break;
            case "dword":
                S7.SetDWordAt(buffer, start, Integer.parseInt(data));
                break;
            case "dint":
                S7.SetDIntAt(buffer, start, Integer.parseInt(data));
                break;
        }
        start = Integer.parseInt(varAddress.substring(2));
        switch (varAddress.charAt(0)) {
            case 'Q':
                codeErr = WriteArea(S7.S7AreaPA, 0, start, amount, buffer);
                break;
            case 'M':
                codeErr = WriteArea(S7.S7AreaMK, 0, start, amount, buffer);
                break;
        }
        checkError();
    }

    /**
     * Writing inside a database, depending of the type of data it is.
     * <p>
     * First, the method gets the database number then it gets the address to where to write inside the DB.
     * Then it gets the bit to where to write if it is a boolean value.
     * Else, just the address of the bytes to write to.
     *
     * @see #Write()
     * @see <a href="http://snap7.sourceforge.net/snap7_client.html#1200_1500">
     * Settings for S7-1200 and S7-1500</a>
     */
    private void writeDB() {
        int start = 0, dbnumber = 0, bit = 0;
        for (byte i = 0; i < varAddress.length(); i++) {
            if (varAddress.charAt(i) == '.') {
                dbnumber = Integer.parseInt(varAddress.substring(2, i));
                //Gets the database number, this one DB7 for example
                break;//break the for loop
            }
        }
        byte counter = 0, firstPoint = 0, i;
        for (i = 0; i < varAddress.length(); i++) {
            counter = 0;
            firstPoint = 0;
            if (varAddress.charAt(i) == '.') {
                ++counter;
                firstPoint = i;//get the first point inside the address if it is a boolean : DB7.DBX2.3
            }
            if (varAddress.charAt(i) == '.' && counter == 2) {
                start = Integer.parseInt(varAddress.substring(firstPoint + 4));
                //Gets the byte from where to start inside the database used.
                //Example : DB7.DBX2.3 : it gets the number 2.
                bit = Integer.parseInt(varAddress.substring(i));//it gets the bit to write to
                break;//break the for loop
            }
        }
        //Now dealing with the type of data
        switch (typeData) {
            case "bool":
                if (data.equals("1"))
                    data = "true";
                start = Integer.parseInt(varAddress.substring(firstPoint + 4, i)); //In DB7.DBX3.2, it is getting the 3 for the example
                S7.SetBitAt(buffer, start, bit, Boolean.parseBoolean(data));
                break;
            case "sint":
            case "int":
                S7.SetShortAt(buffer, start, Integer.parseInt(data));
                break;
            case "uint":
            case "byte":
            case "word":
                S7.SetWordAt(buffer, start, Integer.parseInt(data));
                break;
            case "dint":
                S7.SetDIntAt(buffer, start, Integer.parseInt(data));
                break;
            case "udint":
            case "dword":
                S7.SetDWordAt(buffer, start, Integer.parseInt(data));
                break;
            case "real":
                S7.SetFloatAt(buffer, start, Float.parseFloat(data));
                break;
        }
        codeErr = WriteArea(S7.S7AreaDB, dbnumber, start, amount, buffer);
        checkError();
    }
}
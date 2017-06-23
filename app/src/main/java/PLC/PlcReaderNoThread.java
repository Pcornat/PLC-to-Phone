package PLC;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import Moka7.S7;

/**
 * This class provides all of the methods to read inside the PLC, but doesn't include the
 * {@code Runnable} interface : too heavy inside one file.
 *
 * @author Denef Florent
 * @version 0.1
 * @since 16/05/2017
 */
class PlcReaderNoThread extends PLC implements Parcelable {
    public static final Creator<PlcReaderNoThread> CREATOR = new Creator<PlcReaderNoThread>() {
        @Override
        public PlcReaderNoThread createFromParcel(Parcel in) {
            return new PlcReaderNoThread(in);
        }

        @Override
        public PlcReaderNoThread[] newArray(int size) {
            return new PlcReaderNoThread[size];
        }
    };
    protected String text;
    private byte[] buffer;

    protected PlcReaderNoThread(String address, String password, boolean simpleConnect) {
        super(address, password, simpleConnect);
    }

    protected PlcReaderNoThread(@NonNull String address,
                                @NonNull String password,
                                boolean simpleConnect,
                                @NonNull String varAddress,
                                @NonNull String typeData) {
        super(address, password, simpleConnect, varAddress, typeData);
    }

    protected PlcReaderNoThread(Parcel in) {
        super(in);
        text = in.readString();
        buffer = in.createByteArray();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(text);
        dest.writeByteArray(buffer);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * DO NOT USE IT
     */
    @Override
    public void Write() {
    }

    /**
     * Reads inside the PLC.
     * <p>
     * It determines the size of the buffer, then it chooses (switch case) the right method to use in function
     * of the variable address and its data type.
     *
     * @see #readBool()
     * @see #readB()
     * @see #readW()
     * @see #readD()
     * @see #readReal()
     * @see #readDB()
     */
    @Override
    public void Read() {
        processData();//size = Word * Amount
        //Word = 1 for a bool type.
        buffer = new byte[size];
        if (varAddress.charAt(0) == 'D') {
            readDB();
        } else {
            if (typeData.equals("bool"))
                readBool();
            else {
                switch (varAddress.charAt(1)) {
                    case 'B':
                        readB();
                    case 'W':
                        readW();
                    case 'D':
                        if (typeData.equals("real"))
                            readReal();
                        else
                            readD();
                }
            }
        }
    }

    /**
     * Reads database inside of the PLC, only if the DB is not optimized
     * <p>
     * <b>WARNING :</b> not tested yet.
     *
     * @see #Read()
     */
    private void readDB() {
        int start = 0, dbnumber = 0, bit = 0;
        for (byte i = 0; i < varAddress.length(); i++) {
            if (varAddress.charAt(i) == '.') {
                dbnumber = Integer.parseInt(varAddress.substring(2, i));
                //Gets the database number, this one DB7 for example
                break;//break the for loop
            }
        }
        codeErr = ReadArea(S7.S7AreaDB, dbnumber, start, amount, buffer);
        checkError();
        byte counter, firstPoint = 0, i;
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
                start = Integer.parseInt(varAddress.substring(firstPoint + 4, i)); //In DB7.DBX3.2, it is getting the 3 for the example
                text = String.valueOf(S7.GetBitAt(buffer, start, bit));
            case "sint":
            case "int":
                text = String.valueOf(S7.GetShortAt(buffer, start));
            case "uint":
            case "byte":
            case "word":
                text = String.valueOf(S7.GetWordAt(buffer, start));
            case "dint":
                text = String.valueOf(S7.GetDIntAt(buffer, start));
            case "udint":
            case "dword":
                text = String.valueOf(S7.GetDWordAt(buffer, start));
            case "real":
                text = String.valueOf(S7.GetFloatAt(buffer, start));
        }
    }

    /**
     * Reads a real number, a 32-bit floating point.
     *
     * @see #Read()
     */
    private void readReal() {
        int start = Integer.parseInt(varAddress.substring(2));
        switch (varAddress.charAt(0)) {
            case 'I':
                codeErr = ReadArea(S7.S7AreaPE, 0, start, amount, buffer);
                break;
            case 'Q':
                codeErr = ReadArea(S7.S7AreaPA, 0, start, amount, buffer);
                break;
            case 'M':
                codeErr = ReadArea(S7.S7AreaMK, 0, start, amount, buffer);
                break;
        }
        checkError();
        start = 0;
        text = Float.toString(S7.GetFloatAt(buffer, start));
    }

    /**
     * Reads DWord type of data.
     *
     * @see #Read()
     */
    private void readD() {
        int start = Integer.parseInt(varAddress.substring(2));
        switch (varAddress.charAt(0)) {
            case 'I':
                codeErr = ReadArea(S7.S7AreaPE, 0, start, amount, buffer);
                break;
            case 'Q':
                codeErr = ReadArea(S7.S7AreaPA, 0, start, amount, buffer);
                break;
            case 'M':
                codeErr = ReadArea(S7.S7AreaMK, 0, start, amount, buffer);
                break;
        }
        checkError();
        start = 0;
        switch (typeData) {
            case "udint":
            case "dword":
                text = Long.toString(S7.GetDWordAt(buffer, start));
            case "dint":
                text = Long.toString(S7.GetDIntAt(buffer, start));
        }
    }

    /**
     * It reads a Word type of data
     *
     * @see #Read()
     */
    private void readW() {
        //it gets the number from where to read inside the PLC memory
        int start = Integer.parseInt(varAddress.substring(2));
        //Select different kind of reading depending of the address
        switch (varAddress.charAt(0)) {
            case 'I': //Input
                codeErr = ReadArea(S7.S7AreaPE, 0, start, amount, buffer);
                break;
            case 'Q': //Output
                codeErr = ReadArea(S7.S7AreaPA, 0, start, amount, buffer);
                break;
            case 'M': //Memento
                codeErr = ReadArea(S7.S7AreaMK, 0, start, amount, buffer);
                break;
        }
        checkError(); //As the name says
        start = 0;
        switch (typeData) {
            case "uint":
            case "word":
                text = Integer.toString(S7.GetWordAt(buffer, start));
            case "int":
                text = Integer.toString(S7.GetShortAt(buffer, start));
        }
    }

    /**
     * Reads a Byte type of data
     *
     * @see #Read()
     */
    private void readB() {
        int start = Integer.parseInt(varAddress.substring(2));
        switch (varAddress.charAt(0)) {
            case 'I':
                codeErr = ReadArea(S7.S7AreaPE, 0, start, amount, buffer);
                break;
            case 'Q':
                codeErr = ReadArea(S7.S7AreaPA, 0, start, amount, buffer);
                break;
            case 'M':
                codeErr = ReadArea(S7.S7AreaMK, 0, start, amount, buffer);
                break;
        }
        checkError();
        start = 0;
        switch (typeData) {
            case "usint":
            case "byte":
                text = Integer.toString(S7.GetWordAt(buffer, start));
            case "sint":
                text = Integer.toString(S7.GetShortAt(buffer, start));
        }
    }

    /**
     * Reads a boolean value. An input, output or memento.
     *
     * @see #Read()
     */
    private void readBool() {
        int start = 0;
        //Search the number of bytes to start from. It searches for the address's number
        for (int i = 0; i < varAddress.length(); i++) {
            if (varAddress.charAt(i) == '.') {
                start = Integer.parseInt(varAddress.substring(1, i));//gets this -> Q0. The 0
            }
        }
        switch (varAddress.charAt(0)) {
            case 'I':
                codeErr = ReadArea(S7.S7AreaPE, 0, start, amount, buffer);
                break;
            case 'Q':
                codeErr = ReadArea(S7.S7AreaPA, 0, start, amount, buffer);
                break;
            case 'M':
                codeErr = ReadArea(S7.S7AreaMK, 0, start, amount, buffer);
                break;
        }
        checkError();
        //Now we get the bit to read
        for (int i = 0; i < varAddress.length(); i++) {
            if (varAddress.charAt(i) == '.') {
                start = Integer.parseInt(varAddress.substring(i + 1));
                break;
            }
        }
        text = Boolean.toString(S7.GetBitAt(buffer, 0, start));
    }
}

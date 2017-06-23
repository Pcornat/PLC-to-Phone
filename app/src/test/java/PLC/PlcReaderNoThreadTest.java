package PLC;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Denef Florent on 07/06/2017. To test
 */
public class PlcReaderNoThreadTest {
    private int searchBoolAddress(String varAddress) {
        for (int i = 0; i < varAddress.length(); i++) {
            if (varAddress.charAt(i) == '.') {
                return Integer.parseInt(varAddress.substring(1, i));//gets this -> Q0. The 0
            }
        }
        return 0;
    }

    @Test
    public void testSearchBoolAddress() throws Exception {
        assertEquals(3, searchBoolAddress("Q3.2"));
    }

}
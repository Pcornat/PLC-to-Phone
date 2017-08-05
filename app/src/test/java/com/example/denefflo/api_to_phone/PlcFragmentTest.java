package com.example.denefflo.api_to_phone;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by DenefFlo on 08/06/2017.
 */
public class PlcFragmentTest {
    private List<String> listVarAddress = new ArrayList<>();

    private int numberOfIteration() {
        listVarAddress.add("MW500");
        for (byte i = 0; i < 30; i += 2)
            listVarAddress.add("MW50" + String.valueOf(i));
        return listVarAddress.size();
    }

    @Test
    public void test() throws Exception {
        assertEquals(16, numberOfIteration());
    }


}
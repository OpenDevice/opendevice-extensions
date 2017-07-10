/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package io.opendevice.ext.obd.commands;

import io.opendevice.ext.obd.OBDATCommand;

import java.util.LinkedList;
import java.util.List;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 *         Date: 28/05/17
 */
public abstract class PIDSensorCommand extends OBDATCommand {

    public PIDSensorCommand(String command) {
        super(command);
    }

    public abstract long parse(byte[] response);

    public boolean match(byte[] response){

        try{
            List<Integer> resp = parseInts(response);
            List<Integer> cmd = parseInts(getBytes());

            if(resp.get(0) - 64/*0x40*/ == cmd.get(0) && resp.get(1) == cmd.get(1)){
                return true;
            }
        }catch (Exception ex){
            ex.printStackTrace();
            // ignore number formar exception...
        }

        return false;
    };


    /**
     * Convert response in HEXADECIMAL to Integer array
     * @param respose
     * @return
     */
    public List<Integer> parseInts(byte[] respose){
        String raw = new String(respose);
        raw = raw.replaceAll(" ", ""); // remove blank
        List<Integer> parsed = new LinkedList<Integer>();

        int begin = 0;
        int end = 2;
        while (end <= raw.length()) {
            parsed.add(Integer.decode("0x" + raw.substring(begin, end)));
            begin = end;
            end += 2;
        }

        return parsed;
    }
}

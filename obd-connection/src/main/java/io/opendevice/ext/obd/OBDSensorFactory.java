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

package io.opendevice.ext.obd;

import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Ricardo JL Rufino
 *         Date: 28/05/17
 */
public class OBDSensorFactory {

    public static OBDSensor create(String boardName, OBDSensorPID pid) {
        OBDSensor sensor = new OBDSensor(boardName + ":" + pid.name().toLowerCase(), pid, pid.getType());
        sensor.setTitle(pid.getDescription());
        sensor.setDateCreated(new Date());
        return sensor;
    }

    public static List<OBDSensor> create(String boardName, String pidsAvailable) {

        List<OBDSensor> list = new LinkedList<OBDSensor>();

        // return response for 0..32 PIDs
        String pids = pidsAvailable.substring(4, pidsAvailable.length());
        String bitsStr = new BigInteger(pids, 16).toString(2);

        int pidIndex = 1;
        int mode = 1;
        for (int i = 0; i < bitsStr.length(); i++) {
            if(bitsStr.charAt(i) == '1'){
                OBDSensorPID sensorPID = OBDSensorPID.find(mode, pidIndex);
                if(sensorPID != null) list.add(create(boardName, sensorPID));
                System.out.println(pidIndex + " = " + sensorPID + " >> " + (sensorPID != null ? sensorPID.getCommand() : "null"));
            }
            pidIndex++;
        }

        return list;
    }


    public static void main(String[] args) {
        OBDSensorFactory.create("teste", "4100BE3EB811");
    }
}

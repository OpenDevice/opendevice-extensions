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

import br.com.criativasoft.opendevice.core.model.DeviceType;
import io.opendevice.ext.obd.commands.GenericPercentCommand;
import io.opendevice.ext.obd.commands.GenericTemperatureCommand;
import io.opendevice.ext.obd.commands.PIDSensorCommand;
import io.opendevice.ext.obd.commands.SingleValueCommand;
import io.opendevice.ext.obd.commands.impl.*;

/**
 * @author Ricardo JL Rufino
 *         Date: 28/05/17
 */
public enum OBDSensorPID {
    ENGINE_LOAD(1, 4, "Engine Load", new GenericPercentCommand("01 04 1")),
    ENGINE_TEMPERATURE(1, 5, "Engine coolant temperature", new GenericTemperatureCommand("01 05 1")),
    FUEL_TRIM_SHORT_B1(1, 6, "Short term fuel trim—Bank 1", new FuelTrimCommand("01 06 1"), DeviceType.FLOAT2),
    FUEL_TRIM_LONG_B1(1, 7, "Long term fuel trim—Bank 1", new FuelTrimCommand("01 07 1"), DeviceType.FLOAT2),
    FUEL_TRIM_SHORT_B2(1, 8, "Short term fuel trim—Bank 2", new FuelTrimCommand("01 08 1"), DeviceType.FLOAT2),
    FUEL_TRIM_LONG_B2(1, 9, "Long term fuel trim—Bank 2", new FuelTrimCommand("01 09 1"), DeviceType.FLOAT2),
    FUEL_PRESSURE(1, 10, "Fuel pressure", new FuelPressure()),
    INTAKE_PRESSURE(1, 11, "MAP Sensor", new SingleValueCommand("01 0B 1")),
    ENGINE_RPM(1, 12, "Engine RPM", new RPMCommand()),
    SPEED(1, 13, "Vehicle speed", new SingleValueCommand("01 0D 1")),
    TIMING_ADVANCE(1, 14, "Timing advance", new TimingAdvance()),
    INTAKE_TEMPERATURE(1, 15, "Intake air temperature", new GenericTemperatureCommand("01 0F 1")),
    MAF(1, 16, "MAF air flow rate", new MassAirFlow(), DeviceType.FLOAT2),
    THROTTLE_POSITION(1, 17, "Throttle position", new GenericPercentCommand("01 11 1")),
    ;

    OBDSensorPID(int mode, int pid, String description, PIDSensorCommand command){
        this.mode = mode;
        this.pid = pid;
        this.description = description;
        this.command = command;
        this.type = DeviceType.ANALOG;
    }

    OBDSensorPID(int mode, int pid, String description, PIDSensorCommand command, DeviceType type){
        this.mode = mode;
        this.pid = pid;
        this.description = description;
        this.command = command;
        this.type = type;
    }

    private int mode;
    private int pid;
    private String description;
    private PIDSensorCommand command;
    private DeviceType type;

    public PIDSensorCommand getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public int getMode() {
        return mode;
    }

    public int getPid() {
        return pid;
    }

    public DeviceType getType() {
        return type;
    }


    public static final OBDSensorPID find(int mode, int pidIndex){
        OBDSensorPID[] values = values();
        for (OBDSensorPID value : values) {
            if(value.getMode() == mode && value.getPid() == pidIndex){
                return value;
            }
        }

        return null;
    }
}

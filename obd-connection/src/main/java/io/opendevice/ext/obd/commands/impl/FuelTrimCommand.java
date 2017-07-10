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

package io.opendevice.ext.obd.commands.impl;

import io.opendevice.ext.obd.commands.PIDSensorCommand;

import java.util.List;

/**
 * @author Ricardo JL Rufino
 *         Date: 10/06/17
 */
public class FuelTrimCommand extends PIDSensorCommand {

    public FuelTrimCommand(String command) {
        super(command);
    }

    @Override
    public long parse(byte[] response) {
        List<Integer> values = parseInts(response);
        float rawValue = ((values.get(2) / 1.28f) - 100);
        return Math.round(rawValue*100); // round values after 2 decimal places
    }
}

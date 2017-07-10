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
 *         Date: 28/05/17
 */
public class RPMCommand extends PIDSensorCommand {

    public RPMCommand() {
        super("01 0C 1");
    }

    public long parse(byte[] response){
        List<Integer> values = parseInts(response);
        return (values.get(2) * 256 + values.get(3)) / 4;
    }
}

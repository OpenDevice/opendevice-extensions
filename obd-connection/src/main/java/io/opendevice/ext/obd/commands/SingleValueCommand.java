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

import java.util.List;

/**
 * Base commands for returning single and direct value
 *
 * @author Ricardo JL Rufino
 *         Date: 03/06/17
 */
public class SingleValueCommand extends PIDSensorCommand  {

    public SingleValueCommand(String pid) {
        super(pid);
    }

    public long parse(byte[] response){
        List<Integer> values = parseInts(response);
        // ignore first two bytes [hh hh] of the response
        return values.get(2);
    }
}

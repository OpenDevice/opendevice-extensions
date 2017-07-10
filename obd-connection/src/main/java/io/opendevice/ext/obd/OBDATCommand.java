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

import br.com.criativasoft.opendevice.connection.message.SimpleMessage;

/**
 * @author Ricardo JL Rufino
 *         Date: 28/05/17
 */
public class OBDATCommand extends SimpleMessage {

    public static final OBDATCommand RESET = new OBDATCommand("ATZ");
    public static final OBDATCommand ECHO_ON = new OBDATCommand("ATE1");
    public static final OBDATCommand ECHO_OFF = new OBDATCommand("ATE0");
    public static final OBDATCommand SPACE_ON = new OBDATCommand("ATS1");
    public static final OBDATCommand SPACE_OFF = new OBDATCommand("ATS0");
    public static final OBDATCommand LINE_FEED_ON = new OBDATCommand("ATL1");
    public static final OBDATCommand LINE_FEED_OFF = new OBDATCommand("ATL0");
    public static final OBDATCommand ADAPTIVE_TIMING = new OBDATCommand("ATAT1");
    public static final OBDATCommand PROTOCOL_AUTO = new OBDATCommand("AT SP 0");

    public static final OBDATCommand GET_PIDS_1TO20 = new OBDATCommand("0100");

    public OBDATCommand(String command) {
        super(command);
    }


}

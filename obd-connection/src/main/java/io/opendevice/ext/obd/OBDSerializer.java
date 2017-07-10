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

import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.message.SimpleMessage;
import br.com.criativasoft.opendevice.connection.serialize.StreamSerializer;

import java.util.regex.Pattern;

/**
 * @author Ricardo JL Rufino
 *         Date: 28/05/17
 */
public class OBDSerializer extends StreamSerializer {
    private OBDATCommand lastCommand;

    private static Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

    /**
     * Tha data received from ELM32X is is encoded as string where each byte is a char. <br/>
     * The response ends with two carriage return characters, so we need clean up.
     * @param data
     * @return
     */
    @Override
    public Message parse(byte[] data) {

        String rawData = new String(data);

        // Data may have echo or informative text like "INIT BUS..." or similar.
        rawData = rawData.replaceAll("SEARCHING...","");

        rawData = removeAll(WHITESPACE_PATTERN, rawData);//removes all [ \t\n\x0B\f\r]

        return new SimpleMessage(rawData.getBytes());
    }

    @Override
    public byte[] serialize(Message message) {
        lastCommand = (OBDATCommand) message;
        String s = message.toString();
        s = s.concat("\r");
        return s.getBytes();
    }

    private String removeAll(Pattern pattern, String input) {
        return pattern.matcher(input).replaceAll("");
    }
}

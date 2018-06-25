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

package io.opendevice.sonoff.config;

import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.extension.OpenDeviceExtension;
import io.opendevice.sonoff.SonOffServerConnection;


public class ExtensionPoint extends OpenDeviceExtension {

    @Override
    public String getName() {
        return "sonoff-bridge";
    }

    @Override
    public String getDescription() {
        return "Sonoff transparent integration with OpenDevice ";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public void init(DeviceManager manager) {
        manager.addOutput(SonOffServerConnection.getInstance());
    }

    @Override
    public void destroy() {

    }


}
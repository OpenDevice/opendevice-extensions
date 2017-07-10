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
import br.com.criativasoft.opendevice.core.model.PhysicalDevice;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.opendevice.ext.obd.commands.PIDSensorCommand;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * @author Ricardo JL Rufino
 * Date: 28/05/17
 */
@Entity
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="name")
public class OBDSensor extends PhysicalDevice {

    @Transient
    @JsonIgnore
    private OBDSensorPID pid;

    private boolean enabled = false;

    public OBDSensor(String name, OBDSensorPID pid) {
        this(name, pid, DeviceType.ANALOG);
    }

    public OBDSensor(String name, OBDSensorPID pid, DeviceType type) {
        super(-1, type);
        setName(name);
        setValue(0);
        this.pid = pid;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        notifyListeners(false);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public PIDSensorCommand getCommand() {
        return pid.getCommand();
    }

    public OBDSensorPID getPid() {
        return pid;
    }
}

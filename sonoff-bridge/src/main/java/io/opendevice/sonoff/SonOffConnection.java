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

package io.opendevice.sonoff;

import br.com.criativasoft.opendevice.connection.AbstractConnection;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.listener.DeviceListener;
import br.com.criativasoft.opendevice.core.model.Board;
import br.com.criativasoft.opendevice.core.model.Device;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Ricardo JL Rufino
 *         Date: 03/06/18
 */
public class SonOffConnection extends AbstractConnection {

    @JsonProperty("deviceid")
    private String deviceID;
    private String version;
    private String firmware;
    private String model;
    private String apiKey;
    private String registrationKey;

    @JsonIgnore
    private transient Board board;

    private List<Switch> switches = new LinkedList<Switch>();

    @Override
    public void send(Message message) throws IOException {

    }

    @Override
    public void connect() throws ConnectionException {
        setStatus(ConnectionStatus.CONNECTED);
    }

    @Override
    public void disconnect() throws ConnectionException {
        setStatus(ConnectionStatus.DISCONNECTED);
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFirmware() {
        return firmware;
    }

    public void setFirmware(String firmware) {
        this.firmware = firmware;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setRegistrationKey(String registrationKey) {
        this.registrationKey = registrationKey;
    }

    public String getRegistrationKey() {
        return registrationKey;
    }

    public List<Switch> getSwitches() {
        return switches;
    }

    public void setSwitches(List<Switch> switches) {
        this.switches = switches;
    }

    @JsonIgnore
    public Device getDevice() {
        return board;
    }

    public void setDevice(Board device) {
        this.board = device;
    }

    // =========================================
    // Switch
    // =========================================

    public static final class Switch implements DeviceListener{

        @JsonIgnore
        private SonOffConnection parent;

        @JsonIgnore
        private transient Device physicalDevice;

        @JsonProperty("switch")
        private String status;

        private int outlet;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;

            if(physicalDevice != null){
                int value = (status.equals("on") ? 1 : 0);
                TenantProvider.setCurrentID(parent.getApplicationID());
                physicalDevice.setValue(value);
            }
        }

        public int getOutlet() {
            return outlet;
        }

        public void setOutlet(int outlet) {
            this.outlet = outlet;
        }

        public void setPhysicalDevice(Device physicalDevice) {
            this.physicalDevice = physicalDevice;
            physicalDevice.addListener(this);
        }

        public Device getPhysicalDevice() {
            return physicalDevice;
        }


        public void setParent(SonOffConnection parent) {
            this.parent = parent;
        }

        public SonOffConnection getParent() {
            return parent;
        }

        @Override
        public void onDeviceChanged(Device device) {
            if(device.isON()) status = "on";
            else status = "off";
            SonOffServerConnection.getInstance().sendDeviceChange(parent, this);
        }

        @Override
        public void onDeviceRegistred(Device device) {

        }
    }

}

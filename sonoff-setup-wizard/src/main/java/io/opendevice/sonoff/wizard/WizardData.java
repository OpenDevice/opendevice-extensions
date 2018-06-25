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

package io.opendevice.sonoff.wizard;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class WizardData {

    private final StringProperty currentIP = new SimpleStringProperty();
    private final StringProperty field2 = new SimpleStringProperty();
    private final StringProperty field3 = new SimpleStringProperty();

    private final StringProperty wifiAp = new SimpleStringProperty();
    private final StringProperty wifiKey = new SimpleStringProperty();

    private final StringProperty server = new SimpleStringProperty();
    private final StringProperty serverApiKey = new SimpleStringProperty();

    private final StringProperty deviceID = new SimpleStringProperty();
    private final StringProperty sonoffApiKey = new SimpleStringProperty();

    public void setWifiAp(String wifiAp) {
        this.wifiAp.set(wifiAp);
    }

    public String getWifiAp() {
        return wifiAp.get();
    }

    public StringProperty wifiApProperty() {
        return wifiAp;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID.set(deviceID);
    }

    public String getDeviceID() {
        return deviceID.get();
    }

    public StringProperty deviceIDProperty() {
        return deviceID;
    }

    public String getCurrentIP() {
        return currentIP.get();
    }

    public StringProperty currentIPProperty() {
        return currentIP;
    }

    public void setCurrentIP(String currentIP) {
        this.currentIP.set(currentIP);
    }


    public String getServerApiKey() {
        return serverApiKey.get();
    }

    public StringProperty serverApiKeyProperty() {
        return serverApiKey;
    }

    public void setServerApiKey(String serverApiKey) {
        this.serverApiKey.set(serverApiKey);
    }

    public String getSonoffApiKey() {
        return sonoffApiKey.get();
    }

    public StringProperty sonoffApiKeyProperty() {
        return sonoffApiKey;
    }

    public void setSonoffApiKey(String sonoffApiKey) {
        this.sonoffApiKey.set(sonoffApiKey);
    }

    public String getWifiKey() {
        return wifiKey.get();
    }

    public StringProperty wifiKeyProperty() {
        return wifiKey;
    }

    public void setWifiKey(String wifiKey) {
        this.wifiKey.set(wifiKey);
    }

    public String getServer() {
        return server.get();
    }

    public StringProperty serverProperty() {
        return server;
    }

    public void setServer(String server) {
        this.server.set(server);
    }

    public void reset() {
        currentIP.set("");
        field2.set("");
        field3.set("");
        serverApiKey.set("");
        sonoffApiKey.set("");
        wifiKey.set("");
        server.set("");
    }
}

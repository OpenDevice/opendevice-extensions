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

import io.opendevice.sonoff.wizard.annotations.Submit;
import io.opendevice.sonoff.wizard.annotations.Validate;
import io.opendevice.sonoff.wizard.utils.AlertUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executor;

public class StepSettings {

    private Logger log = LoggerFactory.getLogger(StepSettings.class);

    @FXML
    TextField rfWifiApConf, tfWifiKeyConf, tfServerConf, tfServerKey;

    @Inject
    WizardData model;

    @Inject
    private Executor executor;

    @FXML
    public void initialize() {

        rfWifiApConf.textProperty().bindBidirectional(model.wifiApProperty());
        tfWifiKeyConf.textProperty().bindBidirectional(model.wifiKeyProperty());
        tfServerConf.textProperty().bindBidirectional(model.serverProperty());
        tfServerKey.textProperty().bindBidirectional(model.serverApiKeyProperty());

        File file = getSettingsFile();
        if(file.exists()) {
            try {
                String resp = new String(Files.readAllBytes(file.toPath()));
                JsonParser parser = new JsonParser();
                JsonObject json = (JsonObject) parser.parse(resp);

                model.setWifiAp(json.get("wifiAp").getAsString());
                model.setWifiKey(json.get("wifiKey").getAsString());
                model.setServer(json.get("server").getAsString());
                model.setServerApiKey(json.get("serverApiKey").getAsString());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        // get current AP SSID
//        if(model.wifiApProperty().isEmpty().get()){
//            executor.execute(() -> {
//                String wifi_ssid = NetworkUtil.getWifi_SSID();
//                if(wifi_ssid != null){
//                    log.debug("Detected current Wifi SSID - " + wifi_ssid);
//                    model.setWifiAp(wifi_ssid);
//                }
//            });
//        }
    }

    @Validate
    public boolean validate() throws Exception {

        if(model.wifiApProperty().isEmpty().get() ||
                model.wifiKeyProperty().isEmpty().get() ||
                model.serverProperty().isEmpty().get() ||
                model.serverApiKeyProperty().isEmpty().get()){

            AlertUtils.error("Required Fields", "All fields are required !");
            return false;
        }

        return true;
    }

    @Submit
    public void submit() throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("wifiAp", model.getWifiAp());
        json.addProperty("wifiKey", model.getWifiKey());
        json.addProperty("server", model.getServer());
        json.addProperty("serverApiKey", model.getServerApiKey());

        File file = getSettingsFile();
        if(!file.exists()) file.createNewFile();

        Files.write(Paths.get(file.getPath()), json.toString().getBytes());
        log.debug("[SUBMIT] saving settings : " + file);
    }

    private File getSettingsFile()  {
        String path = WizardMain.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = null;
        try {
            decodedPath = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            decodedPath = path;
        }
        File file = new File(decodedPath, "settings.json");

        return file;
    }


}

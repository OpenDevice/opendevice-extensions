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

import io.opendevice.sonoff.wizard.annotations.OnShow;
import io.opendevice.sonoff.wizard.annotations.Submit;
import io.opendevice.sonoff.wizard.annotations.Validate;
import io.opendevice.sonoff.wizard.utils.NetworkUtil;
import io.opendevice.sonoff.wizard.utils.SystemUtils;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

public class StepConfigureAP {

    private Logger log = LoggerFactory.getLogger(StepConfigureAP.class);

    @FXML
    TextField rfWifiAp, tfDeviceID, tfDeviceApyKey;

    @FXML
    private ProgressIndicator wifiProgress;

    @Inject
    WizardData model;

    @Inject
    private Executor executor;

    @Inject
    private OkHttpClient client;

    private Timer timer = null;

    private boolean valid = false;

    public static final MediaType JSON  = MediaType.parse("application/json");

    @FXML
    public void initialize() {
        rfWifiAp.textProperty().bind(model.wifiApProperty());
        tfDeviceID.textProperty().bind(model.deviceIDProperty());
        tfDeviceApyKey.textProperty().bind(model.sonoffApiKeyProperty());
    }

    @OnShow
    public void onShow() throws Exception {
        wifiProgress.setVisible(false);
    }

    @Validate
    public boolean validate() throws Exception {

        if(valid) return true;

        if(timer == null){
            wifiProgress.setVisible(true);
            executor.execute((Runnable) () -> configureDevice());
        }

        return valid;
    }

    @Submit
    public void submit() throws Exception {

    }

    private void configureDevice(){

        if(client == null) client = new OkHttpClient();

        String[] serverPort =  model.getServer().split(":");
        int port = serverPort.length > 1 ? Integer.parseInt(serverPort[1]) : 443;

        JsonObject json = new JsonObject();
        json.addProperty("version", 4);
        json.addProperty("ssid", model.getWifiAp());
        json.addProperty("password", model.getWifiKey());
        json.addProperty("serverName", serverPort[0]);
        json.addProperty("port", port);

        log.debug(" >>> Send: " + json.toString());

        RequestBody body = RequestBody.create(JSON, json.toString());

        Request request = new Request.Builder()
                .url("http://10.10.7.1/ap")
                .header("User-Agent", "app")
                .header("Accept", "application/json")
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String res = response.body().string();
            log.info("<<< Response : " + res);

            if(response.isSuccessful()){
                if(timer != null) timer.cancel();
                timer = new Timer();
                timer.scheduleAtFixedRate(checkIPTask, 1000, 1000);

                if(SystemUtils.getOSType() == SystemUtils.OSType.Linux){
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            log.debug("Forcing connection using: nmcli c up");
                            String out = SystemUtils.execForResult("nmcli c up " + model.getWifiAp());
                            log.debug("Result:" + out);
                        }
                    });
                }

                Platform.runLater(() -> {
                    wifiProgress.setProgress(0.2);
                });
            }

        } catch (IOException e) {
            Platform.runLater(() -> {
                wifiProgress.setProgress(0);
            });
        }
    }


    TimerTask checkIPTask = new TimerTask() {
        public void run() {
            String ip = NetworkUtil.getIp();
            log.debug("Current IP: " +ip);

            // Fake progress
            Platform.runLater(() -> {
                double progress = wifiProgress.getProgress() + 0.05;
                if(progress < 0.8){
                    wifiProgress.setProgress(progress);
                }else{
                    wifiProgress.setProgress(-1);
                }
            });

            if (ip != null && !ip.startsWith("10.10.7")) {
                timer.cancel();
                timer = null;
                valid = true;
                Platform.runLater(() -> {
                    wifiProgress.setProgress(1);
                    try {Thread.sleep(1000); } catch (InterruptedException e) {}
                    WizardController.getInstance().next();
                });
            }
        }
    };

    public static void main(String[] args) {
        new StepConfigureAP().configureDevice();
    }
}



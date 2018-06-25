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
import io.opendevice.sonoff.wizard.annotations.Validate;
import io.opendevice.sonoff.wizard.utils.AlertUtils;
import io.opendevice.sonoff.wizard.utils.NetworkUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class StepWaitSonOff {

    private Logger log = LoggerFactory.getLogger(StepWaitSonOff.class);

    @Inject
    WizardData model;

    @FXML
    private ProgressIndicator step2Progress;

    private Timer timer = null;

    @Inject
    private OkHttpClient client;

    public static final long TIME = 1000;

    @OnShow
    public void onShow() {
//        tfField4.textProperty().bindBidirectional(model.field4Property());
        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(checkIPTask, TIME, TIME);
        }

        step2Progress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
    }

    @Validate
    public boolean validate() throws Exception {

        if( model.getDeviceID() == null ) {
            AlertUtils.error( "Not completed !",  "This step has not completed !");
            return false;
        }

        return true;
    }

    TimerTask checkIPTask = new TimerTask() {
        public void run() {
            String ip = NetworkUtil.getIp();
            if (ip != null && ip.startsWith("10.10.7")) {

                Platform.runLater(() -> {
                    step2Progress.setProgress(0.5);
                });

                timer.cancel();
                getDeviceSettings();
            }
        }

    };

    private void getDeviceSettings() {
        
        Request request = new Request.Builder()
                .url("http://10.10.7.1/device")
                .build();

        try {
            Response response = client.newCall(request).execute();
            String resp = response.body().string();
            log.debug("resp = "  +resp);

            JsonParser parser = new JsonParser();
            JsonObject json = (JsonObject) parser.parse(resp);
            model.setDeviceID(json.get("deviceid").getAsString());
            model.setSonoffApiKey(json.get("apikey").getAsString());

            Platform.runLater(new Runnable() {
                @Override public void run() {
                    step2Progress.setProgress(1);
                    try {Thread.sleep(1000); } catch (InterruptedException e) {}
                    WizardController.getInstance().next();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

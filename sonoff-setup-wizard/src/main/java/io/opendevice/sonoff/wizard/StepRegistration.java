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
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static io.opendevice.sonoff.wizard.StepConfigureAP.JSON;

/**
 * Call OpenDevice service for finish registration of Device
 */
public class StepRegistration {

    private Logger log = LoggerFactory.getLogger(StepRegistration.class);

    @Inject
    WizardData model;

    @FXML
    private ProgressIndicator registrationProgress;

    @FXML
    Label lbStatus, lbStatusText;

    private Timer timer = null;

    @Inject
    private OkHttpClient client;

    private int tryCount = 0;

    @FXML
    public void initialize() {

    }

    @OnShow
    public void onShow() throws Exception {
        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(checkRegistration, 5000, 5000);
        }

        registrationProgress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
    }


    TimerTask checkRegistration = new TimerTask() {
        public void run() {

//            if(client == null) client = new OkHttpClient();

            Platform.runLater(() -> {
                registrationProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                lbStatusText.setText("Waiting the device registration : try " + (tryCount++));
            });


            JsonObject json = new JsonObject();
            json.addProperty("deviceID", model.getDeviceID());
            json.addProperty("deviceApiKey", model.getSonoffApiKey());
            json.addProperty("apiKey", model.getServerApiKey());
            json.addProperty("wifi", model.getWifiAp());

            log.debug(" >>> Send: " + json.toString());

            RequestBody body = RequestBody.create(JSON, json.toString());

            String[] serverPort =  model.getServer().split(":");
            int port = serverPort.length > 1 ? Integer.parseInt(serverPort[1]) : 443;


            Request request = new Request.Builder()
                    .url("https://"+serverPort[0]+":"+port+"/dispatch/register")
                    .header("User-Agent", "Sonoff Configure App")
                    .header("Accept", "application/json")
                    .post(body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String res = response.body().string();
                log.info("<<< Response : " + res);

                if(response.isSuccessful()){

                    timer.cancel();

                    Platform.runLater(() -> {
                        registrationProgress.setProgress(1);
                        lbStatus.setText("Registration Complete");
                        lbStatusText.setText("Device successfully registered");
                    });
                }

            } catch (IOException e) {
                e.printStackTrace(System.err);
                Platform.runLater(() -> {
                    lbStatus.setText("IO Error");
                    lbStatusText.setText(e.getMessage());
                });
            }
        }

    };
}

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
import br.com.criativasoft.opendevice.connection.ConnectionManager;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.ServerConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.message.Request;
import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.ODev;
import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import br.com.criativasoft.opendevice.wsrest.guice.GuiceInjectProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.nettosphere.Config;
import org.atmosphere.nettosphere.Nettosphere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.*;
import java.util.Collection;

/**
 * <pre>
 * SonOff protocol bridge to OpenDevice.
 * It starts a WebSocket server because it is not possible (or viable) to use the default OpenDevice server
 *
 * Requied configuration properties:
 * sonoff.ssl.certificateFile=conf/sonoff.cert.pem
 * sonoff.ssl.certificateKey=conf/sonoff.key.pem
 *
 * Tested Devices:
 * - SonoffDual
 *
 * See: https://www.websequencediagrams.com/
 * Flow:
 * <code>
     ClientSetup->Device: get /device
     Device --> ClientSetup: {deviceID,ApiKey}
     ClientSetup->Device: post {ssid, key, ip, port}
     Device -> Router: connect
     Device -> ClientWS: [https] /dispatch/device (get websocket)
     ClientWS --> Device : return ws config {ip, port}
     Device -> ClientWS: connect ws
     Device -> ClientWS: [ws][/api/ws] <register> {device spec}
     Device -> ClientWS: [ws] /date
     Device -> ClientWS: [ws] /update
     Device -> ClientWS: [ws] /update
     Device -> ClientWS: [ws] /update
 * </code>
 *
 * References:
 *  - https://wiki.almeroth.com/doku.php?id=projects:sonoff
 *  - https://blog.nanl.de/2017/05/sonota-flashing-itead-sonoff-devices-via-original-ota-mechanism/
 *  - https://blog.ipsumdomus.com/sonoff-switch-complete-hack-without-firmware-upgrade-1b2d6632c01
 *
 * </pre>
 * @author Ricardo JL Rufino
 * Date: 03/06/18
 */
public class SonOffServerConnection extends AbstractConnection implements ServerConnection{

    public static int PORT = 8989;

    private static final Logger log = LoggerFactory.getLogger(Nettosphere.class);

    private ObjectMapper mapper;

    private DeviceManager manager;

    private static SonOffServerConnection INSTANCE;

    public static SonOffServerConnection getInstance() {
        if(INSTANCE == null) INSTANCE = new SonOffServerConnection();
        return INSTANCE;
    }

    private SonOffServerConnection(){
        mapper = new ObjectMapper();
    }

    private Nettosphere server;

    private BroadcasterFactory broadcasterFactory;

    @Override
    public void connect() throws ConnectionException {

        Config.Builder builder = new Config.Builder()
                .host("0.0.0.0")
                .port(PORT)
                .resource(GuiceInjectProvider.class)
                .resource(WebSocketResource.class)
                .resource(SonOffRest.class);

        builder.initParam("org.atmosphere.websocket.messageContentType","application/json");
        builder.initParam("org.atmosphere.websocket.messageMethod","POST");
        builder.initParam("com.sun.jersey.api.json.POJOMappingFeature","true");
        builder.initParam(ApplicationConfig.SCAN_CLASSPATH,"false");

        builder.sslContext(generateSSLContext());

        server = new Nettosphere.Builder().config(builder.build()).build();
        broadcasterFactory = server.framework().getBroadcasterFactory();
        server.start();

        log.info("Sonofff Server started at : " + PORT);

        setStatus(ConnectionStatus.CONNECTED);
    }

    @Override
    public void disconnect() throws ConnectionException {
        server.stop();
        setStatus(ConnectionStatus.DISCONNECTED);
    }

    @Override
    public void send(Message message) throws IOException {

    }

    @Override
    public void setConnectionManager(ConnectionManager manager) {
        super.setConnectionManager(manager);
        if(manager instanceof DeviceManager){
            this.manager = (DeviceManager) manager;
        }
    }

    private SslContext generateSSLContext(){

//        File certFile = config.getFile("sonoff.ssl.certificateFile");
//        if(cert == null) throw new IllegalArgumentException("Certificate not found (check sonoff.ssl.certificateFile) !");
//        File key = config.getFile("sonoff.ssl.certificateKey");
//        if(key == null) throw new IllegalArgumentException("Certificate key must be provided (check sonoff.ssl.certificateKey) !");

        OpenDeviceConfig config = ODev.getConfig();

        InputStream cert = null;
        InputStream key  = null;

        try {
            File certFile = config.getFile("sonoff.ssl.certificateFile");
            if(certFile != null){
                cert = new FileInputStream(certFile);
            }else{
                log.info("Using self-signed embedded certificate ...");
                cert  = getClass().getClassLoader().getResourceAsStream("ssl/cert.pem");
            }

            File keyFile = config.getFile("sonoff.ssl.certificateKey");
            if(keyFile != null){
                key = new FileInputStream(keyFile);
            }else{
                key  = getClass().getClassLoader().getResourceAsStream("ssl/key.pem");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(cert, key);
            sslContextBuilder.sslProvider(SslProvider.JDK);
            SslContext sslContext = sslContextBuilder.build();
            return sslContext;
        } catch (SSLException e) {
            e.printStackTrace();
        }

        return null;
    }


    public void sendDeviceChange(SonOffConnection sonOffConnection, SonOffConnection.Switch aSwitch){

        String chanelID = sonOffConnection.getUID();

        ObjectNode res = mapper.createObjectNode();

        res.put("action", "update");
        res.put("apikey", sonOffConnection.getRegistrationKey());
        res.put("selfApikey", sonOffConnection.getApiKey());
        res.put("deviceid", sonOffConnection.getDeviceID());
        res.put("userAgent", "app");
        res.put("sequence", ""+System.currentTimeMillis());

        ObjectNode params = mapper.createObjectNode();
        JsonNode switches = mapper.valueToTree(sonOffConnection.getSwitches());
        params.set("switches", switches);
        res.set("params", params);

//        var rq = {
//                "apikey": "111111111-1111-1111-1111-111111111111",
//                "selfApikey" : "111111111-1111-1111-1111-111111111111",
//                "action": a.action,
//                "deviceid": a.target,
//                "params": a.value,
//                "userAgent": "app",
//                "sequence": Date.now().toString(),
//        };



        if(chanelID != null){

//            Collection<Broadcaster> broadcasters = broadcasterFactory.lookupAll();
//            for (Broadcaster broadcaster : broadcasters) {
//                System.out.println("- broadcaster : " + broadcaster.getID());
//            }

            Broadcaster broadcaster = broadcasterFactory.lookup("/*");

            if(broadcaster != null){

                Collection<AtmosphereResource> atmosphereResources = broadcaster.getAtmosphereResources();

                for (AtmosphereResource atmosphereResource : atmosphereResources) {

                    if(chanelID.equals(atmosphereResource.uuid())){

                        System.out.println("Sendto:" + atmosphereResource.uuid());

                        try {
                            String value = mapper.writeValueAsString(res);
                            System.out.println(" >> " + value);
//                            atmosphereResource.write(value);
                            broadcaster.broadcast(value, atmosphereResource);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                    
                }
            }else{
                log.error("Broadcast not found !");
            }



        }


    }

    @Override
    public void setPort(int port) {
        PORT = port;
    }

    @Override
    public Message notifyAndWait(Request message) {
        return null;
    }
}

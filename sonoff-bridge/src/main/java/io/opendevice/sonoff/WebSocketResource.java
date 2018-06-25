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

import br.com.criativasoft.opendevice.connection.ConnectionManager;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.model.Board;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.core.model.PhysicalDevice;
import br.com.criativasoft.opendevice.restapi.model.ApiKey;
import br.com.criativasoft.opendevice.restapi.model.dao.AccountDao;
import br.com.criativasoft.opendevice.wsrest.guice.GuiceInjectProvider;
import br.com.criativasoft.opendevice.wsrest.io.WSEventsLogger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Injector;
import org.atmosphere.annotation.Suspend;
import org.atmosphere.config.service.Disconnect;
import org.atmosphere.cpr.AtmosphereResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handle sonoff websocket protocol.
 *
 * @author Ricardo JL Rufino
 *         Date: 03/06/18
 */
@Path("/api/ws")
public class WebSocketResource  {

    private static final Logger log = LoggerFactory.getLogger(WebSocketResource.class);

    private static ObjectMapper mapper = new ObjectMapper();

    private static Map<String, SonOffConnection> registredDevices =  new ConcurrentHashMap<String, SonOffConnection>();

    @Context
    private AtmosphereResource resource;

//    @Context
//    private AtmosphereResourceEvent event;

    @GET
    @Suspend(contentType = "application/json", listeners = {WSEventsLogger.class})
    public Response onConnect() {
        return Response.ok("X").build();
    }

    @Disconnect
    public void onDisconnect() {
//        if (event.isCancelled()) {
//            log.info("Browser {} unexpectedly disconnected", event.getResource().uuid());
//        } else if (event.isClosedByClient()) {
            log.info("Browser {} closed the connection", resource.uuid());
//        }

        ConnectionManager manager = SonOffServerConnection.getInstance().getConnectionManager();
        SonOffConnection connection = (SonOffConnection) manager.findConnection(resource.uuid());
        if (resource != null) {
            manager.removeConnection(connection);
        }

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public ObjectNode onMessageReceived(String data) {

        try {
            JsonNode req = mapper.readTree(data);
            ObjectNode res = mapper.createObjectNode();

            JsonNode actionNode = req.get("action");

            String deviceID = null;

            if(req.hasNonNull("deviceid")){
                deviceID = req.get("deviceid").asText();
            }

            String apikey = getString(req, "apikey");

            res.put("error", 0);
            res.put("deviceid", deviceID);
            res.put("apikey", apikey);

            if (actionNode != null) {
                String action = actionNode.asText();

                if ("register".equals(action)) {
                    log.debug("Register Request | {} - {}", deviceID, apikey);

                    SonOffConnection connection = findDeviceByOriginalKey(apikey);

                    String reg = UUID.randomUUID().toString();
                    // new api key, for subsequent requests
                    // Like a SESSION KEY
                    res.put("apikey", reg);

                    if(connection == null){
                        connection = new SonOffConnection();
                        registredDevices.put(reg, connection);
                    }else{
                        // re-associate using new "sessionKey"
                        log.debug("Re-associate Device using new Old="+connection.getRegistrationKey() + ", New="+reg);
                        try {
                            registredDevices.remove(connection.getRegistrationKey());
                            registredDevices.put(reg, connection);
                        }catch (Exception ex){
                            ex.printStackTrace();
                            throw new RuntimeException(ex);
                        }
                    }

                    connection.setDeviceID(deviceID);
                    connection.setApiKey(getString(req, "apikey"));
                    connection.setVersion(getString(req, "version"));
                    connection.setFirmware(getString(req, "romVersion"));
                    connection.setModel(getString(req, "model"));
                    connection.setUid(resource.uuid());
                    connection.setRegistrationKey(reg);

                    ConnectionManager manager = SonOffServerConnection.getInstance().getConnectionManager();

                    // Set Application ID / Tenant ID
                    if(connection.getApplicationID() == null){
                        log.debug("Add ");
                        Injector injector = GuiceInjectProvider.getInjector();
                        AccountDao accountDao = injector.getProvider(AccountDao.class).get();
                        ApiKey key = accountDao.findKey("SonOff", apikey); // Find SonOff Device Key registred in OpenDevice
                        if(key != null) connection.setApplicationID(key.getAccount().getOwner().getUuid());
                    }

                    // Add only if not exist (may be added by setup).
                    DeviceConnection foundConnection = manager.findConnection(connection.getUID());
                    if(foundConnection == null){
                        log.debug("Add new connection to manager : {}, account: {}",connection.getUID(), connection.getApplicationID());
                        manager.addConnection(connection);
                        try { foundConnection.connect();} catch (ConnectionException e) {}
                    }


                }

                if ("date".equals(action)) {
                    log.debug("GetTime Request | {}", deviceID);
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    res.put("date", df.format(new Date()));
                }

                // Reg: {"userAgent":"device","apikey":"XXXXXXXX","deviceid":"XXXXX","action":"query","params":["timers"]}
                // Res: {"error":0,"deviceid":"XXXXX","apikey":"XXXXXXXXX","params":{}}
                if ("query".equals(action)) {
                    log.debug("Query Request | {} ", deviceID);
                    res.set("params", mapper.createObjectNode());
                }

                if ("update".equals(action)) {

                    log.debug("Update Request | {} - {} ", deviceID, apikey);

                    SonOffConnection connection = findDevice(apikey);

                    if(connection != null){
                        JsonNode switchesNodes = req.get("params").get("switches");

                        if(switchesNodes instanceof ArrayNode){
                            ArrayNode list = (ArrayNode) switchesNodes;
                            for (JsonNode item : list) {
                                updateSwitch(item, connection);
                            }
                        }

                        // Register devices on frist time/re-connection
                        if(connection.getDevice() == null){

                            Injector injector = GuiceInjectProvider.getInjector();
                            AccountDao accountDao = injector.getProvider(AccountDao.class).get();

                            ApiKey accountKey = accountDao.findKey("SonOff", connection.getApiKey());

                            Board board = null;
                            String name = "SonOff_"+connection.getDeviceID();
                            DeviceManager manager = BaseDeviceManager.getInstance();

                            // Find device using current Context.
                            if(accountKey != null){
                                String accountUuid = accountKey.getAccount().getOwner().getUuid();
                                log.info("Using Account: " + accountUuid);
                                TenantProvider.setCurrentID(accountUuid);
                                board = (Board) manager.findDeviceByName(name);
                            }

                            // Register new devices when in setup mode..
                            if(board == null){
                                board = new Board();
                                board.setName(name);
                                board.setTitle(name);
                            }

                            List<SonOffConnection.Switch> switches = connection.getSwitches();
                            for (SonOffConnection.Switch aSwitch : switches) {

                                PhysicalDevice physicalDevice = null;
                                name = connection.getDeviceID() + "_"+aSwitch.getOutlet();

                                // Find existing device
                                if(accountKey != null){
                                    physicalDevice = (PhysicalDevice) manager.findDeviceByName(name);
                                }

                                if (physicalDevice == null){
                                    physicalDevice = new PhysicalDevice();
                                    physicalDevice.setName(connection.getDeviceID() + "_"+aSwitch.getOutlet());
                                    physicalDevice.setType(DeviceType.DIGITAL);
                                    physicalDevice.gpio(aSwitch.getOutlet());
                                    physicalDevice.setTitle("Outlet " + aSwitch.getOutlet());
                                    board.getDevices().add(physicalDevice);
                                }

                                aSwitch.setPhysicalDevice(physicalDevice);
                            }

                            connection.setDevice(board);
                        }

                    }else{
                        log.warn("Device not found: " + connection);
                    }
                }

            }

            log.debug(" >>> " + res.toString());

           return res;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Find by SonOff original ApyKey (generated in device)
     */
    private SonOffConnection findDeviceByOriginalKey(String apikey) {

        SonOffConnection connection = registredDevices.get(apikey);
        if(connection != null) return connection;

        Collection<SonOffConnection> values = registredDevices.values();

        for (SonOffConnection value : values) {
            if(value.getApiKey() != null && value.getApiKey().equals(apikey)) return value;
        }

        return null;
    }

    private void updateSwitch(JsonNode jsonNode,  SonOffConnection device) {

        List<SonOffConnection.Switch> switches = device.getSwitches();

        SonOffConnection.Switch found = null;

        int outlet = getInt(jsonNode, "outlet");

        for (SonOffConnection.Switch aSwitch : switches) {
            if(aSwitch.getOutlet() == outlet){
                found = aSwitch;
            }
        }

        if(found == null){
            found = new SonOffConnection.Switch();
            found.setParent(device);
            device.getSwitches().add(found);
        }

        found.setStatus(getString(jsonNode, "switch"));
        found.setOutlet(outlet);
    }

    private SonOffConnection findDevice(String id) {
        return registredDevices.get(id);
    }

    private String getString(JsonNode node, String name) {
        if(node.hasNonNull(name)) return node.get(name).asText();
        return null;
    }

    private int getInt(JsonNode node, String name) {
        if(node.hasNonNull(name)) return node.get(name).asInt();
        return -1;
    }

    public static Map<String, SonOffConnection> getRegistredDevices() {
        return registredDevices;
    }

}

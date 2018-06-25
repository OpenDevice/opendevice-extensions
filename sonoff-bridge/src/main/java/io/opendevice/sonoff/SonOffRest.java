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
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.command.GetDevicesResponse;
import br.com.criativasoft.opendevice.core.model.Board;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.restapi.io.ErrorResponse;
import br.com.criativasoft.opendevice.restapi.model.*;
import br.com.criativasoft.opendevice.restapi.model.dao.AccountDao;
import br.com.criativasoft.opendevice.restapi.model.dao.UserDao;
import br.com.criativasoft.opendevice.wsrest.guice.GuiceInjectProvider;
import com.google.inject.Injector;
import com.sun.jersey.api.NotFoundException;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/dispatch/")
public class SonOffRest {

    private static final Logger log = LoggerFactory.getLogger(SonOffRest.class);

    /**
     * Callen from Setup to check if server is active
     * @param httpReques
     * @return
     */
    @GET
    @Path("device")
    @Produces(MediaType.TEXT_PLAIN)
    public String verifiy(@Context HttpServletRequest httpReques) {
        log.info("Check service status");
        return "OK";
    }

    /**
     * Called from SonOff Device, to get WebSocket settings.
     * @param httpReques
     * @return
     */
    @POST
    @Path("device")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getConfig(@Context HttpServletRequest httpReques) {

        log.info("Device requesting websocket server information");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("error", 0);
        params.put("reason", "ok");
        params.put("IP", httpReques.getLocalAddr());
        params.put("port", SonOffServerConnection.PORT);

        return params;
    }

    /**
     * Called from Setup Application, to register SonOff in OpenDevice platform
     * @param params
     * @return
     */
    @POST
    @Path("register")
    @Produces(MediaType.APPLICATION_JSON)
    public Response opendeviceRegistration(Map<String, String> params) {
        log.info("OpenDevice Registration: " + params);

        String deviceID = params.get("deviceID");
        String deviceApiKey = params.get("deviceApiKey");
        String apiKey = params.get("apiKey"); // OpenDevice API Key

        Map<String, SonOffConnection> registredDevices = WebSocketResource.getRegistredDevices();

//        registredDevices.clear();

        Collection<SonOffConnection> connections = registredDevices.values();

        boolean found = false;

        for (SonOffConnection connection : connections) {

            if(connection.getDeviceID().equals(deviceID) && connection.getApiKey().equals(deviceApiKey)){

                log.info("Found connection : " + connection.getUID() + " - " + connection.getStatus());

                Board board = (Board) connection.getDevice();

                if(board != null && ! board.getDevices().isEmpty()){

                    // FIXME: Validate OpenDevice apiKey

                    TenantProvider.setCurrentID(apiKey);
                    connection.setApplicationID(apiKey);

                    ConnectionManager manager = SonOffServerConnection.getInstance().getConnectionManager();

                    // Add only if not exist.
                    DeviceConnection foundConnection = manager.findConnection(connection.getUID());
                    if(foundConnection == null){
                        manager.addConnection(connection);
                    }else{
                        foundConnection = connection;
                    }

                    createDeviceAccount(apiKey, deviceID, deviceApiKey);

                    try { foundConnection.connect();} catch (ConnectionException e) {}

                    List<Device> devices = new ArrayList<Device>();
                    devices.add(board);
                    devices.addAll(board.getDevices());

                    GetDevicesResponse response = new GetDevicesResponse(devices, connection.getUID());
                    foundConnection.notifyListeners(response);

                    found = true;
                }else{
                    log.info("Found connection but setup is not complete " + connection);
                    return ErrorResponse.status(Response.Status.NOT_FOUND, "Found connection but setup is not complete");
                }

            }
        }

        if(!found)  return ErrorResponse.status(Response.Status.NOT_FOUND, "Device Not found ");

        return Response.ok().build();
    }

    private void createDeviceAccount(String odevKey, String deviceID, String deviceApiKey) {

        Injector injector = GuiceInjectProvider.getInjector();
        AccountDao accountDao = injector.getProvider(AccountDao.class).get();
        UserDao userDao = injector.getProvider(UserDao.class).get();

        Account account = accountDao.getAccountByApiKey(odevKey);

        if(account == null){
            throw new NotFoundException("Account not found !");
        }

        ApiKey key = accountDao.findKey("SonOff", deviceApiKey);

        if(key == null){

            BaseDeviceManager.getInstance().transactionBegin();

            log.info("Creating new User/Key for Sonoff : " + deviceID);

            //Encrypt
            DefaultPasswordService service = new DefaultPasswordService();
            DefaultHashService hashService = (DefaultHashService) service.getHashService();
            hashService.setHashIterations(1);

            key = new ApiKey("SonOff", deviceApiKey);
            User user = new User("SonOff-"+deviceID, deviceApiKey);
            user.setPassword(service.encryptPassword(user.getPassword()));
            UserAccount userAccount = new UserAccount();
            userAccount.setType(AccountType.DEVICE);
            userAccount.setOwner(account);
            userAccount.setUser(user);
            userAccount.getKeys().add(key);
            user.getAccounts().add(userAccount);
            key.setAccount(userAccount);
            userDao.persist(user);

            BaseDeviceManager.getInstance().transactionEnd();

        }


    }

}

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

import br.com.criativasoft.opendevice.connection.*;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.message.SimpleMessage;
import br.com.criativasoft.opendevice.connection.serialize.DefaultSteamReader;
import br.com.criativasoft.opendevice.core.command.GetDevicesRequest;
import br.com.criativasoft.opendevice.core.connection.ConnectionType;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.connection.EmbeddedGPIO;
import br.com.criativasoft.opendevice.core.model.Board;
import br.com.criativasoft.opendevice.core.model.Device;
import io.opendevice.ext.obd.commands.PIDSensorCommand;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * @author Ricardo JL Rufino
 *         Date: 28/05/17
 */
public class OBDConnection extends AbstractConnection implements EmbeddedGPIO {

    private AbstractStreamConnection transport;

    private Thread readingThread = null;

    private final Set<Device> devices = new CopyOnWriteArraySet<Device>();

    private long readingInterval = 100; // ms

    private boolean autoScanSensors = false;

    private boolean enableAllSensors = false;

    // Some protocols not allow fast polling time (beetween send commands)
    // We set the minimum interval as recommended by the datasheet of the ELM327
    private static final int MIN_INTERVAL_CMD = 50; // ms

    private long lastSend = 0;
    private OBDATCommand lastCommand;
    private final Object waitCommand = new Object();
    private String name;
    private Board board;

    public OBDConnection(String name, String urlOrPath, ConnectionType type) {
        super();
        this.name = name;
        board = new Board(-1, name);

        switch (type){
            case USB:
                transport = Connections.out.usb(urlOrPath);
                ((IUsbConnection) transport).setDeviceBootTime(0);
                break;
            case BLUETOOTH:
                transport = Connections.out.bluetooth(urlOrPath);
                break;
            case WIFI:
                transport = Connections.out.tcp(urlOrPath);
                break;
            case ETHERNET:
                transport = Connections.out.tcp(urlOrPath);
                break;
            default:
                throw new IllegalArgumentException("invalid transport type");
        }

        DefaultSteamReader defaultSteamReader = new DefaultSteamReader();
        defaultSteamReader.setEndOfMessageToken((byte)'>');
        transport.setStreamReader(defaultSteamReader);
        transport.setSerializer(new OBDSerializer());
        transport.addListener(new ConnectionListener() {
            @Override
            public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {
                System.out.println("internal connectionStateChanged : " + status);
            }

            @Override
            public void onMessageReceived(Message message, DeviceConnection connection) {
                // System.out.println("ODBRESPONSE >>> " + message);
                if(lastCommand != null){
                    synchronized (waitCommand){

                        SimpleMessage command = (SimpleMessage) message;
                        byte[] data = command.getBytes();

                        if(lastCommand == OBDATCommand.GET_PIDS_1TO20){
                            //data
                            List<OBDSensor> obdSensors = OBDSensorFactory.create(OBDConnection.this.name, new String(data));
                            for (OBDSensor obdSensor : obdSensors) {
                                AbstractConnection.log.info("Found sensor: " + obdSensor.getName());
                                attach(obdSensor);
                                if(enableAllSensors) obdSensor.setEnabled(true);
                            }

                            if(obdSensors.isEmpty()) AbstractConnection.log.warn("Devices not found, check protocol !");
                        }

                        // Update Sensor..
                        if(lastCommand instanceof PIDSensorCommand){
                            for (Device current : devices) {
                                OBDSensor sensor = (OBDSensor) current;
                                if(sensor.getCommand().match(data)){
                                    long value = sensor.getCommand().parse(data);
                                    System.err.println(sensor.getCommand().getClass().getSimpleName()+" == " + value);
                                    sensor.setValue(value);
                                }
                            }
                        }

                        waitCommand.notify();
                        lastCommand = null;
                    }
                }
            }
        });
    }

    @Override
    public void connect() throws ConnectionException {

        if(!transport.isConnected()) transport.connect();

        if(transport.isConnected()){
            try {
                send(OBDATCommand.RESET);
                 Thread.sleep(2000);
                send(OBDATCommand.ECHO_OFF);
                send(OBDATCommand.SPACE_OFF);
                send(OBDATCommand.ADAPTIVE_TIMING);
                send(OBDATCommand.PROTOCOL_AUTO);

                if(autoScanSensors){

                    scanAvailableDevices();

                    synchronized (waitCommand){
                        try {
                            waitCommand.wait(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }


                setStatus(ConnectionStatus.CONNECTED);
                startPoolingSensors();
            } catch (IOException e) {
                e.printStackTrace();
                setStatus(ConnectionStatus.FAIL);
                disconnect();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else{
            setStatus(transport.getStatus());
        }

    }


    @Override
    public void disconnect() throws ConnectionException {
        if(transport != null){
            transport.disconnect();
        }

        setStatus(ConnectionStatus.DISCONNECTED);

        if(readingThread != null){
            try {
                readingThread.join(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Set interval for read (group of) enabled sensors and report new data.
     * @param readingInterval
     */
    public void setReadingInterval(int readingInterval) {
        this.readingInterval = readingInterval;
    }

    /**
     * Auto-Scan sensors form OBD interface. <br/>
     * NOTE: At this time only first (32) pids are scanned
     * @param autoScanSensors
     */
    public void setAutoScanSensors(boolean autoScanSensors) {
        this.autoScanSensors = autoScanSensors;
    }

    /**
     * This allows you to enable all sensors discovered through {@link #setAutoScanSensors(boolean)}. <br/>
     * Note that this can generate a greater delay in reading for large numbers of sensors
     * since each reading has a range of on average 100ms. <br/>
     * NOTE: There is the option to manually add sensors, using {@link #attach(OBDSensorPID)}
     * @param enableAllSensors
     */
    public void setEnableAllSensors(boolean enableAllSensors) {
        this.enableAllSensors = enableAllSensors;
    }

    public boolean isAutoScanSensors() {
        return autoScanSensors;
    }

    @Override
    public void send(Message message) throws IOException {

        // Sync / Discover devices
        if(message instanceof GetDevicesRequest){
            System.out.println("TODO / GetDevicesRequest: .... need parse this command "); // TODO need parse this command
        }

        if(! (message instanceof OBDATCommand)) return;

        // Waiting response from last command.
        if(lastCommand != null){
            synchronized (waitCommand){
                try {
                    waitCommand.wait(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // WoW, só fast ... need wait to send next command.
        // Avoid send fast commands, ensure interval defined in MIN_INTERVAL_CMD
        if(lastSend > 0){

            long time = System.currentTimeMillis() - lastSend;

            // To fast, need wait to send next command
            if (time < MIN_INTERVAL_CMD) try {
                Thread.sleep(MIN_INTERVAL_CMD);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        lastSend = System.currentTimeMillis();

        // Sync / Discover devices
        if(message instanceof GetDevicesRequest){
            System.out.println("TODO / GetDevicesRequest: .... need parse this command "); // TODO need parse this command
        }

        if(message instanceof OBDATCommand){
            lastCommand = (OBDATCommand) message;
            transport.send(message);
        }

    }

    @Override
    public void attach(Device device) {

        if(device instanceof OBDSensor){

            OBDSensor sensor = (OBDSensor) device;

            for (Device current : devices) {
                if(current.getName() != null && current.getName().equals(sensor.getName())){
                    log.debug("Can't attach, device already exist ! Device = " + device);
                    return;
                }
            }

            sensor.setBoard(board);
            board.getDevices().add(sensor);
            devices.add((OBDSensor) device);
        }

    }

    /** Start thead to chek if data is avaible. <br/>
     * Required only if the encapsulated connection does not provide any mechanism for callback or listener
     */
    protected void startPoolingSensors(){
        if(readingThread == null || ! readingThread.isAlive()){
            readingThread = new PollingSensorsThread();
            readingThread.setName("OBDPooling");
            readingThread.setDaemon(true);
            readingThread.start();
        }
    }


    protected void scanAvailableDevices() throws IOException {
        send(OBDATCommand.GET_PIDS_1TO20); // TODO: add more PIDs.. see #setAutoScanSensors and OBDSensorFactory
    }

    public OBDSensor attach(OBDSensorPID pid) {
        OBDSensor s = OBDSensorFactory.create(name, pid);
        attach(s);
        return s;
    }

    @Override
    public Board getBoardInfo() {
        return board;
    }

    private class PollingSensorsThread extends Thread{

        @Override
        public void run() {

            while(isConnected()){

               for (Device device : devices) {

                   if(! (device instanceof OBDSensor)) continue;

                   OBDSensor sensor = (OBDSensor) device;

                   if(!sensor.isEnabled()) continue;

                   OBDATCommand obdCommand = sensor.getCommand();

                   try {

                       long time = System.currentTimeMillis();

                       send(obdCommand);

                       synchronized (waitCommand){
                           waitCommand.wait(5000); // wait for ECU RESPONSE
                       }

                       System.out.println("<< send/time >> " +  (System.currentTimeMillis() - time) + "ms");

                   } catch (IOException e) {
                       e.printStackTrace();
                       // TODO: send sync error notification
                   } catch (InterruptedException e) {
                       // TODO: send sync error notification
                       e.printStackTrace();
                   }
               }
//                   System.out.println("<< full >> " +  (System.currentTimeMillis() - time) + "ms");

                // Wait for next read
                try {
                    Thread.sleep(readingInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

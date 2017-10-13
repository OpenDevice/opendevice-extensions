'use strict';

const http = require('http');
const uuidv4 = require('uuid/v4');

var OpenDevice = {

    host : "0de3a76f.ngrok.io",
    accessToken : "",

    rest : function(urlPath){

        console.log("ODev.Request: " + urlPath + ", token= " + this.accessToken);
        
        var options = {
            host : this.host,
            method: 'GET',
            path: urlPath,
            headers: {
                'accept': 'application/json',
                'Authorization' : "Google " + this.accessToken
            }
        };

        return new Promise((resolve, reject) => {
        
            var req = http.request(options, function(response){
                console.log("Got response: " + response.statusCode);

                // handle http errors
                if (response.statusCode < 200 || response.statusCode > 299) {
                    reject(new Error('Request Failed, status code: ' + response.statusCode));
                }
                
                const body = [];
                response.on('data', (chunk) => body.push(chunk));
                response.on('end', () => resolve(JSON.parse(body.join(''))));
            });

            req.on('error', (err) => reject(err));

            req.end();
        });
    }
}

OpenDevice.devices = {
    
        path : "/api/devices",
    
        get : function(deviceID){
            return OpenDevice.rest(this.path + "/" + deviceID);
        },
    
        value : function(deviceID, value){
            if(value != null){
                return OpenDevice.rest(this.path + "/" + deviceID + "/value/" + value);
            }else{
                return OpenDevice.rest(this.path + "/" + deviceID + "/value");
            }
        },
    
        list : function(){
            return OpenDevice.rest(this.path + "/");
        }
    };

// Like OpenDevice JAVA-API
var DeviceType = {
    DIGITAL:1,
    ANALOG:2,
    ANALOG_SIGNED:3,
    NUMERIC:4,
    FLOAT2:5,
    FLOAT2_SIGNED:6,
    FLOAT4:7,
    CHARACTER:8,
    BOARD:10,
    MANAGER:11,

    isNumeric : function(type){
        return type == od.DeviceType.ANALOG
        || type == od.DeviceType.FLOAT2
        || type == od.DeviceType.FLOAT4
        || type == od.DeviceType.FLOAT2_SIGNED
    }
};

/**
 * Utility functions
 */
function log(title, msg) {
    console.log(`[${title}] ${msg}`);
}

/**
 * Generate a unique message ID
 *
 * TODO: UUID v4 is recommended as a message ID in production.
 */
function generateMessageID() {
    return uuidv4();
}

/**
 * Generate a response message
 *
 * @param {string} name - Directive name
 * @param {Object} payload - Any special payload required for the response
 * @returns {Object} Response object
 */
function generateResponse(name, payload) {
    return {
        header: {
            messageId: generateMessageID(),
            name: name,
            namespace: 'Alexa.ConnectedHome.Control',
            payloadVersion: '2',
        },
        payload: payload,
    };
}


/**
 * Always returns true for sample code.
 * Validation is done on server 
 */
function isValidToken() {
    return true;
}

/**
 * Always returns truee.
 * Validation is done on server 
 */
function isDeviceOnline(applianceId) {
    return true;
}

function turnOn(applianceId, userAccessToken) {
    
    log('DEBUG', `turnOn (applianceId: ${applianceId})`);

    OpenDevice.accessToken = userAccessToken;
    
    OpenDevice.devices.value(applianceId, 1);

    return generateResponse('TurnOnConfirmation', {});
}

function turnOff(applianceId, userAccessToken) {
    
    log('DEBUG', `turnOff (applianceId: ${applianceId})`);

    OpenDevice.accessToken = userAccessToken;
    
    OpenDevice.devices.value(applianceId, 0);

    return generateResponse('TurnOffConfirmation', {});
}

function setPercentage(applianceId, percentage) {
    log('DEBUG', `setPercentage (applianceId: ${applianceId}), percentage: ${percentage}`);

    // Call device cloud's API to set percentage

    return generateResponse('SetPercentageConfirmation', {});
}

function incrementPercentage(applianceId, delta) {
    log('DEBUG', `incrementPercentage (applianceId: ${applianceId}), delta: ${delta}`);

    // Call device cloud's API to set percentage delta

    return generateResponse('IncrementPercentageConfirmation', {});
}

function decrementPercentage(applianceId, delta) {
    log('DEBUG', `decrementPercentage (applianceId: ${applianceId}), delta: ${delta}`);

    // Call device cloud's API to set percentage delta

    return generateResponse('DecrementPercentageConfirmation', {});
}



/**
 * This function is invoked when we receive a "Discovery" message from Alexa Smart Home Skill.
 * We are expected to respond back with a list of appliances that we have discovered for a given customer.
 *
 * @param {Object} request - The full request object from the Alexa smart home service. This represents a DiscoverAppliancesRequest.
 *     https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/smart-home-skill-api-reference#discoverappliancesrequest
 *
 * @param {function} callback - The callback object on which to succeed or fail the response.
 *     https://docs.aws.amazon.com/lambda/latest/dg/nodejs-prog-model-handler.html#nodejs-prog-model-handler-callback
 *     If successful, return <DiscoverAppliancesResponse>.
 *     https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/smart-home-skill-api-reference#discoverappliancesresponse
 */
function handleDiscovery(request, callback) {
    
    log('DEBUG', `Discovery Request: ${JSON.stringify(request)}`);

    // Get the OAuth token from the request.
    const userAccessToken = request.payload.accessToken.trim();

    OpenDevice.accessToken = userAccessToken;
    
    var onList = OpenDevice.devices.list();
    onList.then(function(devices){
        
        var alexaDevices = [];

        devices.forEach(function(device) {

            if(device.sensor == false && device.type == DeviceType.DIGITAL){

                // Ref: https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/smart-home-skill-api-reference
                alexaDevices.push({
                    applianceId: device.id,
                    manufacturerName: 'Undefined',
                    modelName: 'Undefined',
                    version: '1.0',
                    friendlyName: device.title,
                    friendlyDescription: 'Device linked with OpenDevice',
                    isReachable: true, // Boolean value to represent the status of the device at time of discovery
                    // List the actions the device can support from our API
                    actions: ['turnOn', 'turnOff'],
                    //  actions: ['turnOn', 'turnOff', 'setPercentage', 'incrementPercentage', 'decrementPercentage'],
                    // not used at this time
                    additionalApplianceDetails: {
                        parentID: device.parentID,
                    },
                });
            }

        }, this);

        if (!userAccessToken || !isValidToken(userAccessToken)) {
            const errorMessage = `Discovery Request [${request.header.messageId}] failed. Invalid access token: ${userAccessToken}`;
            log('ERROR', errorMessage);
            callback(new Error(errorMessage));
        }
    
        const response = {
            header: {
                messageId: generateMessageID(),
                name: 'DiscoverAppliancesResponse',
                namespace: 'Alexa.ConnectedHome.Discovery',
                payloadVersion: '2',
            },
            payload: {
                discoveredAppliances: alexaDevices,
            },
        };
    
        //Log the response. These messages will be stored in CloudWatch.
        log('DEBUG', `Discovery Response: ${JSON.stringify(response)}`);
    
        // Return result with successful message.
        callback(null, response);


    }).catch((err) => callback(new Error(err)));
    
}

/**
 * A function to handle control events.
 * This is called when Alexa requests an action such as turning off an appliance.
 *
 * @param {Object} request - The full request object from the Alexa smart home service.
 * @param {function} callback - The callback object on which to succeed or fail the response.
 */
function handleControl(request, callback) {
    log('DEBUG', `Control Request: ${JSON.stringify(request)}`);

    /**
     * Get the access token.
     */
    const userAccessToken = request.payload.accessToken.trim();

    /**
     * Generic stub for validating the token against your cloud service.
     * Replace isValidToken() function with your own validation.
     *
     * If the token is invliad, return InvalidAccessTokenError
     *  https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/smart-home-skill-api-reference#invalidaccesstokenerror
     */
    if (!userAccessToken || !isValidToken(userAccessToken)) {
        log('ERROR', `Discovery Request [${request.header.messageId}] failed. Invalid access token: ${userAccessToken}`);
        callback(null, generateResponse('InvalidAccessTokenError', {}));
        return;
    }

    /**
     * Grab the applianceId from the request.
     */
    const applianceId = request.payload.appliance.applianceId;

    /**
     * If the applianceId is missing, return UnexpectedInformationReceivedError
     *  https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/smart-home-skill-api-reference#unexpectedinformationreceivederror
     */
    if (!applianceId) {
        log('ERROR', 'No applianceId provided in request');
        const payload = { faultingParameter: `applianceId: ${applianceId}` };
        callback(null, generateResponse('UnexpectedInformationReceivedError', payload));
        return;
    }

    /**
     * At this point the applianceId and accessToken are present in the request.
     *
     * Please review the full list of errors in the link below for different states that can be reported.
     * If these apply to your device/cloud infrastructure, please add the checks and respond with
     * accurate error messages. This will give the user the best experience and help diagnose issues with
     * their devices, accounts, and environment
     *  https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/smart-home-skill-api-reference#error-messages
     */
    if (!isDeviceOnline(applianceId, userAccessToken)) {
        log('ERROR', `Device offline: ${applianceId}`);
        callback(null, generateResponse('TargetOfflineError', {}));
        return;
    }

    let response;

    switch (request.header.name) {
        case 'TurnOnRequest':
            response = turnOn(applianceId, userAccessToken);
            break;

        case 'TurnOffRequest':
            response = turnOff(applianceId, userAccessToken);
            break;

        case 'SetPercentageRequest': {
            const percentage = request.payload.percentageState.value;
            if (!percentage) {
                const payload = { faultingParameter: `percentageState: ${percentage}` };
                callback(null, generateResponse('UnexpectedInformationReceivedError', payload));
                return;
            }
            response = setPercentage(applianceId, userAccessToken, percentage);
            break;
        }

        case 'IncrementPercentageRequest': {
            const delta = request.payload.deltaPercentage.value;
            if (!delta) {
                const payload = { faultingParameter: `deltaPercentage: ${delta}` };
                callback(null, generateResponse('UnexpectedInformationReceivedError', payload));
                return;
            }
            response = incrementPercentage(applianceId, userAccessToken, delta);
            break;
        }

        case 'DecrementPercentageRequest': {
            const delta = request.payload.deltaPercentage.value;
            if (!delta) {
                const payload = { faultingParameter: `deltaPercentage: ${delta}` };
                callback(null, generateResponse('UnexpectedInformationReceivedError', payload));
                return;
            }
            response = decrementPercentage(applianceId, userAccessToken, delta);
            break;
        }

        default: {
            log('ERROR', `No supported directive name: ${request.header.name}`);
            callback(null, generateResponse('UnsupportedOperationError', {}));
            return;
        }
    }

    log('DEBUG', `Control Confirmation: ${JSON.stringify(response)}`);

    callback(null, response);
}

/**
 * Main entry point.
 * Incoming events from Alexa service through Smart Home API are all handled by this function.
 */
exports.handler = (request, context, callback) => {
    
    // FIXME: remove....!!!!!!!!!!!!!
    console.log("Request : ", JSON.stringify(request));
    
    
    switch (request.header.namespace) {

        case 'Alexa.ConnectedHome.Discovery':
            handleDiscovery(request, callback);
            break;


        case 'Alexa.ConnectedHome.Control':
            handleControl(request, callback);
            break;

        /**
         * Received an unexpected message
         */
        default: {
            const errorMessage = `No supported namespace: ${request.header.namespace}`;
            log('ERROR', errorMessage);
            callback(new Error(errorMessage));
        }
    }
};

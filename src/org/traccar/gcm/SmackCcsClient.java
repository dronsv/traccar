/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.gcm;

/**
 *
 * @author andrey
 */
/*
 * Most part of this class is copyright Google.
 * It is from https://developer.android.com/google/gcm/ccs.html
 */
import com.google.common.io.CharStreams;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.traccar.Context;
import org.traccar.model.Device;
import org.traccar.model.GCMDevice;
import org.traccar.model.Position;
import org.traccar.model.User;
import org.traccar.web.JsonConverter;

/**
 * Sample Smack implementation of a client for GCM Cloud Connection Server.
 *
 * For illustration purposes only.
 */
public class SmackCcsClient {

	static final String MESSAGE_KEY = "SERVER_MESSAGE";

    /**
     * Broadcast Position to the positions device topic
     * @param position position to broadcast
     */
    public static void broadcastPosition(Position position) {
        String topic = "/topics/device"+Context.getDataManager().getDeviceById(position.getDeviceId()).getUniqueId();        
        JSONObject json = new JSONObject();
        json.put("name", "newPosition");
        json.put("data", position.getJsonObject());
        sendHTTPMessage(topic, json);
    }
    
    /**
     *Subscribe GCM client app to topic
     * @param clientToken - token of GCM client device
     * @param topic - name of topic to subscribe
     */
    
    
    public static void subscribeClientToTopic(String clientToken, String topic){
        try{
            JSONObject jGcmData = new JSONObject();
            URL url;
            HttpURLConnection conn;
            try {
                url = new URL("https://iid.googleapis.com/iid/v1/"+clientToken+"/rel"+topic);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Authorization", "key=" + "AIzaSyC3664RwCOaBtGvbJ19omlGf8giNgz2TWo");
                conn.setRequestProperty("Content-Type", "application/json");
                try {
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    // Send GCM message content.
                    OutputStream outputStream = conn.getOutputStream();
                    try {
                        outputStream.write(jGcmData.toString().getBytes());
                    } catch (IOException ex) {
                        Logger.getLogger(SmackCcsClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // Read GCM response.
                    InputStream inputStream=null;
                    try {
                        inputStream = conn.getInputStream();
                    } catch (IOException ex) {
                        Logger.getLogger(SmackCcsClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    String resp = CharStreams.toString(new InputStreamReader(inputStream,"ASCII"));
//                    Log(resp);
                } catch (ProtocolException ex) {
                    Logger.getLogger(SmackCcsClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(SmackCcsClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {
            Log("Unable to send GCM message.");
            Log("Please ensure that API_KEY has been replaced by the server " +
                    "API key, and that the device's registration token is correct (if specified).");
        }               
    }
    /**
     *Create device group on server
     * @param deviceID id of device
     */
    public static String createDeviceGroup(long deviceID){
        
        try{
            JSONObject jGcmData = new JSONObject();
            jGcmData.put("operation", "create");
            jGcmData.put("registration_ids", "[]");
            jGcmData.put("notification_key_name", "device"+Context.getDataManager().getDeviceById(deviceID).getUniqueId());
            URL url;
            HttpURLConnection conn;
            try {
                url = new URL("https://android.googleapis.com/gcm/notification");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Authorization", "key=" + "AIzaSyC3664RwCOaBtGvbJ19omlGf8giNgz2TWo");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("project_id", Context.getConfig().getString("gcm.projectId"));
                try {
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    // Send GCM message content.
                    OutputStream outputStream = conn.getOutputStream();
                    try {
                        outputStream.write(jGcmData.toString().getBytes());
                    } catch (IOException ex) {
                        Logger.getLogger(SmackCcsClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // Read GCM response.
                    InputStream inputStream=null;
                    try {
                        inputStream = conn.getInputStream();
                    } catch (IOException ex) {
                        Logger.getLogger(SmackCcsClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    String resp = CharStreams.toString(new InputStreamReader(inputStream,"ASCII"));
                    
                    return resp;
                } catch (ProtocolException ex) {
                    Logger.getLogger(SmackCcsClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(SmackCcsClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {
            Log("Unable to send GCM message.");
            Log("Please ensure that API_KEY has been replaced by the server " +
                    "API key, and that the device's registration token is correct (if specified).");
        }  
        return null;        
    }
    
    public static String addToDeviceGroup(Long deviceId, String token){
        String result=new String();
        
        return result;        
    }



    public static void broadcastDeviceLink(long userId, long deviceId, int rights) {
        String topic = "/topics/user"+Long.toString(userId);        
        JSONObject json = new JSONObject();
        try {
            json.put("name", "newDeviceLink");        
            JSONObject data = new JSONObject();
            data.put("user", Context.getDataManager().getUser(userId).getJsonObject(userId));
            data.put("device", Context.getDataManager().getDeviceById(deviceId).getJsonObject(userId));
            json.put("data", data);
        } catch (SQLException ex) {
            Logger.getLogger(SmackCcsClient.class.getName()).log(Level.SEVERE, null, ex);
        }        
        sendHTTPMessage(topic, json);
    }

    public static void broadcastDeviceUnLink(Long userId, Long deviceId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private static XMPPConnection connection;
    private static ConnectionConfiguration config;

    private static void Log(String toString) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
	Logger logger = Logger.getLogger("SmackCcsClient");

	public static final String GCM_SERVER = "gcm-xmpp.googleapis.com";//gcm.googleapis.com";
	public static final int GCM_PORT = 5235;

	public static final String GCM_ELEMENT_NAME = "gcm";
	public static final String GCM_NAMESPACE = "google:mobile:data";

	static Random random = new Random();

    private JSONArray getUserDevicesListJSon(Long userID) {
        JSONArray arr = new JSONArray();
        try {            
            Collection<Device> devices = Context.getDataManager().getDevices(userID);
            for (Device d: devices){                                
                arr.add(0, d.getJsonObject(userID));
            }   
        } catch (SQLException ex) {
            Logger.getLogger(SmackCcsClient.class.getName()).log(Level.SEVERE, null, ex);
        }    
        return arr;
    }

	/**
	 * XMPP Packet Extension for GCM Cloud Connection Server.
	 */
	class GcmPacketExtension extends DefaultPacketExtension {
		String json;

		public GcmPacketExtension(String json) {
			super(GCM_ELEMENT_NAME, GCM_NAMESPACE);
			this.json = json;
		}

		public String getJson() {
			return json;
		}

		@Override
		public String toXML() {
			return String.format("<%s xmlns=\"%s\">%s</%s>", GCM_ELEMENT_NAME,
					GCM_NAMESPACE, json, GCM_ELEMENT_NAME);
		}

		@SuppressWarnings("unused")
		public Packet toPacket() {
			return new Message() {
				// Must override toXML() because it includes a 
				@Override
				public String toXML() {

					StringBuilder buf = new StringBuilder();
					buf.append("");
					buf.append(GcmPacketExtension.this.toXML());
					buf.append("");
					return buf.toString();
				}
			};
		}
	}

	public SmackCcsClient() {
		// Add GcmPacketExtension
		ProviderManager.getInstance().addExtensionProvider(GCM_ELEMENT_NAME,
				GCM_NAMESPACE, new PacketExtensionProvider() {

					@Override
					public PacketExtension parseExtension(XmlPullParser parser)
							throws Exception {
						String json = parser.nextText();
						GcmPacketExtension packet = new GcmPacketExtension(json);
						return packet;
					}
				});
	}

	/**
	 * Returns a random message id to uniquely identify a message.
	 *
	 * Note: This is generated by a pseudo random number generator for
	 * illustration purpose, and is not guaranteed to be unique.
	 *
	 */
	public String getRandomMessageId() {
		return "m-" + Long.toString(random.nextLong());
	}

	/**
	 * Sends a downstream GCM message.
	 */
	public void send(String jsonRequest) {
		Packet request = new GcmPacketExtension(jsonRequest).toPacket();
		connection.sendPacket(request);
	}

	/**
	 * Handles an upstream data message from a device application.
	 *
	 * This sample echo server sends an echo message back to the device.
	 * Subclasses should override this method to process an upstream message.
	 */
	public void handleIncomingDataMessage(Map jsonObject) {
		
		String from = jsonObject.get("from").toString();

		// PackageName of the application that sent this message.
		String category = jsonObject.get("category").toString();

		// Use the packageName as the collapseKey in the echo packet
		String collapseKey = "echo:CollapseKey";
		@SuppressWarnings("unchecked")
		Map payload = (Map) jsonObject
				.get("data");

		String action = payload.get("action").toString();

		if ("echo".equals(action)) {

			String clientMessage = payload.get("CLIENT_MESSAGE").toString();
			payload.put(MESSAGE_KEY, "ECHO: " + clientMessage);

			// Send an ECHO response back
			String echo = createJsonMessage(from, getRandomMessageId(),
					payload, collapseKey, null, false);
			send(echo);
		} else if ("register".equals(action)) {
                    registerGcmDevice(payload);
		}
	}

    /**
     *  Register GCM device to user add device subscription to channels
     * @param request - register GCM device request parameters 
     */
    protected void registerGcmDevice(Map request) {
        try {
            String login = request.get("login").toString();
            String password = request.get("password").toString();
            User user = Context.getDataManager().login(login, password);
            if (user!=null){
                GCMDevice gcmDevice = new GCMDevice();
                
                gcmDevice.setUserId(user.getId());
                gcmDevice.setName(request.get("name").toString());
                gcmDevice.setUuid(request.get("uuid").toString());
                
                String token = request.get("token").toString();
                gcmDevice.setToken(token);
                
                //Subscribe device to user topic
                Long userId = user.getId();
                Collection<GCMDevice> gCMDevices = Context.getDataManager().getGcmDeviceByUuid(gcmDevice.getUuid());
                if (gCMDevices.isEmpty())
                    Context.getDataManager().addGCMDevice(gcmDevice);
                //TODO  solve problem with update device
//                else
//                    Context.getDataManager().updateGCMDevice(gcmDevice);
                String userTopic = "/topics/user"+Long.toString(userId);
                
                subscribeClientToTopic(token, userTopic);
                
                //Subscribe GCM device to GPS device channels. Form array of devices
                JSONArray arr = new JSONArray();
                Collection<Device> devices = Context.getDataManager().getDevices(userId);                
                for (Device d:devices){
                    String deviceTopic = "/topics/device"+d.getUniqueId();
                    subscribeClientToTopic(token, deviceTopic);                    
                    arr.add(d.getJsonObject(userId));
                }
                
                //send list of devices to 
                JSONObject deviceList = new JSONObject();
                deviceList.put("name", "deviceList");
                deviceList.put("data", arr);
                sendHTTPMessage(token, deviceList);                
            }
            else{
                JSONObject message = new JSONObject();
                message.put("name", "error");
                JSONObject data=new JSONObject();
                data.put("message", "authorization failed");
                message.put("data", data);
                sendHTTPMessage(request.get("token").toString(), message);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SmackCcsClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

	/**
	 * Handles an ACK.
	 *
	 * By default, it only logs a INFO message, but subclasses could override it
	 * to properly handle ACKS.
	 */
	public void handleAckReceipt(Map jsonObject) {
		String messageId = jsonObject.get("message_id").toString();
		String from = jsonObject.get("from").toString();
		logger.log(Level.INFO, "handleAckReceipt() from: " + from
				+ ", messageId: " + messageId);
	}

	/**
	 * Handles a NACK.
	 *
	 * By default, it only logs a INFO message, but subclasses could override it
	 * to properly handle NACKS.
	 */
	public void handleNackReceipt(Map jsonObject) {
		String messageId = jsonObject.get("message_id").toString();
		String from = jsonObject.get("from").toString();
		logger.log(Level.INFO, "handleNackReceipt() from: " + from
				+ ", messageId: " + messageId);
	}

	/**
	 * Creates a JSON encoded GCM message.
	 *
	 * @param to
	 *            RegistrationId of the target device (Required).
	 * @param messageId
	 *            Unique messageId for which CCS will send an "ack/nack"
	 *            (Required).
	 * @param payload
	 *            Message content intended for the application. (Optional).
	 * @param collapseKey
	 *            GCM collapse_key parameter (Optional).
	 * @param timeToLive
	 *            GCM time_to_live parameter (Optional).
	 * @param delayWhileIdle
	 *            GCM delay_while_idle parameter (Optional).
	 * @return JSON encoded GCM message.
	 */
	public static String createJsonMessage(String to, String messageId,
		Map payload, String collapseKey, Long timeToLive,
		Boolean delayWhileIdle) {
		Map message = new HashMap();
		message.put("to", to);
		if (collapseKey != null) {
			message.put("collapse_key", collapseKey);
		}
		if (timeToLive != null) {
			message.put("time_to_live", timeToLive);
		}
		if (delayWhileIdle != null && delayWhileIdle) {
			message.put("delay_while_idle", true);
		}
		message.put("message_id", messageId);
		message.put("data", payload);
		return JSONValue.toJSONString(message);
	}

	/**
	 * Creates a JSON encoded ACK message for an upstream message received from
	 * an application.
	 *
	 * @param to
	 *            RegistrationId of the device who sent the upstream message.
	 * @param messageId
	 *            messageId of the upstream message to be acknowledged to CCS.
	 * @return JSON encoded ack.
	 */
	public static String createJsonAck(String to, String messageId) {
		Map message = new HashMap();
		message.put("message_type", "ack");
		message.put("to", to);
		message.put("message_id", messageId);
		return JSONValue.toJSONString(message);
	}

	/**
	 * Connects to GCM Cloud Connection Server using the supplied credentials.
	 *
	 * @param username
	 *            GCM_SENDER_ID@gcm.googleapis.com
	 * @param password
	 *            API Key
	 * @throws XMPPException
	 */
	public void connect(String username, String password) throws XMPPException {
            if (connection == null || !connection.isConnected()){
		config = new ConnectionConfiguration(GCM_SERVER, GCM_PORT);
		config.setSecurityMode(SecurityMode.enabled);
		config.setReconnectionAllowed(true);                
		config.setRosterLoadedAtLogin(false);
		config.setSendPresence(false);
		config.setSocketFactory(SSLSocketFactory.getDefault());

		// NOTE: Set to true to launch a window with information about packets
		// sent and received
		config.setDebuggerEnabled(false);

		// -Dsmack.debugEnabled=true
		XMPPConnection.DEBUG_ENABLED = false;

		connection = new XMPPConnection(config);
		connection.connect();

		connection.addConnectionListener(new ConnectionListener() {

			@Override
			public void reconnectionSuccessful() {
				logger.info("Reconnecting..");
			}

			@Override
			public void reconnectionFailed(Exception e) {
				logger.log(Level.INFO, "Reconnection failed.. ", e);
			}

			@Override
			public void reconnectingIn(int seconds) {
				logger.log(Level.INFO, String.format(Locale.US, "Reconnecting in %d secs", seconds));
			}

			@Override
			public void connectionClosedOnError(Exception e) {
				logger.log(Level.INFO, "Connection closed on error.");
			}

			@Override
			public void connectionClosed() {
				logger.info("Connection closed.");
			}
		});

		// Handle incoming packets
		connection.addPacketListener(new PacketListener() {

			@Override
			public void processPacket(Packet packet) {
				logger.log(Level.INFO, "Received: {0}", packet.toXML());
				Message incomingMessage = (Message) packet;
				GcmPacketExtension gcmPacket = (GcmPacketExtension) incomingMessage
						.getExtension(GCM_NAMESPACE);
				String json = gcmPacket.getJson();
				try {
					@SuppressWarnings("unchecked")
					Map jsonObject = (Map) JSONValue
							.parseWithException(json);

					// present for "ack"/"nack", null otherwise
					Object messageType = jsonObject.get("message_type");

					if (messageType == null) {
						// Normal upstream data message
						handleIncomingDataMessage(jsonObject);

						// Send ACK to CCS
						String messageId = jsonObject.get("message_id")
								.toString();
						String from = jsonObject.get("from").toString();
						String ack = createJsonAck(from, messageId);
						send(ack);
					} else if ("ack".equals(messageType.toString())) {
						// Process Ack
						handleAckReceipt(jsonObject);
					} else if ("nack".equals(messageType.toString())) {
						// Process Nack
						handleNackReceipt(jsonObject);
					} else {
						logger.log(Level.WARNING,
								"Unrecognized message type (%s)",
								messageType.toString());
					}
				} catch (ParseException e) {
					logger.log(Level.SEVERE, "Error parsing JSON " + json, e);
				} 
			}
		}, new PacketTypeFilter(Message.class));

		// Log all outgoing packets
//		connection.setPacketInterceptor(new PacketInterceptor() {
//			@Override
//			public void interceptPacket(Packet packet) {
//				logger.log(Level.INFO, "Sent: {0}", packet.toXML());
//			}
//		}, new PacketTypeFilter(Message.class));

		connection.login(username, password);
            }
                
	}
        
        /** 
        * Send GCM Message by HTTP protocol
             * @param to address of topic, device group or token for message
             * @param message object to send
        */       
        public static void sendHTTPMessage(String to, JSONObject message){
            try{
                JSONObject jGcmData = new JSONObject();
                JSONObject jData = new JSONObject();
                // Where to send GCM message.
                jGcmData.put("to", to);
                // What to send in GCM message.
                jGcmData.put("data", message);

                // Create connection to send GCM Message request.
                URL url;
                HttpURLConnection conn;
                try {
                    url = new URL("https://android.googleapis.com/gcm/send");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Authorization", "key=" + "AIzaSyC3664RwCOaBtGvbJ19omlGf8giNgz2TWo");
                    conn.setRequestProperty("Content-Type", "application/json");
                    try {
                        conn.setRequestMethod("POST");
                        conn.setDoOutput(true);
                        
                        // Send GCM message content.
                        OutputStream outputStream = conn.getOutputStream();
                        try {
                            outputStream.write(jGcmData.toString().getBytes());
                        } catch (IOException ex) {
                            Logger.getLogger(SmackCcsClient.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        // Read GCM response.
                        InputStream inputStream=null;
                        try {
                            inputStream = conn.getInputStream();
                        } catch (IOException ex) {
                            Logger.getLogger(SmackCcsClient.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        String resp = CharStreams.toString(new InputStreamReader(inputStream,"ASCII"));
                        Log("Check your device/emulator for notification or logcat for " +
                                "confirmation of the receipt of the GCM message.");
                    } catch (ProtocolException ex) {
                        Logger.getLogger(SmackCcsClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (MalformedURLException ex) {
                    Logger.getLogger(SmackCcsClient.class.getName()).log(Level.SEVERE, null, ex);
                }       

            } catch (IOException e) {
                Log("Unable to send GCM message.");
                Log("Please ensure that API_KEY has been replaced by the server " +
                        "API key, and that the device's registration token is correct (if specified).");
            }            
        }

	public static void sendMessage(String toDeviceRegId, String message) {
                String userName = Context.getConfig().getString("gcm.login", "");
		String GOOGLE_SERVER_KEY = Context.getConfig().getString("gcm.apiKey", "");
		SmackCcsClient ccsClient = new SmackCcsClient();

		try {
			ccsClient.connect(userName, GOOGLE_SERVER_KEY);
		} catch (XMPPException e) {
			Log(e.toString());
		}

		String messageId = ccsClient.getRandomMessageId();
		Map payload = new HashMap();
		payload.put(MESSAGE_KEY, message);
		payload.put("EmbeddedMessageId", messageId);
		String collapseKey = "sample";
		Long timeToLive = 10000L;
		Boolean delayWhileIdle = true;
		ccsClient.send(createJsonMessage(toDeviceRegId, messageId, payload,
				collapseKey, timeToLive, delayWhileIdle));
	}
}
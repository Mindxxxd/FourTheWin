package lighthouse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.InvalidAttributeValueException;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageStringCodingException;
import org.msgpack.core.MessageTypeCastException;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;
import org.msgpack.value.impl.ImmutableStringValueImpl;

/**
 * This class wraps the network communication with the lighthouse in a simple
 * interface. The network connection is configured and connected upon object creation but
 * the username and token need to be manually set. Afterwards data can be sent to the lighthouse.
 */
public class LighthouseDisplay {
	
	/**
	 * A map which stores the LighthouseDisplay for each URL.
	 */
	private static Map<String, LighthouseDisplay> instances = new HashMap<>();
	
	/**
	 * Returns an existing LighthouseDisplay for the Lighthouse-API which wasn't closed
	 * or creates a new LighthouseDisplay for it.
	 * 
	 * @throws IOException
	 *             if there is an error while connecting or constructing the
	 *             web-socket
	 * @throws Exception
	 *             if there is an error constructing a web-socket-client
	 */
	public static LighthouseDisplay getDisplay() throws IOException, Exception {
		return getDisplay("wss://lighthouse.uni-kiel.de/websocket", false, 0);
	}
	
	/**
	 * Returns an existing LighthouseDisplay for the given URI or Creates a new LighthouseDisplay
	 * with the given settings and connects to the lighthouse server at the given web-socket address and
	 * disables certificate validation if selfSigned is true. Connection is finalized asynchronous.
	 * 
	 * @param destinationURI the URI to connect to
	 * @param allowSelfSigned true if self-signed certificates should be allowed
	 * @param debugOutput sets the debug-level for this connection
	 * 
	 * @return A LighthouseDisplay for the given URI
	 * 
	 * @throws URISyntaxException
	 *             if destUri contains errors
	 * @throws IOException
	 *             if there is an error while connecting or constructing the
	 *             web-socket
	 * @throws Exception
	 *             if there is an error constructing a web-socket-client
	 */
	public static LighthouseDisplay getDisplay(String destinationURI, boolean allowSelfSigned, int debugOutput) throws InvalidAttributeValueException, IOException, Exception  {
		LighthouseDisplay display = instances.get(destinationURI);
		if (display != null) return display;
		
		display = new LighthouseDisplay(debugOutput);
		display.connect(destinationURI, allowSelfSigned);
		
		instances.put(destinationURI, display);
		return display;
	}
	
	

	private boolean closed = false;
	private String destinationURI;
	private String username;
	private String token;
	private LighthouseDisplayHandler handler;
	private WebSocketClient client;
	private int debugOutput;
	private Set<ILighthouseInputListener> observer = new HashSet<>();
	private boolean keyInputEnabled = false;

	/**
	 * Creates a new LighthouseDisplay and
	 * sets weather connect and disconnect messages should be printed in stdOut
	 */
	private LighthouseDisplay(int debugOutput) {
		handler = new LighthouseDisplayHandler(this, debugOutput);
		this.debugOutput = debugOutput;
	}

	/**
	 * Connects to the lighthouse server at the given web-socket address and
	 * disables certificate validation if selfSigned is true. Connection is
	 * finalized asynchronous
	 * 
	 * @throws URISyntaxException
	 *             if destUri contains errors
	 * @throws IOException
	 *             if there is an error while connecting or constructing the
	 *             web-socket
	 * @throws Exception
	 *             if there is an error constructing a web-socket-client
	 */
	private void connect(String destUri, boolean selfSigned) throws URISyntaxException, IOException, Exception {
		if (selfSigned) {
			// Since we use a self-signed certificate, we can't check the
			// validity of the certificate (and we have to disable this check)
			SslContextFactory sec = new SslContextFactory(true);
			client = new WebSocketClient(sec);
		} else {
			client = new WebSocketClient();
		}

		URI targetUri = new URI(destUri);
		destinationURI = destUri;
		ClientUpgradeRequest upgrade = new ClientUpgradeRequest();

		client.start();
		client.connect(handler, targetUri, upgrade);
		if (debugOutput > 0) {
			System.out.printf("LighthouseDisplay, Connecting to: %s\n", targetUri);
		}
	}


	/**
	 * setter for the configured username. The username may be change during a connection.
	 * 
	 * @param username the username
	 * @throws IllegalArgumentException if username is null
	 */
	public void setUsername(String username) {
		if (username == null) {
			throw new IllegalArgumentException("The Username must be a string.");
		}
		this.username = username;
	}

	/**
	 * getter for the configured username
	 * 
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * setter for the token to be used. This token may be change during a connection.
	 * 
	 * @param token the token
	 * @throws IllegalArgumentException if username is null
	 */
	public void setToken(String token) {
		if (token == null) {
			throw new IllegalArgumentException("The Token must be a string.");
		}
		this.token = token;
	}
	
	/**
	 * getter for the configured token
	 * 
	 * @return the token
	 */
	public String getToken() {
		return token;
	}
	
	/**
	 * This enables the Key-Inputs from the Lighthouse-Display. The username and token have to be set before.
	 * This will request the Key-Inputs for the current user or (if not connected) the user at the time of successful connection.
	 * @throws IllegalStateException if the connection is closed or username/token was not set
	 */
	public void enableKeyInputs() throws IllegalStateException {
		if (closed) {
			throw new IllegalStateException("The Connection was already Closed.");
		}
		if (username == null) {
			throw new IllegalStateException("The Username must be set in the LightouseDisplay to be able to send images.");
		}
		if (token == null) {
			throw new IllegalStateException("The Token must be set in the LightouseDisplay to be able to send images.");
		}
		keyInputEnabled = true;
		handler.requestKeyData();
	}

	/**
	 * Sends an image to the lighthouse server (Width: 28, Height: 14).
	 * The data should be a byte array consisting of 1176 bytes.
	 * Each three bytes are the red, green and blue color values of a window.
	 * The windows start at the top-left corner and are ordered in rows.
	 *
	 * Examle for 2x2 windows indexes of red color byte:
	 * 0 3
	 * 6 9
	 *
	 * @param data
	 *            The data to send
	 * @throws IllegalStateException
	 *             if the username or token wasn't set Or the connection was Closed by calling 'close()'.
	 * @throws IllegalArgumentException
	 *             if the image-data is not 1176 Bytes long.
	 * @throws IOException
	 *             if some error occurs during sending of the data.
	 */
	public void sendImage(byte[] data) throws IllegalStateException, IllegalArgumentException, IOException {
		if (closed) {
			throw new IllegalStateException("The Connection was already Closed.");
		}
		if (username == null) {
			throw new IllegalStateException("The Username must be set in the LightouseDisplay to be able to send images.");
		}
		if (token == null) {
			throw new IllegalStateException("The Token must be set in the LightouseDisplay to be able to send images.");
		}
		if (data.length != 1176) {
			throw new IllegalArgumentException("The image data must be exactly 1176 Bytes long (not "+data.length+" Bytes)");
		}
		
		handler.send(data, 0, data.length);
	}

	/**
	 * returns if there is currently a connection open note: connection is
	 * established asynchronous so this value might be false after a call of
	 * connect()
	 * 
	 * @return if the connection is open
	 */
	public boolean isConnected() {
		return handler.isConnected();
	}

	/**
	 * Closes the connection, a new LighthouseDisplay has to be aquired with
	 * 'getDisplay' to be able to send data again.
	 */
	public void close() {
		closed = true;
		instances.remove(destinationURI);
		handler.close();
		try {
			client.stop();
			client = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addButtonListener(ILighthouseInputListener listener) {
		observer.add(listener);
	}

	public void removeButtonListener(ILighthouseInputListener listener) {
		observer.remove(listener);
	}

	/**
	 * private class for handling the web-socket
	 * (has to be public for the api, which it is registered to)
	 */
	@WebSocket(maxTextMessageSize = 1024, maxBinaryMessageSize = 64 * 1024)
	public class LighthouseDisplayHandler {

		private LighthouseDisplay parent;
		private Session session;
		private boolean connected = false;
		private int debug;
		private RemoteEndpoint endpoint = null;
		private ByteBuffer lastPacket;
		private Object sendSynchronizer = new Object();
		private boolean dataSentSinceLastCheck = false;
		private boolean keyDataRequested = false;

		private LighthouseDisplayHandler(LighthouseDisplay parent, int debug) {
			this.parent = parent;
			this.debug = debug;
		}

		/**
		 * this method sends the given data as a lighthouse request to the server
		 * 
		 * @param data
		 *            the data to send
		 * @param offset
		 *            the offset to start in the data
		 * @param length
		 *            the length to send
		 * @throws IOException
		 *             on errors while transmitting the data
		 */
		public void send(byte[] data, int offset, int length) throws IOException {
			// Lighthouse request (as JSON/Type mix):
			// {
			// "VERB" => String // (GET, PUT, STREAM)
			// "PATH" => [String] // (["user",<username>,"model"])
			// "AUTH" => {"USER" => String, "TOKEN" => String}
			// "META" => {* => *}
			// "PAYL" => *
			// "REID" => Int // Request-ID
			// }
			MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
			packer.packMapHeader(6);
			{
				packer.packString("REID");
				packer.packInt(0);

				packer.packString("VERB");
				packer.packString("PUT");

				packer.packString("PATH");
				packer.packArrayHeader(3);
				{
					packer.packString("user");
					packer.packString(parent.getUsername());
					packer.packString("model");
				}

				packer.packString("AUTH");
				packer.packMapHeader(2);
				{
					packer.packString("USER");
					packer.packString(parent.getUsername());

					packer.packString("TOKEN");
					packer.packString(parent.getToken());
				}

				packer.packString("META");
				packer.packMapHeader(0);

				packer.packString("PAYL");
				packer.packBinaryHeader(length);
				packer.addPayload(data, offset, length);
			}
			
			dataSentSinceLastCheck = true;
			lastPacket = ByteBuffer.wrap(packer.toByteArray());
			
			synchronized (sendSynchronizer) {
				if (connected) {
					endpoint.sendBytes(ByteBuffer.wrap(packer.toByteArray()), new WriteCallback() {
						@Override
						public void writeSuccess() {}
						@Override
						public void writeFailed(Throwable err) {
							System.err.println("LighthouseDisplay, ERROR: sending image failed");
						}
					});
					endpoint.flush();
				}
			}
		}
		
		public void requestKeyData() {
			if(!connected || keyDataRequested) return;
			keyDataRequested = true;
			
			// request stream for controller input
			MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
			try {
				packer.packMapHeader(6);
				{
					packer.packString("REID");
					packer.packInt(-1);
	
					packer.packString("VERB");
					packer.packString("STREAM");
	
					packer.packString("PATH");
					packer.packArrayHeader(3);
					{
						packer.packString("user");
						packer.packString(parent.getUsername());
						packer.packString("model");
					}
	
					packer.packString("AUTH");
					packer.packMapHeader(2);
					{
						packer.packString("USER");
						packer.packString(parent.getUsername());
	
						packer.packString("TOKEN");
						packer.packString(parent.getToken());
					}
	
					packer.packString("META");
					packer.packMapHeader(0);
	
					packer.packString("PAYL");
					packer.packNil();
				}
				
				synchronized (sendSynchronizer) {
					endpoint.sendBytes(ByteBuffer.wrap(packer.toByteArray()));
					endpoint.flush();
				}
			} catch (IOException e) {
				System.err.println("LighthouseDisplay, ERROR: requesting controller input stream:");
				e.printStackTrace();
			}
		}

		/**
		 * this method sends the close notification to close this connection
		 */
		public void close() {
			connected = false;
			if (session != null) {
				session.close(StatusCode.NORMAL, "end of data");
			}
		}

		/**
		 * this method tells if a connection is established
		 * 
		 * @return if connection is established
		 */
		public boolean isConnected() {
			return connected && session != null && session.isOpen();
		}

		/**
		 * event target for the web-socket close event
		 */
		@OnWebSocketClose
		public void onClose(int statusCode, String reason) {
			connected = false;
			if (debug > 0) {
				System.out.printf("LighthouseDisplay, Connection closed [%d]: %s%n", statusCode, reason);
			}
		}

		/**
		 * event target for the web-socket connect event
		 */
		@OnWebSocketConnect
		public void onConnect(Session session) {
			// save session for usage in communication
			this.session = session;
			connected = true;
			if (debug > 0) {
				System.out.printf("LighthouseDisplay, Got connection: %s%n", session);
			}
			endpoint = session.getRemote();
			
			if (parent.keyInputEnabled) requestKeyData();
			
			// create a Thread to re-send images every 2s if no image was sent since the last check
			// (to prevent the display from turning off by a timeout)
			new Thread() {
				public void run() {
					while(true) {
						// check outside synchronized to prevent unnecessary locks
						if (connected && !dataSentSinceLastCheck && lastPacket != null) {
							synchronized (sendSynchronizer) {
								// check again just in case it changed
								if (!dataSentSinceLastCheck) {
									try {
										RemoteEndpoint endpoint = session.getRemote();
										endpoint.sendBytes(lastPacket);
										endpoint.flush();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}
						}
						dataSentSinceLastCheck = false;
						
						try {
							sleep(2000);
						} catch (InterruptedException e) {
							// terminate this thread on interrupt
							break;
						}
					}
				};
			}.start();
		}

		@OnWebSocketMessage
		public void onMessage(String msg) {
			if (debug > 1) {
				System.out.printf("LighthouseDisplay, got text Message: %s\n", msg);
			}
		}

		@OnWebSocketMessage
		public void onMessage(byte buf[], int offset, int length) {
			if (debug > 1) {
				System.out.printf("LighthouseDisplay, got binary Message: ");
				for (int i = 0; i < length; i++) {
					System.out.printf("%02X ", buf[offset + i] & 0xFF);
				}
				System.out.printf("%n");
			}
			MessageUnpacker unp = MessagePack.newDefaultUnpacker(buf, offset, length);
			try {
				Value v = unp.unpackValue();
				Map<Value,Value> vmap = v.asMapValue().map();
				int rnum = vmap.get(new ImmutableStringValueImpl("RNUM")).asIntegerValue().toInt();
				if (rnum == 200) {
					int reid = vmap.get(new ImmutableStringValueImpl("REID")).asIntegerValue().toInt();
					if (reid == -1) {
						Value payload = vmap.get(new ImmutableStringValueImpl("PAYL"));
						List<Value> entries; // list of event entries
						if (payload.isArrayValue()) {
							entries = payload.asArrayValue().list();
						} else {
							entries = new ArrayList<Value>(1);
							entries.add(payload);
						}
						for (Value entry : entries) {
							Map<Value,Value> payl = entry.asMapValue().map();
							
							boolean isKeyboard = true;
							Value btn = payl.get(new ImmutableStringValueImpl("key"));
							if (btn == null) {
								btn = payl.get(new ImmutableStringValueImpl("btn"));
								isKeyboard = false;
							}
							
							boolean pressed = payl.get(new ImmutableStringValueImpl("dwn")).asBooleanValue().getBoolean();
							int src = payl.get(new ImmutableStringValueImpl("src")).asIntegerValue().toInt();
							int button = btn.asIntegerValue().toInt();
							
							for (ILighthouseInputListener listener : observer) {
								try {
									if (isKeyboard) {
										listener.keyboardEvent(src, button, pressed);
									} else {
										listener.controllerEvent(src, button, pressed);
									}
								} catch (Exception e) {
									System.err.println(e.getLocalizedMessage());
									e.printStackTrace();
								}
							}
						}
					}
				} else {
					Value responseValue = vmap.get(new ImmutableStringValueImpl("RESPONSE"));
					String response = "";
					if (responseValue.isStringValue()) {
						try {
							response = responseValue.asStringValue().asString();
						} catch (MessageStringCodingException ignored) {}
					}
					System.err.println("LighthouseDisplay, API Error: ("+rnum+") "+response);
				}
			} catch (IOException e) {
				System.err.println(e.getLocalizedMessage());
				e.printStackTrace();
			} catch (NullPointerException ignored) { // in case of malformed message
			} catch (MessageTypeCastException ignored) {} // in case of malformed message
		}

		/**
		 * event target for the web-socket error event
		 */
		@OnWebSocketError
		public void onError(Session session, Throwable error) {
			System.err.println("LighthouseDisplay, WebSocket-Error:");
			System.err.println(error);
			error.printStackTrace(System.err);
			System.err.println(session);
		}
	}

}

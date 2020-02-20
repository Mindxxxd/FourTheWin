package views;

import java.io.IOException;

import data.DataModell;
import lighthouse.LighthouseDisplay;

/** COMMENT
 * @author Mindxxxd. 20.02.2020. */
public class LighthouseView implements View {
	/** The Lighthouse Display to communicate with. */
	private LighthouseDisplay display;
	/** The Data where to fetch informations from. */
	private final DataModell dataModell;

	/** Constructor for Lighthouse. Connects and initializes first display stuff. */
	public LighthouseView(DataModell dataModell) {
		this.dataModell = dataModell;
		connect();
		send(startDisplay());
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		update();
	}

	/** COMMENT */
	@Override
	public void update() {
		byte[] data = new byte[14 * 28 * 3];
		data = getPlayboardPxl(data);
		data = getYellowPxl(data);
		data = getRedPxl(data);
		send(data);

	}

	/** Draws the Playboard in the lighthouse data.
	 * @param data the blanc dataset for display.
	 * @return the data set containing the playboard */
	private byte[] getPlayboardPxl(byte[] data) {
		for (int i = 0; i < data.length; i++) {
			if ((i / (3 * 28)) % 2 == 1) { // every second row
				if (i % 3 == 2) { // coloring blue
					data[i] = (byte) 255;
				}
			} else if ((((i / 3) % 28) % 4 == 0 || ((i / 3) % 28) % 4 == 3) && i / (3 * 14) > 1) {
				// every first and 4th out of 4 columns after row 1
				if (i % 3 == 2) { // coloring blue
					data[i] = (byte) 255;
				}
			}
		}
		return data;
	}

	/** COMMENT
	 * @param data
	 * @return */
	private byte[] getYellowPxl(byte[] data) {
		data[28*4*3+5*3+0] = (byte) 255;
		data[28*4*3+5*3+1] = (byte) 255;
		data[28*4*3+6*3+0] = (byte) 255;
		data[28*4*3+6*3+1] = (byte) 255;
		
		// CODE Yellow lighthouse player
		return data;
	}

	/** COMMENT
	 * @param data
	 * @return */
	private byte[] getRedPxl(byte[] data) {
		data[28*6*3+5*3+0] = (byte) 255;
		data[28*6*3+6*3+0] = (byte) 255;
		// CODE Red lighthouse player
		return data;
	}

	/** Calculates the first
	 * @return Bytearray with one color. */
	private byte[] startDisplay() {
		byte[] data = new byte[14 * 28 * 3];
		for (int i = 0; i < data.length; i++) {
			if (i % 3 == 0) {
				data[i] = (byte) 0;
			} else if (i % 3 == 1) {
				data[i] = (byte) 255;
			} else if (i % 3 == 2) {
				data[i] = (byte) 0;
			}
		}
		return data;
	}

	/** Took this from examplecode. */
	private void connect() {
		// Try connecting to the display
		try {
			display = LighthouseDisplay.getDisplay();
			display.setUsername("Mindxxxd");
			display.setToken("API-TOK_27sP-dGnx-r8UK-rs11-+E9g");
		} catch (Exception e) {
			System.out.println("Connection failed: " + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/** Took this from example code. Sends a bytestring to the lighthouse.
	 * @param data byte array contains pixel data for lighthouse. */
	private void send(byte[] data) {
		try {
			display.sendImage(data);
		} catch (IOException e) {
			System.out.println("Connection failed: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/** closes dataconnection to the lighthouse on closeup. */
	public void close() {
		display.close();
	}

}

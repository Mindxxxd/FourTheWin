package views;

import java.io.IOException;

import lighthouse.LighthouseDisplay;

/** COMMENT
 * @author Mindxxxd. 20.02.2020. */
public class LighthouseView implements View {
	
	
	/**COMMENT
	 * 
	 */
	public LighthouseView() {
		// CODE constructor
	}
	/**
	 * COMMENT
	 */
	@Override
	public void update() {
		// CODE update
		
	}


	// Example Code To be Removed CODE
	LighthouseDisplay display = null;

	// Try connecting to the display
	try	{
		display = LighthouseDisplay.getDisplay();
		display.setUsername("Mindxxxd");
		display.setToken("API-TOK_27sP-dGnx-r8UK-rs11-+E9g");
	} catch(Exception e)	{
		System.out.println("Connection failed: " + e.getMessage());
		e.printStackTrace();
		System.exit(-1);
	}

	// Send data to the display
	try	{
		// This array contains for every window (14 rows, 28 columns) three
		// bytes that define the red, green, and blue component of the color
		// to be shown in that window. See documentation of LighthouseDisplay's
		// send(...) method.
		byte[] data = new byte[14 * 28 * 3];

		// Fill array

		display.sendImage(data);
	} catch(IOException ioe) {
		System.out.println("Connection failed: " + e.getMessage());
		e.printStackTrace();
		System.exit(-1);
	}
	
}

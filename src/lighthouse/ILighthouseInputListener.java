package lighthouse;

/**
 * This Interface provides the methods to get user input through the ProjektLighthouseAPI.
 * These methods are called from the Projekt Lighthouse receiving Thread!
 */
public interface ILighthouseInputListener {
	/**
	 * This method is called when a keyboard button changes its state.
	 * @param source a random number to identify the input device (multiple devices possible)
	 * @param button the number associated with the button being pressed
	 * @param down true if the button was pressed and false if it was released
	 */
	void keyboardEvent(int source, int button, boolean down);
	
	/**
	 * This method is called when a controller button changes its state.
	 * @param source a random number to identify the input device (multiple devices possible)
	 * @param button the number associated with the button being pressed
	 * @param down true if the button was pressed and false if it was released
	 */
	void controllerEvent(int source, int button, boolean down);
}

package views;

import java.awt.Container;
import java.awt.Image;

import data.DataModell;

/** This is the Computer View.
 * @author Mindxxxd. 19.02.2020. */
public class ComputerView implements View {
	/** The Data. */
	private final DataModell data;
	/** The graphics for the yellow player. */
	private final Image yellowStone;
	/** The graphics for the red player. */
	private final Image redStone;
	/** The Graphics for the Board itself. */
	private final Image boardGraphics;
	/** The display container to work with. */
	private final Container display;

	/**COMMENT
	 * @param data		The data modell to fetch data from.
	 * @param display	The display to draw stuff on.
	 */
	public ComputerView(DataModell data, Container display) {
		this.data = data;
		yellowStone = null; //CODE init image
		redStone = null; //CODE init image
		boardGraphics = null; //CODE init image
		this.display = display;
		update();
	}
	/**
	 * COMMENT
	 */
	@Override
	public void update() {
		// CODE Computer View Update

	}

}

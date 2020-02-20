package controller;

import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

import data.DataModell;
import views.ComputerView;

/** This is basically the Controller Class.
 * @author Mindxxxd. 19.02.2020. */
public class FourTheWin implements KeyListener {

	/** Main method to start game.
	 * @param args */
	public static void main(String[] args) {
		new FourTheWin();
	}

	/** The DataModell itself to store stuff. */
	private DataModell dataModell;

	/** Constructor opens the Window, initializes everything. Starts game. */
	public FourTheWin() {
		// Creating the Frame.
		JFrame gameFrame = new JFrame("Four The Win: THE GAME");
		gameFrame.setSize(500, 500);
		gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gameFrame.setVisible(true);

		// Initializing the data Modell
		dataModell = new DataModell(); // CODE update constructor. Till now its a placeholder.

		// Initializing the Views
		ComputerView computerView = new ComputerView(dataModell, gameFrame);
		dataModell.addView(computerView);

		// CODE Initialize Lighthouse View

		// CODE ActionListeners
		gameFrame.addKeyListener(this);
	}

	/** Key pressed handling. Working with (ws)ad!
	 * @param e The key Event. */
	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyChar()) {
		case ' ':
			dataModell.makeTurn();
			break;
		case 'a':
			dataModell.movePlayStone(-1);
			break;
		case 'd':
			dataModell.movePlayStone(1);
			break;

		default:
			break;
		}

	}

	/** Nothing happens when key is typed. */
	@Override
	public void keyTyped(KeyEvent e) {
		// Nothing Happens

	}

	/** Nothing happens when Key is released. */
	@Override
	public void keyReleased(KeyEvent e) {
		// Nothing Happens

	}

}

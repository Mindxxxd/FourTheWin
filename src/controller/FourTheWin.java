package controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import data.DataModell;
import views.ComputerView;
import views.LighthouseView;

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
	/** The Computer View. */
	private ComputerView computerView;
	/** The Lighthosue View. */
	private LighthouseView lighthouseView;
	/** The Computer Frame. */
	private JFrame gameFrame;

	/** Constructor opens the Window, initializes everything. Starts game. */
	public FourTheWin() {
		// Creating the Frame.
		gameFrame = new JFrame("Four The Win: THE GAME");
		gameFrame.setSize(500, 500);
		gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gameFrame.setVisible(true);
		gameFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				lighthouseView.close();
			}
		});

		// Initializing the data Modell.
		dataModell = new DataModell(); // CODE update constructor. Till now its a placeholder.

		// Initializing the Computer View.
		computerView = new ComputerView(dataModell, gameFrame);
		dataModell.addView(computerView);

		// Initializing the Lighthouse View.
		lighthouseView = new LighthouseView(dataModell);
		dataModell.addView(lighthouseView);

		// ActionListeners.
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

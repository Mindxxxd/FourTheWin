package views;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import data.DataModell;

/** This is the Computer View.
 * @author Mindxxxd. 19.02.2020. */
public class ComputerView implements View {
	/** The graphics path for the yellow player. */
	private static final String YELLOW_STONE_PATH = "src/views/yellowStone.png";
	/** The graphics path for the red player. */
	private static final String RED_STONE_PATH = "src/views/redStone.png";
	/** The Graphics path for the Board itself. */
	private static final String BOARD_GRAPHICS_PATH = "src/views/playboard.png";
	/** Graphic for yellow player. */
	private static Image yellowStoneImage;
	/** Graphic for red player. */
	private static Image redStoneImage;
	/** graphic for board itself. */
	private static Image boardImage;

	/** The Data. */
	private final DataModell data;
	/** The display container to work with. */
	private final JFrame gameFrame;

	/** Constructor to load graphics and firstly update the View.
	 * @param data    The data modell to fetch data from.
	 * @param display The display to draw stuff on. */
	public ComputerView(DataModell data, JFrame gameFrame) {
		this.data = data;
		this.gameFrame = gameFrame;
		if (yellowStoneImage == null || redStoneImage == null || boardImage == null) {
			try {
				yellowStoneImage = ImageIO.read(new File(YELLOW_STONE_PATH));
				yellowStoneImage = yellowStoneImage.getScaledInstance(gameFrame.getWidth() / 7,
						gameFrame.getHeight() / 7, 0);
				redStoneImage = ImageIO.read(new File(RED_STONE_PATH));
				redStoneImage = redStoneImage.getScaledInstance(gameFrame.getWidth() / 7, gameFrame.getHeight() / 7, 0);
				boardImage = ImageIO.read(new File(BOARD_GRAPHICS_PATH));
				boardImage = boardImage.getScaledInstance(gameFrame.getWidth(), gameFrame.getHeight(), 0);

			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}

		update();
	}

	/** Calculates the new computer graphic and updates the computer view. */
	@Override
	public void update() {

		// panel stuff
		JPanel panel = new JPanel();
		panel.setSize(gameFrame.getWidth(), gameFrame.getHeight());
		panel.setLayout(new GridLayout(1, 1));

		// draws playboard
		BufferedImage actualPlayboard = new BufferedImage(gameFrame.getWidth(), gameFrame.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics graphics = actualPlayboard.getGraphics();
		graphics.drawImage(boardImage, 0, 0, null);

		// CODE NOW DRAW PLAYSTONES!! THIS IS ONLY FOR TESTING!!
		double yelX = 1400.0 / 7 * 3, yelY = 1400.0 / 7 * 3, redX = 1400.0 / 7 * 4, redY = 1400.0 / 7 * 5;
		graphics.drawImage(yellowStoneImage, (int) yelX * 500 / 1400, (int) yelY * 500 / 1400, null);
		graphics.drawImage(redStoneImage, (int) redX * 500 / 1400, (int) redY * 500 / 1400, null);

		// add everything to panel
		ImageIcon icon = new ImageIcon(actualPlayboard);
		JLabel label = new JLabel(icon);
		label.setVisible(true);
		panel.add(label);
		gameFrame.setContentPane(panel);
	}

}

package net.winsauce.noiseTest;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import org.lwjgl.opengl.GL11;

public class Window {

	/** Desired frame time */
	private static final int FRAMERATE = 40;
	
	public static final int WINDOW_WIDTH = 1280, WINDOW_HEIGHT = 720;
	
	/** Exit the game */
	private static boolean alive;
	
	private Game game;
	
	public Window() {
		try {
			init();
			run();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			Sys.alert(Game.TITLE, "An error occured and the game will exit.");
		} finally {
			cleanup();
		}
	}
	
	/**
	 * Initialise the game
	 * @throws LWJGLException if init fails
	 */
	private void init() throws LWJGLException {
		game = new Game();
		
		Display.setTitle(Game.TITLE);

		Display.setDisplayMode(new DisplayMode(WINDOW_WIDTH, WINDOW_HEIGHT));
		Display.create();
		
		Mouse.setGrabbed(true);
		
		game.init();
		
		alive = true;
	}

	private void run() {
		//Enable Z-Buffer
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		
		while (alive) {
			Display.update();

			// Check for close requests
			alive = !Display.isCloseRequested();
			if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) alive = false;

			game.render();
			
			Display.sync(FRAMERATE);
		}
	}

	/**
	 * Do any game-specific cleanup
	 */
	private void cleanup() {
		// Close the window
		Display.destroy();
	}

	public static void main(String[] args) {
		new Window();
	}
}
package net.winsauce.noiseTest;

import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.util.glu.GLU.gluLookAt;
import static org.lwjgl.util.glu.GLU.gluPerspective;

import org.lwjgl.opengl.GL11;

public class Game {

	//Title of our window
	public static final String TITLE = "Gratuitous Amounts of Energy!";
	
	//World instance, stores basically everything
	private World world;
	
	public void init() throws Exception {
		
		//Size, in points (shared along both axes, so result is square)
		int worldSize = 200;
		
		world = new World(worldSize, worldSize);
        world.init();

		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);

		GL11.glShadeModel(GL11.GL_FLAT);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		gluPerspective(70, (float)Window.WINDOW_WIDTH/(float)Window.WINDOW_HEIGHT, 0.01F, 500);


		glMatrixMode(GL_MODELVIEW);
		gluLookAt(-worldSize/2, (worldSize)/4, 0, 0, 0, 0, 0, 1, 0);
		GL11.glPushMatrix();
		GL11.glPushMatrix();
	}
	
	public void render() {
		render3D();
	}
	
	private void render3D() {
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		// clear the screen
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		world.render();
	}
	

	
}

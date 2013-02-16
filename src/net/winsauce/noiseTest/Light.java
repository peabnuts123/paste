package net.winsauce.noiseTest;

import static org.lwjgl.opengl.GL11.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.lwjgl.util.vector.Vector3f;

public class Light {

	ByteBuffer buffer;
	private int GL_LIGHT_NUMBER;
	
	public Light(int lightNumber, Vector3f position) {
		GL_LIGHT_NUMBER = GL_LIGHT0 + lightNumber;
		
		//Enable Lighting
		glEnable(GL_LIGHTING);
		float lightAmbient[] = { 0.2f, 0.2f, 0.30f, 1.0f };  // Ambient Light Values
		float lightDiffuse[] = { 0.9F, 0.88F, 0.86F, 1.0f };      // Diffuse Light Values
//		float lightAmbient[] = { 1f, 0.3f, 0.4f, 1.0f };  // Ambient Light Values
//		float lightDiffuse[] = { 0F, 1F, 0F, 1.0f };      // Diffuse Light Values
		float lightPosition[] = {position.x, position.y, position.z, 1}; // Light Position
		
		
		buffer = ByteBuffer.allocateDirect(16);
		buffer.order(ByteOrder.nativeOrder());
		glLight(GL_LIGHT_NUMBER, GL_AMBIENT, (FloatBuffer)buffer.asFloatBuffer().put(lightAmbient).flip());             // Setup The Ambient Light
		glLight(GL_LIGHT_NUMBER, GL_DIFFUSE, (FloatBuffer)buffer.asFloatBuffer().put(lightDiffuse).flip());             // Setup The Diffuse Light
		glLight(GL_LIGHT_NUMBER, GL_POSITION,(FloatBuffer)buffer.asFloatBuffer().put(lightPosition).flip());         	// Position The Light
		glEnable(GL_LIGHT_NUMBER);
		
		glEnable(GL_COLOR_MATERIAL);
	}
	
	public void shade() {
		glLight(GL_LIGHT_NUMBER, GL_POSITION, (FloatBuffer)buffer.asFloatBuffer());
	}
}

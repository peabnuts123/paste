package net.winsauce.noiseTest;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import libnoiseforjava.NoiseGen.NoiseQuality;
import libnoiseforjava.exception.ExceptionInvalidParam;
import libnoiseforjava.module.Add;
import libnoiseforjava.module.Billow;
import libnoiseforjava.module.Const;
import libnoiseforjava.module.Multiply;
import libnoiseforjava.module.RidgedMulti;
import libnoiseforjava.util.ImageCafe;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

public class World {

	private float[][] points;
	private Light light;

	float angle = 0;
	
	ImageCafe texture;
	Multiply generator;
	int offset = 0;

	public World(int width, int depth) {
		points = new float[width][depth];

		try {
			
			//Noise Generation computation
			//============================
			Billow billowA = new Billow();
			billowA.setLacunarity(2);
			billowA.setFrequency(1.6);
			billowA.setNoiseQuality(NoiseQuality.QUALITY_FAST);
			billowA.setOctaveCount(2);
			
			Billow billowB = new Billow();
			billowB.setFrequency(1.2);
			billowB.setNoiseQuality(NoiseQuality.QUALITY_FAST);
			billowB.setOctaveCount(4);
			
			Multiply billow = new Multiply(billowA, billowB);
			
			RidgedMulti fractal = new RidgedMulti();
			fractal.setFrequency(2);
			fractal.setOctaveCount(4);

			Add billowFractal = new Add(billow, fractal);
			
			double height = 8;
			
			//Multiply
			Const factor = new Const();
			factor.setConstValue(height);
			generator = new Multiply(billowFractal, factor);
			
			
			////==================================================================================
			////	Reddit
			////	
			//// This is the code in Question, the code to call generator's getValue() method,
			//// Which I intend to call in Parallel
			////
			////==================================================================================
			
			//Code to compute world in Multiple Threads
			boolean computeMultiThread = true;
			if(computeMultiThread) {
				//Establish Thread Pool, to hopefully distribute the generation task over #CPU_COUNT processor cores
				ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

				//Execute the #CPU_COUNT chunks
				for(int coreNumber = 0; coreNumber < pool.getMaximumPoolSize(); coreNumber++) {
					//========================================================
					//The WorldGenThread class is the one I am interested in
					//========================================================
					pool.execute(new WorldGenThread(generator, points, coreNumber, pool.getMaximumPoolSize()));
				}
				
				try {
					//Shutdown our pool once we are finished
					pool.shutdown();
					pool.awaitTermination(20000, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			//Below is the code to compute the world in a Single Thread, it takes the same amount of time
			boolean computeSingleThread = true;
			if(computeSingleThread) {
				
				//Just a timing mechanism
				long startTime = System.currentTimeMillis();
				for(int x=0; x<width; x++) {
					for(int z=0; z<depth; z++) {
						
						//==================================================
						//This is the call that I want to make in Parallel
						//==================================================
						points[x][z] = (float) generator.getValue(x/(float)width, 0, z/(float)depth);
					}
				}

				System.out.println("Single-Thread Processing: " + (System.currentTimeMillis() - startTime) + "ms");
			}
			
			
			
			
		} catch (ExceptionInvalidParam e) {
			e.printStackTrace();
		}

		light = new Light(0, new Vector3f(-(width + 100), 100, (depth + 100)));
	}
	

	public void render() { 
//		angle += 0.3;


		GL11.glPushMatrix();
//		GL11.glEnable(GL11.GL_NORMALIZE);

		GL11.glRotatef(angle, 0, 1, 0);
		GL11.glTranslatef(-points.length/2, 0, -points[0].length/2);

		light.shade();
//		GL11.glColor3f(1, 0.76F, 0.5F);

		/**
		 * Cross Product:
		 * |a.x|		|b.x|
		 * |a.y|		|b.y|
		 * |a.z|		|b.z|
		 * 
		 * 
		 * |a.y*b.z - b.y*a.z|
		 * |a.x*b.z - b.x*a.z|
		 * |a.x*b.y - b.x*a.y|
		 * 
		 * |a.y - b.y|
		 * |1*1 - 1*1|
		 * |b.y - a.y|
		 */

//		ColorCafe pointColor;
//		
//		for(int x=0; x+1<points.length; x++) {
//			for(int z=0; z+1<points[x].length; z++) {
//				
//				GL11.glBegin(GL11.GL_TRIANGLES);
//				GL11.glNormal3f(points[x][z] - points[x+1][z], 1, points[x][z] - points[x][z+1]);
//				pointColor = texture.getValue(x, z);
//				GL11.glColor3f(pointColor.getRed()/255.0F, pointColor.getGreen()/255.0F, pointColor.getBlue()/255.0F);
//				GL11.glVertex3f(x, points[x][z], z);
//				
//				pointColor = texture.getValue(x, z+1);
//				GL11.glColor3f(pointColor.getRed()/255.0F, pointColor.getGreen()/255.0F, pointColor.getBlue()/255.0F);
//				GL11.glVertex3f(x, points[x][z+1], z+1);
//				
//				pointColor = texture.getValue(x+1, z);
//				GL11.glColor3f(pointColor.getRed()/255.0F, pointColor.getGreen()/255.0F, pointColor.getBlue()/255.0F);
//				GL11.glVertex3f(x+1, points[x+1][z], z);
//				
//				
//				GL11.glNormal3f(points[x][z+1] - points[x+1][z+1], 1, points[x+1][z] - points[x+1][z+1]);
//				pointColor = texture.getValue(x+1, z+1);
//				GL11.glColor3f(pointColor.getRed()/255.0F, pointColor.getGreen()/255.0F, pointColor.getBlue()/255.0F);
//				GL11.glVertex3f(x+1, points[x+1][z+1], z+1);
//				
//				pointColor = texture.getValue(x+1, z);
//				GL11.glColor3f(pointColor.getRed()/255.0F, pointColor.getGreen()/255.0F, pointColor.getBlue()/255.0F);
//				GL11.glVertex3f(x+1, points[x+1][z], z);
//				
//				pointColor = texture.getValue(x, z+1);
//				GL11.glColor3f(pointColor.getRed()/255.0F, pointColor.getGreen()/255.0F, pointColor.getBlue()/255.0F);
//				GL11.glVertex3f(x, points[x][z+1], z+1);
//				GL11.glEnd();
//			}
//		}
		for(int x=0; x+1<points.length; x++) {
			for(int z=0; z+1<points[x].length; z++) {

				GL11.glBegin(GL11.GL_TRIANGLES);
				GL11.glNormal3f(points[x][z] - points[x+1][z], 1, points[x][z] - points[x][z+1]);
				GL11.glVertex3f(x, points[x][z], z);
				GL11.glVertex3f(x, points[x][z+1], z+1);
				GL11.glVertex3f(x+1, points[x+1][z], z);


				GL11.glNormal3f(points[x][z+1] - points[x+1][z+1], 1, points[x+1][z] - points[x+1][z+1]);
				GL11.glVertex3f(x+1, points[x+1][z+1], z+1);
				GL11.glVertex3f(x+1, points[x+1][z], z);
				GL11.glVertex3f(x, points[x][z+1], z+1);
				GL11.glEnd();
			}
		}
		
		offset++;
		
		for(int x=1; x<points.length; x++) {
			for(int z=0; z<points[x].length; z++) {
				points[x-1][z] = points[x][z]; 
			}
		}
		
		for(int z=0; z<points[0].length; z++) {
			points[points.length-1][z] = (float) generator.getValue(1 + (offset/(float)points.length), 0, z/(double)points[0].length);
		}
//		for(int x=0; x+1<points.length; x++) {
//			for(int z=0; z+1<points[x].length; z++) {
//
//				GL11.glBegin(GL11.GL_TRIANGLES);
//				GL11.glNormal3f(points[x][z] - points[x+1][z], 1, points[x][z] - points[x][z+1]);
//				GL11.glVertex3f(x, points[x][z], z);
//				GL11.glVertex3f(x, points[x][z+1], z+1);
//				GL11.glVertex3f(x+1, points[x+1][z], z);
//
//
//				GL11.glNormal3f(points[x][z+1] - points[x+1][z+1], 1, points[x+1][z] - points[x+1][z+1]);
//				GL11.glVertex3f(x+1, points[x+1][z+1], z+1);
//				GL11.glVertex3f(x+1, points[x+1][z], z);
//				GL11.glVertex3f(x, points[x][z+1], z+1);
//				GL11.glEnd();
//			}
//		}

//		GL11.glDisable(GL11.GL_NORMALIZE);
		GL11.glPopMatrix();
	}
}

package net.winsauce.noiseTest;

import libnoise.NoiseGen;
import libnoise.module.*;
import libnoise.util.ImageCafe;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class World {

    private static final Logger LOG = LoggerFactory.getLogger(World.class);

    private final int width;
    private final int depth;

	private float[][] points;
	private Light light;

	private float angle = 0;
	
	private ImageCafe texture;
	private Multiply generator;
	private int offset = 0;

	public World(int width, int depth) {
		this.width = width;
        this.depth = depth;
	}

    public void init() throws Exception {

        //Noise Generation computation
        //============================
        final Billow billowA = new Billow();
        billowA.setLacunarity(2);
        billowA.setFrequency(1.6);
        billowA.setNoiseQuality(NoiseGen.NoiseQuality.QUALITY_FAST);
        billowA.setOctaveCount(2);

        final Billow billowB = new Billow();
        billowB.setFrequency(1.2);
        billowB.setNoiseQuality(NoiseGen.NoiseQuality.QUALITY_FAST);
        billowB.setOctaveCount(4);

        final Multiply billow = new Multiply(billowA, billowB);

        final RidgedMulti fractal = new RidgedMulti();
        fractal.setFrequency(2);
        fractal.setOctaveCount(4);

        final Add billowFractal = new Add(billow, fractal);

        final double height = 8;

        //Multiply
        final Const factor = new Const();
        factor.setConstValue(height);
        this.generator = new Multiply(billowFractal, factor);

        // Noise generation

        final int noThreads = Runtime.getRuntime().availableProcessors();
        final NoiseGenerator noiseGenerator = new NoiseGenerator(generator, noThreads);
        noiseGenerator.init();

        final long startMs = System.currentTimeMillis();
        this.points = noiseGenerator.generate(width, 10, depth, 10).get();
        final long elapsedMs = System.currentTimeMillis() - startMs;

        LOG.debug("Noise generated in {} ms", elapsedMs);


        this.light = new Light(0, new Vector3f(-(width + 100), 100, (depth + 100)));
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

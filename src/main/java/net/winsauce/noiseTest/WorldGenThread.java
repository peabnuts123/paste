package net.winsauce.noiseTest;

import libnoise.module.ModuleBase;

public class WorldGenThread implements Runnable {

	private float[][] points;
	private int section, numSections;
	private ModuleBase generator;
	
	private static int finishedCount = 0;
	private static long startTime = -1;
	
	/*
	 * generator 	- Generator instance that I store statically, because fuck.
	 * points 		- the array we will be writing to concurrently. 
	 * 					It's safe because no two threads write to the same index
	 * section 		- The section we are writing to i.e. (1, out of 4). 0-start-indexed.
	 * numSections	- The number of sections, i.e. if numSections if 4 we will have sections 0,1,2,3
	 * 
	 */
	public WorldGenThread(ModuleBase generator, float[][] points, int section, int numSections) {
		this.points = points;
		this.section = section;
		this.numSections = numSections;
		this.generator = generator;
	}
	
	@Override
	public void run() {
		//The first thread will store the start time
		if(startTime == -1) startTime = System.currentTimeMillis();
		
		//Our position to start iterating from
		int start = (points.length/numSections+1) * section;
		

		//All just generation code, the getValue is what I care about
		finished:
		for(int x=0 + start; x<(points.length / numSections + 1) + start; x++) {
			try {
				for(int z=0; z<points[x].length; z++) {
					
					//========================================================================
					// This is the line that takes longer the more threads you run,
					// i.e. 4 times longer if you are running 4 threads etc.
					// It makes calls to the following functions (All of which are contained within libnoiseforjava.module):
					//		Const.getValue()
					// 		Add.getValue()
					// 		Multiply.getValue()
					// 		RidgedMulti.getValue()	(Computationally Intensive)
					// 		Billow.getValue()		(Computationally Intensive)
					//
					//		Const, Add and Multiply are barely worth looking at, since they just make calls to RidgedMulti and Billow,
					//		and simply add or multiply the result, Const.getValue() simply returns a value.
					//========================================================================
					points[x][z] = (float) generator.getValue(x/(double)points.length, 0, z/(double)points[x].length);
				}
			} catch(ArrayIndexOutOfBoundsException e) {
				if(section == numSections-1) break finished;
				else e.printStackTrace();
			}
		}
		
		//The last section to finish prints out the time taken (measured from first thread to start to last thread to finish)
		finishedCount++;
		if(finishedCount == numSections) {
			System.out.println("Parallel Processing: " + (System.currentTimeMillis() - startTime) + "ms");
		}
	}
	
}

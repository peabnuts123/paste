package net.winsauce.noiseTest;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import libnoise.module.Multiply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/**
 * Created by Brian McGee
 */
public class NoiseGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(NoiseGenerator.class);

    private final int noThreads;

    // This executor should probably be a shared system wide executor which can be used for any async tasks across all components
    private ListeningExecutorService executor;
    private Multiply generator;

    public NoiseGenerator(Multiply generator, int noThreads) {
        this.noThreads = noThreads;
        this.generator = generator;
    }

    public void init() throws Exception {
        if(noThreads == 1){
            this.executor = MoreExecutors.sameThreadExecutor();
        }else{
            this.executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(noThreads));
        }
    }

    /**
     * This method performs a 2D de-composition of the problem, sub-dividing it into cells of xChunkSize * yChunkSize
     * and then scheduling those cells for calculation in a shared executor service.
     *
     * @param totalX        Total width of the problem space
     * @param xChunkSize    Determines how the problem space is chunked in the x axis
     * @param totalZ        Total height of the problem space
     * @param zChunkSize    Determines how the probelm space is chunked in the z axis
     * @return
     */
    public ListenableFuture<float[][]> generate(int totalX, int xChunkSize, int totalZ, int zChunkSize){

        // 2D decomposition of the problem space

        final float[][] result = new float[totalX][totalZ];
        final List<ListenableFuture<Object>> futures = new ArrayList<ListenableFuture<Object>>();

        for(int x=0; x < totalX + xChunkSize; x+= xChunkSize){
            for(int z=0; z < totalZ + zChunkSize; z+= zChunkSize){
                final GenerateConfig config = new GenerateConfig();

                config.totalX = totalX;
                config.totalZ = totalZ;

                // x config
                config.startX = (x < totalX) ? x : totalX;
                config.endX = config.startX + xChunkSize;
                if(config.endX > totalX){
                    config.endX = totalX;
                }

                // z config
                config.startZ = (z < totalZ) ? z : totalZ;
                config.endZ = config.startZ + zChunkSize;
                if(config.endZ > totalZ){
                    config.endZ = totalZ;
                }

                // Schedule for calculation
                final GenerateTask task = new GenerateTask(result, config);
                futures.add(executor.submit(task));
            }
        }

        final ListenableFuture<List<Object>> futureAll = Futures.allAsList(futures);
        final ListenableFuture<float[][]> resultFuture = Futures.transform(futureAll, new Function<List<Object>, float[][]>() {
            @Override
            public float[][] apply(List<Object> objects) {
                return result;
            }
        });

        return resultFuture;
    }

    /**
     * Restricts the section of the global result array that each thread calculates noise for
     */
    private class GenerateConfig {

        int totalX = 0;
        int totalZ = 0;

        int startX = 0;
        int endX = 0;
        int startZ = 0;
        int endZ = 0;


        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("GenerateConfig");
            sb.append("{totalX=").append(totalX);
            sb.append(", totalZ=").append(totalZ);
            sb.append(", startX=").append(startX);
            sb.append(", endX=").append(endX);
            sb.append(", startZ=").append(startZ);
            sb.append(", endZ=").append(endZ);
            sb.append('}');
            return sb.toString();
        }
    }

    /**
     * A task which generates the noise for a given cell
     *
     * Implements Callable although it doesn't return a value, this is to allow for use of Guava's Futures utilities.
     */
    private final class GenerateTask implements Callable<Object> {

        private final float[][] result;         // Global array into which each thread writes values for it's cell
        private final GenerateConfig config;    // Config which determines the cell size

        private GenerateTask(float[][] result, GenerateConfig config) {
            this.result = result;
            this.config = config;
        }

        @Override
        public Object call() throws Exception {

            final int startX = config.startX;
            final int endX = config.endX;
            final int totalX = config.totalX;

            final int startZ = config.startZ;
            final int endZ = config.endZ;
            final int totalZ = config.totalZ;

            for(int x=startX; x<endX; x++) {
                for(int z=startZ; z<endZ; z++) {

                    final double xValue = (x * 1.0) / totalX;
                    final double yValue = 0;
                    final double zValue = (z * 1.0) / totalZ;

                    result[x][z] = (float) generator.getValue(xValue, yValue, zValue);
                }
            }

            return null;    // Not concerned with the return type as the result array is common to all tasks
        }

    }

}

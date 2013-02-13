/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.grid.op;

// External Imports
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.j3d.geom.GeometryData;
import org.j3d.geom.TorusGenerator;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

// Internal Imports
import abfab3d.geom.TorusCreator;
import abfab3d.geom.TriangleModelCreator;
import abfab3d.grid.*;
import abfab3d.io.output.BoxesX3DExporter;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;

/**
 * Tests the functionality of ErosinShape Operation
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestErosionShapeMT extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestErosionShapeMT.class);
    }

    public void _testBlockErosion() {

        int nx = 300, ny = 350, nz = 400;

        int offset = 10;

        AttributeGrid grid;

        for(int k = 2; k <= 6; k++){
            
            grid = makeBlock(nx+2*offset, ny+2*offset, nz+2*offset, offset);
            VoxelShape shape = VoxelShapeFactory.getBall(k,0,0);

            int origVolume =  grid.findCount(0);
            ErosionShape dil = new ErosionShape();
            dil.setVoxelShape(shape);            
            grid = dil.execute(grid);            
            
            //writeFile(grid, fmt("/tmp/erosionBlockEroded_%d.x3d", k));
            
            int erodedVolume =  grid.findCount(0);
            int exactVolume = (nx-2*k)*(ny-2*k)*(nz-2*k);

            grid = makeBlock(nx+2*offset, ny+2*offset, nz+2*offset, offset);
            // testing MT erosion 
            ErosionShapeMT dilm = new ErosionShapeMT();
            dilm.setThreadCount(4);
            dilm.setSliceSize(5);

            dilm.setVoxelShape(shape);            
            grid = dilm.execute(grid);            
            //writeFile(grid, fmt("/tmp/erosionBlockErodedMT_%d.x3d", k));
            int erodedVolumeMT =  grid.findCount(0);

            printf("orig volume: %d eroded volume: %d erodeVolumeMT: %d, exactVolume: %d\n",origVolume, erodedVolume, erodedVolumeMT, exactVolume);
            assertTrue("test of eroded block volume", (erodedVolume == exactVolume) && ((erodedVolumeMT == exactVolume)));

        }
    }

    public void testSpeed() {

        int size = 100;
        //int nx = 100, ny = 200, nz = 300;
        int nx = 500, ny = 500, nz = 500;
        int erosionSize = 4;
        int offset = erosionSize+1;
        int threadCount = 4;

        printf("grid: %d x %d x %d\n", nx, ny, nz);
        printf("erosion Size: %d\n", erosionSize);
        
        AttributeGrid grid = makeBlock(nx, ny, nz, offset);
        
        VoxelShape shape = VoxelShapeFactory.getBall(erosionSize,0,0);

        ErosionShape op = new ErosionShape();        
        op.setVoxelShape(shape);
        long t0 = time();
        grid = op.execute(grid);            
        printf("ErosionShape time: %d ms\n", (time() - t0));
        
        for(int t = 1; t <= 4; t++){
            threadCount = t;
            // test MT erosion
            grid = makeBlock(nx, ny, nz, offset);
            ErosionShapeMT opm = new ErosionShapeMT();        
            opm.setVoxelShape(shape);
            opm.setThreadCount(threadCount);
            opm.setSliceSize(2);
            t0 = time();
            opm.execute(grid);
            printf("ErosionShapeMT(%d) time: %d ms\n", threadCount, (time() - t0));            
        }

        // test old sphere dilation
        //grid = makeBlock(nx, ny, nz, offset);
        //ErosionMask ops = new ErosionMask(erosionSize,0);        
        //t0 = time();
        //ops.execute(grid);
        //printf("old spherical erosion time: %d ms\n", (time() - t0));
        
    }

    private AttributeGrid makeBlock(int nx, int ny, int nz, int offset) {
        
        AttributeGrid grid = new GridShortIntervals(nx, ny, nz, 0.001, 0.001);
        //AttributeGrid grid = new ArrayAttributeGridByte(nx, ny, nz, 0.001, 0.001);

        for (int y = offset; y < ny - offset; y++) {
            for (int x = offset; x < nx - offset; x++) {
                for (int z = offset; z < nz - offset; z++) {
                    //printf("%d %d %d\n",x,y,z);
                    grid.setState(x,y,z, Grid.INTERIOR);
                }
            }
        }
        return grid;
    }

    private void writeFile(Grid grid, String filename) {

        try {

            ErrorReporter console = new PlainTextErrorReporter();

            FileOutputStream fos = new FileOutputStream(filename);
            String encoding = filename.substring(filename.lastIndexOf(".")+1);
            BoxesX3DExporter exporter = new BoxesX3DExporter(encoding, fos, console);

            HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
            colors.put(new Integer(Grid.INTERIOR), new float[] {0,1,0});
            //colors.put(new Integer(Grid.EXTERIOR), new float[] {1,0,0});
            //colors.put(new Integer(Grid.OUTSIDE), new float[] {0,1,1});

            HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
            transparency.put(new Integer(Grid.INTERIOR), new Float(0));
            //transparency.put(new Integer(Grid.EXTERIOR), new Float(0.5));
            //transparency.put(new Integer(Grid.OUTSIDE), new Float(1));

            exporter.writeDebug(grid, colors, transparency);

            exporter.close();

            fos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    


    public static void main(String[] args) {


    }
}
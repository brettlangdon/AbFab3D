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

package abfab3d.datasources;

// External Imports


import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.awt.Font;
import java.awt.Insets;


import java.io.File;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import javax.vecmath.Matrix4d;

// external imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// Internal Imports
import abfab3d.grid.Grid;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.GridShortIntervals;

import abfab3d.util.Vec;
import abfab3d.util.MathUtil;
import abfab3d.util.TextUtil;
import abfab3d.util.Symmetry;
import abfab3d.util.VecTransform;

import abfab3d.datasources.Box;
import abfab3d.datasources.Sphere;
import abfab3d.datasources.Ring;
import abfab3d.datasources.DataTransformer;
import abfab3d.datasources.Intersection;
import abfab3d.datasources.Union;
import abfab3d.datasources.Subtraction;

import abfab3d.transforms.RingWrap;
import abfab3d.transforms.FriezeSymmetry;
import abfab3d.transforms.WallpaperSymmetry;
import abfab3d.transforms.Rotation;
import abfab3d.transforms.CompositeTransform;
import abfab3d.transforms.Scale;
import abfab3d.transforms.SphereInversion;
import abfab3d.transforms.Translation;
import abfab3d.transforms.PlaneReflection;

import abfab3d.io.output.IsosurfaceMaker;
import abfab3d.io.output.STLWriter;
import abfab3d.io.output.MeshMakerMT;

import abfab3d.util.ImageMipMap;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;
import static abfab3d.util.MathUtil.TORAD;

import static java.lang.System.currentTimeMillis;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;
import static java.lang.Math.PI;

import static abfab3d.util.VecTransform.RESULT_OK;

/**
 * Tests the functionality of Text2D
 *
 * @version
 */
public class TestText2D extends TestCase {

    
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestText2D.class);
    }

    int gridMaxAttributeValue = 127;

    public void testBitmapSize() {

        printf("testBitmapSize()\n");
        String text = "Shapeways";

        Text2D t = new Text2D(text);
        t.set("fontSize", 15);
        t.set("inset", 0.1);

        t.initialize();
        BufferedImage image = t.getImage();
        try {
            ImageIO.write(image, "png", new File("/tmp/text.png"));
        } catch(Exception e){
            e.printStackTrace();
        }

    }
   
    public static void main(String[] args) {
        new TestText2D().testBitmapSize();
    }
}
/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package opencl;

import javax.vecmath.Vector3d;

import abfab3d.param.Parameterizable;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.param.SNode;

import abfab3d.datasources.Embossing;

import static opencl.CLUtils.floatToInt;

import static abfab3d.util.Output.printf;

/**
   OpenCL code for embossing

   @author Vladimir Bulatov
 */
public class CLEmbossing extends CLNodeBase {
    /*
// embossing
typedef struct {
    int size;
    int opcode;
    float blendWidth;
    float minValue;  // min threshold
    float maxValue;  // max threshold
    float vFactor;   // displacement = vFactor*v + vOffset
    float vOffset;  
} sEmbossing;

    */

    static int STRUCTSIZE = 8;
    
    int buffer[] = new int[STRUCTSIZE];
    
    public int getCLCode(Parameterizable pnode, CLCodeBuffer codeBuffer) {

        Embossing node = (Embossing)pnode;


        Parameterizable shape = (Parameterizable)node.get("baseShape");
        Parameterizable embosser = (Parameterizable)node.get("embosser");
        
        double blendWidth = (Double)node.get("blend");
        double minValue = (Double)node.get("minValue");
        double maxValue = (Double)node.get("maxValue");
        double factor = (Double)node.get("factor");
        double offset = (Double)node.get("offset");

        int wcount =  super.getTransformCLCode(node,codeBuffer);

        // save working register 
        wcount += CLUtils.addOPCode(Opcodes.oPUSH_D2, codeBuffer);

        // we calculate shapes in opposite order to save resisters movement 
        CLCodeGenerator clnode = CLNodeFactory.getCLNode((Parameterizable)embosser);

        wcount += CLUtils.addOPCode(Opcodes.oPUSH_P1, codeBuffer);                   
        wcount += clnode.getCLCode(embosser, codeBuffer);
        wcount += CLUtils.addOPCode(Opcodes.oPOP_P1, codeBuffer);                   
        wcount += CLUtils.addOPCode(Opcodes.oCOPY_D1D2, codeBuffer);

        clnode = CLNodeFactory.getCLNode((Parameterizable)shape);
        wcount += CLUtils.addOPCode(Opcodes.oPUSH_P1, codeBuffer);                   
        wcount += clnode.getCLCode((Parameterizable)shape, codeBuffer);
        wcount += CLUtils.addOPCode(Opcodes.oPOP_P1, codeBuffer);                   
        // 
        // emboss(D1, D2, D2) 
        //
        int c = 0;
        buffer[c++] = STRUCTSIZE;
        buffer[c++] = Opcodes.oEMBOSSING;
        buffer[c++] = floatToInt(blendWidth);  
        buffer[c++] = floatToInt(minValue);  
        buffer[c++] = floatToInt(maxValue);  
        buffer[c++] = floatToInt(factor);  
        buffer[c++] = floatToInt(offset);  

        codeBuffer.add(buffer, STRUCTSIZE);

        wcount += CLUtils.addOPCode(Opcodes.oCOPY_D2D1, codeBuffer);           
        // restore working register 
        wcount += CLUtils.addOPCode(Opcodes.oPOP_D2, codeBuffer);
        // get material code 
        wcount +=  super.getMaterialCLCode(node,codeBuffer);
 
       return wcount;
    }
}
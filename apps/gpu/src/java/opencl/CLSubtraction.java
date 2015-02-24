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

import abfab3d.datasources.Subtraction;

import static opencl.CLUtils.floatToInt;

import static abfab3d.util.Output.printf;

/**
  OpenCL code for subtraction

   @author Vladimir Bulatov
 */
public class CLSubtraction extends CLNodeBase {

    static int OPCODE = Opcodes.oBLENDMAX;
    static int STRUCTSIZE = 4;
    
    int buffer[] = new int[STRUCTSIZE];
    
    public int getCLCode(Parameterizable node, CLCodeBuffer codeBuffer) {

        Subtraction sub = (Subtraction)node;


        SNode[] children = sub.getChildren();
        if(children.length < 2) {
            // no children - do nothing 
            return 0;
        }
        
        double blendWidth = ((DoubleParameter)sub.getParam("blend")).getValue();

        int wcount =  super.getTransformCLCode(node,codeBuffer);

        // save working register 
        wcount += CLUtils.addOPCode(Opcodes.oPUSH_D2, codeBuffer);

        // we calculate shapes in opposite order to save resisters movement 
        Parameterizable child = (Parameterizable)children[1];
        CLCodeGenerator clnode = CLNodeFactory.getCLNode((Parameterizable)child);

        wcount += CLUtils.addOPCode(Opcodes.oPUSH_P1, codeBuffer);                   
        wcount += clnode.getCLCode((Parameterizable)child, codeBuffer);
        wcount += CLUtils.addOPCode(Opcodes.oPOP_P1, codeBuffer);                   
        wcount += CLUtils.addOPCode(Opcodes.oCOPY_D1D2, codeBuffer);

        child = (Parameterizable)children[0];
        clnode = CLNodeFactory.getCLNode((Parameterizable)child);
        wcount += CLUtils.addOPCode(Opcodes.oPUSH_P1, codeBuffer);                   
        wcount += clnode.getCLCode((Parameterizable)child, codeBuffer);
        wcount += CLUtils.addOPCode(Opcodes.oPOP_P1, codeBuffer);                   
        // 
        // subtraction(D1, D2, D2) 
        //
        if(blendWidth != 0.) {
            int c = 0;
            buffer[c++] = STRUCTSIZE;
            buffer[c++] = Opcodes.oBLENDSUBTRACT;
            buffer[c++] = floatToInt(blendWidth);  
            codeBuffer.add(buffer, STRUCTSIZE);
        } else {
            wcount += CLUtils.addOPCode(Opcodes.oSUBTRACT, codeBuffer); 
        }        

        wcount += CLUtils.addOPCode(Opcodes.oCOPY_D2D1, codeBuffer);           
        // restore working register 
        wcount += CLUtils.addOPCode(Opcodes.oPOP_D2, codeBuffer);
        // get material code 
        wcount +=  super.getMaterialCLCode(node,codeBuffer);
 
       return wcount;
    }
}
/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.icw.contract.vm.instructions.references;

import io.icw.contract.vm.Frame;
import io.icw.contract.vm.MethodArgs;
import io.icw.contract.vm.ObjectRef;
import io.icw.contract.vm.code.MethodCode;
import io.icw.contract.vm.code.VariableType;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;

public class Invokeinterface {

    public static void invokeinterface(Frame frame) {
        MethodInsnNode methodInsnNode = frame.methodInsnNode();
        String interfaceName = methodInsnNode.owner;
        String interfaceMethodName = methodInsnNode.name;
        String interfaceMethodDesc = methodInsnNode.desc;

        List<VariableType> variableTypes = VariableType.parseArgs(interfaceMethodDesc);
        MethodArgs methodArgs = new MethodArgs(variableTypes, frame.operandStack, false);
        ObjectRef objectRef = methodArgs.objectRef;
        if (objectRef == null) {
            frame.throwNullPointerException();
            return;
        }

        String className = objectRef.getVariableType().getType();
        MethodCode methodCode = frame.methodArea.loadMethod(className, interfaceMethodName, interfaceMethodDesc);

        //Log.opcode(frame.getCurrentOpCode(), className, interfaceMethodName, interfaceMethodDesc);

        frame.vm.run(methodCode, methodArgs.frameArgs, true);
    }

}

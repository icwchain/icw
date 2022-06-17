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
import io.icw.contract.vm.code.Descriptors;
import org.objectweb.asm.tree.FieldInsnNode;

public class Putstatic {

    public static void putstatic(Frame frame) {
        FieldInsnNode fieldInsnNode = frame.fieldInsnNode();
        String className = fieldInsnNode.owner;
        String fieldName = fieldInsnNode.name;
        String fieldDesc = fieldInsnNode.desc;
        Object value;
        if (Descriptors.LONG_DESC.equals(fieldDesc)) {
            value = frame.operandStack.popLong();
        } else if (Descriptors.DOUBLE_DESC.equals(fieldDesc)) {
            value = frame.operandStack.popDouble();
        } else {
            value = frame.operandStack.pop();
        }
        frame.heap.putStatic(className, fieldName, value);

        //Log.result(frame.getCurrentOpCode(), value, className, fieldName);
    }

}

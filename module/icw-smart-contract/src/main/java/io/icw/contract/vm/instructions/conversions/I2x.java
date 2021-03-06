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
package io.icw.contract.vm.instructions.conversions;

import io.icw.contract.vm.Frame;

public class I2x {

    public static void i2l(Frame frame) {
        int value = frame.operandStack.popInt();
        long result = (long) value;
        frame.operandStack.pushLong(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void i2f(Frame frame) {
        int value = frame.operandStack.popInt();
        float result = (float) value;
        frame.operandStack.pushFloat(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void i2d(Frame frame) {
        int value = frame.operandStack.popInt();
        double result = (double) value;
        frame.operandStack.pushDouble(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void i2b(Frame frame) {
        int value = frame.operandStack.popInt();
        byte result = (byte) value;
        frame.operandStack.pushByte(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void i2c(Frame frame) {
        int value = frame.operandStack.popInt();
        char result = (char) value;
        frame.operandStack.pushChar(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void i2s(Frame frame) {
        int value = frame.operandStack.popInt();
        short result = (short) value;
        frame.operandStack.pushShort(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

}

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
package io.icw.cmd.client;


import io.icw.core.model.StringUtils;

/**
 * @author: Niels Wang
 */
public class CommandBuilder {
    private StringBuilder builder = new StringBuilder();
    private static final String LINE_SEPARATOR = "line.separator";
    private int i = 0;

    public CommandBuilder newLine(String content) {
        if (StringUtils.isBlank(content)) {
            return this.newLine();
        }
        builder.append(content).append(System.getProperty(LINE_SEPARATOR));
        if (i++ == 0) {
            this.newLine("\tOPTIONS:");
        }
        return this;
    }

    public CommandBuilder newLine() {
        builder.append(System.getProperty(LINE_SEPARATOR));
        return this;
    }

    @Override
    public String toString() {
        if (i == 2) {
            this.newLine("\tnone");
        }
        return builder.toString();
    }
}

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.asterix.fuzzyjoin.tokenizer;

import java.io.DataOutput;
import java.io.IOException;

public abstract class AbstractUTF8Token implements IToken {
    public static final int GOLDEN_RATIO_32 = 0x09e3779b9;

    protected int length;
    protected int tokenLength;
    protected int start;
    protected int tokenCount;
    protected byte[] data;
    protected final byte tokenTypeTag;
    protected final byte countTypeTag;

    public AbstractUTF8Token() {
        tokenTypeTag = -1;
        countTypeTag = -1;
    }

    public AbstractUTF8Token(byte tokenTypeTag, byte countTypeTag) {
        this.tokenTypeTag = tokenTypeTag;
        this.countTypeTag = countTypeTag;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public int getLength() {
        return length;
    }

    public int getLowerCaseUTF8Len(int size) {
        int lowerCaseUTF8Len = 0;
        int pos = start;
        for (int i = 0; i < size; i++) {
            char c = StringUtils.toLowerCase(StringUtils.charAt(data, pos));
            lowerCaseUTF8Len += StringUtils.getModifiedUTF8Len(c);
            pos += StringUtils.charSize(data, pos);
        }
        return lowerCaseUTF8Len;
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getTokenLength() {
        return tokenLength;
    }

    public void handleCountTypeTag(DataOutput dos) throws IOException {
        if (countTypeTag > 0) {
            dos.write(countTypeTag);
        }
    }

    public void handleTokenTypeTag(DataOutput dos) throws IOException {
        if (tokenTypeTag > 0) {
            dos.write(tokenTypeTag);
        }
    }

    @Override
    public void reset(byte[] data, int start, int length, int tokenLength, int tokenCount) {
        this.data = data;
        this.start = start;
        this.length = length;
        this.tokenLength = tokenLength;
        this.tokenCount = tokenCount;
    }

    @Override
    public void serializeTokenCount(DataOutput dos) throws IOException {
        handleCountTypeTag(dos);
        dos.writeInt(tokenCount);
    }
}

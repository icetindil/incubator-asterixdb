/*
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
package org.apache.asterix.external.indexing.dataflow;

import java.io.DataOutput;
import java.io.InputStream;

import org.apache.asterix.external.indexing.input.AbstractHDFSReader;
import org.apache.asterix.formats.nontagged.AqlSerializerDeserializerProvider;
import org.apache.asterix.om.base.AMutableInt32;
import org.apache.asterix.om.base.AMutableInt64;
import org.apache.asterix.om.types.ARecordType;
import org.apache.asterix.om.types.BuiltinType;
import org.apache.hyracks.api.comm.IFrameWriter;
import org.apache.hyracks.api.comm.VSizeFrame;
import org.apache.hyracks.api.context.IHyracksTaskContext;
import org.apache.hyracks.api.dataflow.value.ISerializerDeserializer;
import org.apache.hyracks.api.exceptions.HyracksDataException;
import org.apache.hyracks.dataflow.common.comm.io.ArrayTupleBuilder;
import org.apache.hyracks.dataflow.common.comm.io.FrameTupleAppender;
import org.apache.hyracks.dataflow.std.file.ITupleParser;

public abstract class AbstractIndexingTupleParser implements ITupleParser{

    protected ArrayTupleBuilder tb;
    protected DataOutput dos;
    protected final FrameTupleAppender appender;
    protected final ARecordType recType;
    protected final IHyracksTaskContext ctx;
    protected final IAsterixHDFSRecordParser deserializer;
    protected final AMutableInt32 aMutableInt = new AMutableInt32(0);
    protected final AMutableInt64 aMutableLong = new AMutableInt64(0);
    
    @SuppressWarnings("rawtypes")
    protected final ISerializerDeserializer intSerde = AqlSerializerDeserializerProvider.INSTANCE.getSerializerDeserializer(BuiltinType.AINT32);
    @SuppressWarnings("rawtypes")
    protected final ISerializerDeserializer longSerde = AqlSerializerDeserializerProvider.INSTANCE.getSerializerDeserializer(BuiltinType.AINT64);
    
    public AbstractIndexingTupleParser(IHyracksTaskContext ctx, ARecordType recType, IAsterixHDFSRecordParser deserializer) throws HyracksDataException {
        appender = new FrameTupleAppender(new VSizeFrame(ctx));
        this.recType = recType;
        this.ctx = ctx;
        this.deserializer = deserializer;
    }

    @Override
    public void parse(InputStream in, IFrameWriter writer) throws HyracksDataException {
        AbstractHDFSReader inReader = (AbstractHDFSReader) in;
        Object record;
        try {
            inReader.initialize();
            record = inReader.readNext();
            while (record != null) {
                tb.reset();
                deserializer.parse(record, tb.getDataOutput());
                tb.addFieldEndOffset();
                //append indexing fields
                appendIndexingData(tb, inReader);
                addTupleToFrame(writer);
                record = inReader.readNext();
            }
            appender.flush(writer, true);
        } catch (Exception e) {
            throw new HyracksDataException(e);
        }
    }

    protected abstract void appendIndexingData(ArrayTupleBuilder tb,
            AbstractHDFSReader inReader) throws Exception;

    protected void addTupleToFrame(IFrameWriter writer) throws HyracksDataException {
        if (!appender.append(tb.getFieldEndOffsets(), tb.getByteArray(), 0, tb.getSize())) {
            appender.flush(writer, true);
            if (!appender.append(tb.getFieldEndOffsets(), tb.getByteArray(), 0, tb.getSize())) {
                throw new IllegalStateException("Record is too big to fit in a frame");
            }
        }
    }

}

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
package org.apache.asterix.test.aql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import org.apache.asterix.aql.base.Statement;
import org.apache.asterix.aql.parser.AQLParser;
import org.apache.asterix.aql.parser.ParseException;
import org.apache.asterix.common.config.GlobalConfig;
import org.apache.asterix.common.exceptions.AsterixException;
import org.apache.hyracks.algebricks.common.exceptions.AlgebricksException;

public class AQLTestCase extends TestCase {

    private final File queryFile;

    AQLTestCase(File queryFile) {
        super("testAQL");
        this.queryFile = queryFile;
    }

    @Test
    public void testAQL() throws UnsupportedEncodingException, FileNotFoundException, ParseException, AsterixException,
            AlgebricksException {
        Reader fis = new BufferedReader(new InputStreamReader(new FileInputStream(queryFile), "UTF-8"));
        AQLParser parser = new AQLParser(fis);
        List<Statement> statements;
        GlobalConfig.ASTERIX_LOGGER.info(queryFile.toString());
        try {
            statements = parser.parse();
        } catch (ParseException e) {
            GlobalConfig.ASTERIX_LOGGER.warning("Failed while testing file " + fis);
            StringWriter sw = new StringWriter();
            PrintWriter writer = new PrintWriter(sw);
            e.printStackTrace(writer);
            GlobalConfig.ASTERIX_LOGGER.warning(sw.toString());
            throw new ParseException("Parsing " + queryFile.toString());
        }

    }
}

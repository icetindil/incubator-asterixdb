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
package org.apache.asterix.aql.expression;

import org.apache.asterix.aql.base.Statement;
import org.apache.asterix.aql.expression.visitor.IAqlExpressionVisitor;
import org.apache.asterix.common.exceptions.AsterixException;
import org.apache.asterix.common.functions.FunctionSignature;
import org.apache.hyracks.algebricks.common.utils.Pair;

/**
 * Represents the AQL statement for creating a secondary feed.
 * A secondary feed is one that derives its data from another (primary/secondary) feed.
 */
public class CreateSecondaryFeedStatement extends CreateFeedStatement implements Statement {

    /** The source feed that provides data for this secondary feed. */
    private final Pair<Identifier, Identifier> sourceQName;

    public CreateSecondaryFeedStatement(Pair<Identifier, Identifier> qName, Pair<Identifier, Identifier> sourceQName,
            FunctionSignature appliedFunction, boolean ifNotExists) {
        super(qName, appliedFunction, ifNotExists);
        this.sourceQName = sourceQName;
    }

    public String getSourceFeedDataverse() {
        return sourceQName.first != null ? sourceQName.first.toString()
                : getDataverseName() != null ? getDataverseName().getValue() : null;
    }

    public String getSourceFeedName() {
        return sourceQName.second != null ? sourceQName.second.toString() : null;
    }

    @Override
    public Kind getKind() {
        return Kind.CREATE_SECONDARY_FEED;
    }

    @Override
    public <R, T> R accept(IAqlExpressionVisitor<R, T> visitor, T arg) throws AsterixException {
        return visitor.visitCreateSecondaryFeedStatement(this, arg);
    }

}

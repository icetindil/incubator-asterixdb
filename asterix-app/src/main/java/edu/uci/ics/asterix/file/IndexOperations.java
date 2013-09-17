/*
 * Copyright 2009-2013 by The Regents of the University of California
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License from
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.uci.ics.asterix.file;

import edu.uci.ics.asterix.common.config.AsterixStorageProperties;
import edu.uci.ics.asterix.common.config.OptimizationConfUtil;
import edu.uci.ics.asterix.common.context.AsterixVirtualBufferCacheProvider;
import edu.uci.ics.asterix.common.exceptions.AsterixException;
import edu.uci.ics.asterix.common.ioopcallbacks.LSMBTreeIOOperationCallbackFactory;
import edu.uci.ics.asterix.metadata.MetadataException;
import edu.uci.ics.asterix.metadata.declared.AqlMetadataProvider;
import edu.uci.ics.asterix.metadata.entities.Dataset;
import edu.uci.ics.asterix.om.util.AsterixAppContextInfo;
import edu.uci.ics.asterix.transaction.management.opcallbacks.SecondaryIndexOperationTrackerProvider;
import edu.uci.ics.asterix.transaction.management.service.transaction.AsterixRuntimeComponentsProvider;
import edu.uci.ics.asterix.translator.CompiledStatements.CompiledCreateIndexStatement;
import edu.uci.ics.asterix.translator.CompiledStatements.CompiledIndexDropStatement;
import edu.uci.ics.hyracks.algebricks.common.constraints.AlgebricksPartitionConstraint;
import edu.uci.ics.hyracks.algebricks.common.constraints.AlgebricksPartitionConstraintHelper;
import edu.uci.ics.hyracks.algebricks.common.exceptions.AlgebricksException;
import edu.uci.ics.hyracks.algebricks.common.utils.Pair;
import edu.uci.ics.hyracks.algebricks.core.rewriter.base.PhysicalOptimizationConfig;
import edu.uci.ics.hyracks.api.job.JobSpecification;
import edu.uci.ics.hyracks.dataflow.std.file.IFileSplitProvider;
import edu.uci.ics.hyracks.storage.am.common.dataflow.IndexDropOperatorDescriptor;
import edu.uci.ics.hyracks.storage.am.lsm.btree.dataflow.LSMBTreeDataflowHelperFactory;

public class IndexOperations {

    private static final PhysicalOptimizationConfig physicalOptimizationConfig = OptimizationConfUtil
            .getPhysicalOptimizationConfig();

    public static JobSpecification buildSecondaryIndexCreationJobSpec(CompiledCreateIndexStatement createIndexStmt,
            AqlMetadataProvider metadataProvider) throws AsterixException, AlgebricksException {
        SecondaryIndexCreator secondaryIndexCreator = SecondaryIndexCreator.createIndexCreator(createIndexStmt,
                metadataProvider, physicalOptimizationConfig);
        return secondaryIndexCreator.buildCreationJobSpec();
    }

    public static JobSpecification buildSecondaryIndexLoadingJobSpec(CompiledCreateIndexStatement createIndexStmt,
            AqlMetadataProvider metadataProvider) throws AsterixException, AlgebricksException {
        SecondaryIndexCreator secondaryIndexCreator = SecondaryIndexCreator.createIndexCreator(createIndexStmt,
                metadataProvider, physicalOptimizationConfig);
        return secondaryIndexCreator.buildLoadingJobSpec();
    }

    public static JobSpecification buildDropSecondaryIndexJobSpec(CompiledIndexDropStatement indexDropStmt,
            AqlMetadataProvider metadataProvider, Dataset dataset) throws AlgebricksException, MetadataException {
        String dataverseName = indexDropStmt.getDataverseName() == null ? metadataProvider.getDefaultDataverseName()
                : indexDropStmt.getDataverseName();
        String datasetName = indexDropStmt.getDatasetName();
        String indexName = indexDropStmt.getIndexName();
        JobSpecification spec = JobSpecificationUtils.createJobSpecification();

        Pair<IFileSplitProvider, AlgebricksPartitionConstraint> splitsAndConstraint = metadataProvider
                .splitProviderAndPartitionConstraintsForInternalOrFeedDataset(dataverseName, datasetName, indexName);
        AsterixStorageProperties storageProperties = AsterixAppContextInfo.getInstance().getStorageProperties();
        IndexDropOperatorDescriptor btreeDrop = new IndexDropOperatorDescriptor(spec,
                AsterixRuntimeComponentsProvider.RUNTIME_PROVIDER, AsterixRuntimeComponentsProvider.RUNTIME_PROVIDER,
                splitsAndConstraint.first, new LSMBTreeDataflowHelperFactory(new AsterixVirtualBufferCacheProvider(
                        dataset.getDatasetId()), AsterixRuntimeComponentsProvider.RUNTIME_PROVIDER,
                        new SecondaryIndexOperationTrackerProvider(LSMBTreeIOOperationCallbackFactory.INSTANCE, dataset
                                .getDatasetId()), AsterixRuntimeComponentsProvider.RUNTIME_PROVIDER,
                        AsterixRuntimeComponentsProvider.RUNTIME_PROVIDER,
                        storageProperties.getBloomFilterFalsePositiveRate()));
        AlgebricksPartitionConstraintHelper
                .setPartitionConstraintInJobSpec(spec, btreeDrop, splitsAndConstraint.second);
        spec.addRoot(btreeDrop);

        return spec;
    }
}

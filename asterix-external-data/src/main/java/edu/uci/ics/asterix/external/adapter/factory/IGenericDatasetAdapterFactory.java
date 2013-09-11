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
package edu.uci.ics.asterix.external.adapter.factory;

import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.asterix.external.dataset.adapter.IControlledAdapter;
import edu.uci.ics.asterix.external.dataset.adapter.IDatasourceAdapter;
import edu.uci.ics.asterix.om.types.IAType;

/**
 * A base interface for an adapter factory that creates instance of an adapter kind that
 * is 'generic' in nature. A 'typed' adapter returns records with a configurable datatype.
 */
public interface IGenericDatasetAdapterFactory extends IAdapterFactory {

    public static final String KEY_TYPE_NAME = "output-type-name";

    /**
     * Creates an instance of IDatasourceAdapter.
     * 
     * @param configuration
     *            The configuration parameters for the adapter that is instantiated.
     *            The passed-in configuration is used to configure the created instance of the adapter.
     * @param atype
     *            The type for the ADM records that are returned by the adapter.
     * @return An instance of IDatasourceAdapter.
     * @throws Exception
     */
    public IDatasourceAdapter createAdapter(Map<String, Object> configuration, IAType atype) throws Exception;

    /**
     * Creates an instance of IDatasourceAdapter that is used to read records and their RIDs.
     * 
     * @param configuration
     *            The configuration parameters for the adapter that is instantiated.
     *            The passed-in configuration is used to configure the created instance of the adapter.
     * @param atype
     *            The type for the ADM records that are returned by the adapter (contains both original fields and RID fields).
     * @return An instance of IDatasourceAdapter.
     * @throws Exception
     */
    public IDatasourceAdapter createIndexingAdapter(Map<String, Object> configuration, IAType atype, Map<String,Integer> files) throws Exception;

    /**
     * Creates an instance of IDatasourceAdapter that is used to read records using their RIDs.
     * 
     * @param configuration
     *            The configuration parameters for the adapter that is instantiated.
     *            The passed-in configuration is used to configure the created instance of the adapter.
     * @param atype
     *            The type for the ADM records that are returned by the adapter.
     * @return An instance of IControlledAdapter.
     * @throws Exception
     */
    public IControlledAdapter createAccessByRIDAdapter(Map<String, Object> configuration, IAType atype, HashMap<Integer, String> files) throws Exception;
}

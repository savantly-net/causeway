/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.viewer.json.viewer.representations.HttpMethod;
import org.apache.isis.viewer.json.viewer.representations.Rel;

public class MutatorSpec {

    public static MutatorSpec of(Rel rel, Class<? extends Facet> validationFacetType, Class<? extends Facet> mutatorFacetType, HttpMethod httpMethod, BodyArgs argSpec) {
        return of(rel, validationFacetType, mutatorFacetType, httpMethod, argSpec, null);
    }

    public static MutatorSpec of(Rel rel, Class<? extends Facet> validationFacetType, Class<? extends Facet> mutatorFacetType, HttpMethod httpMethod, BodyArgs argSpec, String suffix) {
        return new MutatorSpec(rel, validationFacetType, mutatorFacetType, httpMethod, argSpec, suffix);
    }

    public final Rel rel;
    public final Class<? extends Facet> validationFacetType;
    public final Class<? extends Facet> mutatorFacetType;
    public final HttpMethod httpMethod;
    public final String suffix;
    public final BodyArgs arguments;

    private MutatorSpec(Rel rel, Class<? extends Facet> validationFacetType, Class<? extends Facet> mutatorFacetType, HttpMethod httpMethod, BodyArgs bodyArgs, String suffix) {
        this.rel = rel;
        this.validationFacetType = validationFacetType;
        this.mutatorFacetType = mutatorFacetType;
        this.httpMethod = httpMethod;
        this.arguments = bodyArgs;
        this.suffix = suffix;
    }

}
/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.viewer.graphql.viewer.source;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import graphql.schema.GraphQLCodeRegistry;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class QueryFieldFactory {

    private static ObjectAction objectAction;
    private final ServiceRegistry serviceRegistry;
    private final SpecificationLoader specificationLoader;

    public void queryFieldFromObjectSpecification(
            final ObjectSpecification objectSpec,
            final GqlvTopLevelQueryStructure topLevelQueryStructure,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder) {

        serviceRegistry.lookupBeanById(objectSpec.getLogicalTypeName())
        .ifPresent(service -> {
            addService(objectSpec, service, topLevelQueryStructure, codeRegistryBuilder);
        });
    }

    private void addService(
            final ObjectSpecification serviceSpec,
            final Object service,
            final GqlvTopLevelQueryStructure topLevelQueryStructure,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder) {

        val serviceStructure = new GqlvServiceStructure(serviceSpec, topLevelQueryStructure, specificationLoader);

        List<ObjectAction> objectActionList = serviceSpec.streamRuntimeActions(MixedIn.INCLUDED)
                .map(ObjectAction.class::cast)
                .collect(Collectors.toList());

        if (objectActionList.isEmpty()) {
            return;
        }

        objectActionList.forEach(serviceStructure::addAction);

        serviceStructure.buildObjectGqlType();

        GqlvServiceBehaviour serviceBehaviour = new GqlvServiceBehaviour(serviceStructure, service, specificationLoader, codeRegistryBuilder);

        serviceStructure.getSafeActions().entrySet().forEach(serviceBehaviour::addDataFetcher);

        topLevelQueryStructure.addFieldFor(serviceStructure, serviceBehaviour, codeRegistryBuilder);
    }

}

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
package org.apache.causeway.viewer.graphql.model.util;

import lombok.experimental.UtilityClass;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;

@UtilityClass
public final class TypeNames {

    public static String objectTypeNameFor(ObjectSpecification objectSpecification) {
        return sanitized(objectSpecification.getLogicalTypeName());
    }

    public static String metaTypeNameFor(ObjectSpecification objectSpecification) {
        return objectTypeNameFor(objectSpecification) + "__gql_meta";
    }

    public static String mutationsTypeNameFor(ObjectSpecification objectSpecification) {
        return objectTypeNameFor(objectSpecification) + "__gql_mutations";
    }

    public static String inputTypeNameFor(ObjectSpecification objectSpecification) {
        return objectTypeNameFor(objectSpecification) + "__gql_input";
    }

    public static String actionTypeNameFor(ObjectMember objectMember, ObjectSpecification objectSpecification) {
        String typeName = objectTypeNameFor(objectSpecification) + "__" + objectMember.getId();
        return typeName;
    }

    private static String sanitized(final String name) {
        return name.replace('.', '_').replace("#", "__").replace("()","");
    }

}

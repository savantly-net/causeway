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
package org.apache.isis.metamodel.services.swagger.internal;

import java.util.List;
import java.util.Set;

import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.springframework.stereotype.Component;

public abstract class ClassExcluderAbstract implements ClassExcluder {

    private final Set<String> packageNamesToIgnore = _Sets.newHashSet();

    protected void ignorePackage(final String packageName) {
        packageNamesToIgnore.add(packageName);
    }

    @Override
    public boolean exclude(final ObjectSpecification objectSpec) {
        if(objectSpec == null) {
            return false;
        }
        if (objectSpec.isExcludedFromMetamodel()) {
            return true;
        }

        return packageNamesToIgnore.stream()
                .anyMatch(packageName ->  objectSpec.getCorrespondingClass().getName().startsWith(packageName));
    }

    @Override
    public boolean exclude(final ObjectAction objectAction) {
        final ObjectSpecification returnType = objectAction.getReturnType();
        if(exclude(returnType)) {
            return true;
        }

        final List<ObjectSpecification> parameterTypes = objectAction.getParameterTypes();
        for (ObjectSpecification parameterType : parameterTypes) {
            if(exclude(parameterType)) {
                return true;
            }
        }
        return false;
    }
}

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
package org.apache.isis.config.registry;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;

@RequiredArgsConstructor(staticName = "of")
public final class TypeMetaData {

    /**
     * Fully qualified name of the underlying class.
     */
    @Getter private final String className;
    
    /**
     * As proposed by IoC, before any overrides.
     */
    @Getter private final String proposedBeanName;
    
    /**
     * Name override, applied only if not empty. 
     */
    @Getter @Setter
    private String beanNameOverride;
    
    /**
     * Whether this type should be made available to resolve injection points.  
     */
    @Getter @Setter
    private boolean injectable = true;
    
    @Getter(lazy=true)
    private final Class<?> underlyingClass = resolveClass();
    
    /**
     * @return the underlying class of this TypeMetaData
     */
    private Class<?> resolveClass() {
        try {
            return _Context.loadClass(className);
        } catch (ClassNotFoundException e) {
            val msg = String.format("Failed to load class for name '%s'", className);
            throw _Exceptions.unrecoverable(msg, e);
        }
    }

    public String getEffectiveBeanName() {
        return _Strings.isNullOrEmpty(beanNameOverride)
                ? proposedBeanName 
                        : beanNameOverride;
    }


}

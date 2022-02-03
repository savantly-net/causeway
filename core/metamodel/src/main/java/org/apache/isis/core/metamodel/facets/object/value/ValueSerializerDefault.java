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
package org.apache.isis.core.metamodel.facets.object.value;

import org.apache.isis.applib.value.semantics.ValueDecomposition;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Casts;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "forSemantics")
public class ValueSerializerDefault<T>
implements ValueSerializer<T> {

    public static final String ENCODED_NULL = "NULL";

    private final @NonNull ValueSemanticsProvider<T> semantics;

    @Override
    public T fromEncodedString(final Format format, final String encodedData) {
        _Assert.assertNotNull(encodedData);
        if (ENCODED_NULL.equals(encodedData)) {
            return null;
        } else {
            return semantics.compose(
                    ValueDecomposition.fromJson(semantics.getSchemaValueType(), encodedData));
        }
    }

    @Override
    public String toEncodedString(final Format format, final T value) {
        return value == null
                ? ENCODED_NULL
                : semantics.decompose(_Casts.uncheckedCast(value)).toJson();
    }

}
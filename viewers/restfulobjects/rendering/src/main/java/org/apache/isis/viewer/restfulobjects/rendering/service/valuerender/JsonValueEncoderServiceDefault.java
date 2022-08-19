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
package org.apache.isis.viewer.restfulobjects.rendering.service.valuerender;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.applib.value.semantics.ValueDecomposition;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.facets.object.value.ValueSerializer.Format;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.util.Facets;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.viewer.restfulobjects.applib.IsisModuleViewerRestfulObjectsApplib;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.rendering.service.valuerender.JsonValueConverter.Context;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named(IsisModuleViewerRestfulObjectsApplib.NAMESPACE + ".JsonValueEncoderDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Log4j2
public class JsonValueEncoderServiceDefault implements JsonValueEncoderService {

    @Inject private SpecificationLoader specificationLoader;

    private Map<Class<?>, JsonValueConverter> converterByClass = _Maps.newLinkedHashMap();

    @PostConstruct
    public void init() {
        new _JsonValueConverters().asList()
            .forEach(converter->converterByClass.put(converter.getValueClass(), converter));
    }

    @Override
    public ManagedObject asAdapter(
            final ObjectSpecification objectSpec,
            final JsonRepresentation valueRepr,
            final JsonValueConverter.Context context) {

        if(valueRepr == null) {
            return null;
        }
        if (objectSpec == null) {
            throw new IllegalArgumentException("ObjectSpecification is required");
        }
        if (!valueRepr.isValue()) {
            throw new IllegalArgumentException("Representation must be of a value");
        }

        val valueClass = objectSpec.getCorrespondingClass();
        val valueSerializer =
                Facets.valueSerializerElseFail(objectSpec, valueClass);

        final JsonValueConverter jvc = converterByClass.get(ClassUtils.resolvePrimitiveIfNecessary(valueClass));
        if(jvc == null) {
            // best effort
            if (valueRepr.isString()) {
                final String argStr = valueRepr.asString();
                return ManagedObject.of(objectSpec,
                        valueSerializer.fromEncodedString(Format.JSON, argStr));
            }
            throw new IllegalArgumentException("Unable to parse value");
        }

        val valueAsPojo = jvc.recoverValueAsPojo(valueRepr, context);
        if(valueAsPojo != null) {
            return ManagedObject.lazy(specificationLoader, valueAsPojo);
        }

        // last attempt
        if (valueRepr.isString()) {
            final String argStr = valueRepr.asString();
            try {
                return ManagedObject.of(objectSpec,
                        valueSerializer.fromEncodedString(Format.JSON, argStr));
            } catch(TextEntryParseException ex) {
                throw new IllegalArgumentException(ex.getMessage());
            }
        }

        throw new IllegalArgumentException("Could not parse value '"
                + valueRepr.asString()
                + "' as a "
                + objectSpec.getFullIdentifier());
    }

    @Override
    public void appendValueAndFormat(
            final ManagedObject valueAdapter,
            final JsonRepresentation repr,
            final Context context) {

        val valueSpec = valueAdapter.getSpecification();
        val valueClass = valueSpec.getCorrespondingClass();
        val jsonValueConverter = converterByClass.get(valueClass);
        if(jsonValueConverter != null) {
            jsonValueConverter.appendValueAndFormat(valueAdapter, context, repr);
            return;
        } else {
            final Optional<ValueDecomposition> valueDecompositionIfAny = decompose(valueAdapter);
            if(valueDecompositionIfAny.isPresent()) {
                val valueDecomposition = valueDecompositionIfAny.get();
                val valueAsJson = valueDecomposition.toJson();
                valueDecomposition.accept(
                        simple->{
                            // special treatment for BLOB/CLOB/ENUM as these are better represented by a map
                            if(simple.getType() == ValueType.BLOB
                                    || simple.getType() == ValueType.CLOB
                                    || simple.getType() == ValueType.ENUM) {
                                val decompRepr = JsonRepresentation.jsonAsMap(valueAsJson);
                                // amend emums with "enumTitle"
                                if(simple.getType() == ValueType.ENUM) {
                                    decompRepr.mapPutString("enumTitle", valueAdapter.titleString());
                                }
                                repr.mapPutJsonRepresentation("value", decompRepr);
                                appendFormats(repr, null, simple.getType().value(), context.isSuppressExtensions());
                            } else {
                                // using string representation from value semantics
                                repr.mapPutString("value", valueAsJson);
                                appendFormats(repr, "string", simple.getType().value(), context.isSuppressExtensions());
                            }
                        },
                        tuple->{
                            val decompRepr = JsonRepresentation.jsonAsMap(valueAsJson);
                            repr.mapPutJsonRepresentation("value", decompRepr);

                            val typeTupleAsFormat = "{"
                                    + tuple.getElements().stream()
                                        .map(el->el.getType().value())
                                        .collect(Collectors.joining(","))
                                    + "}";

                            appendFormats(repr, null, typeTupleAsFormat, context.isSuppressExtensions());
                        });
            } else {
                appendNullAndFormat(repr, context.isSuppressExtensions());
            }
        }
    }

    private static Optional<ValueDecomposition> decompose(final ManagedObject valueAdapter) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(valueAdapter)) {
            return Optional.empty();
        }
        val valueClass = valueAdapter.getSpecification().getCorrespondingClass();
        val decompositionIfAny = Facets.valueDefaultSemantics(valueAdapter.getSpecification(), valueClass)
                .map(composer->composer.decompose(_Casts.uncheckedCast(valueAdapter.getPojo())));
        if(decompositionIfAny.isEmpty()) {
            val valueSpec = valueAdapter.getSpecification();
            log.warn("{Could not resolve a ValueComposer for {}, "
                    + "falling back to rendering as 'null'. "
                    + "Make sure the framework has access to a ValueSemanticsProvider<{}> "
                    + "that implements ValueComposer<{}>}",
                    valueSpec.getLogicalTypeName(),
                    valueSpec.getCorrespondingClass().getSimpleName(),
                    valueSpec.getCorrespondingClass().getSimpleName());
        }
        return decompositionIfAny;
    }

    @Override
    @Nullable
    public Object asObject(final @NonNull ManagedObject adapter, final JsonValueConverter.Context context) {

        val objectSpec = adapter.getSpecification();
        val cls = objectSpec.getCorrespondingClass();

        val jsonValueConverter = converterByClass.get(cls);
        if(jsonValueConverter != null) {
            return jsonValueConverter.asObject(adapter, context);
        }

        // else
        return Facets.valueSerializerElseFail(objectSpec, cls)
                .toEncodedString(Format.JSON, _Casts.uncheckedCast(adapter.getPojo()));
    }

    /**
     * JUnit support
     */
    public static JsonValueEncoderServiceDefault forTesting(final SpecificationLoader specificationLoader) {
        val jsonValueEncoder = new JsonValueEncoderServiceDefault();
        jsonValueEncoder.specificationLoader = specificationLoader;
        jsonValueEncoder.init();
        return jsonValueEncoder;
    }

}
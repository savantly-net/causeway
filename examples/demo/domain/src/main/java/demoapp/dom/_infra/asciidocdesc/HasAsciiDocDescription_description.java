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
package demoapp.dom._infra.asciidocdesc;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Snapshot;
import org.apache.causeway.applib.annotation.ValueSemantics;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.events.domain.PropertyDomainEvent;
import org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc;

import lombok.RequiredArgsConstructor;

import demoapp.dom._infra.resources.AsciiDocReaderService;

@Property(
        snapshot = Snapshot.EXCLUDED,
        domainEvent = HasAsciiDocDescription_description.DomainEvent.class
)
@ValueSemantics(provider = "demo-adoc-pre-processor")
@RequiredArgsConstructor
public class HasAsciiDocDescription_description {

    public static class DomainEvent
            extends PropertyDomainEvent<HasAsciiDocDescription, AsciiDoc> {}

    private final HasAsciiDocDescription hasAsciiDocDescription;

    @PropertyLayout(labelPosition = LabelPosition.NONE, hidden = Where.ALL_TABLES,
            fieldSetId = "description", sequence = "1")
    public AsciiDoc prop() {
        return asciiDocReaderService.readFor(hasAsciiDocDescription, "description");
    }

    @Inject AsciiDocReaderService asciiDocReaderService;

}

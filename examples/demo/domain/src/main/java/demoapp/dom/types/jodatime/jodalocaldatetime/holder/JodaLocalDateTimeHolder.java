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
package demoapp.dom.types.jodatime.jodalocaldatetime.holder;

import org.apache.isis.applib.annotation.LogicalTypeName;

@LogicalTypeName("demo.JodaLocalDateTimeHolder")
//tag::class[]
public interface JodaLocalDateTimeHolder {

    org.joda.time.LocalDateTime getReadOnlyProperty();
    void setReadOnlyProperty(org.joda.time.LocalDateTime c);

    org.joda.time.LocalDateTime getReadWriteProperty();
    void setReadWriteProperty(org.joda.time.LocalDateTime c);

    org.joda.time.LocalDateTime getReadOnlyOptionalProperty();
    void setReadOnlyOptionalProperty(org.joda.time.LocalDateTime c);

    org.joda.time.LocalDateTime getReadWriteOptionalProperty();
    void setReadWriteOptionalProperty(org.joda.time.LocalDateTime c);

}
//end::class[]

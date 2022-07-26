/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.isis.regressiontests.cmdexecauditsess.generic.integtest.model;

import javax.inject.Named;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Publishing;

@Named("cmdexecauditsess.test.Counter")
@DomainObject(nature = Nature.ENTITY, entityChangePublishing = Publishing.ENABLED)
public abstract class Counter implements Comparable<Counter> {

    @Property(
            editing = Editing.ENABLED,
            commandPublishing = Publishing.ENABLED,
            executionPublishing = Publishing.ENABLED
    )
    public abstract Long getNum();
    public abstract void setNum(Long num);

    public abstract String getName();
    public abstract void setName(String name);

    @Action(
            commandPublishing = Publishing.ENABLED,
            executionPublishing = Publishing.ENABLED
    )
    public Counter bumpUsingDeclaredAction() {
        return doBump();
    }

    Counter doBump() {
        if (getNum() == null) {
            setNum(1L);
        } else {
            setNum(getNum() + 1);
        }
        return this;
    }

    @Override
    public int compareTo(final Counter o) {
        return this.getName().compareTo(o.getName());
    }
}
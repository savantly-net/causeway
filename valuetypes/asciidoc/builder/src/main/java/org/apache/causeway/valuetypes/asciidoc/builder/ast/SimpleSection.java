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
package org.apache.causeway.valuetypes.asciidoc.builder.ast;

import org.asciidoctor.ast.Section;

import lombok.ToString;

@ToString(callSuper = true)
public class SimpleSection extends SimpleStructuralNode implements Section {

    @Override public int index() {
        return getIndex();
    }
    @Override public int getIndex() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override public int number() {
        return getNumber();
    }
    @Override public int getNumber() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getNumeral() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override public String sectname() {
        return getSectionName();
    }
    @Override public String getSectionName() {
        return "section";
    }

    @Override public boolean special() {
        return isSpecial();
    }
    @Override public boolean isSpecial() {
        return false;
    }

    @Override public boolean numbered() {
        return isNumbered();
    }
    @Override public boolean isNumbered() {
        return false;
    }

    @Override
    public void setTitle(final String title) {
        super.setTitle(title);
        setAttribute("title", title, true);
    }

}

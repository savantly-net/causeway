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
package org.apache.causeway.security.shiro;

import org.apache.causeway.security.shiro.authorization.CausewayPermission;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class IsisPermissionTest_setParts {

    @Test
    public void noVeto() throws Exception {
        CausewayPermission ip = new CausewayPermission("com.mycompany.myapp:Customer:changeAddress:r");
        assertThat(ip.toString(), is("com.mycompany.myapp:customer:changeaddress:r"));
    }

    @Test
    public void withVetoableDomain() throws Exception {
        CausewayPermission ip = new CausewayPermission("foo/com.mycompany.myapp:Customer:changeAddress:r");
        assertThat(ip.toString(), is("foo/com.mycompany.myapp:customer:changeaddress:r"));
    }

    @Test
    public void withVetoAndVetoableDomain() throws Exception {
        CausewayPermission ip = new CausewayPermission("!foo/com.mycompany.myapp:Customer:changeAddress:r");
        assertThat(ip.toString(), is("!foo/com.mycompany.myapp:customer:changeaddress:r"));
    }


    @Test
    public void xxx() throws Exception {
        CausewayPermission ip = new CausewayPermission("schwartz/com.mycompany.myapp:Order:submit:*");
        CausewayPermission ip2 = new CausewayPermission("com.mycompany.myapp:Customer:remove:r");

        assertThat(ip2.implies(ip), is(false));
        assertThat(ip.implies(ip2), is(false));
    }

}

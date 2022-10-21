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
package org.apache.causeway.testdomain.config;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayModuleCoreConfig;
import org.apache.causeway.core.config.presets.CausewayPresets;

@SpringBootTest(
        classes = {
                CausewayModuleCoreConfig.class
        })
@TestPropertySource({
    "classpath:/application-config-test.properties",
    CausewayPresets.UseLog4j2Test
})

class IsisConfigBeanTest {

    @Inject private CausewayConfiguration isisConfiguration;

    @Test
    void configurationBean_shouldBePickedUpBySpring() {
        assertNotNull(isisConfiguration);
        assertTrue(isisConfiguration
                .getCore().getMetaModel().getIntrospector().getPolicy()
                .getMemberAnnotationPolicy().isMemberAnnotationsRequired());
    }

}

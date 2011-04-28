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
package org.apache.isis.viewer.scimpi.dispatcher.view;

import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;


public class HelpLink {

    private static String site;
    private static String suffix;

    public static void append(Request request, String description, String helpReference) {
        request.appendHtml(createHelpSegment(description, helpReference));
    }

    public static String createHelpSegment(String description, String helpReference) {
        if (site == null) {
            site = IsisContext.getConfiguration().getString(ConfigurationConstants.ROOT + "scimpi.help-site", "/help/");
        }
        if (suffix == null) {
            suffix = IsisContext.getConfiguration().getString(ConfigurationConstants.ROOT + "scimpi.help-suffix", "shtml");
            if (suffix == null || suffix.equals("")) {
                suffix = "";
            } else {
                suffix = "." + suffix;
            }
        }

        if (helpReference == null || helpReference.equals("No help available")) {
            return "";
        } else {
            String elementClass = "help-link";
            String link = site + helpReference + suffix;
            String linkText = "Help";
            String target = "scimpi-help";
            return "<a class=\"" + elementClass + "\" href=\"" + link + "\" target=\"" + target + "\" title=\"" + description
                    + "\"><img src=\"/images/help.png\" alt=\"" + linkText + "\" /></a>";
        }
    }

}

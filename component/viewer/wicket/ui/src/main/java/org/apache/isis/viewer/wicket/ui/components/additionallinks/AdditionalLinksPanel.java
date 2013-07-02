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

package org.apache.isis.viewer.wicket.ui.components.additionallinks;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;

import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.links.ListOfLinksModel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

/**
 * Panel for rendering scalars of type {@link String}.
 */
public class AdditionalLinksPanel extends PanelAbstract<ListOfLinksModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ADDITIONAL_LINK_LIST = "additionalLinkList";
    private static final String ID_ADDITIONAL_LINK_ITEM = "additionalLinkItem";
    private static final String ID_ADDITIONAL_LINK_TITLE = "additionalLinkTitle";
    
    private List<LinkAndLabel> linkAndLabels;
    
    public AdditionalLinksPanel(final String id, final List<LinkAndLabel> links) {
        super(id, new ListOfLinksModel(links));

        this.linkAndLabels = getModel().getObject();
        
        final WebMarkupContainer container = new WebMarkupContainer(ID_ADDITIONAL_LINK_LIST);
        addOrReplace(container);
        
        container.setOutputMarkupId(true);
        
        setOutputMarkupId(true);
        
        final ListView<LinkAndLabel> listView = new ListView<LinkAndLabel>(ID_ADDITIONAL_LINK_ITEM, this.linkAndLabels) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<LinkAndLabel> item) {
                final LinkAndLabel linkAndLabel = item.getModelObject();
                
                final AbstractLink link = linkAndLabel.getLink();
                        
                Label viewTitleLabel = new Label(ID_ADDITIONAL_LINK_TITLE, linkAndLabel.getLabel());
                String disabledReasonIfAny = linkAndLabel.getDisabledReasonIfAny();
                if(disabledReasonIfAny != null) {
                    viewTitleLabel.add(new AttributeAppender("title", disabledReasonIfAny));
                }
                if(linkAndLabel.isBlobOrClob()) {
                    link.add(new CssClassAppender("noVeil"));
                }
                if(linkAndLabel.isPrototype()) {
                    link.add(new CssClassAppender("prototype"));
                }
                link.add(new CssClassAppender(linkAndLabel.getActionIdentifier()));
                viewTitleLabel.add(new CssClassAppender(StringUtils.toLowerDashed(linkAndLabel.getLabel())));
                link.addOrReplace(viewTitleLabel);
                item.addOrReplace(link);
            }
        };
        container.addOrReplace(listView);
    }
    
}

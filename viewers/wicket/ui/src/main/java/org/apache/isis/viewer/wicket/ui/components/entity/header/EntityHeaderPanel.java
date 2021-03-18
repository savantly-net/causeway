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

package org.apache.isis.viewer.wicket.ui.components.entity.header;

import java.util.List;

import org.apache.wicket.Component;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.AdditionalLinksPanel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.EntityActionLinkFactory;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.LinkAndLabelUtil;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

import lombok.val;

/**
 * {@link PanelAbstract Panel} representing the summary details (title, icon and
 * actions) of an entity, as per the provided {@link EntityModel}.
 */
public class EntityHeaderPanel 
extends PanelAbstract<ManagedObject, EntityModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ENTITY_ACTIONS = "entityActions";



    public EntityHeaderPanel(final String id, final EntityModel entityModel) {
        super(id, entityModel);
    }

    /**
     * For the {@link EntityActionLinkFactory}.
     */
    public EntityModel getEntityModel() {
        return getModel();
    }

    @Override
    protected void onBeforeRender() {
        buildGui();
        super.onBeforeRender();
    }

    private void buildGui() {
        addOrReplaceIconAndTitle();
        buildEntityActionsGui();
    }

    private void addOrReplaceIconAndTitle() {
        final ComponentFactory componentFactory = getComponentFactoryRegistry().findComponentFactory(ComponentType.ENTITY_ICON_TITLE_AND_COPYLINK, getEntityModel());
        final Component component = componentFactory.createComponent(getEntityModel());
        addOrReplace(component);
    }


    private void buildEntityActionsGui() {
        final EntityModel model = getModel();
        val adapter = model.getObject();
        if (adapter != null) {
            final List<ObjectAction> topLevelActions = ObjectAction.Util
                    .findTopLevel(adapter);

            final List<LinkAndLabel> entityActionLinks = LinkAndLabelUtil
                    .asActionLinksForAdditionalLinksPanel(model, topLevelActions, null);

            AdditionalLinksPanel.addAdditionalLinks(this, ID_ENTITY_ACTIONS, entityActionLinks, AdditionalLinksPanel.Style.INLINE_LIST);
        } else {
            permanentlyHide(ID_ENTITY_ACTIONS);
        }
    }



}

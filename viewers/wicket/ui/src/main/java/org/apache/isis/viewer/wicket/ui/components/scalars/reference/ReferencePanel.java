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
package org.apache.isis.viewer.wicket.ui.components.scalars.reference;

import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.wicketstuff.select2.ChoiceProvider;
import org.wicketstuff.select2.Settings;

import org.apache.isis.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.common.model.object.ObjectUiModel.HasRenderingHints;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelSelectAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.FrameFragment;
import org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;
import org.apache.isis.viewer.wicket.ui.components.widgets.entitysimplelink.EntityLinkSimplePanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.Select2;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers.ObjectAdapterMementoProviderForReferenceChoices;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers.ObjectAdapterMementoProviderForReferenceObjectAutoComplete;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers.ObjectAdapterMementoProviderForReferenceParamOrPropertyAutoComplete;
import org.apache.isis.viewer.wicket.ui.util.Wkt;
import org.apache.isis.viewer.wicket.ui.util.Wkt.EventTopic;
import org.apache.isis.viewer.wicket.ui.util.WktComponents;
import org.apache.isis.viewer.wicket.ui.util.WktTooltips;

import lombok.val;

/**
 * Panel for rendering scalars which of are of reference type (as opposed to
 * value types).
 */
public class ReferencePanel extends ScalarPanelSelectAbstract {

    private static final long serialVersionUID = 1L;

    private static final String ID_AUTO_COMPLETE = "autoComplete";
    private static final String ID_ENTITY_ICON_TITLE = "entityIconAndTitle";

    private EntityLinkSelect2Panel entityLink;
    private EntityLinkSimplePanel entityLinkOutputFormat;
    private boolean isOutputFormat = false;

    public ReferencePanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }


    Select2 getSelect2() {
        return select2;
    }

    // //////////////////////////////////////

    // First called as a side-effect of {@link #beforeRender()}
    @Override
    protected Component createCompactFrame() {

        this.isOutputFormat = true;

        final ScalarModel scalarModel = getModel();
        final String name = scalarModel.getFriendlyName();

        this.entityLinkOutputFormat = (EntityLinkSimplePanel) getComponentFactoryRegistry()
                .createComponent(ComponentType.ENTITY_LINK, scalarModel);

        entityLinkOutputFormat.setOutputMarkupId(true);
        entityLinkOutputFormat.setLabel(Model.of(name));

        val labelIfOutput = FrameFragment.COMPACT
                .createComponent(WebMarkupContainer::new); 
                
        labelIfOutput.add(entityLinkOutputFormat);

        return labelIfOutput;
    }

    // First called as a side-effect of {@link #beforeRender()}
    @Override
    protected FormGroup createRegularFrame() {

        this.entityLink = new EntityLinkSelect2Panel(ComponentType.ENTITY_LINK.getId(), this);

        entityLink.setRequired(getModel().isRequired());
        this.select2 = createSelect2AndSemantics();
        entityLink.addOrReplace(select2.asComponent());

        entityLink.setOutputMarkupId(true);

        FormComponent<?> formComponent = this.entityLink;

        return createFormGroup(formComponent);
    }


    private Select2 createSelect2AndSemantics() {

        final Select2 select2 = createSelect2(ID_AUTO_COMPLETE);


        final Settings settings = select2.getSettings();

        // one of these three case should be true
        // (as per the isEditableWithEitherAutoCompleteOrChoices() guard above)
        if(getModel().hasChoices()) {

            settings.setPlaceholder(getModel().getFriendlyName());

        } else if(getModel().hasAutoComplete()) {

            final int minLength = getModel().getAutoCompleteMinLength();
            settings.setMinimumInputLength(minLength);
            settings.setPlaceholder(getModel().getFriendlyName());

        } else if(hasObjectAutoComplete()) {
            final ObjectSpecification typeOfSpecification = getModel().getScalarTypeSpec();
            final AutoCompleteFacet autoCompleteFacet = typeOfSpecification.getFacet(AutoCompleteFacet.class);
            final int minLength = autoCompleteFacet.getMinLength();
            settings.setMinimumInputLength(minLength);
        }

        return select2;
    }


    // //////////////////////////////////////

    @Override
    protected InlinePromptConfig getInlinePromptConfig() {
        return isOutputFormat
                ? InlinePromptConfig.notSupported()
                : InlinePromptConfig.supportedAndHide(select2.asComponent());
    }

    @Override
    protected IModel<String> obtainInlinePromptModel() {
        return select2.obtainInlinePromptModel();
    }


    // //////////////////////////////////////
    // onBeforeRender*
    // //////////////////////////////////////

    @Override
    protected void onInitializeEditable() {
        super.onInitializeEditable();
        if(isOutputFormat) return;
        entityLink.setEnabled(true);
        syncWithInput();
    }

    @Override
    protected void onInitializeNotEditable() {
        super.onInitializeNotEditable();
        if(isOutputFormat) return;
        entityLink.setEnabled(false);
        syncWithInput();
    }

    @Override
    protected void onInitializeReadonly(final String disableReason) {
        super.onInitializeReadonly(disableReason);
        if(isOutputFormat) return;
        val entityLinkModel = (HasRenderingHints) entityLink.getModel();
        entityLinkModel.toViewMode();
        entityLink.setEnabled(false);
        WktTooltips.addTooltip(entityLink, disableReason);
        syncWithInput();
    }

    @Override
    protected void onNotEditable(final String disableReason, final Optional<AjaxRequestTarget> target) {
        super.onNotEditable(disableReason, target);
        if(isOutputFormat) return;
        entityLink.setEnabled(false);
        Wkt.attributeReplace(entityLink, "title", disableReason);
    }

    @Override
    protected void onEditable(final Optional<AjaxRequestTarget> target) {
        super.onEditable(target);
        if(isOutputFormat) return;
        entityLink.setEnabled(true);
        Wkt.attributeReplace(entityLink, "title", "");
    }

    // called from onInitialize*
    // (was previous called by EntityLinkSelect2Panel in onBeforeRender, this responsibility now moved)
    private void syncWithInput() {
        val scalarModel = getModel();
        val adapter = scalarModel.getObject();

        // syncLinkWithInput
        final MarkupContainer componentForRegular = getRegularFrame();

        if(componentForRegular != null) {

            val componentFactory = getComponentFactoryRegistry()
                    .findComponentFactory(ComponentType.ENTITY_ICON_AND_TITLE, scalarModel);
            val component = componentFactory
                    .createComponent(ComponentType.ENTITY_ICON_AND_TITLE.getId(), scalarModel);
            componentForRegular.addOrReplace(component);

            val isInlinePrompt = scalarModel.isInlinePrompt();
            if(isInlinePrompt) {
                // bit of a hack... allows us to suppress the title using CSS
                Wkt.cssAppend(component, "inlinePrompt");
            }

            if(adapter != null
                    || isInlinePrompt) {
                WktComponents.permanentlyHide(componentForRegular, "entityTitleIfNull");
            } else {
                Wkt.labelAdd(componentForRegular, "entityTitleIfNull", "(none)");
            }

        }


        // syncLinkWithInputIfAutoCompleteOrChoices
        if(isEditableWithEitherAutoCompleteOrChoices()) {

            if(select2 == null) {
                throw new IllegalStateException("select2 should be created already");
            } else {
                //
                // the select2Choice already exists, so the widget has been rendered before.  If it is
                // being re-rendered now, it may be because some other property/parameter was invalid.
                // when the form was submitted, the selected object (its oid as a string) would have
                // been saved as rawInput.  If the property/parameter had been valid, then this rawInput
                // would be correctly converted and processed by the select2Choice's choiceProvider.  However,
                // an invalid property/parameter means that the webpage is re-rendered in another request,
                // and the rawInput can no longer be interpreted.  The net result is that the field appears
                // with no input.
                //
                // The fix is therefore (I think) simply to clear any rawInput, so that the select2Choice
                // renders its state from its model.
                //
                // see: FormComponent#getInputAsArray()
                // see: Select2Choice#renderInitializationScript()
                //
                select2.clearInput();
            }

            if(componentForRegular != null) {
                WktComponents.permanentlyHide(componentForRegular, ID_ENTITY_ICON_TITLE);
                WktComponents.permanentlyHide(componentForRegular, "entityTitleIfNull");
            }

            // syncUsability
            if(select2 != null) {
                final boolean mutability = entityLink.isEnableAllowed() && !getModel().isViewMode();
                select2.setEnabled(mutability);
            }

            WktComponents.permanentlyHide(entityLink, "entityLinkIfNull");
        } else {
            // this is horrid; adds a label to the id
            // should instead be a 'temporary hide'
            WktComponents.permanentlyHide(entityLink, ID_AUTO_COMPLETE);
            // setSelect2(null); // this forces recreation next time around
        }

    }

    // //////////////////////////////////////
    // setProviderAndCurrAndPending
    // //////////////////////////////////////

    @Override
    protected ChoiceProvider<ObjectMemento> buildChoiceProvider() {

        val scalarModel = getModel();

        if (scalarModel.hasChoices()) {
            return new ObjectAdapterMementoProviderForReferenceChoices(scalarModel);
        }

        if(scalarModel.hasAutoComplete()) {
            return new ObjectAdapterMementoProviderForReferenceParamOrPropertyAutoComplete(scalarModel);
        }

        return new ObjectAdapterMementoProviderForReferenceObjectAutoComplete(scalarModel);
    }

    // called by setProviderAndCurrAndPending
    @Override
    protected void syncIfNull(final Select2 select2) {
        if(getModel().isScalar()) {
            if(select2.isEmpty()) {
                select2.clear(); // why?
                getModel().setObject(null);
            }
        }
    }

    // //////////////////////////////////////
    // getInput, convertInput
    // //////////////////////////////////////

    // called by EntityLinkSelect2Panel
    String getInput() {
        val pendingElseCurrentAdapter = getModel().getObject();
        return pendingElseCurrentAdapter != null? pendingElseCurrentAdapter.titleString(): "(no object)";
    }

    // //////////////////////////////////////

    // called by EntityLinkSelect2Panel
    void convertInput() {
        if(isEditableWithEitherAutoCompleteOrChoices()) {

            // flush changes to pending

            val adapter = select2.getConvertedInputValue();
            getModel().setObject(adapter);
            getModel().clearPending();
        }

        val pendingAdapter = getModel().getObject();
        entityLink.setConvertedInput(pendingAdapter);
    }

    // //////////////////////////////////////

    @Override
    public void onUpdate(final AjaxRequestTarget target, final ScalarPanelAbstract scalarPanel) {
        super.onUpdate(target, scalarPanel);
        Wkt.javaScriptAdd(target, EventTopic.CLOSE_SELECT2, getMarkupId());
    }


    // //////////////////////////////////////
    // helpers querying model state
    // //////////////////////////////////////

    // called from convertInput, syncWithInput
    private boolean isEditableWithEitherAutoCompleteOrChoices() {
        if(getModel().getRenderingHint().isInTable()) {
            return false;
        }
        // doesn't apply if not editable, either
        if(getModel().isViewMode()) {
            return false;
        }
        return getModel().hasChoices() || getModel().hasAutoComplete() || hasObjectAutoComplete();
    }

    // called by isEditableWithEitherAutoCompleteOrChoices
    private boolean hasObjectAutoComplete() {
        final ObjectSpecification typeOfSpecification = getModel().getScalarTypeSpec();
        final AutoCompleteFacet autoCompleteFacet =
                (typeOfSpecification != null)? typeOfSpecification.getFacet(AutoCompleteFacet.class):null;
                return autoCompleteFacet != null;
    }

}



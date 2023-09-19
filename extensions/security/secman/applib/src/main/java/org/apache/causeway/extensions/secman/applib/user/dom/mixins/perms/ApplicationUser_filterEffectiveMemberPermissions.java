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
package org.apache.causeway.extensions.secman.applib.user.dom.mixins.perms;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.MinLength;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.appfeat.ApplicationFeature;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;
import org.apache.causeway.extensions.secman.applib.feature.api.ApplicationFeatureChoices;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;

import lombok.RequiredArgsConstructor;

/**
 *
 * @since 2.0 {@index}
 */
@Action(
        commandPublishing = Publishing.DISABLED,
        domainEvent = ApplicationUser_filterEffectiveMemberPermissions.DomainEvent.class,
        executionPublishing = Publishing.DISABLED,
        semantics = SemanticsOf.SAFE
)
@ActionLayout(
        associateWith = "effectiveMemberPermissions",
        named = "Filter",
        promptStyle = PromptStyle.DIALOG_MODAL,
        sequence = "1"
)
@RequiredArgsConstructor
public class ApplicationUser_filterEffectiveMemberPermissions {

    public static class DomainEvent
            extends CausewayModuleExtSecmanApplib.ActionDomainEvent<ApplicationUser_filterEffectiveMemberPermissions> {}

    @Inject private FactoryService factory;
    @Inject private ApplicationFeatureRepository featureRepository;
    @Inject private ApplicationFeatureChoices applicationFeatureChoices;

    private final ApplicationUser user;

    @MemberSupport public List<UserPermissionViewModel> act(

            @ParameterLayout(
                    describedAs = ApplicationFeatureChoices.DESCRIBED_AS
            )
            final ApplicationFeatureChoices.AppFeat feature) {

        return featureRepository
            .allMembers()
            .stream()
            .map(ApplicationFeature::getFeatureId)
            .filter(feature.getFeatureId()::contains)
            .map(UserPermissionViewModel.asViewModel(user, factory))
            .collect(Collectors.toList());
    }

    @MemberSupport public java.util.Collection<ApplicationFeatureChoices.AppFeat> autoComplete0Act(
            final @MinLength(3) String search) {
        return applicationFeatureChoices.autoCompleteFeature(search);
    }


}

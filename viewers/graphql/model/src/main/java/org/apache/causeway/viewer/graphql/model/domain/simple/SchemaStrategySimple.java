package org.apache.causeway.viewer.graphql.model.domain.simple;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.common.SchemaStrategy;
import org.apache.causeway.viewer.graphql.model.domain.common.query.GqlvDomainObject;
import org.apache.causeway.viewer.graphql.model.domain.common.query.GqlvMeta;
import org.apache.causeway.viewer.graphql.model.domain.simple.query.GqlvAction;
import org.apache.causeway.viewer.graphql.model.domain.simple.query.GqlvCollection;
import org.apache.causeway.viewer.graphql.model.domain.common.query.GqlvMemberHolder;
import org.apache.causeway.viewer.graphql.model.domain.simple.query.GqlvProperty;

public class SchemaStrategySimple implements SchemaStrategy {

    @Override
    public SchemaType getSchemaType() {
        return SchemaType.SIMPLE;
    }

    @Override
    public String topLevelFieldNameFrom(CausewayConfiguration.Viewer.Graphql graphqlConfiguration) {
        return graphqlConfiguration.getTopLevelFieldNameForSimple();
    }

    public GqlvAbstractCustom newGqlvProperty(
            final GqlvMemberHolder holder,
            final OneToOneAssociation otoa,
            final Context context
    ) {
        return new GqlvProperty(holder, otoa, context);
    };
    public GqlvAbstractCustom newGqlvCollection(
            final GqlvMemberHolder holder,
            final OneToManyAssociation otma,
            final Context context
    ) {
        return new GqlvCollection(holder, otma, context);
    }
    public GqlvAbstractCustom newGqlvAction(
            final GqlvMemberHolder holder,
            final ObjectAction objectAction,
            final Context context
    ) {
        return new GqlvAction(holder, objectAction, context);
    }

    @Override
    public GqlvAbstractCustom newGqlvMeta(GqlvDomainObject gqlvDomainObject, Context context) {
        return new GqlvMeta(gqlvDomainObject, context);
    }

}

package org.apache.causeway.viewer.graphql.viewer.source;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import graphql.schema.GraphQLOutputType;

import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.viewer.graphql.viewer.util._BiMap;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class GqlvServiceStructure {

    @Getter private final ObjectSpecification serviceSpec;
    @Getter private final GqlvTopLevelQueryStructure topLevelQueryStructure;
    private final SpecificationLoader specificationLoader;

    private GraphQLFieldDefinition topLevelQueryField;


    private String getLogicalTypeName() {
        return serviceSpec.getLogicalTypeName();
    }

    public String getLogicalTypeNameSanitized() {
        return _LTN.sanitized(serviceSpec);
    }

    private final GraphQLObjectType.Builder gqlObjectTypeBuilder;
    private final GraphQLObjectType.Builder queryBuilder;

    private GraphQLObjectType gqlObjectType;

    public GqlvServiceStructure(
            final ObjectSpecification serviceSpec,
            final GqlvTopLevelQueryStructure topLevelQueryStructure,
            final SpecificationLoader specificationLoader
    ) {
        this.serviceSpec = serviceSpec;
        this.topLevelQueryStructure = topLevelQueryStructure;

        this.gqlObjectTypeBuilder = newObject().name(_LTN.sanitized(serviceSpec));

        this.queryBuilder = topLevelQueryStructure.getQueryBuilder();
        this.specificationLoader = specificationLoader;
    }


    private final _BiMap<ObjectAction, GraphQLFieldDefinition> safeActionToField = new _BiMap<>();
    private final _BiMap<ObjectAction, GraphQLFieldDefinition> mutatorActionToField = new _BiMap<>();


    Map<ObjectAction, GraphQLFieldDefinition> getSafeActions() {
        return safeActionToField.getForwardMapAsImmutable();
    }

    Map<ObjectAction, GraphQLFieldDefinition> getMutatorActions() {
        return mutatorActionToField.getForwardMapAsImmutable();
    }


    /**
     * @see #getGqlObjectType()
     */
    public GraphQLObjectType buildObjectGqlType() {
        if (gqlObjectType != null) {
            throw new IllegalArgumentException(String.format("GqlObjectType has already been built for %s", getLogicalTypeName()));
        }
        return gqlObjectType = gqlObjectTypeBuilder.build();
    }

    /**
     * @see #buildObjectGqlType()
     */
    public GraphQLObjectType getGqlObjectType() {
        if (gqlObjectType == null) {
            throw new IllegalStateException(String.format(
                    "GraphQLObjectType has not yet been built for %s", getLogicalTypeName()));
        }
        return gqlObjectType;
    }

    /**
     * @see #getTopLevelQueryField()
     */
    public GraphQLFieldDefinition buildTopLevelQueryField() {
        if (topLevelQueryField != null) {
            throw new IllegalStateException(String.format(
                    "queryField has already been added to top-level Query, for %s", getLogicalTypeName()));
        }
        topLevelQueryField = newFieldDefinition()
                .name(_LTN.sanitized(serviceSpec))
                .type(gqlObjectTypeBuilder)
                .build();
        return topLevelQueryField;
    }

    /**
     * @see #buildTopLevelQueryField()
     */
    public GraphQLFieldDefinition getTopLevelQueryField() {
        if (topLevelQueryField == null) {
            throw new IllegalStateException(String.format(
                    "queryField has not yet been added to top-level Query, for %s", getLogicalTypeName()));
        }
        return topLevelQueryField;
    }


    void addAction(final ObjectAction objectAction) {

        String fieldName = objectAction.getId();

        GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition()
                .name(fieldName)
                .type((GraphQLOutputType) TypeMapper.typeForObjectAction(objectAction));
        if (objectAction.getParameters().isNotEmpty()) {
            fieldBuilder.arguments(objectAction.getParameters().stream()
                    .map(objectActionParameter -> GraphQLArgument.newArgument()
                            .name(objectActionParameter.getId())
                            .type(TypeMapper.inputTypeFor(objectActionParameter))
                            .build())
                    .collect(Collectors.toList()));
        }
        GraphQLFieldDefinition fieldDefinition = fieldBuilder.build();
        gqlObjectTypeBuilder.field(fieldDefinition);

        // TODO: either safe or mutator
        safeActionToField.put(objectAction, fieldDefinition);
    }


}

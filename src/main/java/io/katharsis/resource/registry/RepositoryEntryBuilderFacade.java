package io.katharsis.resource.registry;

import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.repository.NotFoundRepository;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.resource.registry.repository.DirectResourceEntry;
import io.katharsis.resource.registry.repository.ResourceEntry;
import io.katharsis.resource.registry.repository.WithRelationshipEntry;
import org.reflections.Reflections;

import java.util.LinkedList;
import java.util.List;

/**
 * Contains a strategy to decide which implementation of an entry will be provided. Keep in mind that there can be a
 * case in which there will be two repositories of the same types.
 */
public class RepositoryEntryBuilderFacade implements RepositoryEntryBuilder {

    private final DirectRepositoryEntryBuilder directRepositoryEntryBuilder;
    private final AnnotatedRepositoryEntryBuilder annotatedRepositoryEntryBuilder;

    public RepositoryEntryBuilderFacade(JsonServiceLocator jsonServiceLocator) {
        this.directRepositoryEntryBuilder = new DirectRepositoryEntryBuilder(jsonServiceLocator);
        this.annotatedRepositoryEntryBuilder = new AnnotatedRepositoryEntryBuilder(jsonServiceLocator);
    }

    @Override
    public ResourceEntry<?, ?> buildResourceRepository(Reflections reflections, Class<?> resourceClass) {
        ResourceEntry<?, ?> resourceEntry = annotatedRepositoryEntryBuilder
            .buildResourceRepository(reflections, resourceClass);
        if (resourceEntry == null) {
            resourceEntry = directRepositoryEntryBuilder.buildResourceRepository(reflections, resourceClass);
        }
        if (resourceEntry == null) {
            resourceEntry = new DirectResourceEntry<>(new NotFoundRepository<>(resourceClass));
        }

        return resourceEntry;
    }

    @Override
    public List<WithRelationshipEntry<RelationshipRepository, ?, ?>> buildRelationshipRepositories(Reflections reflections, Class<?> resourceClass) {
        List<WithRelationshipEntry<RelationshipRepository, ?, ?>> annotationEntries = annotatedRepositoryEntryBuilder
            .buildRelationshipRepositories(reflections, resourceClass);
        List<WithRelationshipEntry<RelationshipRepository, ?, ?>> targetEntries = new LinkedList<>(annotationEntries);
        List<WithRelationshipEntry<RelationshipRepository, ?, ?>> directEntries = directRepositoryEntryBuilder
            .buildRelationshipRepositories(reflections, resourceClass);

        directEntries.forEach(
            directEntry -> {
                if (!contains(targetEntries, directEntry)) {
                    targetEntries.add(directEntry);
                }
            }
        );

        return targetEntries;
    }

    private boolean contains(List<WithRelationshipEntry<RelationshipRepository, ?, ?>> targetEntries,
                             WithRelationshipEntry<RelationshipRepository, ?, ?> directEntry) {
        boolean contains = false;
        for (WithRelationshipEntry<?, ?, ?> targetEntry : targetEntries) {
            if (targetEntry.getTargetAffiliation().equals(directEntry.getTargetAffiliation())) {
                contains = true;
                break;
            }
        }
        return contains;
    }
}

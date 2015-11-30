package io.katharsis.resource.registry;

import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.repository.FieldRepository;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.exception.RepositoryInstanceNotFoundException;
import io.katharsis.resource.registry.repository.DirectResourceEntry;
import io.katharsis.resource.registry.repository.DirectWithRelationshipEntry;
import io.katharsis.resource.registry.repository.ResourceEntry;
import io.katharsis.resource.registry.repository.WithRelationshipEntry;
import net.jodah.typetools.TypeResolver;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Repository entries builder for classes implementing repository interfaces.
 */
public class DirectRepositoryEntryBuilder implements RepositoryEntryBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectRepositoryEntryBuilder.class);

    private final JsonServiceLocator jsonServiceLocator;

    public DirectRepositoryEntryBuilder(JsonServiceLocator jsonServiceLocator) {
        this.jsonServiceLocator = jsonServiceLocator;
    }

    @Override
    public ResourceEntry<?, ?> buildResourceRepository(Reflections reflections, Class<?> resourceClass) {
        Optional<Class<? extends ResourceRepository>> repoClass = reflections.getSubTypesOf(ResourceRepository.class)
            .stream()
            .filter(clazz -> {
                Class<?>[] typeArgs = TypeResolver.resolveRawArguments(ResourceRepository.class, clazz);
                return typeArgs[0] == resourceClass;
            })
            .findFirst();
        if (!repoClass.isPresent()) {
            return null;
        }
        ResourceRepository<?, ?> repoInstance = jsonServiceLocator.getInstance(repoClass.get());
        if (repoInstance == null) {
            throw new RepositoryInstanceNotFoundException(repoClass.get().getCanonicalName());
        }
        return new DirectResourceEntry<>(repoInstance);
    }

    @Override
    public List<WithRelationshipEntry<RelationshipRepository, ?, ?>> buildRelationshipRepositories(Reflections reflections, Class<?> resourceClass) {
        Set<Class<? extends RelationshipRepository>> relationshipRepositoryClasses = reflections
            .getSubTypesOf(RelationshipRepository.class);

        Set<Class<? extends RelationshipRepository>> relationshipRepositories =
            findRelationshipRepositories(resourceClass, relationshipRepositoryClasses);

        List<WithRelationshipEntry<RelationshipRepository, ?, ?>> relationshipEntries = new LinkedList<>();
        for (Class<? extends RelationshipRepository> relationshipRepositoryClass : relationshipRepositories) {
            RelationshipRepository relationshipRepository = jsonServiceLocator.getInstance(relationshipRepositoryClass);
            if (relationshipRepository == null) {
                throw new RepositoryInstanceNotFoundException(relationshipRepositoryClass.getCanonicalName());
            }

            LOGGER.debug("Assigned {} RelationshipRepository  to {} resource class",
                relationshipRepositoryClass.getCanonicalName(), resourceClass.getCanonicalName());

            relationshipEntries.add(new DirectWithRelationshipEntry<>(relationshipRepository, RelationshipRepository.class));
        }
        return relationshipEntries;
    }

    private Set<Class<? extends RelationshipRepository>> findRelationshipRepositories(Class resourceClass,
                                                                                      Set<Class<? extends RelationshipRepository>> relationshipRepositoryClasses) {
        Set<Class<? extends RelationshipRepository>> foundRelationshipRepositories = new LinkedHashSet<>(2);
        for (Class<? extends RelationshipRepository> relationshipRepository : relationshipRepositoryClasses) {
            Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepository.class, relationshipRepository);
            if (typeArgs[0] == resourceClass) {
                foundRelationshipRepositories.add(relationshipRepository);
            }
        }
        return foundRelationshipRepositories;
    }

    @Override
    public List<WithRelationshipEntry<FieldRepository, ?, ?>> buildFieldRepositories(Reflections reflections, Class<?> resourceClass) {
        Set<Class<? extends FieldRepository>> fieldRepositoryClasses = reflections
            .getSubTypesOf(FieldRepository.class);

        Set<Class<? extends FieldRepository>> fieldRepositories =
            findFieldRepositories(resourceClass, fieldRepositoryClasses);

        List<WithRelationshipEntry<FieldRepository, ?, ?>> relationshipEntries = new LinkedList<>();
        for (Class<? extends FieldRepository> relationshipRepositoryClass : fieldRepositories) {
            FieldRepository relationshipRepository = jsonServiceLocator.getInstance(relationshipRepositoryClass);
            if (relationshipRepository == null) {
                throw new RepositoryInstanceNotFoundException(relationshipRepositoryClass.getCanonicalName());
            }

            LOGGER.debug("Assigned {} RelationshipRepository  to {} resource class",
                relationshipRepositoryClass.getCanonicalName(), resourceClass.getCanonicalName());

            relationshipEntries.add(new DirectWithRelationshipEntry<>(relationshipRepository, FieldRepository.class));
        }
        return relationshipEntries;
    }

    private Set<Class<? extends FieldRepository>> findFieldRepositories(Class resourceClass,
                                                                        Set<Class<? extends FieldRepository>> relationshipRepositoryClasses) {
        Set<Class<? extends FieldRepository>> foundFieldRepositories = new LinkedHashSet<>(2);
        for (Class<? extends FieldRepository> fieldRepository : relationshipRepositoryClasses) {
            Class<?>[] typeArgs = TypeResolver.resolveRawArguments(FieldRepository.class, fieldRepository);
            if (typeArgs[0] == resourceClass) {
                foundFieldRepositories.add(fieldRepository);
            }
        }
        return foundFieldRepositories;
    }
}

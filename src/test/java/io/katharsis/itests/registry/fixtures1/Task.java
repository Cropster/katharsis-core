package io.katharsis.itests.registry.fixtures1;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;
import lombok.Data;

@Data
@JsonApiResource(type = "tasks")
public class Task {

    @JsonApiId
    Integer id;

}
package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.request.Request;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.dto.ResourceRelationships;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.User;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.resource.mock.repository.UserToProjectRepository;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.HttpStatus;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Collections;

import static io.katharsis.request.path.JsonApiPath.parsePathFromStringUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class RelationshipsResourceDeleteTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = HttpMethod.DELETE.name();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final QueryParams REQUEST_PARAMS = new QueryParams();

    @Test
    public void onValidRequestShouldAcceptIt() {
        // GIVEN
        Request request = new Request(parsePathFromStringUrl("http://domain.local/tasks/1/relationships/project"), REQUEST_TYPE, null, parameterProvider);

        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        RelationshipsResourceDelete sut = new RelationshipsResourceDelete(resourceRegistry, typeParser, queryParamsBuilder);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    public void onNonRelationRequestShouldDenyIt() {
        // GIVEN
        Request request = new Request(parsePathFromStringUrl("http://domain.local/tasks"), REQUEST_TYPE, null, parameterProvider);

        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        RelationshipsResourceDelete sut = new RelationshipsResourceDelete(resourceRegistry, typeParser, queryParamsBuilder);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onExistingToOneRelationshipShouldRemoveIt() throws Exception {
        // GIVEN

        RequestBody newTaskBody = new RequestBody();
        DataBody data = new DataBody();
        newTaskBody.setData(data);
        data.setType("tasks");
        data.setAttributes(OBJECT_MAPPER.createObjectNode().put("name", "sample task"));
        data.setRelationships(new ResourceRelationships());

        byte[] body = new byte[]{};
        JsonApiPath path = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks");
        Request request = new Request(path, REQUEST_TYPE, new ByteArrayInputStream(body), parameterProvider);

        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, OBJECT_MAPPER, queryParamsBuilder);

        // WHEN -- adding a task
        BaseResponseContext taskResponse = resourcePost.handle(request);

        // THEN
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (taskResponse.getResponse().getEntity())).getId();
        assertThat(taskId).isNotNull();

        /* ------- */

        // GIVEN
        RequestBody newProjectBody = new RequestBody();
        data = new DataBody();
        newProjectBody.setData(data);
        data.setType("projects");
        data.setAttributes(OBJECT_MAPPER.createObjectNode().put("name", "sample project"));


        // WHEN -- adding a project
        BaseResponseContext projectResponse = resourcePost.handle(request);

        // THEN
        assertThat(projectResponse.getResponse().getEntity()).isExactlyInstanceOf(Project.class);
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getId()).isNotNull();
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getName()).isEqualTo("sample project");
        Long projectId = ((Project) (projectResponse.getResponse().getEntity())).getId();
        assertThat(projectId).isNotNull();

        /* ------- */

        // GIVEN
        RequestBody newTaskToProjectBody = new RequestBody();
        data = new DataBody();
        newTaskToProjectBody.setData(data);
        data.setType("projects");
        data.setId(projectId.toString());

        JsonApiPath savedTaskPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/" + taskId + "/relationships/project");
        request = new Request(savedTaskPath, REQUEST_TYPE, new ByteArrayInputStream(body), parameterProvider);
        RelationshipsResourcePost relationshipsResourcePost =
                new RelationshipsResourcePost(resourceRegistry, typeParser, queryParamsBuilder);

        // WHEN -- adding a relation between task and project
        BaseResponseContext projectRelationshipResponse =
                relationshipsResourcePost.handle(request);
        assertThat(projectRelationshipResponse).isNotNull();

        // THEN
        TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
        Project project = taskToProjectRepository.findOneTarget(taskId, "project", REQUEST_PARAMS);
        assertThat(project.getId()).isEqualTo(projectId);

        /* ------- */

        // GIVEN
        RelationshipsResourceDelete sut = new RelationshipsResourceDelete(resourceRegistry, typeParser, queryParamsBuilder);

        // WHEN -- removing a relation between task and project
        BaseResponseContext result = sut.handle(request);
        assertThat(result).isNotNull();

        // THEN
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
        Project nullProject = taskToProjectRepository.findOneTarget(taskId, "project", REQUEST_PARAMS);
        assertThat(nullProject).isNull();
    }

    @Test
    public void onExistingToManyRelationshipShouldRemoveIt() throws Exception {
        // GIVEN
        RequestBody newUserBody = new RequestBody();
        DataBody data = new DataBody();
        newUserBody.setData(data);
        data.setType("users");
        data.setAttributes(OBJECT_MAPPER.createObjectNode().put("name", "sample task"));
        data.setRelationships(new ResourceRelationships());

        JsonApiPath taskPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/users");
        Request request = new Request(taskPath, REQUEST_TYPE, null, parameterProvider);
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, OBJECT_MAPPER, queryParamsBuilder);

        // WHEN -- adding a user
        BaseResponseContext taskResponse = resourcePost.handle(request);

        // THEN
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(User.class);
        Long userId = ((User) (taskResponse.getResponse().getEntity())).getId();
        assertThat(userId).isNotNull();

        /* ------- */

        // GIVEN
        RequestBody newProjectBody = new RequestBody();
        data = new DataBody();
        newProjectBody.setData(data);
        data.setType("projects");
        data.setAttributes(OBJECT_MAPPER.createObjectNode().put("name", "sample project"));

        JsonApiPath projectPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/projects");
        request = new Request(projectPath, REQUEST_TYPE, null, parameterProvider);
        // WHEN -- adding a project
        BaseResponseContext projectResponse = resourcePost.handle(request);

        // THEN
        assertThat(projectResponse.getResponse().getEntity()).isExactlyInstanceOf(Project.class);
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getId()).isNotNull();
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getName()).isEqualTo("sample project");
        Long projectId = ((Project) (projectResponse.getResponse().getEntity())).getId();
        assertThat(projectId).isNotNull();

        /* ------- */

        // GIVEN
        RequestBody newTaskToProjectBody = new RequestBody();
        data = new DataBody();
        newTaskToProjectBody.setData(Collections.singletonList(data));
        data.setType("projects");
        data.setId(projectId.toString());

        JsonApiPath savedTaskPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/users/" + userId + "/relationships/assignedProjects");
        request = new Request(savedTaskPath, REQUEST_TYPE, null, parameterProvider);
        RelationshipsResourcePost relationshipsResourcePost =
                new RelationshipsResourcePost(resourceRegistry, typeParser, queryParamsBuilder);

        // WHEN -- adding a relation between user and project
        BaseResponseContext projectRelationshipResponse =
                relationshipsResourcePost.handle(request);
        assertThat(projectRelationshipResponse).isNotNull();

        // THEN
        UserToProjectRepository userToProjectRepository = new UserToProjectRepository();
        Project project = userToProjectRepository.findOneTarget(userId, "assignedProjects", REQUEST_PARAMS);
        assertThat(project.getId()).isEqualTo(projectId);

        /* ------- */

        // GIVEN
        RelationshipsResourceDelete sut = new RelationshipsResourceDelete(resourceRegistry, typeParser, queryParamsBuilder);

        // WHEN -- removing a relation between task and project
        BaseResponseContext result = sut.handle(request);
        assertThat(result).isNotNull();

        // THEN
        Project nullProject = userToProjectRepository.findOneTarget(userId, "assignedProjects", REQUEST_PARAMS);
        assertThat(nullProject).isNull();
    }
}

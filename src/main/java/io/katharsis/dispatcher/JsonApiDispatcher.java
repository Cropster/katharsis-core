package io.katharsis.dispatcher;


import io.katharsis.errorhandling.exception.KatharsisMatchingException;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;
import io.katharsis.request.Request;
import io.katharsis.utils.java.Optional;
import lombok.Data;
import lombok.NonNull;

/**
 * A class that can be used to integrate Katharsis with external frameworks like Jersey, Spring etc. See katharsis-rs
 * and katharsis-servlet for usage.
 */
@Data
public class JsonApiDispatcher {

    private ExceptionMapperRegistry exceptionMapperRegistry;

    private JsonApiGet apiGet;
    private JsonApiPost apiPost;
    private JsonApiPatch apiPatch;
    private JsonApiDelete apiDelete;

    /**
     * Dispatch the request from a client
     *
     * @param request - the request we need to process
     * @return the response form the Katharsis
     */
    public ResponseContext dispatchRequest(Request request) {
        ResponseContext response = null;
        try {
            /**
             * Extract informations from the request. Based on those we can route the request.
             *
             * Filter first by HTTP method.
             * After that, we can need to determine if we are in one of the situations:
             * - collection - when we have no ID on the path
             * - multiple elements - many ID's
             * - individual element - we have one ID
             * - relationship - we have ID and a relationship
             * - field - we have ID and a field name
             *
             * No extra processing needs to be done - body parsing, etc.
             */

            switch (request.getMethod()) {
                case GET:
                    response = apiGet.handle(request);
                    break;
                case POST:
                    response = apiPost.handle(request);
                    break;
                case PATCH:
                    response = apiPatch.handle(request);
                    break;
                case DELETE:
                    response = apiDelete.handle(request);
                    break;
                default:
                    throw new MethodNotFoundException(request);
            }

        } catch (KatharsisMatchingException ke) {
            response.setHttpStatus(406);
        } catch (Exception e) {
            response = toErrorResponse(e, 422);
        } finally {
            return response;
        }
    }

    public ResponseContext toErrorResponse(@NonNull Throwable e, int statusCode) {
        Optional<JsonApiExceptionMapper> exceptionMapper = exceptionMapperRegistry.findMapperFor(e.getClass());
        ResponseContext errorResponse = null;

        if (exceptionMapper.isPresent()) {
//            errorResponse = exceptionMapper.get().toErrorResponse(e);
        } else {
//            errorResponse = ErrorResponse.builder()
//                    .setStatus(statusCode)
//                    .setSingleErrorData(ErrorData.builder()
//                            .setDetail(e.getMessage() + e)
//                            .build())
//                    .build();
        }

        return errorResponse;
    }


}

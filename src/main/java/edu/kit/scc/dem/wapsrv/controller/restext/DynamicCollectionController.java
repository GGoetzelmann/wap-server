package edu.kit.scc.dem.wapsrv.controller.restext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.controller.PageConstants;
import edu.kit.scc.dem.wapsrv.exceptions.WapException;
import edu.kit.scc.dem.wapsrv.model.FormattableObject;
import edu.kit.scc.dem.wapsrv.model.Page;
import edu.kit.scc.dem.wapsrv.model.formats.ContentNegotiator;
import edu.kit.scc.dem.wapsrv.model.formats.FormatRegistry;
import edu.kit.scc.dem.wapsrv.model.formats.Formatter;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdProfileRegistry;
import edu.kit.scc.dem.wapsrv.service.restext.QueryCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Extension of WAP Server with additional request options not directly aligned with the Web Annotation Protocol
 * The extension allows for implementation of dynamic collections instead of static containers, allowing for retrieval of annotations based on common values
 */
@RestController
@RequestMapping("/restext/")
public class DynamicCollectionController {

    @Autowired
    private QueryCollectionService collectionService;

    @Autowired
    JsonLdProfileRegistry profileRegistry;
    /**
     * The format registry
     */
    @Autowired
    FormatRegistry formatRegistry;

    /**
     * Controller for dynamic collection request. Parameters align with selected WADM properties
     * @param targetParam parameter for target/@id or target/source
     * @param selectorParam paramter for selector/value
     * @param creatorParam parameter for creator/@id or creator/name
     * @param bodyParam parameter for body/@id or body/value
     * @param headers request headers
     * @return annotation page with matching results on success. Returns error if no parameter is provided
     * @throws WapException
     */
    @RequestMapping(
            value = "/collection",
            //params = {"target", "selector", "creator", "body"},
            method = RequestMethod.GET)
    public ResponseEntity<?> getCollectionByParams(@RequestParam(value = "target", required = false) String targetParam,
                                                   @RequestParam(value = "selector", required = false) String selectorParam,
                                                   @RequestParam(value = "creator", required = false) String creatorParam,
                                                   @RequestParam(value = "body", required = false) String bodyParam,
                                                   @RequestParam(value = "targetContains", required = false) String targetContainsParam,
                                                   @RequestParam(value = "selectorContains", required = false) String selectorContainsParam,
                                                   @RequestHeader HttpHeaders headers)
            throws WapException {
        Map<String, Pair<String, QueryCollectionService.MatchType>> propMap = new HashMap<>();

        if ((targetParam != null && targetContainsParam != null) || (selectorParam != null && selectorContainsParam != null)) {
            return ResponseEntity.badRequest().body("exact match parameter and contains parameter of same type are not allowed together");
        }

        if(targetParam != null || targetContainsParam != null) {
            String target = targetParam != null
                   ? URLDecoder.decode(targetParam, StandardCharsets.UTF_8)
                   : URLDecoder.decode(targetContainsParam, StandardCharsets.UTF_8);
            QueryCollectionService.MatchType matchType = targetParam != null
                   ? QueryCollectionService.MatchType.EXACT
                   : QueryCollectionService.MatchType.CONTAINS;
            propMap.put("target", Pair.of(target, matchType));
        }
        if (selectorParam != null || selectorContainsParam != null) {
            String selector = selectorParam != null
                    ? URLDecoder.decode(selectorParam, StandardCharsets.UTF_8)
                    : URLDecoder.decode(selectorContainsParam, StandardCharsets.UTF_8);
            QueryCollectionService.MatchType matchType = selectorParam != null
                    ? QueryCollectionService.MatchType.EXACT
                    : QueryCollectionService.MatchType.CONTAINS;
            propMap.put("selector", Pair.of(selector, matchType));
        }
        if (creatorParam != null) {
            String creator = URLDecoder.decode(creatorParam, StandardCharsets.UTF_8);
            propMap.put("creator", Pair.of(creator, QueryCollectionService.MatchType.EXACT));
        }
        if (bodyParam != null) {
            String body = URLDecoder.decode(bodyParam, StandardCharsets.UTF_8);
            propMap.put("body", Pair.of(body, QueryCollectionService.MatchType.EXACT));
        }

        if (!propMap.isEmpty()) {
            return getAnnoCollectionByPropMap(propMap, headers);
        }

        //if we have not returned by this point, we were not able to process any request parameters. Return error
        Method method;
        try {
            method = this.getClass().getMethod("getCollectionByParams", String.class, String.class, String.class, String.class, String.class, String.class, HttpHeaders.class);
        } catch (NoSuchMethodException e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }

        List<String> parameterNames = Arrays.stream(method.getParameters())
                .map(param -> {
                    RequestParam requestParam = param.getAnnotation(RequestParam.class);
                    return requestParam != null ? requestParam.value() : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        String allowedParams = String.join(", ", parameterNames);
        return ResponseEntity.badRequest().body("At least one parameter must be provided to create a collection. Allowed parameters are: " + allowedParams);

    }

    /*
    @RequestMapping(
            value = "/creator/{creator:.+}",
            method = RequestMethod.GET)
    public ResponseEntity<?> getCollectionByCreator(@PathVariable("creator") String encodedCreatorString, @RequestHeader HttpHeaders headers)
            throws WapException {
        try {
            Map<String, String> propMap = new HashMap<>();
            String target = URLDecoder.decode(encodedCreatorString, "UTF-8");
            propMap.put("creator", target);
            return getAnnoCollectionByPropMap(propMap, headers);
        } catch (UnsupportedEncodingException e) {
            return null; //TODO: return useful error
        }
    }
     */

    private ResponseEntity<?> getAnnoCollectionByPropMap(Map<String, Pair<String, QueryCollectionService.MatchType>> propMap, HttpHeaders headers) {

        Page page = collectionService.getPage(propMap);

        final ContentNegotiator contentNegotiator
                = getContentNegotiator(headers.get("Accept").get(0), FormattableObject.Type.PAGE, profileRegistry, formatRegistry);
        final Formatter formatter = contentNegotiator.getFormatter();
        String responseBody = formatter.format(page);


        //If request used a target filter, the returned page is conform to IIIF annotation page specification. Therefore the necessary context is added to be used this way
        //TODO: this is only a simple implementation and should be handled more thoroughly
        if (propMap.containsKey("target")) {
            // read the json strings and convert it into JsonNode
            try {
                responseBody = addIIIFcontext(responseBody);
            } catch (JsonProcessingException e) {
                return new ResponseEntity<>("Error on adding IIIF context to response\n" + responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        // Attention : spring(or jetty) does not add the body to head requests automatically
        // but they need it to calculate corrects length. Therefore add it (and ignore in tests)
        final HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setAllow(PageConstants.ALLOWED_METHODS);
        responseHeaders.setVary(PageConstants.VARY_LIST);
        return new ResponseEntity<>(responseBody, responseHeaders,
                HttpStatus.valueOf(PageConstants.GET_PAGE_SUCCESS_CODE));
    }

    /*
    util function / workaround to add IIIF v3 context for applicable requests
     */
    private String addIIIFcontext(String responseBody) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(responseBody);
        // Get the @context node
        JsonNode contextNode = node.get("@context");

        // New context to add
        String newContext = "http://iiif.io/api/presentation/3/context.json";

        // Check if @context is a string or an array
        if (contextNode.isTextual()) {
            // Case 1: @context is a string, convert to array with existing and new context
            ArrayNode newContextArray = mapper.createArrayNode();
            newContextArray.add(contextNode.textValue());
            newContextArray.add(newContext);
            ((ObjectNode) node).set("@context", newContextArray);
        } else if (contextNode.isArray()) {
            // Case 2: @context is an array, add new context to array
            ((ArrayNode) contextNode).add(newContext);
        }

        // Convert the JsonNode back to a JSON-LD string
        return mapper.writeValueAsString(node);
    }

    protected ContentNegotiator getContentNegotiator(String acceptHeader, FormattableObject.Type type,
                                                     JsonLdProfileRegistry profileRegistry, FormatRegistry formatRegistry) {
        if (WapServerConfig.getInstance().isContentNegotiationEnabled()) {
            return new ContentNegotiator(acceptHeader, type, profileRegistry, formatRegistry);
        } else {
            return new ContentNegotiator(null, type, profileRegistry, formatRegistry);
        }
    }
}

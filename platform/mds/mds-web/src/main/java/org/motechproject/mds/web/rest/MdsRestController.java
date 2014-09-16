package org.motechproject.mds.web.rest;

import org.motechproject.mds.ex.rest.RestBadBodyFormatException;
import org.motechproject.mds.ex.rest.RestLookupExecutionForbbidenException;
import org.motechproject.mds.ex.rest.RestLookupNotFoundException;
import org.motechproject.mds.ex.rest.RestNotSupportedException;
import org.motechproject.mds.ex.rest.RestOperationNotSupportedException;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.rest.MdsRestFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * The main entry point for the MDS Rest api. It delegates requests
 * to published {@link org.motechproject.mds.rest.MdsRestFacade} objects.
 */
@Controller
@RequestMapping("/rest")
public class MdsRestController  {

    private static final Logger LOG = LoggerFactory.getLogger(MdsRestController.class);

    @Autowired
    private MdsRestFacadeRetriever restFacadeRetriever;

    @RequestMapping(value = "/{moduleName}/{namespace}/{entityName}", method = RequestMethod.GET)
    @ResponseBody
    public Object get(@PathVariable String moduleName, @PathVariable String namespace,
                    @PathVariable String entityName, @RequestParam Map<String, String> requestParams) {
        return doGet(entityName, moduleName, namespace, requestParams);
    }

    @RequestMapping(value = "/{moduleName}/{entityName}", method = RequestMethod.GET)
    @ResponseBody
    public Object get(@PathVariable String moduleName, @PathVariable String entityName,
                    @RequestParam Map<String, String> requestParams) {
        return doGet(entityName, moduleName, null, requestParams);
    }

    @RequestMapping(value = "/{entityName}", method = RequestMethod.GET)
    @ResponseBody
    public Object get(@PathVariable String entityName, @RequestParam Map<String, String> requestParams) {
        return doGet(entityName, null, null, requestParams);
    }

    private Object doGet(String entityName, String moduleName, String namespace,
                       Map<String, String> requestParams) {
        debugRequest("GET", entityName, moduleName, namespace);

        MdsRestFacade restFacade = restFacadeRetriever.getRestFacade(entityName, moduleName, namespace);

        QueryParams queryParams = ParamParser.buildQueryParams(requestParams);
        String lookupName = ParamParser.getLookupName(requestParams);
        Long id = ParamParser.getId(requestParams);

        if (lookupName != null) {
            // lookup
            return restFacade.executeLookup(lookupName, requestParams, queryParams);
        } else if (id != null) {
            // retrieve by id
            return restFacade.get(id);
        } else {
            // get records
            return restFacade.get(queryParams);
        }
    }

    @RequestMapping(value = "/{moduleName}/{namespace}/{entityName}", method = RequestMethod.POST)
    @ResponseBody
    public Object post(@PathVariable String moduleName, @PathVariable String namespace,
                    @PathVariable String entityName, HttpServletRequest request) {
        return doPost(entityName, moduleName, namespace, request);
    }

    @RequestMapping(value = "/{moduleName}/{entityName}", method = RequestMethod.POST)
    @ResponseBody
    public Object post(@PathVariable String moduleName, @PathVariable String entityName,
                     HttpServletRequest request) {
        return doPost(entityName, moduleName, null, request);
    }

    @RequestMapping(value = "/{entityName}", method = RequestMethod.POST)
    @ResponseBody
    public Object post(@PathVariable String entityName, HttpServletRequest request) {
        return doPost(entityName, null, null, request);
    }

    private Object doPost(String entityName, String moduleName, String namespace,
                        HttpServletRequest request) {
        debugRequest("POST", entityName, moduleName, namespace);

        MdsRestFacade restFacade = restFacadeRetriever.getRestFacade(entityName, moduleName, namespace);
        try (InputStream bodyInStream = request.getInputStream()) {
            return restFacade.create(bodyInStream);
        } catch (IOException e) {
            throw new RestBadBodyFormatException("Unable to read request body", e);
        }
    }

    @RequestMapping(value = "/{moduleName}/{namespace}/{entityName}", method = RequestMethod.PUT)
    @ResponseBody
    public Object put(@PathVariable String moduleName, @PathVariable String namespace,
                     @PathVariable String entityName, HttpServletRequest request) {
        return doPut(entityName, moduleName, namespace, request);
    }

    @RequestMapping(value = "/{moduleName}/{entityName}", method = RequestMethod.PUT)
    @ResponseBody
    public Object put(@PathVariable String moduleName, @PathVariable String entityName,
                     HttpServletRequest request) {
        return doPut(entityName, moduleName, null, request);
    }

    @RequestMapping(value = "/{entityName}", method = RequestMethod.PUT)
    @ResponseBody
    public Object put(@PathVariable String entityName, HttpServletRequest request) {
        return doPut(entityName, null, null, request);
    }

    private Object doPut(String entityName, String moduleName, String namespace,
                        HttpServletRequest request) {
        debugRequest("PUT", entityName, moduleName, namespace);

        MdsRestFacade restFacade = restFacadeRetriever.getRestFacade(entityName, moduleName, namespace);
        try (InputStream bodyInStream = request.getInputStream()) {
            return restFacade.update(bodyInStream);
        } catch (IOException e) {
            throw new RestBadBodyFormatException("Unable to read request body", e);
        }
    }

    @RequestMapping(value = "/{moduleName}/{namespace}/{entityName}/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable String moduleName, @PathVariable String namespace,
                    @PathVariable String entityName, @PathVariable Long id) {
        doDelete(entityName, moduleName, namespace, id);
    }

    @RequestMapping(value = "/{moduleName}/{entityName}/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable String moduleName, @PathVariable String entityName,
                    @PathVariable Long id) {
        doDelete(entityName, moduleName, null, id);
    }

    @RequestMapping(value = "/{entityName}/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable String entityName, @PathVariable Long id) {
        doDelete(entityName, null, null, id);
    }

    private void doDelete(String entityName, String moduleName, String namespace, Long id) {
        debugRequest("DELETE", entityName, moduleName, namespace);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Delete request for id {}", id);
        }

        MdsRestFacade restFacade = restFacadeRetriever.getRestFacade(entityName, moduleName, namespace);
        restFacade.delete(id);
    }

    private void debugRequest(String requestType, String entityName, String moduleName, String namespace) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Received {} request for: entity={} module={} namespace={}",
                    new Object[] { requestType, entityName, moduleName, namespace });
        }
    }

    @ExceptionHandler({RestNotSupportedException.class, RestLookupNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleRestNotSupportedException() {
    }

    @ExceptionHandler({RestOperationNotSupportedException.class, RestLookupExecutionForbbidenException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void handleRestOperationNotSupportedException() {
    }
}
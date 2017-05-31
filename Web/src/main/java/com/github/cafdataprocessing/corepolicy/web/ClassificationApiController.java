/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cafdataprocessing.corepolicy.web;

import com.github.cafdataprocessing.corepolicy.common.dto.DtoBase;
import com.github.cafdataprocessing.corepolicy.common.dto.PageOfResults;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.DeleteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
@RequestMapping(value = "/classification")
@RestController
public class ClassificationApiController extends BaseErrorHandlingController {

    private static final Logger logger = LoggerFactory.getLogger(ClassificationApiController.class);
    private static DtoBaseExtractor dtoBaseExtractor = new DtoBaseExtractor();
    private static UpdateRequestExtractor updateRequestExtractor = new UpdateRequestExtractor();
    private static RetrieveRequestExtractor retrieveRequestExtractor = new RetrieveRequestExtractor();
    private static DeleteRequestExtractor deleteRequestExtractor = new DeleteRequestExtractor();

    private final ControllerApi controllerApi;

    @Autowired
    public ClassificationApiController(ControllerApi controllerApi) {
        this.controllerApi = controllerApi;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/create")
    public DtoBase create(HttpServletRequest request) {
        logRequest(logger, request);
        return controllerApi.create(dtoBaseExtractor.extract(request));
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/update")
    public DtoBase update(HttpServletRequest request){
        logRequest(logger, request);
        return controllerApi.update(updateRequestExtractor.extract(request));
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/retrieve")
    public PageOfResults retrieve(HttpServletRequest request){
        logRequest(logger, request);
        return controllerApi.retrieve(retrieveRequestExtractor.extract(request));
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/delete")
    public DeleteResponse delete(HttpServletRequest request){
        logRequest(logger, request);
        return controllerApi.delete(deleteRequestExtractor.extract(request));
    }
}

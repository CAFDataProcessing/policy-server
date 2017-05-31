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

import com.github.cafdataprocessing.corepolicy.common.exceptions.ConditionEngineException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.InvalidFieldValueCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.MissingRequiredParameterCpeException;
import com.github.cafdataprocessing.corepolicy.common.logging.LogHelper;
import com.github.cafdataprocessing.corepolicy.common.shared.ErrorCodes;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;


/**
 * When used as a base class, provides catch all exception handling to ensure that the exception is returned as json.
 * There seems to be a problem using @ControllerAdvice and spring boot, where the error handler is not called for
 * errors, so having per-controller error handling is the only way to ensure errors are handled like we want.
 */
public abstract class BaseErrorHandlingController {

    final static Logger baseLogger = LoggerFactory.getLogger(BaseErrorHandlingController.class);




    /**
     * Util method to log the whole http request as a debug log message, for you know, debugging. Only logs get params
     * (i.e. the url + query string), because logging post params could be horrific in size.
     * TODO: this should really be handled with an interceptor (automatically for all requests)
     * @param log       The Logger to use to log the request. This is needed so we don't use the base class logger.
     * @param request   The request to log.
     */
    protected static void logRequest(Logger log, HttpServletRequest request){
        log.info("REQUEST " + DateTime.now().toString("YYYY/MM/dd HH:mm:ss") + " " + buildUrlLogStringFromRequest(request));
    }




    private static String buildUrlLogStringFromRequest(HttpServletRequest request) {

        //Note it is not possible to log the contents of the POST request ourselves here, the methods on
        // HttpServletRequest to obtain this can be used ONLY ONCE IN THE ENTIRE LIFETIME of the request, including
        //when params are parsed to the method arguments - in short by the time the request is here, we got nothing!

        return LogHelper.removeWhiteSpace( //Remove new line characters
                request.getScheme() + "://" + //http scheme
                        request.getServerName() + //server name
                        ("http".equals(request.getScheme()) && request.getServerPort() == 80 || "https".equals(request.getScheme()) && request.getServerPort() == 443 ? "" : ":" + request.getServerPort()) + //port
                        request.getRequestURI() + //request path
                        (request.getQueryString() != null ? "?" + request.getQueryString() : "")//query string
        );
    }




    @ExceptionHandler(ConditionEngineException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse resourceNotFoundErrorHandler(HttpServletRequest request, ConditionEngineException e) throws Exception {
        baseLogger.error("ERROR ON REQUEST: " + buildUrlLogStringFromRequest(request) + System.lineSeparator() + e.getMessage(), e);
        return new ErrorResponse(e.getErrorCode(), e.getLocalizedMessage());
    }




    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse defaultErrorHandler(HttpServletRequest request, Exception e) throws Exception {
        // If the exception is annotated with @ResponseStatus rethrow it and let
        // the framework handle it.
        //we ar not using ResponseStatus annotations for exceptions, we want to return json for everything and letting
        //the exceptions propagate causes a redirect to /error by default.
//        if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null)
//            throw e;
        baseLogger.error("ERROR ON REQUEST: " + buildUrlLogStringFromRequest(request) + System.lineSeparator() + e.getMessage(), e);

        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        CpeException cpeException = getCpeException(e);
        if(cpeException != null) {
            return new ErrorResponse(cpeException.getErrorCode(), cpeException.getLocalizedMessage(), cpeException.getError().getMessage(Locale.ENGLISH), cpeException.getCorrelationCode());
        }

        return new ErrorResponse(ErrorCodes.GENERIC_ERROR, e.getLocalizedMessage());
    }

    @ExceptionHandler(MissingRequiredParameterCpeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse defaultErrorHandler(HttpServletRequest request, MissingRequiredParameterCpeException cpeException) throws Exception {
        return createBadRequestResponse(request, cpeException);
    }

    @ExceptionHandler(InvalidFieldValueCpeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse defaultErrorHandler(HttpServletRequest request, InvalidFieldValueCpeException cpeException) throws Exception {
        return createBadRequestResponse(request, cpeException);
    }

    private CpeException getCpeException(Throwable exception) {
        if(exception == null){
            return null;
        }
        if(exception instanceof CpeException) {
            return (CpeException)exception;
        }
        return getCpeException(exception.getCause());
    }

    private static ErrorResponse createBadRequestResponse(HttpServletRequest request, CpeException cpeException){
        baseLogger.error("ERROR ON REQUEST: " + buildUrlLogStringFromRequest(request) + System.lineSeparator() + cpeException.getMessage(), cpeException);
        return new ErrorResponse(cpeException.getErrorCode(), cpeException.getLocalizedMessage(), cpeException.getError().getMessage(Locale.ENGLISH), cpeException.getCorrelationCode());
    }

}

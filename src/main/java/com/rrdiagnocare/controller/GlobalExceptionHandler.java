package com.rrdiagnocare.controller;

import com.rrdiagnocare.exception.BusinessException;
import com.rrdiagnocare.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/** Routes unhandled controller exceptions to a friendly error.jsp with the right HTTP status. */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({ResourceNotFoundException.class, NoHandlerFoundException.class})
    public ModelAndView notFound(Exception ex, HttpServletResponse response) {
        String msg = ex instanceof ResourceNotFoundException ? ex.getMessage() : "The page you are looking for does not exist.";
        return error(response, HttpServletResponse.SC_NOT_FOUND, "Page not found", msg, null);
    }

    @ExceptionHandler(BusinessException.class)
    public ModelAndView business(BusinessException ex, HttpServletResponse response) {
        return error(response, HttpServletResponse.SC_BAD_REQUEST, "Request could not be completed", ex.getMessage(), null);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ModelAndView badRequest(Exception ex, HttpServletResponse response) {
        return error(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request",
                "Some information in the request was missing or invalid.", null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ModelAndView methodNotAllowed(Exception ex, HttpServletResponse response) {
        return error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Not allowed",
                "This action is not available this way. Please use the buttons in the application.", null);
    }

    @ExceptionHandler(DataAccessException.class)
    public ModelAndView database(DataAccessException ex, HttpServletRequest request, HttpServletResponse response) {
        String ref = reference();
        log.error("Database error [{}] on {}: {}", ref, request.getRequestURI(), ex.getMessage(), ex);
        return error(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Database unavailable",
                "We could not reach the RR Diagno Care database. Please make sure MySQL is running and try again.", ref);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView unexpected(Exception ex, HttpServletRequest request, HttpServletResponse response) {
        String ref = reference();
        log.error("Unhandled error [{}] on {}", ref, request.getRequestURI(), ex);
        return error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Something went wrong",
                "An unexpected error occurred. Please try again or contact the centre.", ref);
    }

    private static ModelAndView error(HttpServletResponse response, int status, String title, String message, String ref) {
        response.setStatus(status);
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("statusCode", status);
        mv.addObject("errorTitle", title);
        mv.addObject("errorMessage", message);
        mv.addObject("errorRef", ref);
        return mv;
    }

    private static String reference() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

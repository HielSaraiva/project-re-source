package edu.br.resource.resourcesystem.controller.error;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class ResourceErrorController implements ErrorController {
    private final ErrorResponses errors;

    @RequestMapping("/error")
    public Object error(HttpServletRequest request, HttpServletResponse response) {
        Object value = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = value instanceof Integer code ? code : 404;
        return errors.respond(status, request, response);
    }
}

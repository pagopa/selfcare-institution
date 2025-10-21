package it.pagopa.selfcare.mscore.web.util;

import it.pagopa.selfcare.mscore.api.UserRegistryConnector;
import it.pagopa.selfcare.mscore.exception.ResourceNotFoundException;
import it.pagopa.selfcare.mscore.model.user.User;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EncryptedPathVariableResolver implements HandlerMethodArgumentResolver {

    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private static final Pattern CF_PATTERN = Pattern.compile(".*[A-Za-z].*");

    private final UserRegistryConnector userRegistryConnector;

    public EncryptedPathVariableResolver(UserRegistryConnector userRegistryConnector) {
        this.userRegistryConnector = userRegistryConnector;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(EncryptedPathVariable.class)
                && String.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        EncryptedPathVariable annotation = parameter.getParameterAnnotation(EncryptedPathVariable.class);
        String paramName = (annotation != null && !annotation.value().isEmpty())
                ? annotation.value()
                : parameter.getParameterName();
        boolean required = annotation == null || annotation.required();


        Map<String, String> uriVariables = webRequest.getAttribute(
                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
                RequestAttributes.SCOPE_REQUEST) instanceof Map<?, ?> map
                ? map.entrySet().stream()
                .filter(e -> e.getKey() instanceof String && e.getValue() instanceof String)
                .collect(Collectors.toMap(
                        e -> (String) e.getKey(),
                        e -> (String) e.getValue()))
                : Collections.emptyMap();


        String taxCode = uriVariables.get(paramName);

        if (!StringUtils.hasText(taxCode)) {
            if (required) {
                assert paramName != null;
                throw new MissingPathVariableException(paramName, parameter);
            } else {
                return null;
            }
        }

        if (CF_PATTERN.matcher(taxCode).matches() && !UUID_PATTERN.matcher(taxCode).matches()) {
            try {
                User user = userRegistryConnector.getUserByFiscalCode(taxCode);
                return user != null ? user.getId() : taxCode;
            } catch (ResourceNotFoundException e) {
                // 404: user not found →  return the original taxCode
                return taxCode;
            }
        }

        return taxCode;
    }
}


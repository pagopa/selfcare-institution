package it.pagopa.selfcare.mscore.web.util;

import it.pagopa.selfcare.mscore.api.UserRegistryConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.regex.Pattern;

@Component
public class EncryptedTaxCodeParamResolver implements HandlerMethodArgumentResolver {

    private final UserRegistryConnector userRegistryConnector;

    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private static final Pattern CF_PATTERN = Pattern.compile(".*[A-Za-z].*");

    @Autowired
    public EncryptedTaxCodeParamResolver(UserRegistryConnector userRegistryConnector) {
        this.userRegistryConnector = userRegistryConnector;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(EncryptedTaxCodeParam.class)
                && String.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        EncryptedTaxCodeParam annotation = parameter.getParameterAnnotation(EncryptedTaxCodeParam.class);
        String paramName = (annotation != null && !annotation.value().isEmpty())
                ? annotation.value()
                : parameter.getParameterName();

        assert paramName != null;
        String taxCode = webRequest.getParameter(paramName);

        if (!StringUtils.hasText(taxCode)) {
            return null;
        }

        if (CF_PATTERN.matcher(taxCode).matches() && !UUID_PATTERN.matcher(taxCode).matches()) {
            return userRegistryConnector.getUserByFiscalCode(taxCode).getId();
        }

        return taxCode;
    }
}

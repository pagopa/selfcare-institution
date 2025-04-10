package it.pagopa.selfcare.mscore.web.config;

import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.types.TypePlaceHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.reactive.context.StandardReactiveWebEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import springfox.documentation.spring.web.plugins.Docket;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {SwaggerConfig.class, TypeResolver.class})
@ExtendWith(SpringExtension.class)
class SwaggerConfigTest {
    @MockBean
    private Environment environment;

    @Autowired
    private SwaggerConfig swaggerConfig;

    /**
     * Method under test: {@link SwaggerConfig#swaggerSpringPlugin(TypeResolver)}
     */
    @Test
    void testSwaggerSpringPlugin() {

        SwaggerConfig swaggerConfig = new SwaggerConfig(new StandardReactiveWebEnvironment());
        Docket actualSwaggerSpringPluginResult = swaggerConfig.swaggerSpringPlugin(new TypeResolver());
        assertTrue(actualSwaggerSpringPluginResult.isEnabled());
        assertEquals("default", actualSwaggerSpringPluginResult.getGroupName());
    }

    /**
     * Method under test: {@link SwaggerConfig#swaggerSpringPlugin(TypeResolver)}
     */
    @Test
    void testSwaggerSpringPlugin3() {

        StandardEnvironment standardEnvironment = mock(StandardEnvironment.class);
        when(standardEnvironment.getProperty(any())).thenReturn("Property");
        when(standardEnvironment.getProperty( any(), (String) any())).thenReturn("Property");
        SwaggerConfig swaggerConfig = new SwaggerConfig(standardEnvironment);
        Docket actualSwaggerSpringPluginResult = swaggerConfig.swaggerSpringPlugin(new TypeResolver());
        assertTrue(actualSwaggerSpringPluginResult.isEnabled());
        assertEquals("default", actualSwaggerSpringPluginResult.getGroupName());
        verify(standardEnvironment, atLeast(1)).getProperty(any());
        verify(standardEnvironment, atLeast(1)).getProperty(any(), (String) any());
    }

    /**
     * Method under test: {@link SwaggerConfig#swaggerSpringPlugin(TypeResolver)}
     */
    @Test
    void testSwaggerSpringPlugin5() {

        StandardEnvironment standardEnvironment = mock(StandardEnvironment.class);
        when(standardEnvironment.getProperty(any())).thenReturn("Property");
        when(standardEnvironment.getProperty(any(), (String) any())).thenReturn("Property");
        SwaggerConfig swaggerConfig = new SwaggerConfig(standardEnvironment);
        TypeResolver typeResolver = mock(TypeResolver.class);
        when(typeResolver.resolve(any(), (Type[]) any())).thenReturn(new TypePlaceHolder(1));
        Docket actualSwaggerSpringPluginResult = swaggerConfig.swaggerSpringPlugin(typeResolver);
        assertTrue(actualSwaggerSpringPluginResult.isEnabled());
        assertEquals("default", actualSwaggerSpringPluginResult.getGroupName());
        verify(standardEnvironment, atLeast(1)).getProperty(any());
        verify(standardEnvironment, atLeast(1)).getProperty(any(), (String) any());
        verify(typeResolver).resolve(any(), (Type[]) any());
    }

    /**
     * Method under test: {@link SwaggerConfig#swaggerSpringPlugin(TypeResolver)}
     */
    @Test
    void testSwaggerSpringPlugin6() {

        StandardEnvironment standardEnvironment = mock(StandardEnvironment.class);
        when(standardEnvironment.getProperty(any())).thenReturn("Property");
        when(standardEnvironment.getProperty(any(), (String) any())).thenReturn("Property");
        SwaggerConfig swaggerConfig = new SwaggerConfig(standardEnvironment);
        TypeResolver typeResolver = mock(TypeResolver.class);
        when(typeResolver.resolve(any(), (Type[]) any())).thenReturn(null);
        Docket actualSwaggerSpringPluginResult = swaggerConfig.swaggerSpringPlugin(typeResolver);
        assertTrue(actualSwaggerSpringPluginResult.isEnabled());
        assertEquals("default", actualSwaggerSpringPluginResult.getGroupName());
        verify(standardEnvironment, atLeast(1)).getProperty(any());
        verify(standardEnvironment, atLeast(1)).getProperty(any(), (String) any());
        verify(typeResolver).resolve(any(), (Type[]) any());
    }

}


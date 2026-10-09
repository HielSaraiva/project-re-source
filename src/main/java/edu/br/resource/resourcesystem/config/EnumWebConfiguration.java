package edu.br.resource.resourcesystem.config;

import edu.br.resource.resourcesystem.model.enums.DatabaseEnum;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
public class EnumWebConfiguration implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new DatabaseEnumConverterFactory());
    }

    private static final class DatabaseEnumConverterFactory implements ConverterFactory<String, DatabaseEnum> {
        @Override
        public <T extends DatabaseEnum> Converter<String, T> getConverter(Class<T> targetType) {
            return source -> {
                if (source.isBlank()) {
                    return null;
                }
                for (T value : targetType.getEnumConstants()) {
                    if (value.getValue().equals(source.strip())) {
                        return value;
                    }
                }
                throw new IllegalArgumentException("Valor inválido para " + targetType.getSimpleName() + ": " + source);
            };
        }
    }
}

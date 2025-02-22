import com.swirlds.config.api.spi.ConfigurationBuilderFactory;
import com.swirlds.config.impl.internal.ConfigurationBuilderFactoryImpl;

module com.swirlds.config.impl {
    exports com.swirlds.config.impl.converters;
    exports com.swirlds.config.impl.validators;

    requires transitive com.swirlds.config.api;
    requires com.swirlds.base;
    requires com.swirlds.config.extensions;
    requires static com.github.spotbugs.annotations;
    requires static com.google.auto.service;

    provides ConfigurationBuilderFactory with
            ConfigurationBuilderFactoryImpl;
}

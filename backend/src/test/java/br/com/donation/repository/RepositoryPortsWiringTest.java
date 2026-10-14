package br.com.donation.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Garante o contrato de desacoplamento da camada de persistência, sem precisar de banco:
 * toda porta ({@code *Repository}) deve ter exatamente um adaptador registrado, e os
 * adaptadores JDBC devem ficar confinados em {@code br.com.donation.repository.jdbc}.
 */
class RepositoryPortsWiringTest {

    private static final String PORTS_PACKAGE = "br.com.donation.repository";
    private static final String ADAPTERS_PACKAGE = PORTS_PACKAGE + ".jdbc";

    @Test
    void everyRepositoryPortHasExactlyOneJdbcAdapter() throws ClassNotFoundException {
        List<Class<?>> ports = discoverPorts();
        assertFalse(ports.isEmpty(), "Nenhuma porta de repositório encontrada em " + PORTS_PACKAGE);

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            // DriverManagerDataSource só conecta sob demanda: o wiring é validado sem banco.
            context.registerBean(DataSource.class, () -> new DriverManagerDataSource("jdbc:none:"));
            context.scan(ADAPTERS_PACKAGE);
            context.refresh();

            for (Class<?> port : ports) {
                Map<String, ?> adapters = context.getBeansOfType(port);
                assertEquals(1, adapters.size(),
                        port.getSimpleName() + " deve ter exatamente um adaptador, mas tem: " + adapters.keySet());

                Class<?> adapterType = ClassUtils.getUserClass(adapters.values().iterator().next());
                assertEquals(ADAPTERS_PACKAGE, adapterType.getPackageName(),
                        "Adaptador de " + port.getSimpleName() + " fora do pacote esperado");
            }
        }
    }

    private static List<Class<?>> discoverPorts() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isInterface();
            }
        };
        scanner.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*Repository$")));

        List<Class<?>> ports = new java.util.ArrayList<>();
        for (var definition : scanner.findCandidateComponents(PORTS_PACKAGE)) {
            // Apenas as portas do pacote raiz (ignora eventuais subpacotes).
            if (definition.getBeanClassName().lastIndexOf('.') == PORTS_PACKAGE.length()) {
                ports.add(Class.forName(definition.getBeanClassName()));
            }
        }
        return ports;
    }
}

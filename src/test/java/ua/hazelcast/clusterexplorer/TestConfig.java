package ua.hazelcast.clusterexplorer;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesCrudDispatcher;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.fabric8.mockwebserver.Context;
import okhttp3.mockwebserver.MockWebServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.HashMap;

@Profile("test")
@Configuration
public class TestConfig {

    private KubernetesMockServer kubernetesMockServer;

    private NamespacedKubernetesClient kubernetesClient;

    @PostConstruct
    public void init() {
        kubernetesMockServer = new KubernetesMockServer(new Context(), new MockWebServer(), new HashMap<>(),
                new KubernetesCrudDispatcher(Collections.emptyList()), false);
        kubernetesClient = kubernetesMockServer.createClient();
    }

    @Bean
    public KubernetesClient getClient() {
        return kubernetesClient;
    }

    @PreDestroy
    public void shutDown() {
        kubernetesMockServer.shutdown();
    }
}

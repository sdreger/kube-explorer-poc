package ua.hazelcast.clusterexplorer.config;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class ClusterClientConfiguration {

    @Bean
    public KubernetesClient apiClient() {
        return new DefaultKubernetesClient();
    }
}

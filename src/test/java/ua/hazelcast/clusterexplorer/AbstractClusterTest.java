package ua.hazelcast.clusterexplorer;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import ua.hazelcast.clusterexplorer.service.ClusterService;

import java.util.Map;
import java.util.UUID;

@SpringBootTest
public abstract class AbstractClusterTest {

    protected static final String NS_DEFAULT = "default";

    protected static final String DEPLOYMENT_NAME_PREFIX = "test-nginx-deployment-";

    protected static final Integer REPLICAS = 2;

    protected static final Map<String, String> DEPLOYMENT_LABELS = Map.of("app", "nginx");

    protected static final String CONTAINER_NAME = "nginx";

    protected static final String CONTAINER_IMAGE = "nginx:1.14.2";

    protected static final int CONTAINER_PORT = 80;

    protected Deployment testDeployment;

    protected String deploymentName;

    @SpyBean
    protected KubernetesClient kubernetesClient;

    @Autowired
    protected ClusterService clusterService;

    @BeforeEach
    void setUp() throws KubernetesClientException {

        deploymentName = DEPLOYMENT_NAME_PREFIX + UUID.randomUUID();
        testDeployment = new DeploymentBuilder()
                .withNewMetadata()
                    .withName(deploymentName)
                    .addToLabels(DEPLOYMENT_LABELS)
                    .endMetadata()
                .withNewSpec()
                    .withReplicas(REPLICAS)
                    .withNewSelector()
                        .withMatchLabels(DEPLOYMENT_LABELS)
                    .endSelector()
                    .withNewTemplate()
                        .withNewMetadata()
                            .addToLabels(DEPLOYMENT_LABELS)
                        .endMetadata()
                        .withNewSpec()
                            .addNewContainer()
                                .withName(CONTAINER_NAME)
                                .withImage(CONTAINER_IMAGE)
                                .withPorts()
                                    .addNewPort()
                                        .withContainerPort(CONTAINER_PORT)
                                    .endPort()
                            .endContainer()
                        .endSpec()
                    .endTemplate()
                .endSpec()
            .build();

        kubernetesClient.apps().deployments().inNamespace(NS_DEFAULT).createOrReplace(testDeployment);
    }

    @AfterEach
    void shutDown() throws KubernetesClientException {
        kubernetesClient.apps().deployments().inNamespace(NS_DEFAULT).delete(testDeployment);
    }
}

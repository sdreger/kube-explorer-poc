package ua.hazelcast.clusterexplorer;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ActiveProfiles("test")
public abstract class AbstractClusterTest {

    protected static final String NS_DEFAULT = "default";

    protected static final String DEPLOYMENT_NAME = "test-nginx-deployment";

    protected static final String POD_NAME_PREFIX = DEPLOYMENT_NAME + "-";

    protected static final Integer REPLICAS = 2;

    protected static final Map<String, String> DEPLOYMENT_LABELS = Map.of("app", "nginx");

    protected static final String CONTAINER_NAME = "nginx";

    protected static final String CONTAINER_IMAGE = "nginx:1.14.2";

    protected static final int CONTAINER_PORT = 80;

    protected Deployment testDeployment;

    protected List<Pod> testPods = new ArrayList<>(REPLICAS);

    @Autowired
    protected KubernetesClient client;

    @BeforeEach
    void setUp() throws KubernetesClientException {

        testDeployment = new DeploymentBuilder()
                .withNewMetadata()
                    .withName(DEPLOYMENT_NAME)
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

        client.apps().deployments().inNamespace(NS_DEFAULT).createOrReplace(testDeployment);

        // We have to create pods manually, because in the API mode deployment creation doesn't trigger pods creation
        for (int i = 0; i < REPLICAS; i++) {
            Pod testPod = new PodBuilder()
                    .withNewMetadata()
                        .withName(POD_NAME_PREFIX + UUID.randomUUID())
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
                    .build();

            client.pods().inNamespace(NS_DEFAULT).createOrReplace(testPod);
            testPods.add(testPod);
        }
    }

    @AfterEach
    void shutDown() throws KubernetesClientException {
        testPods.forEach(pod -> client.pods().inNamespace(NS_DEFAULT).delete(pod));
        client.apps().deployments().inNamespace(NS_DEFAULT).delete(testDeployment);
    }
}

package ua.hazelcast.clusterexplorer;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import ua.hazelcast.clusterexplorer.service.ClusterService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
public abstract class AbstractClusterTest {

    protected static final String NS_DEFAULT = "default";

    protected static final String DEPLOYMENT_NAME_PREFIX = "test-nginx-deployment-";

    protected static final Integer REPLICAS = 2;

    protected static final Map<String, String> DEPLOYMENT_LABELS = Map.of("app", "nginx");

    protected String deploymentName;

    @SpyBean
    protected AppsV1Api kubeAppsApi;

    @Autowired
    protected ClusterService clusterService;

    @BeforeEach
    void setUp() throws ApiException {
        deploymentName = DEPLOYMENT_NAME_PREFIX + UUID.randomUUID();
        final V1ObjectMeta deploymentMetadata = new V1ObjectMeta();
        deploymentMetadata.setName(deploymentName);
        deploymentMetadata.setLabels(DEPLOYMENT_LABELS);

        final V1LabelSelector deploymentLabelSelector = new V1LabelSelector().matchLabels(DEPLOYMENT_LABELS);

        final V1Container container1 = new V1Container();
        container1.setName("nginx");
        container1.setImage("nginx:1.14.2");

        final V1ContainerPort containerPort1 = new V1ContainerPort();
        containerPort1.setName("http");
        containerPort1.setContainerPort(80);
        container1.setPorts(List.of(containerPort1));

        final V1PodSpec podSpec = new V1PodSpec();
        podSpec.setContainers(List.of(container1));
        podSpec.setTerminationGracePeriodSeconds(1L);

        final V1PodTemplateSpec deploymentTemplate = new V1PodTemplateSpec();
        deploymentTemplate.setMetadata(new V1ObjectMeta().labels(DEPLOYMENT_LABELS));
        deploymentTemplate.setSpec(podSpec);

        final V1DeploymentSpec deploymentSpec = new V1DeploymentSpec();
        deploymentSpec.setReplicas(REPLICAS);
        deploymentSpec.setSelector(deploymentLabelSelector);
        deploymentSpec.setTemplate(deploymentTemplate);

        final V1Deployment deployment = new V1Deployment();
        deployment.metadata(deploymentMetadata);

        deployment.setSpec(deploymentSpec);
        kubeAppsApi.createNamespacedDeployment(NS_DEFAULT, deployment, "true", null, "test");
    }

    @AfterEach
    void shutDown() throws ApiException {
        kubeAppsApi.deleteNamespacedDeployment(deploymentName, NS_DEFAULT, null, null, 1, null, "Background", null);
    }
}

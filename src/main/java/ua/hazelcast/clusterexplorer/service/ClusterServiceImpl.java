package ua.hazelcast.clusterexplorer.service;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.hazelcast.clusterexplorer.exception.ApplicationException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClusterServiceImpl implements ClusterService {

    private final KubernetesClient kubernetesClient;

    @Autowired
    public ClusterServiceImpl(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    @Override
    public List<String> getNamespaceDeploymentsNames(final String namespace) {

        return processApiCallWithTryCatchBlock(() -> {
            final DeploymentList deploymentList = kubernetesClient.apps()
                    .deployments()
                    .inNamespace(namespace)
                    .list();
            return getDeploymentsNames(deploymentList);
        });
    }

    @Override
    public List<String> getAllNamespacesDeploymentsNames() {

        return processApiCallWithTryCatchBlock(() -> {
            final DeploymentList deploymentList = kubernetesClient.apps()
                    .deployments()
                    .inAnyNamespace()
                    .list();
            return getDeploymentsNames(deploymentList);
        });
    }

    @Override
    public List<String> getNamespacePodNames(final String namespace) {

        return processApiCallWithTryCatchBlock(() -> {
            final PodList podList = kubernetesClient.pods()
                    .inNamespace(namespace)
                    .list();
            return getPodNames(podList);
        });
    }

    @Override
    public List<String> getAllNamespacesPodNames() {

        return processApiCallWithTryCatchBlock(() -> {
            final PodList podList = kubernetesClient.pods()
                    .inAnyNamespace()
                    .list();
            return getPodNames(podList);
        });
    }

    private List<String> getDeploymentsNames(final DeploymentList deployments) {
        if (deployments == null) {
            return Collections.emptyList();
        }

        return deployments.getItems()
                .stream()
                .map(Deployment::getMetadata)
                .filter(Objects::nonNull)
                .map(ObjectMeta::getName)
                .collect(Collectors.toList());
    }

    private List<String> getPodNames(final PodList podList) {
        if (podList == null) {
            return Collections.emptyList();
        }

        return podList.getItems()
                .stream()
                .map(Pod::getMetadata)
                .filter(Objects::nonNull)
                .map(ObjectMeta::getName)
                .collect(Collectors.toList());
    }

    /**
     * Method which performs common exception handling.
     *
     * @param consumer - which do a call to Kube API
     * @param <T>      - expected return type
     * @return - call result
     */
    static <T> T processApiCallWithTryCatchBlock(KubeApiCall<T> consumer) {
        try {
            return consumer.process();
        } catch (KubernetesClientException e) {
            log.error("Api call error: {}", e.getMessage());
            throw new ApplicationException(GENERIC_ERROR_MESSAGE);
        }
    }

    @FunctionalInterface
    public interface KubeApiCall<T> {
        T process() throws KubernetesClientException;
    }
}

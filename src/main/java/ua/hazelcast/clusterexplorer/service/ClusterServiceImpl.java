package ua.hazelcast.clusterexplorer.service;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ua.hazelcast.clusterexplorer.exception.ApplicationException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClusterServiceImpl implements ClusterService {

    private final AppsV1Api kubeAppsApi;

    private final CoreV1Api coreApi;

    @Autowired
    public ClusterServiceImpl(final AppsV1Api kubeAppsApi, final CoreV1Api coreApi) {
        this.kubeAppsApi = kubeAppsApi;
        this.coreApi = coreApi;
    }

    @Override
    public List<String> getNamespaceDeploymentsNames(final String namespace) {
        return processApiCallWithTryCatchBlock(() -> {
            final List<V1Deployment> deployments = kubeAppsApi
                    .listNamespacedDeployment(namespace, null, null, null, null, null, null, null, null, null)
                    .getItems();
            return getDeploymentsNames(deployments);
        });
    }

    @Override
    public List<String> getAllNamespacesDeploymentsNames() {
        return processApiCallWithTryCatchBlock(() -> {
            final List<V1Deployment> deployments = kubeAppsApi
                    .listDeploymentForAllNamespaces(null, null, null, null, null, null, null, null, null)
                    .getItems();
            return getDeploymentsNames(deployments);
        });
    }

    @Override
    public List<String> getNamespacePodNames(final String namespace) {

        return processApiCallWithTryCatchBlock(() -> {

            final List<V1Pod> podList = coreApi
                    .listNamespacedPod(namespace, null, null, null, null, null, null, null, null, null)
                    .getItems();
            return getPodNames(podList);
        });
    }

    @Override
    public List<String> getAllNamespacesPodNames() {

        return processApiCallWithTryCatchBlock(() -> {
            final List<V1Pod> podList = coreApi
                    .listPodForAllNamespaces(null, null, null, null, null, null, null, null, null)
                    .getItems();
            return getPodNames(podList);
        });
    }

    private List<String> getDeploymentsNames(final List<V1Deployment> deployments) {
        if (CollectionUtils.isEmpty(deployments)) {
            return Collections.emptyList();
        }

        return deployments.stream()
                .map(V1Deployment::getMetadata)
                .filter(Objects::nonNull)
                .map(V1ObjectMeta::getName)
                .collect(Collectors.toList());
    }

    private List<String> getPodNames(final List<V1Pod> podList) {
        if (CollectionUtils.isEmpty(podList)) {
            return Collections.emptyList();
        }

        return podList.stream()
                .map(V1Pod::getMetadata)
                .filter(Objects::nonNull)
                .map(V1ObjectMeta::getName)
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
        } catch (ApiException e) {
            log.error("Api call error: {}", e.getMessage());
            throw new ApplicationException(GENERIC_ERROR_MESSAGE);
        }
    }

    @FunctionalInterface
    public interface KubeApiCall<T> {
        T process() throws ApiException;
    }
}

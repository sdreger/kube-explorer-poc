package ua.hazelcast.clusterexplorer.service;

import java.util.List;

public interface ClusterService {

    // TODO: move to the 'messages.properties' file.
    String GENERIC_ERROR_MESSAGE = "Can not get the requested data, please contact technical support.";

    /**
     * Get deployment names for the particular namespace.
     *
     * @param namespace - deployments namespace to be listed.
     * @return - a list of deployments names.
     */
    List<String> getNamespaceDeploymentsNames(final String namespace);

    /**
     * Get deployment names for all namespaces.
     *
     * @return - a list of deployments names.
     */
    List<String> getAllNamespacesDeploymentsNames();

    /**
     * Get pod names for the particular namespace.
     *
     * @param namespace - pod namespace to be listed.
     * @return - a list of pods names.
     */
    List<String> getNamespacePodNames(final String namespace);

    /**
     * Get pod names for all namespaces.
     *
     * @return - a list of pods names.
     */
    List<String> getAllNamespacesPodNames();
}

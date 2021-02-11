package ua.hazelcast.clusterexplorer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClusterServiceTest extends AbstractClusterTest {

    @Test
    void shouldGetDefaultNamespaceDeployment() {
        final List<String> deploymentsNames = clusterService.getNamespaceDeploymentsNames(NS_DEFAULT);
        assertThat(deploymentsNames)
                .isNotEmpty()
                .contains(deploymentName);

        final List<String> podNames = clusterService.getNamespacePodNames(NS_DEFAULT);
        assertThat(podNames)
                .isNotEmpty()
                .filteredOn(podName -> podName.startsWith(deploymentName))
                .hasSize(REPLICAS);
    }

    @Test
    void shouldGetAllNamespacesDeployments() {
        final List<String> deploymentsNames = clusterService.getAllNamespacesDeploymentsNames();
        assertThat(deploymentsNames)
                .isNotEmpty()
                .hasSizeGreaterThan(1)
                .contains(deploymentName);

        final List<String> podNames = clusterService.getAllNamespacesPodNames();
        assertThat(podNames)
                .isNotEmpty()
                .hasSizeGreaterThan(REPLICAS)
                .filteredOn(podName -> podName.startsWith(deploymentName))
                .hasSize(REPLICAS);
    }
}

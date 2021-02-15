package ua.hazelcast.clusterexplorer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ua.hazelcast.clusterexplorer.service.ClusterService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ClusterServiceTest extends AbstractClusterTest {

    @Autowired
    protected ClusterService clusterService;

    @Test
    void shouldGetDefaultNamespaceDeployment() {
        final List<String> deploymentsNames = clusterService.getNamespaceDeploymentsNames(NS_DEFAULT);
        assertThat(deploymentsNames)
                .isNotEmpty()
                .contains(DEPLOYMENT_NAME);

        final List<String> podNames = clusterService.getNamespacePodNames(NS_DEFAULT);
        assertThat(podNames)
                .isNotEmpty()
                .filteredOn(podName -> podName.startsWith(DEPLOYMENT_NAME))
                .hasSize(REPLICAS);
    }

    @Test
    void shouldGetAllNamespacesDeployments() {
        final List<String> deploymentsNames = clusterService.getAllNamespacesDeploymentsNames();
        assertThat(deploymentsNames)
                .isNotEmpty()
                .hasSize(1)
                .contains(DEPLOYMENT_NAME);

        final List<String> podNames = clusterService.getAllNamespacesPodNames();
        assertThat(podNames)
                .isNotEmpty()
                .hasSize(REPLICAS);
    }
}

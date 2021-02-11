package ua.hazelcast.clusterexplorer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.hazelcast.clusterexplorer.service.ClusterService;

import java.util.List;

@RestController
@RequestMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class ClusterController {

    private final ClusterService clusterService;

    @Autowired
    public ClusterController(final ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @GetMapping(path = "/deployments")
    public List<String> getDeployments(@RequestParam(value = "namespace", required = false) String namespace) {
        return namespace == null
                ? clusterService.getAllNamespacesDeploymentsNames()
                : clusterService.getNamespaceDeploymentsNames(namespace);
    }

    @GetMapping(path = "/pods")
    public List<String> getPods(@RequestParam(value = "namespace", required = false) String namespace) {
        return namespace == null
                ? clusterService.getAllNamespacesPodNames()
                : clusterService.getNamespacePodNames(namespace);
    }
}

package ua.hazelcast.clusterexplorer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ua.hazelcast.clusterexplorer.service.ClusterService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class ClusterControllerTest extends AbstractClusterTest {

    private static final String URL_DEPLOYMENTS = "/deployments";

    private static final String NAMESPACE_FILTER_PARAM = "namespace";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldReturnDeploymentNames() throws Exception {
        final MvcResult result = this.mockMvc
                .perform(get(URL_DEPLOYMENTS).param(NAMESPACE_FILTER_PARAM, NS_DEFAULT))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        final List<String> deploymentsNames =
                objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<String>>() {
                });
        assertThat(deploymentsNames)
                .isNotEmpty()
                .contains(deploymentName);
    }

    @Test
    void shouldReturnAnError() throws Exception {

        doThrow(new ApiException("Request error"))
                .when(kubeAppsApi).listNamespacedDeployment(eq(NS_DEFAULT), any(), any(), any(),
                any(), any(), any(), any(), any(), any());

        this.mockMvc
                .perform(get(URL_DEPLOYMENTS).param(NAMESPACE_FILTER_PARAM, NS_DEFAULT))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(ClusterService.GENERIC_ERROR_MESSAGE));
    }

    // TODO: other endpoints tests go here..
}

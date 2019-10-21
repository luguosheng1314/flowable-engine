/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.cmmn.test.eventregistry;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.cmmn.engine.test.CmmnDeployment;
import org.flowable.cmmn.engine.test.FlowableCmmnTestCase;
import org.flowable.common.engine.api.eventregistry.OutboundEventChannelAdapter;
import org.flowable.common.engine.api.eventregistry.definition.EventPayloadTypes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Joram Barrez
 */
public class SendEventTaskTest extends FlowableCmmnTestCase {

    protected TestOutboundEventChannelAdapter outboundEventChannelAdapter;

    @Before
    public void registerEventDefinition() {
        outboundEventChannelAdapter = setupTestChannel();

        cmmnEngineConfiguration.getEventRegistry().newEventDefinition()
            .outboundChannelKey("out-channel")
            .key("testEvent")
            .payload("payload1", EventPayloadTypes.STRING)
            .payload("payload2", EventPayloadTypes.INTEGER)
            .register();
    }

    protected TestOutboundEventChannelAdapter setupTestChannel() {
        TestOutboundEventChannelAdapter outboundEventChannelAdapter = new TestOutboundEventChannelAdapter();

        cmmnEngineConfiguration.getEventRegistry().newOutboundChannelDefinition()
            .key("out-channel")
            .channelAdapter(outboundEventChannelAdapter)
            .jsonSerializer()
            .register();

        return outboundEventChannelAdapter;
    }


    @After
    public void unregisterEventDefinition() {
        cmmnEngineConfiguration.getEventRegistry().removeChannelDefinition("test-channel");
        cmmnEngineConfiguration.getEventRegistry().removeEventDefinition("myEvent");
    }

    @Test
    @CmmnDeployment
    public void testSendEventTaskNoPayload() throws Exception {
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
            .caseDefinitionKey("testSendEvent")
            .variable("myVariable", "Hello World!")
            .start();

         assertThat(outboundEventChannelAdapter.receivedEvents).hasSize(1);

        JsonNode jsonNode = cmmnEngineConfiguration.getObjectMapper().readTree(outboundEventChannelAdapter.receivedEvents.get(0));
        assertThat(jsonNode).hasSize(1);
        assertThat(jsonNode.get("customerId").asText()).isEqualTo("Hello World!");
    }

    public static class TestOutboundEventChannelAdapter implements OutboundEventChannelAdapter {

        public List<String> receivedEvents = new ArrayList<>();

        @Override
        public void sendEvent(String rawEvent) {
            receivedEvents.add(rawEvent);
        }

    }

}
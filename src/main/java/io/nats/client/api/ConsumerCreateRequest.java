// Copyright 2020 The NATS Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.nats.client.api;

import io.nats.client.support.JsonSerializable;
import io.nats.client.support.JsonUtils;

import static io.nats.client.support.ApiConstants.*;
import static io.nats.client.support.JsonUtils.*;

/**
 * Object used to make a request to create a consumer. Used Internally
 */
public class ConsumerCreateRequest implements JsonSerializable {
    public enum Action {
        Create("create"),
        Update("update"),
        CreateOrUpdate(null);

        public final String actionText;

        Action(String actionText) {
            this.actionText = actionText;
        }
    }

    public final String streamName;
    public final ConsumerConfiguration config;
    public final Action action;
    public final boolean pedantic;

    /**
     * Instantiate a ConsumerCreateRequest with Action.Create and pedantic=false
     * @param streamName the stream name
     * @param config the consumer config
     * @return a ConsumerCreateRequest
     */
    public static ConsumerCreateRequest create(String streamName, ConsumerConfiguration config) {
        return new ConsumerCreateRequest(streamName, config, Action.Create, false);
    }

    /**
     * Instantiate a ConsumerCreateRequest with Action.Create and pedantic=true
     * @param streamName the stream name
     * @param config the consumer config
     * @return a ConsumerCreateRequest
     */
    public static ConsumerCreateRequest createPedantic(String streamName, ConsumerConfiguration config) {
        return new ConsumerCreateRequest(streamName, config, Action.Create, true);
    }

    /**
     * Instantiate a ConsumerCreateRequest with Action.Update and pedantic=false
     * @param streamName the stream name
     * @param config the consumer config
     * @return a ConsumerCreateRequest
     */
    public static ConsumerCreateRequest update(String streamName, ConsumerConfiguration config) {
        return new ConsumerCreateRequest(streamName, config, Action.Update, false);
    }

    /**
     * Instantiate a ConsumerCreateRequest with Action.Update and pedantic=true
     * @param streamName the stream name
     * @param config the consumer config
     * @return a ConsumerCreateRequest
     */
    public static ConsumerCreateRequest updatePedantic(String streamName, ConsumerConfiguration config) {
        return new ConsumerCreateRequest(streamName, config, Action.Update, true);
    }

    /**
     * Instantiate a ConsumerCreateRequest with Action.CreateOrUpdate and pedantic=false
     * @param streamName the stream name
     * @param config the consumer config
     * @return a ConsumerCreateRequest
     */
    public static ConsumerCreateRequest createOrUpdate(String streamName, ConsumerConfiguration config) {
        return new ConsumerCreateRequest(streamName, config, Action.CreateOrUpdate, false);
    }

    /**
     * Instantiate a ConsumerCreateRequest with Action.CreateOrUpdate and pedantic=true
     * @param streamName the stream name
     * @param config the consumer config
     * @return a ConsumerCreateRequest
     */
    public static ConsumerCreateRequest createOrUpdatePedantic(String streamName, ConsumerConfiguration config) {
        return new ConsumerCreateRequest(streamName, config, Action.CreateOrUpdate, true);
    }

    /**
     * Instantiate a ConsumerCreateRequest with Action.CreateOrUpdate and pedantic=false
     * @param streamName the stream name
     * @param config the consumer config
     */
    public ConsumerCreateRequest(String streamName, ConsumerConfiguration config) {
        this(streamName, config, Action.CreateOrUpdate, false);
    }

    /**
     * Instantiate a ConsumerCreateRequest with pedantic=false
     * @param streamName the stream name
     * @param config the consumer config
     * @param action the type of action. Null is considered to be CreateOrUpdate
     */
    public ConsumerCreateRequest(String streamName, ConsumerConfiguration config, Action action) {
        this(streamName, config, action, false);
    }

    /**
     * Instantiate a ConsumerCreateRequest with pedantic=false
     * @param streamName the stream name
     * @param config the consumer config
     * @param action the type of action. Null is considered to be CreateOrUpdate
     * @param pedantic the pedantic flag
     */
    public ConsumerCreateRequest(String streamName, ConsumerConfiguration config, Action action, boolean pedantic) {
        this.streamName = streamName;
        this.config = config;
        this.action = action == null ? Action.CreateOrUpdate : action;
        this.pedantic = pedantic;
    }

    public String getStreamName() {
        return streamName;
    }

    public ConsumerConfiguration getConfig() {
        return config;
    }

    public Action getAction() {
        return action;
    }

    public boolean isPedantic() {
        return pedantic;
    }

    @Override
    public String toJson() {
        StringBuilder sb = beginJson();

        addField(sb, STREAM_NAME, streamName);
        JsonUtils.addField(sb, ACTION, action.actionText);
        JsonUtils.addField(sb, CONFIG, config);
        JsonUtils.addFldWhenTrue(sb, PEDANTIC, pedantic);

        return endJson(sb).toString();
    }

    @Override
    public String toString() {
        return "ConsumerCreateRequest{" +
            "streamName='" + streamName + '\'' +
            "pedantic='" + pedantic + '\'' +
            ", " + config +
            '}';
    }
}

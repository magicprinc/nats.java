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

import static io.nats.client.support.NatsJetStreamConstants.JSAPI_STREAM_CREATE;
import static io.nats.client.support.NatsJetStreamConstants.JSAPI_STREAM_UPDATE;

/**
 * Used for advanced stream create or update
 */
public class StreamManagementOptions {
    private final String jsApiTemplate;
    private final boolean pedantic;

    public static StreamManagementOptions create() {
        return new StreamManagementOptions(JSAPI_STREAM_CREATE, false);
    }

    public static StreamManagementOptions createPedantic() {
        return new StreamManagementOptions(JSAPI_STREAM_CREATE, true);
    }

    public static StreamManagementOptions update() {
        return new StreamManagementOptions(JSAPI_STREAM_UPDATE, false);
    }

    public static StreamManagementOptions updatePedantic() {
        return new StreamManagementOptions(JSAPI_STREAM_UPDATE, true);
    }

    private StreamManagementOptions(String jsApiTemplate, boolean pedantic) {
        this.jsApiTemplate = jsApiTemplate;
        this.pedantic = pedantic;
    }

    public String getJsApiTemplate() {
        return jsApiTemplate;
    }

    public boolean isPedantic() {
        return pedantic;
    }
}

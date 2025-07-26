// Copyright 2021-2025 The NATS Authors
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

package io.nats.client.impl;

import io.nats.client.Connection;
import io.nats.client.Consumer;
import io.nats.client.ErrorListener;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.nats.client.support.Status;
import lombok.CustomLog;

@CustomLog
public class ErrorListenerLoggerImpl implements ErrorListener {

    @Override
    public void errorOccurred(Connection conn, String error) {
        logger.severe(() -> supplyMessage("errorOccurred", conn, null, null, "Error: ", error));
    }

    @Override
    public void exceptionOccurred(Connection conn, Exception exp) {
        logger.severe(() -> supplyMessage("exceptionOccurred", conn, null, null, "Exception: ", exp));
    }

    @Override
    public void slowConsumerDetected(Connection conn, Consumer consumer) {
        logger.warning(() -> supplyMessage("slowConsumerDetected", conn, consumer, null));
    }

    @Override
    public void messageDiscarded(Connection conn, Message msg) {
        logger.info(() -> supplyMessage("messageDiscarded", conn, null, null, "Message: ", msg));
    }

    @Override
    public void heartbeatAlarm (
            Connection conn, JetStreamSubscription sub, long lastStreamSequence, long lastConsumerSequence
    ){
        logger.severe(() -> supplyMessage("heartbeatAlarm", conn, null, sub, "lastStreamSequence: ", lastStreamSequence, "lastConsumerSequence: ", lastConsumerSequence));
    }

    @Override
    public void unhandledStatus(Connection conn, JetStreamSubscription sub, Status status) {
        logger.warning(() -> supplyMessage("unhandledStatus", conn, null, sub, "Status:", status));
    }

    @Override
    public void pullStatusWarning(Connection conn, JetStreamSubscription sub, Status status) {
        logger.warning(() -> supplyMessage("pullStatusWarning", conn, null, sub, "Status:", status));
    }

    @Override
    public void pullStatusError(Connection conn, JetStreamSubscription sub, Status status) {
        logger.severe(() -> supplyMessage("pullStatusError", conn, null, sub, "Status:", status));
    }

    @Override
    public void flowControlProcessed(Connection conn, JetStreamSubscription sub, String idAkaSubject, FlowControlSource source) {
        logger.info(() -> supplyMessage("flowControlProcessed", conn, null, sub, "FlowControlSource:", source));
    }

    @Override
    public void socketWriteTimeout(Connection conn) {
        logger.severe(() -> supplyMessage("socketWriteTimeout", conn, null, null));
    }
}

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
import io.nats.client.support.JsonValue;
import io.nats.client.support.JsonValueUtils;
import io.nats.client.support.Status;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static io.nats.client.support.ApiConstants.CODE;
import static io.nats.client.support.ApiConstants.DESCRIPTION;
import static io.nats.client.support.ApiConstants.ERR_CODE;

/**
 * Error returned from an api request.
 */
public class Error implements JsonSerializable {

    public static final int NOT_SET = -1;

    private final JsonValue jv;

    static @Nullable Error optionalInstance(JsonValue vError) {
        return vError == null ? null : new Error(vError);
    }

    Error(JsonValue jv) {
        this.jv = jv;
    }

    Error(int code, String desc) {
        this(code, NOT_SET, desc);
    }

    Error(int code, int apiErrorCode, String desc) {
        jv = new JsonValueUtils.MapBuilder()
            .put(CODE, code)
            .put(ERR_CODE, apiErrorCode)
            .put(DESCRIPTION, desc)
            .toJsonValue();
    }

    @Override
    public @NonNull String toJson() {
        return jv.toJson();
    }

    @Override
    public @NonNull JsonValue toJsonValue() {
        return jv;
    }

    public int getCode() {
        return JsonValueUtils.readInteger(jv, CODE, NOT_SET);
    }

    public int getApiErrorCode() {
        return JsonValueUtils.readInteger(jv, ERR_CODE, NOT_SET);
    }

    public @NonNull String getDescription() {
        return JsonValueUtils.readString(jv, DESCRIPTION, "Unknown JetStream Error");
    }

    @Override
    public String toString() {
        int apiErrorCode = getApiErrorCode();
        int code = getCode();
        if (apiErrorCode == NOT_SET) {
            if (code == NOT_SET) {
                return getDescription();
            }
            return getDescription() + " (" + code + ")";
        }
        if (code == NOT_SET) {
            return getDescription();
        }
        return getDescription() + " [" + apiErrorCode + "]";
    }

    public static @NonNull Error convert(Status status) {
        switch (status.getCode()) {
            case 404:
                return JsNoMessageFoundErr;
            case 408:
                return JsBadRequestErr;
        }
        return new Error(status.getCode(), NOT_SET, status.getMessage());
    }

    /**
     * Error representing 400 / 10003 / "bad request"
     */
    public static final @NonNull Error JsBadRequestErr = new Error(400, 10003, "bad request");

    /**
     * Error representing 404 / 10037 / "no message found"
     */
    public static final @NonNull Error JsNoMessageFoundErr = new Error(404, 10037, "no message found");
}

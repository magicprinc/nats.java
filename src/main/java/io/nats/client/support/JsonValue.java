// Copyright 2023-2025 The NATS Authors
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

package io.nats.client.support;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static io.nats.client.support.JsonUtils.addField;
import static io.nats.client.support.JsonUtils.beginArray;
import static io.nats.client.support.JsonUtils.beginJson;
import static io.nats.client.support.JsonUtils.endArray;
import static io.nats.client.support.JsonUtils.endJson;

@SuppressWarnings("unchecked")
public interface JsonValue extends JsonSerializable {
    String NULL_STR = "null";

    JsonValue NULL = new JsonValue(){
        @Override public @Nullable Object value() { return null; }
        @Override public @NonNull String toJson() { return NULL_STR; }
        @Override public int size() { return 0; }
        @Override public boolean equals(Object o) { return this == o; }
        @Override public int hashCode() { return 0; }
        @Override public String toString() { return toJson(); }
    };
    JVBool TRUE = new JsonValue.JVBool(true);
    JVBool FALSE = new JsonValue.JVBool(false);
    JVArray EMPTY_ARRAY = new JVArray(Collections.emptyList());
    JVMap EMPTY_MAP = new JVMap(Collections.emptyMap());

    <T> T value();
    @Override @NonNull String toJson();
    default int size() { return 1; }
    default boolean isEmpty() { return size() <= 0; }
    default Set<String> keySet() { return Collections.emptySet(); }
    default Set<Map.Entry<String,JsonValue>> entrySet() { return Collections.emptySet(); }
    default void remove(String key) {}

    default String toString(Class<?> c) {
        return toString(c.getSimpleName());
    }

    default String toString(String key) {
        return '"' + key + "\":" + toJson();
    }

    @Override default @NonNull JsonValue toJsonValue() { return this; }

    default @Nullable Boolean bool() {
        return value() instanceof Boolean ? (Boolean)value() : null;
    }

    default @Nullable String string() {
        return value() instanceof String ? (String)value() : null;
    }

    default @Nullable Integer i() {
        return value() instanceof Integer ? (Integer) value() : null;
    }

    default @Nullable Long l() {
        return value() instanceof Long ? (Long) value() : null;
    }

    default @Nullable Double d() {
        return value() instanceof Double ? (Double) value() : null;
    }

    default @Nullable Float f() {
        return value() instanceof Float ? (Float) value() : null;
    }

    default @Nullable BigDecimal bd() {
        return value() instanceof BigDecimal ? (BigDecimal) value() : null;
    }

    default @Nullable BigInteger bi() {
        return value() instanceof BigInteger ? (BigInteger) value() : null;
    }

    default @Nullable List<@NonNull JsonValue> array() {
        return value() instanceof List ? (List<JsonValue>) value() : null;
    }

    default @Nullable Map<String,JsonValue> map() {
        return value() instanceof Map ? (Map<String,JsonValue>) value() : null;
    }
    

    static JsonValue of(@Nullable Boolean v) {
        return v == null ? NULL
                : v ? TRUE : FALSE;
    }

    static JsonValue of(@Nullable String v) {
        return v == null ? NULL
                : new JVString(v);
    }

    // char ?

    static JVInt of(int v) { return new JVInt(v); }

    static JVLong of(long v) { return new JVLong(v); }

    static JVDouble of(double v) { return new JVDouble(v); }

    static JVFloat of(float v) { return new JVFloat(v); }

    static JsonValue of(@Nullable BigDecimal v) {
        return v == null ? NULL
                : new JVBigDecimal(v);
    }

    static JsonValue of(@Nullable BigInteger v) {
        return v == null ? NULL
                : new JVBigInteger(v);
    }

    static JsonValue of(@Nullable Map<String,JsonValue> v) {
        return v == null ? NULL
                : new JVMap(v);
    }

    static JsonValue of(@Nullable List<JsonValue> list) {
        return list == null ? NULL
            : new JVArray(list);
    }

    static JsonValue of(JsonValue[] values) {
        return values == null ? NULL
            : new JVArray(Arrays.asList(values));
    }


    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    final class JVBool implements JsonValue {
        public final boolean bool;
        @Override public Boolean value() { return bool ? Boolean.TRUE : Boolean.FALSE; }
        @Override public @NonNull String toJson() { return bool ? "true" : "false"; }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof JVBool)) return false;
            JVBool jv = (JVBool) o;
            return bool == jv.bool;
        }
        @Override public int hashCode() { return bool ? 1 : 0; }
        @Override public String toString() { return toJson(); }
    }

    @Getter(onMethod_=@Override)  @Accessors(fluent = true)
    final class JVString extends JVObj<String> {
        public JVString(@NonNull String value) { super(value); }
        @Override public @NonNull String toJson() { return '"' + Encoding.jsonEncode(value()) + '"'; }
        @Override public boolean isEmpty() { return value.isEmpty(); }
    }

    @Getter(onMethod_=@Override)  @Accessors(fluent = true)
    final class JVBigDecimal extends JVObj<BigDecimal> {
        public JVBigDecimal(@NonNull BigDecimal value) { super(value); }
    }

    @Getter(onMethod_=@Override)  @Accessors(fluent = true)
    final class JVBigInteger extends JVObj<BigInteger> {
        public JVBigInteger(@NonNull BigInteger value) { super(value); }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    final class JVInt implements JsonValue {
        public final int value;
        @Override public Integer value() { return value; }
        @Override public @NonNull String toJson() { return Integer.toString(value); }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof JsonValue)) return false;
            JsonValue jv = (JsonValue) o;
            return Objects.equals(value(), jv.value());
        }
        @Override public int hashCode() { return value; }
        @Override public String toString() { return toJson(); }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    final class JVLong implements JsonValue {
        public final long value;
        @Override public Long value() { return value; }
        @Override public @NonNull String toJson() { return Long.toString(value); }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof JsonValue)) return false;
            JsonValue jv = (JsonValue) o;
            return Objects.equals(value(), jv.value());
        }
        @Override public int hashCode() { return Long.hashCode(value); }
        @Override public String toString() { return toJson(); }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    final class JVDouble implements JsonValue {
        public final double value;
        @Override public Double value() { return value; }
        @Override public @NonNull String toJson() { return Double.toString(value); }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof JsonValue)) return false;
            JsonValue jv = (JsonValue) o;
            return Objects.equals(value(), jv.value());
        }
        @Override public int hashCode() { return Double.hashCode(value); }
        @Override public String toString() { return toJson(); }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    final class JVFloat implements JsonValue {
        public final float value;
        @Override public Float value() { return value; }
        @Override public @NonNull String toJson() { return Float.toString(value); }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof JsonValue)) return false;
            JsonValue jv = (JsonValue) o;
            return Objects.equals(value(), jv.value());
        }
        @Override public int hashCode() { return Float.hashCode(value); }
        @Override public String toString() { return toJson(); }
    }

    final class JVMap extends JVObj<Map<String,JsonValue>> {
        public final ArrayList<String> mapOrder = new ArrayList<>();

        private JVMap(Map<String, JsonValue> value) { super(value); }// not thread safe

        public static JVMap create() { return new JVMap(new HashMap<>()); }

        @Override public int size() { return value.size(); }

        @Override
        public @NonNull String toJson() {
            StringBuilder sbo = beginJson();
            if (!mapOrder.isEmpty()) {
                for (String key : mapOrder) {
                    addField(sbo, key, value.get(key));
                }
            } else {
                for (Map.Entry<String, JsonValue> entry : value.entrySet()) {
                    addField(sbo, entry.getKey(), entry.getValue());
                }
            }
            return endJson(sbo).toString();
        }

        @Override public Set<String> keySet() { return value.keySet(); }

        @Override public Set<Map.Entry<String, JsonValue>> entrySet() { return value.entrySet(); }

        public void put(String key, JsonValue v) {
            value.put(key, v);
        }

        @Override
        public void remove(String key) {
            value.remove(key);
        }
    }

    final class JVArray extends JVObj<List<JsonValue>> {
        private JVArray(List<JsonValue> value) { super(value); }// not thread safe

        @Override public int size() { return value.size(); }

        public static JVArray create () { return new JVArray(new ArrayList<>()); }

        @Override
        public @NonNull String toJson() {
            StringBuilder sba = beginArray(value.size());
            for (JsonValue v : value) {
                sba.append(v.toJson());
                sba.append(',');
            }
            return endArray(sba).toString();
        }

        public void add (@Nullable JsonValue item) {
            if (item != null) {
                value.add(item);
            }
        }
    }
}
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter(onMethod_=@Override)  @Accessors(fluent = true)
class JVObj<V> implements JsonValue {
    public final @NonNull V value;
    @Override public @NonNull String toJson() { return value().toString(); }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JsonValue)) return false;
        JsonValue jv = (JsonValue) o;
        return Objects.equals(value(), jv.value());
    }
    @Override public int hashCode() { return value().hashCode(); }
    @Override public String toString() { return toJson(); }
}

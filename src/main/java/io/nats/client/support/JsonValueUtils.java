// Copyright 2023 The NATS Authors
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
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static io.nats.client.support.Encoding.base64BasicDecode;
import static io.nats.client.support.JsonValue.EMPTY_MAP;
import static io.nats.client.support.JsonValue.JVArray;
import static io.nats.client.support.JsonValue.JVMap;

/**
 * Internal json value helpers.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonValueUtils {

    @FunctionalInterface
    public interface JsonValueSupplier<T> {
        T get(JsonValue v);
    }

    public static <T> T read(@Nullable JsonValue jsonValue, String key, JsonValueSupplier<T> valueSupplier) {
        JsonValue v = jsonValue instanceof JVMap ? ((JVMap) jsonValue).value.get(key) : null;
        return valueSupplier.get(v);
    }

    public static JsonValue readValue(JsonValue jsonValue, String key) {
        return read(jsonValue, key, v -> v);
    }

    public static JsonValue readObject(JsonValue jsonValue, String key) {
        return read(jsonValue, key, v -> v == null ? EMPTY_MAP : v);
    }

    public static List<JsonValue> readArray(JsonValue jsonValue, String key) {
        return read(jsonValue, key, v -> v instanceof JVArray ? ((JVArray)v).value : Collections.emptyList());
    }

    public static @Nullable Map<String, String> readStringStringMap(JsonValue jv, String key) {
        JsonValue o = readObject(jv, key);
        if (o instanceof JVMap && o.size() > 0) {
            Map<String, String> temp = new HashMap<>();
            for (String k : o.keySet()) {
                String value = readString(o, k);
                if (value != null) {
                    temp.put(k, value);
                }
            }
            return temp.isEmpty() ? null : temp;
        }
        return null;
    }

    public static String readString(JsonValue jsonValue, String key) {
        return read(jsonValue, key, v -> v == null ? null : v.string());
    }

    public static String readStringEmptyAsNull(JsonValue jsonValue, String key) {
        return read(jsonValue, key, v -> v == null ? null : (v.isEmpty() ? null : v.string()));
    }

    public static String readString(JsonValue jsonValue, String key, String dflt) {
        return read(jsonValue, key, v -> v == null ? dflt : v.string());
    }

    public static ZonedDateTime readDate(JsonValue jsonValue, String key) {
        return read(jsonValue, key,
            v -> v == null || v.string() == null ? null : DateTimeUtils.parseDateTimeThrowParseError(v.string()));
    }

    public static Integer readInteger(JsonValue jsonValue, String key) {
        return read(jsonValue, key, v -> v == null ? null : getInteger(v));
    }

    public static int readInteger(JsonValue jsonValue, String key, int dflt) {
        return read(jsonValue, key, v -> {
            if (v != null) {
                Integer i = getInteger(v);
                if (i != null) {
                    return i;
                }
            }
            return dflt;
        });
    }

    public static Long readLong(JsonValue jsonValue, String key) {
        return read(jsonValue, key, v -> v == null ? null : getLong(v));
    }

    public static long readLong(JsonValue jsonValue, String key, long dflt) {
        return read(jsonValue, key, v -> {
            if (v != null) {
                Long l = getLong(v);
                if (l != null) {
                    return l;
                }
            }
            return dflt;
        });
    }

    public static boolean readBoolean(JsonValue jsonValue, String key) {
        return readBoolean(jsonValue, key, false);
    }

    public static Boolean readBoolean(JsonValue jsonValue, String key, Boolean dflt) {
        return read(jsonValue, key,
            v -> v == null || v.bool() == null ? dflt : v.bool());
    }

    public static @Nullable Duration readNanos(JsonValue jsonValue, String key) {
        Long l = readLong(jsonValue, key);
        return l == null ? null : Duration.ofNanos(l);
    }

    public static Duration readNanos(JsonValue jsonValue, String key, Duration dflt) {
        Long l = readLong(jsonValue, key);
        return l == null ? dflt : Duration.ofNanos(l);
    }

    public static <T> List<T> listOf(JsonValue v, Function<@NonNull JsonValue, T> provider) {
        ArrayList<T> list = new ArrayList<>();
        if (v instanceof JVArray) {
            list.ensureCapacity(v.size());
            for (JsonValue jv : ((JVArray)v).value) {
                T t = provider.apply(jv);
                if (t != null) {
                    list.add(t);
                }
            }
        }
        return list;
    }

    public static <T> @Nullable List<T> optionalListOf(JsonValue v, Function<@NonNull JsonValue, T> provider) {
        List<T> list = listOf(v, provider);
        return list.isEmpty() ? null : list;
    }

    public static List<String> readStringList(JsonValue jsonValue, String key) {
        return read(jsonValue, key, v -> listOf(v, JsonValue::string));
    }

    public static List<String> readStringListIgnoreEmpty(JsonValue jsonValue, String key) {
        return read(jsonValue, key, v -> listOf(v, jv -> {
            String s = jv.string();
            if (s != null) {
                s = s.trim();
                if (!s.isEmpty()) {
                    return s;
                }
            }
            return null;
        }));
    }

    public static List<String> readOptionalStringList(JsonValue jsonValue, String key) {
        return read(jsonValue, key, v -> optionalListOf(v, JsonValue::string));
    }

    public static List<Long> readLongList(JsonValue jsonValue, String key) {
        return read(jsonValue, key, v -> listOf(v, JsonValueUtils::getLong));
    }
    public static @Nullable List<Duration> readNanosList(JsonValue jsonValue, String key) {
        return readNanosList(jsonValue, key, false);
    }

    public static @Nullable List<Duration> readNanosList(JsonValue jsonValue, String key, boolean nullIfEmpty) {
        List<Duration> list = read(jsonValue, key,
            v -> listOf(v, vv -> {
                Long l = getLong(vv);
                return l == null ? null : Duration.ofNanos(l);
            })
        );
        return list.isEmpty() && nullIfEmpty ? null : list;
    }

    public static byte @Nullable [] readBytes(JsonValue jsonValue, String key) {
        String s = readString(jsonValue, key);
        return s == null ? null : s.getBytes(StandardCharsets.UTF_8);
    }

    public static byte @Nullable [] readBase64(JsonValue jsonValue, String key) {
        String b64 = readString(jsonValue, key);
        return b64 == null ? null : base64BasicDecode(b64);
    }

    public static @Nullable Integer getInteger(JsonValue v) {
        if (v.i() != null) {
            return v.i();
        }
        // just in case the number was stored as a long, which is unlikely, but I want to handle it
        Long l = v.l();
        if (l != null && l <= Integer.MAX_VALUE && l >= Integer.MIN_VALUE) {
            return l.intValue();
        }
        return null;
    }

    public static @Nullable Long getLong(JsonValue v) {
        return v.l() != null ? v.l() 
                : (v.i() != null ? v.i().longValue() : null);
    }

    public static long getLong(JsonValue v, long dflt) {
        return v.l() != null ? v.l() 
                : (v.i() != null ? v.i().longValue() : dflt);
    }

    public static JsonValue instance(Duration d) {
        return JsonValue.of(d.toNanos());
    }

    @SuppressWarnings("rawtypes")
    public static JsonValue instance(Collection list) {
        JVArray v = JVArray.create();
        for (Object o : list) {
            v.add(toJsonValue(o));
        }
        return v;
    }

    @SuppressWarnings("rawtypes")
    public static JsonValue instance(Map<?,?> map) {
        JVMap v = JVMap.create();
        for (Map.Entry entry : map.entrySet()) {
            v.put(entry.getKey().toString(), toJsonValue(entry.getValue()));
        }
        return v;
    }

    public static JsonValue toJsonValue(Object o) {
        if (o == null) {
            return JsonValue.NULL;
        }
        if (o instanceof JsonValue) {
            return (JsonValue)o;
        }
        if (o instanceof JsonSerializable) {
            return ((JsonSerializable)o).toJsonValue();
        }
        if (o instanceof Map) {
            //noinspection unchecked,rawtypes
            return JsonValue.of((Map)o);
        }
        if (o instanceof List) {
            //noinspection unchecked,rawtypes
            return JsonValue.of((List)o);
        }
        if (o instanceof Set) {
            //noinspection unchecked,rawtypes
            return JsonValue.of(new ArrayList<>((Set)o));
        }
        if (o instanceof String) {
            String s = ((String)o).trim();
            return s.isEmpty() ? JsonValue.NULL : JsonValue.of(s);
        }
        if (o instanceof Boolean) {
            return JsonValue.of((Boolean)o);
        }
        if (o instanceof Integer) {
            return JsonValue.of((Integer)o);
        }
        if (o instanceof Long) {
            return JsonValue.of((Long)o);
        }
        if (o instanceof Double) {
            return JsonValue.of((Double)o);
        }
        if (o instanceof Float) {
            return JsonValue.of((Float)o);
        }
        if (o instanceof BigDecimal) {
            return JsonValue.of((BigDecimal)o);
        }
        if (o instanceof BigInteger) {
            return JsonValue.of((BigInteger)o);
        }
        return JsonValue.of(o.toString());
    }

    public static class MapBuilder implements JsonSerializable {
        public final JVMap jv;

        public MapBuilder() {
            jv = JVMap.create();
        }

        public MapBuilder(JVMap jv) {
            this.jv = jv;
        }

        public MapBuilder put(String s, @Nullable Object o) {
            if (o != null) {
                JsonValue vv = JsonValueUtils.toJsonValue(o);
                if (vv.value() != null) {
                    jv.put(s, vv);
                    jv.mapOrder.add(s);
                }
            }
            return this;
        }

        public MapBuilder put(String s, @Nullable Map<String, String> stringMap) {
            if (stringMap != null) {
                MapBuilder mb = new MapBuilder();
                for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                    mb.put(entry.getKey(), entry.getValue());
                }
                jv.put(s, mb.jv);
                jv.mapOrder.add(s);
            }
            return this;
        }

        @Override public @NonNull String toJson() { return jv.toJson(); }

        @Override public @NonNull JVMap toJsonValue() { return jv; }

        @Deprecated public JVMap getJsonValue() { return jv; }
    }

    public static class ArrayBuilder implements JsonSerializable {
        public final JVArray jv = JVArray.create();

        public ArrayBuilder add(@Nullable Object o) {
            if (o != null) {
                JsonValue v = JsonValueUtils.toJsonValue(o);
                if (v.value() != null) {
                    jv.add(v);
                }
            }
            return this;
        }

        @Override public @NonNull String toJson() { return jv.toJson(); }

        @Override public @NonNull JVArray toJsonValue() { return jv; }

        @Deprecated public JVArray getJsonValue() { return jv; }
    }
}

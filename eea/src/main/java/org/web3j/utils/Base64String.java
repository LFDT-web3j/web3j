/*
 * Copyright 2019 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.web3j.utils;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;

@JsonSerialize(using = Base64String.Serializer.class)
@JsonDeserialize(using = Base64String.Deserializer.class)
public class Base64String {

    public static class Serializer extends ValueSerializer<Base64String> {
        @Override
        public void serialize(
                final Base64String value,
                final JsonGenerator gen,
                final SerializationContext serializers) {
            gen.writeString(value.toString());
        }
    }

    public static class Deserializer extends ValueDeserializer<Base64String> {
        @Override
        public Base64String deserialize(final JsonParser p, final DeserializationContext ctxt) {
            return Base64String.wrap(p.getValueAsString());
        }
    }

    private static final Base64.Decoder DECODER = Base64.getDecoder();
    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    private final byte[] enclaveB64Value;

    public static Base64String wrap(final String base64String) {
        return new Base64String(base64String);
    }

    public static Base64String wrap(final byte[] base64Array) {
        return new Base64String(ENCODER.encodeToString(base64Array));
    }

    public static List<Base64String> wrapList(final List<String> base64Strings) {
        return base64Strings.stream().map(Base64String::wrap).collect(Collectors.toList());
    }

    public static List<Base64String> wrapList(final String... base64Strings) {
        return Arrays.stream(base64Strings).map(Base64String::wrap).collect(Collectors.toList());
    }

    public static List<String> unwrapList(final List<Base64String> base64Strings) {
        return base64Strings.stream().map(Base64String::toString).collect(Collectors.toList());
    }

    public static RlpList unwrapListToRlp(final List<Base64String> base64Strings) {
        return new RlpList(
                base64Strings.stream().map(Base64String::asRlp).collect(Collectors.toList()));
    }

    private Base64String(final String base64String) {
        if (!isValid(base64String)) {
            throw new IllegalArgumentException(base64String + " is not a valid base 64 value");
        }
        this.enclaveB64Value = DECODER.decode(base64String);
    }

    public String toString() {
        return ENCODER.encodeToString(enclaveB64Value);
    }

    public byte[] raw() {
        return enclaveB64Value;
    }

    public RlpString asRlp() {
        return RlpString.create(raw());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Base64String that = (Base64String) o;
        return Arrays.equals(enclaveB64Value, that.enclaveB64Value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(enclaveB64Value);
    }

    private boolean isValid(final String b64String) {
        //   ([0-9a-zA-Z+/]{4})*      # Groups of 4 valid characters decode
        //                            # to 24 bits of data for each group
        //   (                        # ending with either:
        //   ([A-Za-z0-9+/]{2}==)     # two valid characters followed by ==
        //   ([0-9a-zA-Z+/]{3}=)      # OR three valid characters followed by =
        return b64String.matches("(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)");
    }
}

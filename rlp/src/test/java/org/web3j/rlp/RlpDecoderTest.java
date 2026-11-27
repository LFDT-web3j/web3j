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
package org.web3j.rlp;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import org.web3j.utils.Numeric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RlpDecoderTest {

    /**
     * Examples taken from https://github.com/ethereum/wiki/wiki/RLP#examples. For further examples
     * see https://github.com/ethereum/tests/tree/develop/RLPTests.
     */
    @Test
    public void testRLPDecode() {

        // big positive number should stay positive after encoding-decoding
        // https://github.com/web3j/web3j/issues/562
        long value = 3000000000L;
        assertEquals(
                RlpString.create(BigInteger.valueOf(value)).asPositiveBigInteger().longValue(),
                (value));

        // empty array of binary
        assertTrue(RlpDecoder.decode(new byte[] {}).getValues().isEmpty());

        // The string "dog" = [ 0x83, 'd', 'o', 'g' ]
        assertEquals(
                RlpDecoder.decode(new byte[] {(byte) 0x83, 'd', 'o', 'g'}).getValues().get(0),
                (RlpString.create("dog")));

        // The list [ "cat", "dog" ] = [ 0xc8, 0x83, 'c', 'a', 't', 0x83, 'd', 'o', 'g' ]
        RlpList rlpList =
                (RlpList)
                        RlpDecoder.decode(
                                        new byte[] {
                                            (byte) 0xc8,
                                            (byte) 0x83,
                                            'c',
                                            'a',
                                            't',
                                            (byte) 0x83,
                                            'd',
                                            'o',
                                            'g'
                                        })
                                .getValues()
                                .get(0);

        assertEquals(rlpList.getValues().get(0), (RlpString.create("cat")));

        assertEquals(rlpList.getValues().get(1), (RlpString.create("dog")));

        // The empty string ('null') = [ 0x80 ]
        assertEquals(
                RlpDecoder.decode(new byte[] {(byte) 0x80}).getValues().get(0),
                (RlpString.create("")));

        assertEquals(
                RlpDecoder.decode(new byte[] {(byte) 0x80}).getValues().get(0),
                (RlpString.create(new byte[] {})));

        assertEquals(
                RlpDecoder.decode(new byte[] {(byte) 0x80}).getValues().get(0),
                (RlpString.create(BigInteger.ZERO)));

        // The empty list = [ 0xc0 ]
        assertEquals(
                RlpDecoder.decode(new byte[] {(byte) 0xc0}).getValues().get(0).getClass(),
                (RlpList.class));

        assertTrue(
                ((RlpList) RlpDecoder.decode(new byte[] {(byte) 0xc0}).getValues().get(0))
                        .getValues()
                        .isEmpty());

        // The encoded integer 0 ('\x00') = [ 0x00 ]
        assertEquals(
                RlpDecoder.decode(new byte[] {(byte) 0x00}).getValues().get(0),
                (RlpString.create(BigInteger.valueOf(0).byteValue())));

        // The encoded integer 15 ('\x0f') = [ 0x0f ]
        assertEquals(
                RlpDecoder.decode(new byte[] {(byte) 0x0f}).getValues().get(0),
                (RlpString.create(BigInteger.valueOf(15).byteValue())));

        // The encoded integer 1024 ('\x04\x00') = [ 0x82, 0x04, 0x00 ]
        assertEquals(
                RlpDecoder.decode(new byte[] {(byte) 0x82, (byte) 0x04, (byte) 0x00})
                        .getValues()
                        .get(0),
                (RlpString.create(BigInteger.valueOf(0x0400))));

        // The set theoretical representation of three,
        // [ [], [[]], [ [], [[]] ] ] = [ 0xc7, 0xc0, 0xc1, 0xc0, 0xc3, 0xc0, 0xc1, 0xc0 ]
        rlpList =
                RlpDecoder.decode(
                        new byte[] {
                            (byte) 0xc7,
                            (byte) 0xc0,
                            (byte) 0xc1,
                            (byte) 0xc0,
                            (byte) 0xc3,
                            (byte) 0xc0,
                            (byte) 0xc1,
                            (byte) 0xc0
                        });
        assertEquals(rlpList.getClass(), RlpList.class);

        assertEquals(rlpList.getValues().size(), (1));

        assertEquals(rlpList.getValues().get(0).getClass(), (RlpList.class));

        assertEquals(((RlpList) rlpList.getValues().get(0)).getValues().size(), (3));

        assertEquals(
                ((RlpList) rlpList.getValues().get(0)).getValues().get(0).getClass(),
                RlpList.class);

        assertEquals(
                ((RlpList) ((RlpList) rlpList.getValues().get(0)).getValues().get(0))
                        .getValues()
                        .size(),
                (0));

        assertEquals(
                ((RlpList) ((RlpList) rlpList.getValues().get(0)).getValues().get(1))
                        .getValues()
                        .size(),
                (1));

        assertEquals(
                ((RlpList) ((RlpList) rlpList.getValues().get(0)).getValues().get(2))
                        .getValues()
                        .size(),
                (2));

        assertEquals(
                ((RlpList) ((RlpList) rlpList.getValues().get(0)).getValues().get(2))
                        .getValues()
                        .get(0)
                        .getClass(),
                (RlpList.class));

        assertEquals(
                ((RlpList)
                                ((RlpList)
                                                ((RlpList) rlpList.getValues().get(0))
                                                        .getValues()
                                                        .get(2))
                                        .getValues()
                                        .get(0))
                        .getValues()
                        .size(),
                (0));

        assertEquals(
                ((RlpList)
                                ((RlpList)
                                                ((RlpList) rlpList.getValues().get(0))
                                                        .getValues()
                                                        .get(2))
                                        .getValues()
                                        .get(1))
                        .getValues()
                        .size(),
                (1));

        // The string "Lorem ipsum dolor sit amet,
        // consectetur adipisicing elit" =
        // [ 0xb8, 0x38, 'L', 'o', 'r', 'e', 'm', ' ', ... , 'e', 'l', 'i', 't' ]

        assertEquals(
                RlpDecoder.decode(
                                new byte[] {
                                    (byte) 0xb8,
                                    (byte) 0x38,
                                    'L',
                                    'o',
                                    'r',
                                    'e',
                                    'm',
                                    ' ',
                                    'i',
                                    'p',
                                    's',
                                    'u',
                                    'm',
                                    ' ',
                                    'd',
                                    'o',
                                    'l',
                                    'o',
                                    'r',
                                    ' ',
                                    's',
                                    'i',
                                    't',
                                    ' ',
                                    'a',
                                    'm',
                                    'e',
                                    't',
                                    ',',
                                    ' ',
                                    'c',
                                    'o',
                                    'n',
                                    's',
                                    'e',
                                    'c',
                                    't',
                                    'e',
                                    't',
                                    'u',
                                    'r',
                                    ' ',
                                    'a',
                                    'd',
                                    'i',
                                    'p',
                                    'i',
                                    's',
                                    'i',
                                    'c',
                                    'i',
                                    'n',
                                    'g',
                                    ' ',
                                    'e',
                                    'l',
                                    'i',
                                    't'
                                })
                        .getValues()
                        .get(0),
                (RlpString.create("Lorem ipsum dolor sit amet, consectetur adipisicing elit")));

        // https://github.com/paritytech/parity/blob/master/util/rlp/tests/tests.rs#L239
        assertEquals(
                RlpDecoder.decode(new byte[] {(byte) 0x00}).getValues().get(0),
                (RlpString.create(new byte[] {0})));

        rlpList =
                RlpDecoder.decode(
                        new byte[] {
                            (byte) 0xc6,
                            (byte) 0x82,
                            (byte) 0x7a,
                            (byte) 0x77,
                            (byte) 0xc1,
                            (byte) 0x04,
                            (byte) 0x01
                        });

        assertEquals(((RlpList) rlpList.getValues().get(0)).getValues().size(), (3));

        assertEquals(
                ((RlpList) rlpList.getValues().get(0)).getValues().get(0).getClass(),
                (RlpString.class));

        assertEquals(
                ((RlpList) rlpList.getValues().get(0)).getValues().get(1).getClass(),
                (RlpList.class));

        assertEquals(
                ((RlpList) rlpList.getValues().get(0)).getValues().get(2).getClass(),
                (RlpString.class));

        assertEquals(
                ((RlpList) rlpList.getValues().get(0)).getValues().get(0),
                (RlpString.create("zw")));

        assertEquals(
                ((RlpList) ((RlpList) rlpList.getValues().get(0)).getValues().get(1))
                        .getValues()
                        .get(0),
                (RlpString.create(4)));

        assertEquals(
                ((RlpList) rlpList.getValues().get(0)).getValues().get(2), (RlpString.create(1)));

        // payload more than 55 bytes
        String data =
                "F86E12F86B80881BC16D674EC8000094CD2A3D9F938E13CD947EC05ABC7FE734D"
                        + "F8DD8268609184E72A00064801BA0C52C114D4F5A3BA904A9B3036E5E118FE0DBB987"
                        + "FE3955DA20F2CD8F6C21AB9CA06BA4C2874299A55AD947DBC98A25EE895AABF6B625C"
                        + "26C435E84BFD70EDF2F69";

        byte[] payload = Numeric.hexStringToByteArray(data);
        rlpList = RlpDecoder.decode(payload);

        assertEquals(((RlpList) rlpList.getValues().get(0)).getValues().size(), (2));

        assertEquals(
                ((RlpList) rlpList.getValues().get(0)).getValues().get(0).getClass(),
                (RlpString.class));

        assertEquals(
                ((RlpList) rlpList.getValues().get(0)).getValues().get(1).getClass(),
                (RlpList.class));

        assertEquals(
                ((RlpList) ((RlpList) rlpList.getValues().get(0)).getValues().get(1))
                        .getValues()
                        .size(),
                (9));

        // Regression test: this would previously throw OutOfMemoryError as it tried to allocate 2GB
        // for the non-existent data
        assertThrows(
                RuntimeException.class,
                (() ->
                        RlpDecoder.decode(
                                new byte[] {
                                    (byte) 0xbb, (byte) 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff
                                })));
    }

    @Test
    public void testNestedListExceedingParentLengthIsRejected() {
        // outer list declares a 2 byte payload, but the nested list claims 3 bytes,
        // reaching past the end of its parent: [0xc2, 0xc3, 0x01, 0x02, 0x03]
        assertThrows(
                RuntimeException.class,
                () ->
                        RlpDecoder.decode(
                                new byte[] {
                                    (byte) 0xc2, (byte) 0xc3, (byte) 0x01, (byte) 0x02, (byte) 0x03
                                }));

        // same for a long list nested in a short list
        byte[] longList = new byte[60];
        longList[0] = (byte) 0xc2;
        longList[1] = (byte) 0xf8;
        longList[2] = (byte) 56;
        assertThrows(RuntimeException.class, () -> RlpDecoder.decode(longList));
    }
}

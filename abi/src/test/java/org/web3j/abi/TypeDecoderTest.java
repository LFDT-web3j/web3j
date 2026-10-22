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
package org.web3j.abi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Bytes;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Int;
import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes1;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Bytes4;
import org.web3j.abi.datatypes.generated.Bytes6;
import org.web3j.abi.datatypes.generated.Int104;
import org.web3j.abi.datatypes.generated.Int112;
import org.web3j.abi.datatypes.generated.Int120;
import org.web3j.abi.datatypes.generated.Int128;
import org.web3j.abi.datatypes.generated.Int136;
import org.web3j.abi.datatypes.generated.Int144;
import org.web3j.abi.datatypes.generated.Int152;
import org.web3j.abi.datatypes.generated.Int16;
import org.web3j.abi.datatypes.generated.Int160;
import org.web3j.abi.datatypes.generated.Int168;
import org.web3j.abi.datatypes.generated.Int176;
import org.web3j.abi.datatypes.generated.Int184;
import org.web3j.abi.datatypes.generated.Int192;
import org.web3j.abi.datatypes.generated.Int200;
import org.web3j.abi.datatypes.generated.Int208;
import org.web3j.abi.datatypes.generated.Int216;
import org.web3j.abi.datatypes.generated.Int224;
import org.web3j.abi.datatypes.generated.Int232;
import org.web3j.abi.datatypes.generated.Int24;
import org.web3j.abi.datatypes.generated.Int240;
import org.web3j.abi.datatypes.generated.Int248;
import org.web3j.abi.datatypes.generated.Int256;
import org.web3j.abi.datatypes.generated.Int32;
import org.web3j.abi.datatypes.generated.Int40;
import org.web3j.abi.datatypes.generated.Int48;
import org.web3j.abi.datatypes.generated.Int56;
import org.web3j.abi.datatypes.generated.Int64;
import org.web3j.abi.datatypes.generated.Int72;
import org.web3j.abi.datatypes.generated.Int8;
import org.web3j.abi.datatypes.generated.Int80;
import org.web3j.abi.datatypes.generated.Int88;
import org.web3j.abi.datatypes.generated.Int96;
import org.web3j.abi.datatypes.generated.StaticArray1;
import org.web3j.abi.datatypes.generated.StaticArray2;
import org.web3j.abi.datatypes.generated.StaticArray3;
import org.web3j.abi.datatypes.generated.Uint104;
import org.web3j.abi.datatypes.generated.Uint112;
import org.web3j.abi.datatypes.generated.Uint120;
import org.web3j.abi.datatypes.generated.Uint128;
import org.web3j.abi.datatypes.generated.Uint136;
import org.web3j.abi.datatypes.generated.Uint144;
import org.web3j.abi.datatypes.generated.Uint152;
import org.web3j.abi.datatypes.generated.Uint16;
import org.web3j.abi.datatypes.generated.Uint160;
import org.web3j.abi.datatypes.generated.Uint168;
import org.web3j.abi.datatypes.generated.Uint176;
import org.web3j.abi.datatypes.generated.Uint184;
import org.web3j.abi.datatypes.generated.Uint192;
import org.web3j.abi.datatypes.generated.Uint200;
import org.web3j.abi.datatypes.generated.Uint208;
import org.web3j.abi.datatypes.generated.Uint216;
import org.web3j.abi.datatypes.generated.Uint224;
import org.web3j.abi.datatypes.generated.Uint232;
import org.web3j.abi.datatypes.generated.Uint24;
import org.web3j.abi.datatypes.generated.Uint240;
import org.web3j.abi.datatypes.generated.Uint248;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.abi.datatypes.generated.Uint40;
import org.web3j.abi.datatypes.generated.Uint48;
import org.web3j.abi.datatypes.generated.Uint56;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.abi.datatypes.generated.Uint72;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.abi.datatypes.generated.Uint80;
import org.web3j.abi.datatypes.generated.Uint88;
import org.web3j.abi.datatypes.generated.Uint96;
import org.web3j.utils.Numeric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypeDecoderTest {

    @Test
    public void testBoolDecode() throws Exception {
        assertEquals(
                TypeDecoder.decodeBool(
                        "0000000000000000000000000000000000000000000000000000000000000000", 0),
                (new Bool(false)));

        assertEquals(
                TypeDecoder.decodeBool(
                        "0000000000000000000000000000000000000000000000000000000000000001", 0),
                (new Bool(true)));

        assertEquals(TypeDecoder.instantiateType("bool", true), (new Bool(true)));

        assertEquals(TypeDecoder.instantiateType("bool", 1), (new Bool(true)));

        assertEquals(TypeDecoder.instantiateType("bool", false), (new Bool(false)));

        assertEquals(TypeDecoder.instantiateType("bool", 0), (new Bool(false)));
    }

    @Test
    public void testBoolDecodeGivenOffset() {
        // Decode second parameter as Bool
        assertEquals(
                TypeDecoder.decode(
                        "0000000000000000000000000000000000000000000000007fffffffffffffff"
                                + "0000000000000000000000000000000000000000000000000000000000000000"
                                + "0000000000000000000000000000000000000000000000007fffffffffffffff",
                        64,
                        Bool.class),
                (new Bool(false)));

        assertEquals(
                TypeDecoder.decode(
                        "0000000000000000000000000000000000000000000000007fffffffffffffff"
                                + "0000000000000000000000000000000000000000000000000000000000000001"
                                + "0000000000000000000000000000000000000000000000007fffffffffffffff",
                        64,
                        Bool.class),
                (new Bool(true)));
    }

    @Test
    public void testUintDecode() throws Exception {
        assertEquals(TypeDecoder.instantiateType("uint", 123), (new Uint(BigInteger.valueOf(123))));
        assertEquals(
                TypeDecoder.instantiateType("uint", 1.0e20), (new Uint(BigInteger.TEN.pow(20))));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint8.class),
                new Uint8(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "00000000000000000000000000000000000000000000000000000000000000ff",
                        Uint8.class),
                new Uint8(BigInteger.valueOf(255)));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint16.class),
                new Uint16(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "000000000000000000000000000000000000000000000000000000000000ffff",
                        Uint16.class),
                new Uint16(BigInteger.valueOf(65535)));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint24.class),
                new Uint24(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000ffffff",
                        Uint24.class),
                new Uint24(BigInteger.valueOf(16777215)));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint32.class),
                new Uint32(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "00000000000000000000000000000000000000000000000000000000ffffffff",
                        Uint32.class),
                new Uint32(BigInteger.valueOf(4294967295L)));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint40.class),
                new Uint40(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "000000000000000000000000000000000000000000000000000000ffffffffff",
                        Uint40.class),
                new Uint40(BigInteger.valueOf(1099511627775L)));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint48.class),
                new Uint48(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000ffffffffffff",
                        Uint48.class),
                new Uint48(BigInteger.valueOf(281474976710655L)));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint56.class),
                new Uint56(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "00000000000000000000000000000000000000000000000000ffffffffffffff",
                        Uint56.class),
                new Uint56(BigInteger.valueOf(72057594037927935L)));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint64.class),
                (new Uint64(BigInteger.ZERO)));

        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000007fffffffffffffff",
                        Uint64.class),
                (new Uint64(BigInteger.valueOf(Long.MAX_VALUE))));

        assertEquals(
                TypeDecoder.decodeNumeric(
                        "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                        Uint64.class),
                (new Uint64(new BigInteger("0ffffffffffffffff", 16))));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint72.class),
                new Uint72(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000ffffffffffffffffff",
                        Uint72.class),
                new Uint72(new BigInteger("4722366482869645213695")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint80.class),
                new Uint80(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "00000000000000000000000000000000000000000000ffffffffffffffffffff",
                        Uint80.class),
                new Uint80(new BigInteger("1208925819614629174706175")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint88.class),
                new Uint88(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "000000000000000000000000000000000000000000ffffffffffffffffffffff",
                        Uint88.class),
                new Uint88(new BigInteger("309485009821345068724781055")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint96.class),
                new Uint96(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000ffffffffffffffffffffffff",
                        Uint96.class),
                new Uint96(new BigInteger("79228162514264337593543950335")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint104.class),
                new Uint104(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "00000000000000000000000000000000000000ffffffffffffffffffffffffff",
                        Uint104.class),
                new Uint104(new BigInteger("20282409603651670423947251286015")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint112.class),
                new Uint112(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "000000000000000000000000000000000000ffffffffffffffffffffffffffff",
                        Uint112.class),
                new Uint112(new BigInteger("5192296858534827628530496329220095")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint120.class),
                new Uint120(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000ffffffffffffffffffffffffffffff",
                        Uint120.class),
                new Uint120(new BigInteger("1329227995784915872903807060280344575")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint128.class),
                new Uint128(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "00000000000000000000000000000000ffffffffffffffffffffffffffffffff",
                        Uint128.class),
                new Uint128(new BigInteger("340282366920938463463374607431768211455")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint136.class),
                new Uint136(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "000000000000000000000000000000ffffffffffffffffffffffffffffffffff",
                        Uint136.class),
                new Uint136(new BigInteger("87112285931760246646623899502532662132735")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint144.class),
                new Uint144(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000ffffffffffffffffffffffffffffffffffff",
                        Uint144.class),
                new Uint144(new BigInteger("22300745198530623141535718272648361505980415")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint152.class),
                new Uint152(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "00000000000000000000000000ffffffffffffffffffffffffffffffffffffff",
                        Uint152.class),
                new Uint152(new BigInteger("5708990770823839524233143877797980545530986495")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint160.class),
                new Uint160(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "000000000000000000000000ffffffffffffffffffffffffffffffffffffffff",
                        Uint160.class),
                new Uint160(new BigInteger("1461501637330902918203684832716283019655932542975")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint168.class),
                new Uint168(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000ffffffffffffffffffffffffffffffffffffffffff",
                        Uint168.class),
                new Uint168(new BigInteger("374144419156711147060143317175368453031918731001855")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint176.class),
                new Uint176(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "00000000000000000000ffffffffffffffffffffffffffffffffffffffffffff",
                        Uint176.class),
                new Uint176(
                        new BigInteger("95780971304118053647396689196894323976171195136475135")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint184.class),
                new Uint184(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "000000000000000000ffffffffffffffffffffffffffffffffffffffffffffff",
                        Uint184.class),
                new Uint184(
                        new BigInteger(
                                "24519928653854221733733552434404946937899825954937634815")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint192.class),
                new Uint192(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000ffffffffffffffffffffffffffffffffffffffffffffffff",
                        Uint192.class),
                new Uint192(
                        new BigInteger(
                                "6277101735386680763835789423207666416102355444464034512895")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint200.class),
                new Uint200(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "00000000000000ffffffffffffffffffffffffffffffffffffffffffffffffff",
                        Uint200.class),
                new Uint200(
                        new BigInteger(
                                "1606938044258990275541962092341162602522202993782792835301375")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint208.class),
                new Uint208(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "000000000000ffffffffffffffffffffffffffffffffffffffffffffffffffff",
                        Uint208.class),
                new Uint208(
                        new BigInteger(
                                "411376139330301510538742295639337626245683966408394965837152255")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint216.class),
                new Uint216(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000ffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                        Uint216.class),
                new Uint216(
                        new BigInteger(
                                "105312291668557186697918027683670432318895095400549111254310977535")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint224.class),
                new Uint224(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "00000000ffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                        Uint224.class),
                new Uint224(
                        new BigInteger(
                                "26959946667150639794667015087019630673637144422540572481103610249215")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint232.class),
                new Uint232(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "000000ffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                        Uint232.class),
                new Uint232(
                        new BigInteger(
                                "6901746346790563787434755862277025452451108972170386555162524223799295")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint240.class),
                new Uint240(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                        Uint240.class),
                new Uint240(
                        new BigInteger(
                                "1766847064778384329583297500742918515827483896875618958121606201292619775")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint248.class),
                new Uint248(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "00ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                        Uint248.class),
                new Uint248(
                        new BigInteger(
                                "452312848583266388373324160190187140051835877600158453279131187530910662655")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Uint256.class),
                new Uint256(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                        Uint256.class),
                new Uint256(
                        new BigInteger(
                                "115792089237316195423570985008687907853269984665640564039457584007913129639935")));

        assertEquals(TypeDecoder.instantiateType("uint", 123), (new Uint(BigInteger.valueOf(123))));
        assertEquals(
                TypeDecoder.instantiateType("uint", 1.0e20), (new Uint(BigInteger.TEN.pow(20))));
    }

    @Test
    public void testIntDecode() throws Exception {
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int8.class),
                new Int8(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "000000000000000000000000000000000000000000000000000000000000007f",
                        Int8.class),
                new Int8(BigInteger.valueOf(127)));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int16.class),
                new Int16(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000007FFF",
                        Int16.class),
                new Int16(BigInteger.valueOf(32767)));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int24.class),
                new Int24(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "00000000000000000000000000000000000000000000000000000000007fffff",
                        Int24.class),
                new Int24(BigInteger.valueOf(8388607)));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int32.class),
                new Int32(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "000000000000000000000000000000000000000000000000000000007fffffff",
                        Int32.class),
                new Int32(BigInteger.valueOf(2147483647)));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int40.class),
                new Int40(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000007FFFFFFFFF",
                        Int40.class),
                new Int40(BigInteger.valueOf(549755813887L)));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int48.class),
                new Int48(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "00000000000000000000000000000000000000000000000000007FFFFFFFFFFF",
                        Int48.class),
                new Int48(BigInteger.valueOf(140737488355327L)));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int56.class),
                new Int56(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "000000000000000000000000000000000000000000000000007FFFFFFFFFFFFF",
                        Int56.class),
                new Int56(BigInteger.valueOf(36028797018963967L)));

        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int64.class),
                (new Int64(BigInteger.ZERO)));

        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000007fffffffffffffff",
                        Int64.class),
                (new Int64(BigInteger.valueOf(Long.MAX_VALUE))));

        assertEquals(
                TypeDecoder.decodeNumeric(
                        "fffffffffffffffffffffffffffffffffffffffffffffff88000000000000000",
                        Int64.class),
                (new Int64(BigInteger.valueOf(Long.MIN_VALUE))));

        assertEquals(
                TypeDecoder.decodeNumeric(
                        "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                        Int64.class),
                (new Int64(BigInteger.valueOf(-1))));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int72.class),
                new Int72(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "00000000000000000000000000000000000000000000007fffffffffffffffff",
                        Int72.class),
                new Int72(new BigInteger("2361183241434822606847")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int80.class),
                new Int80(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "000000000000000000000000000000000000000000007fffffffffffffffffff",
                        Int80.class),
                new Int80(new BigInteger("604462909807314587353087")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int88.class),
                new Int88(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000007fffffffffffffffffffff",
                        Int88.class),
                new Int88(new BigInteger("154742504910672534362390527")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int96.class),
                new Int96(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "00000000000000000000000000000000000000007fffffffffffffffffffffff",
                        Int96.class),
                new Int96(new BigInteger("39614081257132168796771975167")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int104.class),
                new Int104(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "000000000000000000000000000000000000007fffffffffffffffffffffffff",
                        Int104.class),
                new Int104(new BigInteger("10141204801825835211973625643007")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int112.class),
                new Int112(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000007fffffffffffffffffffffffffff",
                        Int112.class),
                new Int112(new BigInteger("2596148429267413814265248164610047")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int120.class),
                new Int120(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "00000000000000000000000000000000007fffffffffffffffffffffffffffff",
                        Int120.class),
                new Int120(new BigInteger("664613997892457936451903530140172287")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int128.class),
                new Int128(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "000000000000000000000000000000007fffffffffffffffffffffffffffffff",
                        Int128.class),
                new Int128(new BigInteger("170141183460469231731687303715884105727")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int136.class),
                new Int136(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000007fffffffffffffffffffffffffffffffff",
                        Int136.class),
                new Int136(new BigInteger("43556142965880123323311949751266331066367")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int144.class),
                new Int144(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "00000000000000000000000000007fffffffffffffffffffffffffffffffffff",
                        Int144.class),
                new Int144(new BigInteger("11150372599265311570767859136324180752990207")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int152.class),
                new Int152(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "000000000000000000000000007fffffffffffffffffffffffffffffffffffff",
                        Int152.class),
                new Int152(new BigInteger("2854495385411919762116571938898990272765493247")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int160.class),
                new Int160(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000007fffffffffffffffffffffffffffffffffffffff",
                        Int160.class),
                new Int160(new BigInteger("730750818665451459101842416358141509827966271487")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int168.class),
                new Int168(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "00000000000000000000007fffffffffffffffffffffffffffffffffffffffff",
                        Int168.class),
                new Int168(new BigInteger("187072209578355573530071658587684226515959365500927")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int176.class),
                new Int176(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "000000000000000000007fffffffffffffffffffffffffffffffffffffffffff",
                        Int176.class),
                new Int176(
                        new BigInteger("47890485652059026823698344598447161988085597568237567")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int184.class),
                new Int184(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000007fffffffffffffffffffffffffffffffffffffffffffff",
                        Int184.class),
                new Int184(
                        new BigInteger(
                                "12259964326927110866866776217202473468949912977468817407")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int192.class),
                new Int192(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "00000000000000007fffffffffffffffffffffffffffffffffffffffffffffff",
                        Int192.class),
                new Int192(
                        new BigInteger(
                                "3138550867693340381917894711603833208051177722232017256447")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int200.class),
                new Int200(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "000000000000007fffffffffffffffffffffffffffffffffffffffffffffffff",
                        Int200.class),
                new Int200(
                        new BigInteger(
                                "803469022129495137770981046170581301261101496891396417650687")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int208.class),
                new Int208(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000007fffffffffffffffffffffffffffffffffffffffffffffffffff",
                        Int208.class),
                new Int208(
                        new BigInteger(
                                "205688069665150755269371147819668813122841983204197482918576127")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int216.class),
                new Int216(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "00000000007fffffffffffffffffffffffffffffffffffffffffffffffffffff",
                        Int216.class),
                new Int216(
                        new BigInteger(
                                "52656145834278593348959013841835216159447547700274555627155488767")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int224.class),
                new Int224(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "000000007fffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                        Int224.class),
                new Int224(
                        new BigInteger(
                                "13479973333575319897333507543509815336818572211270286240551805124607")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int232.class),
                new Int232(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000007fffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                        Int232.class),
                new Int232(
                        new BigInteger(
                                "3450873173395281893717377931138512726225554486085193277581262111899647")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int240.class),
                new Int240(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "00007fffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                        Int240.class),
                new Int240(
                        new BigInteger(
                                "883423532389192164791648750371459257913741948437809479060803100646309887")));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Int248.class),
                new Int248(BigInteger.ZERO));
        assertEquals(
                TypeDecoder.decodeNumeric(
                        "007fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                        Int248.class),
                new Int248(
                        new BigInteger(
                                "226156424291633194186662080095093570025917938800079226639565593765455331327")));

        assertEquals(
                TypeDecoder.decodeNumeric(
                        "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                        Int256.class),
                (new Int256(BigInteger.valueOf(-1))));

        assertEquals(
                TypeDecoder.decodeNumeric(
                        TypeEncoder.encodeNumeric(new Int256(BigInteger.TWO.pow(248))),
                        Int256.class),
                new Int256(BigInteger.TWO.pow(248)));

        assertEquals(
                TypeDecoder.decodeNumeric(
                        TypeEncoder.encodeNumeric(
                                new Int256(
                                        BigInteger.TWO.pow(248).negate().subtract(BigInteger.ONE))),
                        Int256.class),
                new Int256(BigInteger.TWO.pow(248).negate().subtract(BigInteger.ONE)));

        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0x7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                        Int256.class),
                new Int256(
                        new BigInteger(
                                "57896044618658097711785492504343953926634992332820282019728792003956564819967")));

        assertEquals(
                TypeDecoder.decodeNumeric(
                        "0x8000000000000000000000000000000000000000000000000000000000000000",
                        Int256.class),
                new Int256(
                        new BigInteger(
                                "-57896044618658097711785492504343953926634992332820282019728792003956564819968")));

        assertEquals(TypeDecoder.instantiateType("int", 123), (new Int(BigInteger.valueOf(123))));

        assertEquals(TypeDecoder.instantiateType("int", -123), (new Int(BigInteger.valueOf(-123))));
    }

    @Test
    public void testInt16MinMax() throws Exception {
        assertEquals(
                TypeDecoder.decodeNumeric(
                        TypeEncoder.encodeNumeric(
                                new Int16(BigInteger.valueOf((long) Math.pow(2, 15) - 1))),
                        Int16.class),
                new Int16(BigInteger.valueOf((long) Math.pow(2, 15) - 1)));

        assertEquals(
                TypeDecoder.decodeNumeric(
                        TypeEncoder.encodeNumeric(
                                new Int16(BigInteger.valueOf((long) -Math.pow(2, 15)))),
                        Int16.class),
                new Int16(BigInteger.valueOf((long) -Math.pow(2, 15))));
    }

    @Test
    public void testUint16Max() throws Exception {
        assertEquals(
                TypeDecoder.decodeNumeric(
                        TypeEncoder.encodeNumeric(
                                new Uint16(BigInteger.valueOf((long) Math.pow(2, 16) - 1))),
                        Uint16.class),
                new Uint16(BigInteger.valueOf((long) Math.pow(2, 16) - 1)));
    }

    /*
    TODO: Enable once Solidity supports fixed types - see
    https://github.com/ethereum/solidity/issues/409

    @Test
    public void testUfixedDecode() {
        assertEquals(TypeDecoder.decodeNumeric(
                "0000000000000000000000000000000000000000000000000000000000000000",
                Ufixed24x40.class),
                (new Ufixed24x40(BigInteger.ZERO)));

        assertEquals(TypeDecoder.decodeNumeric(
                "0000000000000000000000000000000000000000000000007fffffffffffffff",
                Ufixed24x40.class),
                (new Ufixed24x40(BigInteger.valueOf(Long.MAX_VALUE))));

        assertEquals(TypeDecoder.decodeNumeric(
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                Ufixed.class),
                (new Ufixed(
                        new BigInteger(
                                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                                16
                        ))));
    }

    @Test
    public void testFixedDecode() {
        assertEquals(TypeDecoder.decodeNumeric(
                "0000000000000000000000000000000000000000000000000000000000000000",
                Fixed24x40.class),
                (new Fixed24x40(BigInteger.ZERO)));

        assertEquals(TypeDecoder.decodeNumeric(
                "0000000000000000000000000000000000000000000000007fffffffffffffff",
                Fixed24x40.class),
                (new Fixed24x40(BigInteger.valueOf(Long.MAX_VALUE))));

        assertEquals(TypeDecoder.decodeNumeric(
                "ffffffffffffffffffffffffffffffffffffffffffffffff8000000000000000",
                Fixed24x40.class),
                (new Fixed24x40(BigInteger.valueOf(Long.MIN_VALUE))));

        assertEquals(TypeDecoder.decodeNumeric(
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                Fixed.class),
                (new Fixed(BigInteger.valueOf(-1))));
    }
    */

    @Test
    public void testStaticBytes() throws Exception {
        byte[] testbytes = new byte[] {0, 1, 2, 3, 4, 5};
        Bytes6 staticBytes = new Bytes6(testbytes);
        assertEquals(
                TypeDecoder.decodeBytes(
                        "0001020304050000000000000000000000000000000000000000000000000000",
                        Bytes6.class),
                (staticBytes));

        Bytes empty = new Bytes1(new byte[] {0});
        assertEquals(
                TypeDecoder.decodeBytes(
                        "0000000000000000000000000000000000000000000000000000000000000000",
                        Bytes1.class),
                (empty));

        Bytes dave = new Bytes4("dave".getBytes());
        assertEquals(
                TypeDecoder.decodeBytes(
                        "6461766500000000000000000000000000000000000000000000000000000000",
                        Bytes4.class),
                (dave));

        assertEquals(TypeDecoder.instantiateType("bytes6", testbytes), (new Bytes6(testbytes)));
    }

    @Test
    public void testDynamicBytes() throws Exception {
        byte[] testbytes = new byte[] {0, 1, 2, 3, 4, 5};
        DynamicBytes dynamicBytes = new DynamicBytes(testbytes);
        assertEquals(
                TypeDecoder.decodeDynamicBytes(
                        "0000000000000000000000000000000000000000000000000000000000000006" // length
                                + "0001020304050000000000000000000000000000000000000000000000000000",
                        0),
                (dynamicBytes));

        DynamicBytes empty = new DynamicBytes(new byte[] {0});
        assertEquals(
                TypeDecoder.decodeDynamicBytes(
                        "0000000000000000000000000000000000000000000000000000000000000001"
                                + "0000000000000000000000000000000000000000000000000000000000000000",
                        0),
                (empty));

        DynamicBytes dave = new DynamicBytes("dave".getBytes());
        assertEquals(
                TypeDecoder.decodeDynamicBytes(
                        "0000000000000000000000000000000000000000000000000000000000000004"
                                + "6461766500000000000000000000000000000000000000000000000000000000",
                        0),
                (dave));

        DynamicBytes loremIpsum =
                new DynamicBytes(
                        ("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod "
                                        + "tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim "
                                        + "veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex "
                                        + "ea commodo consequat. Duis aute irure dolor in reprehenderit in "
                                        + "voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur "
                                        + "sint occaecat cupidatat non proident, sunt in culpa qui officia "
                                        + "deserunt mollit anim id est laborum.")
                                .getBytes());

        assertEquals(
                TypeDecoder.decodeDynamicBytes(
                        "00000000000000000000000000000000000000000000000000000000000001bd"
                                + "4c6f72656d20697073756d20646f6c6f722073697420616d65742c20636f6e73"
                                + "656374657475722061646970697363696e6720656c69742c2073656420646f20"
                                + "656975736d6f642074656d706f7220696e6369646964756e74207574206c6162"
                                + "6f726520657420646f6c6f7265206d61676e6120616c697175612e2055742065"
                                + "6e696d206164206d696e696d2076656e69616d2c2071756973206e6f73747275"
                                + "6420657865726369746174696f6e20756c6c616d636f206c61626f726973206e"
                                + "69736920757420616c697175697020657820656120636f6d6d6f646f20636f6e"
                                + "7365717561742e2044756973206175746520697275726520646f6c6f7220696e"
                                + "20726570726568656e646572697420696e20766f6c7570746174652076656c69"
                                + "7420657373652063696c6c756d20646f6c6f726520657520667567696174206e"
                                + "756c6c612070617269617475722e204578636570746575722073696e74206f63"
                                + "63616563617420637570696461746174206e6f6e2070726f6964656e742c2073"
                                + "756e7420696e2063756c706120717569206f666669636961206465736572756e"
                                + "74206d6f6c6c697420616e696d20696420657374206c61626f72756d2e000000",
                        0),
                (loremIpsum));

        assertEquals(
                TypeDecoder.instantiateType("bytes", testbytes), (new DynamicBytes(testbytes)));
    }

    @Test
    public void testAddress() throws Exception {
        assertEquals(
                TypeDecoder.decodeAddress(
                        "000000000000000000000000be5422d15f39373eb0a97ff8c10fbd0e40e29338"),
                (new Address("0xbe5422d15f39373eb0a97ff8c10fbd0e40e29338")));

        assertEquals(
                TypeDecoder.decodeAddress(
                        "000000000000000000000000be5422d15f39373eb0a97ff8c10fbd0e40e29338"),
                (new Address("0xbe5422d15f39373eb0a97ff8c10fbd0e40e29338")));

        assertEquals(
                TypeDecoder.instantiateType(
                        "address", "0xbe5422d15f39373eb0a97ff8c10fbd0e40e29338"),
                (new Address("0xbe5422d15f39373eb0a97ff8c10fbd0e40e29338")));

        assertEquals(
                TypeDecoder.instantiateType("address", BigInteger.ONE),
                (new Address("0x0000000000000000000000000000000000000001")));
    }

    @Test
    public void testUtf8String() throws Exception {
        assertEquals(
                TypeDecoder.decodeUtf8String(
                        "000000000000000000000000000000000000000000000000000000000000000d" // length
                                + "48656c6c6f2c20776f726c642100000000000000000000000000000000000000",
                        0),
                (new Utf8String("Hello, world!")));

        assertEquals(
                TypeDecoder.instantiateType("string", "Hello, world!"),
                (new Utf8String("Hello, world!")));
    }

    @Test
    public void testStaticArray() throws Exception {
        assertEquals(
                TypeDecoder.decodeStaticArray(
                        "000000000000000000000000000000000000000000000000000000000000000a"
                                + "0000000000000000000000000000000000000000000000007fffffffffffffff",
                        0,
                        new TypeReference.StaticArrayTypeReference<StaticArray<Uint256>>(2) {},
                        2),
                (new StaticArray2<>(
                        Uint256.class,
                        new Uint256(BigInteger.TEN),
                        new Uint256(BigInteger.valueOf(Long.MAX_VALUE)))));

        assertEquals(
                TypeDecoder.decodeStaticArray(
                        "0000000000000000000000000000000000000000000000000000000000000040"
                                + "0000000000000000000000000000000000000000000000000000000000000080"
                                + "000000000000000000000000000000000000000000000000000000000000000d"
                                + "48656c6c6f2c20776f726c642100000000000000000000000000000000000000"
                                + "000000000000000000000000000000000000000000000000000000000000000d"
                                + "776f726c64212048656c6c6f2c00000000000000000000000000000000000000",
                        0,
                        new TypeReference.StaticArrayTypeReference<StaticArray<Utf8String>>(2) {},
                        2),
                (new StaticArray2<>(
                        Utf8String.class,
                        new Utf8String("Hello, world!"),
                        new Utf8String("world! Hello,"))));

        Type arr = TypeDecoder.instantiateType("uint256[2]", new long[] {10, Long.MAX_VALUE});

        assertTrue(arr instanceof StaticArray2);
        StaticArray2 staticArray2 = (StaticArray2) arr;

        assertEquals(staticArray2.getComponentType(), Uint256.class);

        assertEquals(staticArray2.getValue().get(0), (new Uint256(BigInteger.TEN)));
        assertEquals(
                staticArray2.getValue().get(1), (new Uint256(BigInteger.valueOf(Long.MAX_VALUE))));
    }

    @Test
    public void testEmptyStaticArray() {
        assertThrows(
                UnsupportedOperationException.class,
                () ->
                        TypeDecoder.decodeStaticArray(
                                "0000000000000000000000000000000000000000000000000000000000000000",
                                0,
                                new TypeReference.StaticArrayTypeReference<StaticArray<Uint256>>(
                                        0) {},
                                0));
    }

    @Test
    public void testDynamicArray() throws Exception {
        assertEquals(
                TypeDecoder.decodeDynamicArray(
                        "0000000000000000000000000000000000000000000000000000000000000000", // length
                        0,
                        new TypeReference<DynamicArray<Uint256>>() {}),
                (new DynamicArray<>(Uint256.class)));

        assertEquals(
                TypeDecoder.decodeDynamicArray(
                        "0000000000000000000000000000000000000000000000000000000000000002" // length
                                + "000000000000000000000000000000000000000000000000000000000000000a"
                                + "0000000000000000000000000000000000000000000000007fffffffffffffff",
                        0,
                        new TypeReference<DynamicArray<Uint256>>() {}),
                (new DynamicArray<>(
                        Uint256.class,
                        new Uint256(BigInteger.TEN),
                        new Uint256(BigInteger.valueOf(Long.MAX_VALUE)))));

        assertEquals(
                TypeDecoder.decodeDynamicArray(
                        "0000000000000000000000000000000000000000000000000000000000000002" // length
                                + "0000000000000000000000000000000000000000000000000000000000000040"
                                + "0000000000000000000000000000000000000000000000000000000000000080"
                                + "000000000000000000000000000000000000000000000000000000000000000d"
                                + "48656c6c6f2c20776f726c642100000000000000000000000000000000000000"
                                + "000000000000000000000000000000000000000000000000000000000000000d"
                                + "776f726c64212048656c6c6f2c00000000000000000000000000000000000000",
                        0,
                        new TypeReference<DynamicArray<Utf8String>>() {}),
                (new DynamicArray<>(
                        Utf8String.class,
                        new Utf8String("Hello, world!"),
                        new Utf8String("world! Hello,"))));

        Type arr =
                TypeDecoder.instantiateType(
                        "string[]", new String[] {"Hello, world!", "world! Hello,"});
        assertTrue(arr instanceof DynamicArray);
        DynamicArray dynamicArray = (DynamicArray) arr;

        assertEquals(dynamicArray.getComponentType(), Utf8String.class);

        assertEquals(dynamicArray.getValue().get(0), (new Utf8String("Hello, world!")));

        assertEquals(dynamicArray.getValue().get(1), (new Utf8String("world! Hello,")));
    }

    @Test
    public void testDynamicArrayOfDynamicArrays() throws Exception {
        assertEquals(
                TypeDecoder.decodeDynamicArray(
                        "0000000000000000000000000000000000000000000000000000000000000002"
                                + "0000000000000000000000000000000000000000000000000000000000000040"
                                + "00000000000000000000000000000000000000000000000000000000000000a0"
                                + "0000000000000000000000000000000000000000000000000000000000000001"
                                + "0000000000000000000000000000000000000000000000000000000000000000"
                                + "0000000000000000000000000000000000000000000000000000000000000000"
                                + "0000000000000000000000000000000000000000000000000000000000000001"
                                + "0000000000000000000000000000000000000000000000000000000000000001"
                                + "0000000000000000000000000000000000000000000000000000000000000000",
                        0,
                        new TypeReference<DynamicArray<DynamicArray<AbiV2TestFixture.Bar>>>() {}),
                new DynamicArray(
                        DynamicArray.class,
                        Arrays.asList(
                                new DynamicArray(
                                        AbiV2TestFixture.Bar.class,
                                        new AbiV2TestFixture.Bar(
                                                new Uint256(BigInteger.ZERO),
                                                new Uint256(BigInteger.ZERO))),
                                new DynamicArray(
                                        AbiV2TestFixture.Bar.class,
                                        new AbiV2TestFixture.Bar(
                                                new Uint256(BigInteger.ONE),
                                                new Uint256(BigInteger.ZERO))))));
    }

    @Test
    public void testDynamicArrayOfStaticArrays() throws Exception {
        assertEquals(
                TypeDecoder.decodeDynamicArray(
                        "0000000000000000000000000000000000000000000000000000000000000002"
                                + "0000000000000000000000000000000000000000000000000000000000000000"
                                + "0000000000000000000000000000000000000000000000000000000000000000"
                                + "0000000000000000000000000000000000000000000000000000000000000001"
                                + "0000000000000000000000000000000000000000000000000000000000000000",
                        0,
                        new TypeReference<DynamicArray<StaticArray1<AbiV2TestFixture.Bar>>>() {}),
                new DynamicArray(
                        StaticArray1.class,
                        Arrays.asList(
                                new StaticArray1(
                                        AbiV2TestFixture.Bar.class,
                                        new AbiV2TestFixture.Bar(
                                                new Uint256(BigInteger.ZERO),
                                                new Uint256(BigInteger.ZERO))),
                                new StaticArray1(
                                        AbiV2TestFixture.Bar.class,
                                        new AbiV2TestFixture.Bar(
                                                new Uint256(BigInteger.ONE),
                                                new Uint256(BigInteger.ZERO))))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void multiDimArrays() throws Exception {
        byte[] bytes1d = new byte[] {1, 2, 3};
        byte[][] bytes2d = new byte[][] {bytes1d, bytes1d, bytes1d};
        final byte[][][] bytes3d = new byte[][][] {bytes2d, bytes2d, bytes2d};

        assertEquals(TypeDecoder.instantiateType("bytes", bytes1d), (new DynamicBytes(bytes1d)));

        Type twoDim = TypeDecoder.instantiateType("uint256[][3]", bytes2d);
        assertTrue(twoDim instanceof StaticArray3);
        StaticArray3<DynamicArray<Uint256>> staticArray3 =
                (StaticArray3<DynamicArray<Uint256>>) twoDim;
        assertEquals(staticArray3.getComponentType(), DynamicArray.class);
        DynamicArray<Uint256> row1 = staticArray3.getValue().get(1);
        assertEquals(row1.getValue().get(2), new Uint256(3));

        Type threeDim = TypeDecoder.instantiateType("uint256[][3][3]", bytes3d);
        assertTrue(threeDim instanceof StaticArray3);
        StaticArray3<StaticArray3<DynamicArray<Uint256>>> staticArray3StaticArray3 =
                (StaticArray3<StaticArray3<DynamicArray<Uint256>>>) threeDim;
        assertEquals(staticArray3StaticArray3.getComponentType(), StaticArray3.class);
        row1 = staticArray3StaticArray3.getValue().get(1).getValue().get(1);
        assertEquals(row1.getValue().get(1), (new Uint256(2)));
    }

    @Test
    public void testDirectlyConstructedTypesDecodeAtOffset() {
        String uint8Word = "00000000000000000000000000000000000000000000000000000000000000ff";
        String int128Word = "ffffffffffffffffffffffffffffffff80000000000000000000000000000000";
        String int256Word = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
        String addressWord = "000000000000000000000000be5422d15f39373eb0a97ff8c10fbd0e40e29338";
        String bytes32Word = "0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20";
        String bytes4Word = "deadbeef00000000000000000000000000000000000000000000000000000000";
        String uint256Word = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
        String input =
                uint8Word
                        + int128Word
                        + int256Word
                        + addressWord
                        + bytes32Word
                        + bytes4Word
                        + uint256Word;

        Uint8 uint8 = TypeDecoder.decode(input, 0, Uint8.class);
        Int128 int128 = TypeDecoder.decode(input, 64, Int128.class);
        Int256 int256 = TypeDecoder.decode(input, 128, Int256.class);
        Address address = TypeDecoder.decode(input, 192, Address.class);
        Bytes32 bytes32 = TypeDecoder.decode(input, 256, Bytes32.class);
        Bytes4 bytes4 = TypeDecoder.decode(input, 320, Bytes4.class);
        Uint256 uint256 = TypeDecoder.decode(input, 384, Uint256.class);

        assertEquals(new Uint8(255), uint8);
        assertEquals(new Int128(BigInteger.ONE.shiftLeft(127).negate()), int128);
        assertEquals(new Int256(BigInteger.ONE.negate()), int256);
        assertEquals(new Address("0xbe5422d15f39373eb0a97ff8c10fbd0e40e29338"), address);
        assertEquals(new Bytes32(Numeric.hexStringToByteArray(bytes32Word)), bytes32);
        assertEquals(
                new Bytes4(new byte[] {(byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef}),
                bytes4);
        assertEquals(new Uint256(BigInteger.ONE.shiftLeft(256).subtract(BigInteger.ONE)), uint256);
    }

    @Test
    public void testDecodeNumericReadsOnlyFirstWord() {
        String word = "0000000000000000000000000000000000000000000000000000000000000007";
        String trailing = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";

        assertEquals(new Uint256(7), TypeDecoder.decodeNumeric(word + trailing, Uint256.class));
        assertEquals(
                new Uint256(7), TypeDecoder.decodeNumeric("0x" + word + trailing, Uint256.class));
        assertEquals(new Uint24(7), TypeDecoder.decodeNumeric("0x" + word, Uint24.class));
        assertEquals(
                new Address("0x0000000000000000000000000000000000000007"),
                TypeDecoder.decodeAddress("0x" + word + trailing));
    }

    @Test
    public void testDecodeBytesWithMalformedInputKeepsReflectiveError() {
        String input = "0x" + "11".repeat(32);

        UnsupportedOperationException e =
                assertThrows(
                        UnsupportedOperationException.class,
                        () -> TypeDecoder.decodeBytes(input, Bytes32.class));

        assertEquals("Unable to create instance of " + Bytes32.class.getName(), e.getMessage());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testDecodeLargeNumberOfValues() {
        int count = 4000;
        StringBuilder input = new StringBuilder("0x");
        List<TypeReference<Type>> outputParameters = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            input.append(Numeric.toHexStringNoPrefixZeroPadded(BigInteger.valueOf(i), 64));
            outputParameters.add((TypeReference) new TypeReference<Uint256>() {});
        }

        List<Type> result = FunctionReturnDecoder.decode(input.toString(), outputParameters);

        assertEquals(count, result.size());
        for (int i = 0; i < count; i++) {
            assertEquals(new Uint256(i), result.get(i));
        }
    }
}

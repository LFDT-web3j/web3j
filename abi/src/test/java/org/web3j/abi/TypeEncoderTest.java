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
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import org.web3j.abi.AbiV2TestFixture.Bar;
import org.web3j.abi.AbiV2TestFixture.Foo;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Bytes;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Int;
import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.Ufixed;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes1;
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
import org.web3j.abi.datatypes.primitive.Byte;
import org.web3j.abi.datatypes.primitive.Char;
import org.web3j.abi.datatypes.primitive.Long;
import org.web3j.abi.datatypes.primitive.Short;
import org.web3j.utils.Numeric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.web3j.abi.TypeEncoder.encode;

public class TypeEncoderTest {

    @Test
    public void testBoolEncode() {
        assertEquals(
                TypeEncoder.encodeBool(new Bool(false)),
                ("0000000000000000000000000000000000000000000000000000000000000000"));
        assertEquals(
                TypeEncoder.encodeBool(new Bool(true)),
                ("0000000000000000000000000000000000000000000000000000000000000001"));
    }

    @Test
    public void testUintEncode() {
        Uint zero8 = new Uint8(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero8),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max8 = new Uint8(255);
        assertEquals(
                TypeEncoder.encodeNumeric(max8),
                "00000000000000000000000000000000000000000000000000000000000000ff");

        Uint zero16 = new Uint16(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero16),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max16 = new Uint16(65535);
        assertEquals(
                TypeEncoder.encodeNumeric(max16),
                "000000000000000000000000000000000000000000000000000000000000ffff");

        Uint zero24 = new Uint24(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero24),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max24 = new Uint24(16777215);
        assertEquals(
                TypeEncoder.encodeNumeric(max24),
                "0000000000000000000000000000000000000000000000000000000000ffffff");

        Uint zero32 = new Uint32(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero32),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max32 = new Uint32(BigInteger.valueOf(4294967295L));
        assertEquals(
                TypeEncoder.encodeNumeric(max32),
                "00000000000000000000000000000000000000000000000000000000ffffffff");

        Uint zero40 = new Uint40(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero40),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max40 = new Uint40(BigInteger.valueOf(1099511627775L));
        assertEquals(
                TypeEncoder.encodeNumeric(max40),
                "000000000000000000000000000000000000000000000000000000ffffffffff");

        Uint zero48 = new Uint48(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero48),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max48 = new Uint48(BigInteger.valueOf(281474976710655L));
        assertEquals(
                TypeEncoder.encodeNumeric(max48),
                "0000000000000000000000000000000000000000000000000000ffffffffffff");

        Uint zero56 = new Uint56(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero56),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max56 = new Uint56(BigInteger.valueOf(72057594037927935L));
        assertEquals(
                TypeEncoder.encodeNumeric(max56),
                "00000000000000000000000000000000000000000000000000ffffffffffffff");

        Uint zero64 = new Uint64(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero64),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Uint maxLong = new Uint64(BigInteger.valueOf(java.lang.Long.MAX_VALUE));
        assertEquals(
                TypeEncoder.encodeNumeric(maxLong),
                ("0000000000000000000000000000000000000000000000007fffffffffffffff"));

        Uint maxValue64 =
                new Uint(
                        new BigInteger(
                                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                                16));
        assertEquals(
                TypeEncoder.encodeNumeric(maxValue64),
                ("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));

        Uint largeValue =
                new Uint(
                        new BigInteger(
                                "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe",
                                16));
        assertEquals(
                TypeEncoder.encodeNumeric(largeValue),
                ("fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe"));

        Uint zero72 = new Uint72(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero72),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max72 = new Uint72(new BigInteger("4722366482869645213695"));
        assertEquals(
                TypeEncoder.encodeNumeric(max72),
                "0000000000000000000000000000000000000000000000ffffffffffffffffff");

        Uint zero80 = new Uint80(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero80),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max80 = new Uint80(new BigInteger("1208925819614629174706175"));
        assertEquals(
                TypeEncoder.encodeNumeric(max80),
                "00000000000000000000000000000000000000000000ffffffffffffffffffff");

        Uint zero88 = new Uint88(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero88),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max88 = new Uint88(new BigInteger("309485009821345068724781055"));
        assertEquals(
                TypeEncoder.encodeNumeric(max88),
                "000000000000000000000000000000000000000000ffffffffffffffffffffff");

        Uint zero96 = new Uint96(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero96),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max96 = new Uint96(new BigInteger("79228162514264337593543950335"));
        assertEquals(
                TypeEncoder.encodeNumeric(max96),
                "0000000000000000000000000000000000000000ffffffffffffffffffffffff");

        Uint zero104 = new Uint104(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero104),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max104 = new Uint104(new BigInteger("20282409603651670423947251286015"));
        assertEquals(
                TypeEncoder.encodeNumeric(max104),
                "00000000000000000000000000000000000000ffffffffffffffffffffffffff");

        Uint zero112 = new Uint112(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero112),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max112 = new Uint112(new BigInteger("5192296858534827628530496329220095"));
        assertEquals(
                TypeEncoder.encodeNumeric(max112),
                "000000000000000000000000000000000000ffffffffffffffffffffffffffff");

        Uint zero120 = new Uint120(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero120),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max120 = new Uint120(new BigInteger("1329227995784915872903807060280344575"));
        assertEquals(
                TypeEncoder.encodeNumeric(max120),
                "0000000000000000000000000000000000ffffffffffffffffffffffffffffff");

        Uint zero128 = new Uint128(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero128),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max128 = new Uint128(new BigInteger("340282366920938463463374607431768211455"));
        assertEquals(
                TypeEncoder.encodeNumeric(max128),
                "00000000000000000000000000000000ffffffffffffffffffffffffffffffff");

        Uint zero136 = new Uint136(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero136),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max136 = new Uint136(new BigInteger("87112285931760246646623899502532662132735"));
        assertEquals(
                TypeEncoder.encodeNumeric(max136),
                "000000000000000000000000000000ffffffffffffffffffffffffffffffffff");

        Uint zero144 = new Uint144(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero144),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max144 = new Uint144(new BigInteger("22300745198530623141535718272648361505980415"));
        assertEquals(
                TypeEncoder.encodeNumeric(max144),
                "0000000000000000000000000000ffffffffffffffffffffffffffffffffffff");

        Uint zero152 = new Uint152(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero152),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max152 = new Uint152(new BigInteger("5708990770823839524233143877797980545530986495"));
        assertEquals(
                TypeEncoder.encodeNumeric(max152),
                "00000000000000000000000000ffffffffffffffffffffffffffffffffffffff");

        Uint zero160 = new Uint160(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero160),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max160 =
                new Uint160(new BigInteger("1461501637330902918203684832716283019655932542975"));
        assertEquals(
                TypeEncoder.encodeNumeric(max160),
                "000000000000000000000000ffffffffffffffffffffffffffffffffffffffff");

        Uint zero168 = new Uint168(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero168),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max168 =
                new Uint168(new BigInteger("374144419156711147060143317175368453031918731001855"));
        assertEquals(
                TypeEncoder.encodeNumeric(max168),
                "0000000000000000000000ffffffffffffffffffffffffffffffffffffffffff");

        Uint zero176 = new Uint176(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero176),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max176 =
                new Uint176(
                        new BigInteger("95780971304118053647396689196894323976171195136475135"));
        assertEquals(
                TypeEncoder.encodeNumeric(max176),
                "00000000000000000000ffffffffffffffffffffffffffffffffffffffffffff");

        Uint zero184 = new Uint184(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero184),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max184 =
                new Uint184(
                        new BigInteger("24519928653854221733733552434404946937899825954937634815"));
        assertEquals(
                TypeEncoder.encodeNumeric(max184),
                "000000000000000000ffffffffffffffffffffffffffffffffffffffffffffff");

        Uint zero192 = new Uint192(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero192),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max192 =
                new Uint192(
                        new BigInteger(
                                "6277101735386680763835789423207666416102355444464034512895"));
        assertEquals(
                TypeEncoder.encodeNumeric(max192),
                "0000000000000000ffffffffffffffffffffffffffffffffffffffffffffffff");

        Uint zero200 = new Uint200(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero200),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max200 =
                new Uint200(
                        new BigInteger(
                                "1606938044258990275541962092341162602522202993782792835301375"));
        assertEquals(
                TypeEncoder.encodeNumeric(max200),
                "00000000000000ffffffffffffffffffffffffffffffffffffffffffffffffff");

        Uint zero208 = new Uint208(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero208),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max208 =
                new Uint208(
                        new BigInteger(
                                "411376139330301510538742295639337626245683966408394965837152255"));
        assertEquals(
                TypeEncoder.encodeNumeric(max208),
                "000000000000ffffffffffffffffffffffffffffffffffffffffffffffffffff");

        Uint zero216 = new Uint216(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero216),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max216 =
                new Uint216(
                        new BigInteger(
                                "105312291668557186697918027683670432318895095400549111254310977535"));
        assertEquals(
                TypeEncoder.encodeNumeric(max216),
                "0000000000ffffffffffffffffffffffffffffffffffffffffffffffffffffff");

        Uint zero224 = new Uint224(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero224),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max224 =
                new Uint224(
                        new BigInteger(
                                "26959946667150639794667015087019630673637144422540572481103610249215"));
        assertEquals(
                TypeEncoder.encodeNumeric(max224),
                "00000000ffffffffffffffffffffffffffffffffffffffffffffffffffffffff");

        Uint zero232 = new Uint232(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero232),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max232 =
                new Uint232(
                        new BigInteger(
                                "6901746346790563787434755862277025452451108972170386555162524223799295"));
        assertEquals(
                TypeEncoder.encodeNumeric(max232),
                "000000ffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");

        Uint zero240 = new Uint232(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero240),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max240 =
                new Uint240(
                        new BigInteger(
                                "1766847064778384329583297500742918515827483896875618958121606201292619775"));
        assertEquals(
                TypeEncoder.encodeNumeric(max240),
                "0000ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");

        Uint zero248 = new Uint248(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero248),
                "0000000000000000000000000000000000000000000000000000000000000000");
        Uint max248 =
                new Uint248(
                        new BigInteger(
                                "452312848583266388373324160190187140051835877600158453279131187530910662655"));
        assertEquals(
                TypeEncoder.encodeNumeric(max248),
                "00ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
    }

    @Test
    public void testInvalidUintEncode() {
        assertThrows(UnsupportedOperationException.class, () -> new Uint64(BigInteger.valueOf(-1)));
    }

    @Test
    public void testTooLargeUintEncode() {
        // 1 more than "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        assertThrows(
                UnsupportedOperationException.class,
                () ->
                        new Uint(
                                new BigInteger(
                                        "10000000000000000000000000000000000000000000000000000000000000000",
                                        16)));
    }

    @Test
    public void testIntEncode() {
        Int zero8 = new Int8(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero8),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max8 = new Int8(BigInteger.valueOf(127));
        assertEquals(
                TypeEncoder.encodeNumeric(max8),
                ("000000000000000000000000000000000000000000000000000000000000007f"));

        Int min8 = new Int8(BigInteger.valueOf(-128));
        assertEquals(
                TypeEncoder.encodeNumeric(min8),
                ("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff80"));

        Int zero16 = new Int16(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero16),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max16 = new Int16(BigInteger.valueOf(32767));
        assertEquals(
                TypeEncoder.encodeNumeric(max16),
                ("0000000000000000000000000000000000000000000000000000000000007fff"));

        Int min16 = new Int16(BigInteger.valueOf(-32768));
        assertEquals(
                TypeEncoder.encodeNumeric(min16),
                ("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8000"));

        Int zero24 = new Int24(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero24),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max24 = new Int24(BigInteger.valueOf(8388607));
        assertEquals(
                TypeEncoder.encodeNumeric(max24),
                ("00000000000000000000000000000000000000000000000000000000007fffff"));

        Int min24 = new Int24(BigInteger.valueOf(-8388608));
        assertEquals(
                TypeEncoder.encodeNumeric(min24),
                ("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffff800000"));

        Int zero32 = new Int32(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero32),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max32 = new Int32(BigInteger.valueOf(2147483647));
        assertEquals(
                TypeEncoder.encodeNumeric(max32),
                ("000000000000000000000000000000000000000000000000000000007fffffff"));

        Int min32 = new Int32(BigInteger.valueOf(-2147483648));
        assertEquals(
                TypeEncoder.encodeNumeric(min32),
                ("ffffffffffffffffffffffffffffffffffffffffffffffffffffffff80000000"));

        Int zero40 = new Int40(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero40),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max40 = new Int40(BigInteger.valueOf(549755813887L));
        assertEquals(
                TypeEncoder.encodeNumeric(max40),
                ("0000000000000000000000000000000000000000000000000000007fffffffff"));

        Int min40 = new Int40(BigInteger.valueOf(-549755813888L));
        assertEquals(
                TypeEncoder.encodeNumeric(min40),
                ("ffffffffffffffffffffffffffffffffffffffffffffffffffffff8000000000"));

        Int zero48 = new Int48(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero48),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max48 = new Int48(BigInteger.valueOf(140737488355327L));
        assertEquals(
                TypeEncoder.encodeNumeric(max48),
                ("00000000000000000000000000000000000000000000000000007fffffffffff"));

        Int min48 = new Int48(BigInteger.valueOf(-140737488355328L));
        assertEquals(
                TypeEncoder.encodeNumeric(min48),
                ("ffffffffffffffffffffffffffffffffffffffffffffffffffff800000000000"));

        Int zero56 = new Int48(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero56),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max56 = new Int56(BigInteger.valueOf(36028797018963967L));
        assertEquals(
                TypeEncoder.encodeNumeric(max56),
                ("000000000000000000000000000000000000000000000000007fffffffffffff"));

        Int min56 = new Int56(BigInteger.valueOf(-36028797018963968L));
        assertEquals(
                TypeEncoder.encodeNumeric(min56),
                ("ffffffffffffffffffffffffffffffffffffffffffffffffff80000000000000"));

        Int zero64 = new Int64(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero64),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max64 = new Int64(BigInteger.valueOf(java.lang.Long.MAX_VALUE));
        assertEquals(
                TypeEncoder.encodeNumeric(max64),
                ("0000000000000000000000000000000000000000000000007fffffffffffffff"));

        Int min64 = new Int64(BigInteger.valueOf(java.lang.Long.MIN_VALUE));
        assertEquals(
                TypeEncoder.encodeNumeric(min64),
                ("ffffffffffffffffffffffffffffffffffffffffffffffff8000000000000000"));

        Int zero72 = new Int72(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero72),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max72 = new Int72(new BigInteger("2361183241434822606847"));
        assertEquals(
                TypeEncoder.encodeNumeric(max72),
                ("00000000000000000000000000000000000000000000007fffffffffffffffff"));

        Int min72 = new Int72(new BigInteger("-2361183241434822606848"));
        assertEquals(
                TypeEncoder.encodeNumeric(min72),
                ("ffffffffffffffffffffffffffffffffffffffffffffff800000000000000000"));

        Int zero80 = new Int80(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero80),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max80 = new Int80(new BigInteger("604462909807314587353087"));
        assertEquals(
                TypeEncoder.encodeNumeric(max80),
                ("000000000000000000000000000000000000000000007fffffffffffffffffff"));

        Int min80 = new Int80(new BigInteger("-604462909807314587353088"));
        assertEquals(
                TypeEncoder.encodeNumeric(min80),
                ("ffffffffffffffffffffffffffffffffffffffffffff80000000000000000000"));

        Int zero88 = new Int88(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero88),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max88 = new Int88(new BigInteger("154742504910672534362390527"));
        assertEquals(
                TypeEncoder.encodeNumeric(max88),
                ("0000000000000000000000000000000000000000007fffffffffffffffffffff"));

        Int min88 = new Int88(new BigInteger("-154742504910672534362390528"));
        assertEquals(
                TypeEncoder.encodeNumeric(min88),
                ("ffffffffffffffffffffffffffffffffffffffffff8000000000000000000000"));

        Int zero96 = new Int96(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero96),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max96 = new Int96(new BigInteger("39614081257132168796771975167"));
        assertEquals(
                TypeEncoder.encodeNumeric(max96),
                ("00000000000000000000000000000000000000007fffffffffffffffffffffff"));

        Int min96 = new Int96(new BigInteger("-39614081257132168796771975168"));
        assertEquals(
                TypeEncoder.encodeNumeric(min96),
                ("ffffffffffffffffffffffffffffffffffffffff800000000000000000000000"));

        Int zero104 = new Int104(BigInteger.ZERO);
        assertEquals(
                TypeEncoder.encodeNumeric(zero104),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max104 = new Int104(new BigInteger("10141204801825835211973625643007"));
        assertEquals(
                TypeEncoder.encodeNumeric(max104),
                ("000000000000000000000000000000000000007fffffffffffffffffffffffff"));

        Int min104 = new Int104(new BigInteger("-10141204801825835211973625643008"));
        assertEquals(
                TypeEncoder.encodeNumeric(min104),
                ("ffffffffffffffffffffffffffffffffffffff80000000000000000000000000"));

        Int zero112 = new Int112(BigInteger.ZERO);

        assertEquals(
                TypeEncoder.encodeNumeric(zero112),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max112 = new Int112(new BigInteger("2596148429267413814265248164610047"));
        assertEquals(
                TypeEncoder.encodeNumeric(max112),
                ("0000000000000000000000000000000000007fffffffffffffffffffffffffff"));

        Int min112 = new Int112(new BigInteger("-2596148429267413814265248164610048"));
        assertEquals(
                TypeEncoder.encodeNumeric(min112),
                ("ffffffffffffffffffffffffffffffffffff8000000000000000000000000000"));

        Int zero120 = new Int120(BigInteger.ZERO);

        assertEquals(
                TypeEncoder.encodeNumeric(zero120),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max120 = new Int120(new BigInteger("664613997892457936451903530140172287"));
        assertEquals(
                TypeEncoder.encodeNumeric(max120),
                ("00000000000000000000000000000000007fffffffffffffffffffffffffffff"));

        Int min120 = new Int120(new BigInteger("-664613997892457936451903530140172288"));
        assertEquals(
                TypeEncoder.encodeNumeric(min120),
                ("ffffffffffffffffffffffffffffffffff800000000000000000000000000000"));

        Int zero128 = new Int128(BigInteger.ZERO);

        assertEquals(
                TypeEncoder.encodeNumeric(zero128),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max128 = new Int128(new BigInteger("170141183460469231731687303715884105727"));
        assertEquals(
                TypeEncoder.encodeNumeric(max128),
                ("000000000000000000000000000000007fffffffffffffffffffffffffffffff"));

        Int min128 = new Int128(new BigInteger("-170141183460469231731687303715884105728"));
        assertEquals(
                TypeEncoder.encodeNumeric(min128),
                ("ffffffffffffffffffffffffffffffff80000000000000000000000000000000"));

        Int zero136 = new Int136(BigInteger.ZERO);

        assertEquals(
                TypeEncoder.encodeNumeric(zero136),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max136 = new Int136(new BigInteger("43556142965880123323311949751266331066367"));
        assertEquals(
                TypeEncoder.encodeNumeric(max136),
                ("0000000000000000000000000000007fffffffffffffffffffffffffffffffff"));

        Int min136 = new Int136(new BigInteger("-43556142965880123323311949751266331066368"));
        assertEquals(
                TypeEncoder.encodeNumeric(min136),
                ("ffffffffffffffffffffffffffffff8000000000000000000000000000000000"));

        Int zero144 = new Int144(BigInteger.ZERO);

        assertEquals(
                TypeEncoder.encodeNumeric(zero144),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max144 = new Int144(new BigInteger("11150372599265311570767859136324180752990207"));
        assertEquals(
                TypeEncoder.encodeNumeric(max144),
                ("00000000000000000000000000007fffffffffffffffffffffffffffffffffff"));

        Int min144 = new Int144(new BigInteger("-11150372599265311570767859136324180752990208"));
        assertEquals(
                TypeEncoder.encodeNumeric(min144),
                ("ffffffffffffffffffffffffffff800000000000000000000000000000000000"));

        Int zero152 = new Int152(BigInteger.ZERO);

        assertEquals(
                TypeEncoder.encodeNumeric(zero152),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max152 = new Int152(new BigInteger("2854495385411919762116571938898990272765493247"));
        assertEquals(
                TypeEncoder.encodeNumeric(max152),
                ("000000000000000000000000007fffffffffffffffffffffffffffffffffffff"));

        Int min152 = new Int152(new BigInteger("-2854495385411919762116571938898990272765493248"));
        assertEquals(
                TypeEncoder.encodeNumeric(min152),
                ("ffffffffffffffffffffffffff80000000000000000000000000000000000000"));

        Int zero160 = new Int160(BigInteger.ZERO);

        assertEquals(
                TypeEncoder.encodeNumeric(zero160),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max160 = new Int160(new BigInteger("730750818665451459101842416358141509827966271487"));
        assertEquals(
                TypeEncoder.encodeNumeric(max160),
                ("0000000000000000000000007fffffffffffffffffffffffffffffffffffffff"));

        Int min160 =
                new Int160(new BigInteger("-730750818665451459101842416358141509827966271488"));
        assertEquals(
                TypeEncoder.encodeNumeric(min160),
                ("ffffffffffffffffffffffff8000000000000000000000000000000000000000"));

        Int zero168 = new Int168(BigInteger.ZERO);

        assertEquals(
                TypeEncoder.encodeNumeric(zero168),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max168 =
                new Int168(new BigInteger("187072209578355573530071658587684226515959365500927"));
        assertEquals(
                TypeEncoder.encodeNumeric(max168),
                ("00000000000000000000007fffffffffffffffffffffffffffffffffffffffff"));

        Int min168 =
                new Int168(new BigInteger("-187072209578355573530071658587684226515959365500928"));
        assertEquals(
                TypeEncoder.encodeNumeric(min168),
                ("ffffffffffffffffffffff800000000000000000000000000000000000000000"));

        Int zero176 = new Int176(BigInteger.ZERO);

        assertEquals(
                TypeEncoder.encodeNumeric(zero176),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max176 =
                new Int176(new BigInteger("47890485652059026823698344598447161988085597568237567"));
        assertEquals(
                TypeEncoder.encodeNumeric(max176),
                ("000000000000000000007fffffffffffffffffffffffffffffffffffffffffff"));

        Int min176 =
                new Int176(
                        new BigInteger("-47890485652059026823698344598447161988085597568237568"));
        assertEquals(
                TypeEncoder.encodeNumeric(min176),
                ("ffffffffffffffffffff80000000000000000000000000000000000000000000"));

        Int zero184 = new Int184(BigInteger.ZERO);

        assertEquals(
                TypeEncoder.encodeNumeric(zero184),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max184 =
                new Int184(
                        new BigInteger("12259964326927110866866776217202473468949912977468817407"));
        assertEquals(
                TypeEncoder.encodeNumeric(max184),
                ("0000000000000000007fffffffffffffffffffffffffffffffffffffffffffff"));

        Int min184 =
                new Int184(
                        new BigInteger(
                                "-12259964326927110866866776217202473468949912977468817408"));
        assertEquals(
                TypeEncoder.encodeNumeric(min184),
                ("ffffffffffffffffff8000000000000000000000000000000000000000000000"));

        Int zero192 = new Int192(BigInteger.ZERO);

        assertEquals(
                TypeEncoder.encodeNumeric(zero192),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max192 =
                new Int192(
                        new BigInteger(
                                "3138550867693340381917894711603833208051177722232017256447"));
        assertEquals(
                TypeEncoder.encodeNumeric(max192),
                ("00000000000000007fffffffffffffffffffffffffffffffffffffffffffffff"));

        Int min192 =
                new Int192(
                        new BigInteger(
                                "-3138550867693340381917894711603833208051177722232017256448"));
        assertEquals(
                TypeEncoder.encodeNumeric(min192),
                ("ffffffffffffffff800000000000000000000000000000000000000000000000"));

        Int zero200 = new Int200(BigInteger.ZERO);

        assertEquals(
                TypeEncoder.encodeNumeric(zero200),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max200 =
                new Int200(
                        new BigInteger(
                                "803469022129495137770981046170581301261101496891396417650687"));
        assertEquals(
                TypeEncoder.encodeNumeric(max200),
                ("000000000000007fffffffffffffffffffffffffffffffffffffffffffffffff"));

        Int min200 =
                new Int200(
                        new BigInteger(
                                "-803469022129495137770981046170581301261101496891396417650688"));
        assertEquals(
                TypeEncoder.encodeNumeric(min200),
                ("ffffffffffffff80000000000000000000000000000000000000000000000000"));

        Int zero208 = new Int208(BigInteger.ZERO);

        assertEquals(
                TypeEncoder.encodeNumeric(zero208),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max208 =
                new Int208(
                        new BigInteger(
                                "205688069665150755269371147819668813122841983204197482918576127"));
        assertEquals(
                TypeEncoder.encodeNumeric(max208),
                ("0000000000007fffffffffffffffffffffffffffffffffffffffffffffffffff"));

        Int min208 =
                new Int208(
                        new BigInteger(
                                "-205688069665150755269371147819668813122841983204197482918576128"));
        assertEquals(
                TypeEncoder.encodeNumeric(min208),
                ("ffffffffffff8000000000000000000000000000000000000000000000000000"));

        Int zero216 = new Int216(BigInteger.ZERO);

        assertEquals(
                TypeEncoder.encodeNumeric(zero216),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max216 =
                new Int216(
                        new BigInteger(
                                "52656145834278593348959013841835216159447547700274555627155488767"));
        assertEquals(
                TypeEncoder.encodeNumeric(max216),
                ("00000000007fffffffffffffffffffffffffffffffffffffffffffffffffffff"));

        Int min216 =
                new Int216(
                        new BigInteger(
                                "-52656145834278593348959013841835216159447547700274555627155488768"));
        assertEquals(
                TypeEncoder.encodeNumeric(min216),
                ("ffffffffff800000000000000000000000000000000000000000000000000000"));

        Int zero224 = new Int224(BigInteger.ZERO);

        assertEquals(
                TypeEncoder.encodeNumeric(zero224),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max224 =
                new Int224(
                        new BigInteger(
                                "13479973333575319897333507543509815336818572211270286240551805124607"));
        assertEquals(
                TypeEncoder.encodeNumeric(max224),
                ("000000007fffffffffffffffffffffffffffffffffffffffffffffffffffffff"));

        Int min224 =
                new Int224(
                        new BigInteger(
                                "-13479973333575319897333507543509815336818572211270286240551805124608"));
        assertEquals(
                TypeEncoder.encodeNumeric(min224),
                ("ffffffff80000000000000000000000000000000000000000000000000000000"));

        Int zero232 = new Int232(BigInteger.ZERO);

        assertEquals(
                TypeEncoder.encodeNumeric(zero232),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max232 =
                new Int232(
                        new BigInteger(
                                "3450873173395281893717377931138512726225554486085193277581262111899647"));
        assertEquals(
                TypeEncoder.encodeNumeric(max232),
                ("0000007fffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));

        Int min232 =
                new Int232(
                        new BigInteger(
                                "-3450873173395281893717377931138512726225554486085193277581262111899648"));
        assertEquals(
                TypeEncoder.encodeNumeric(min232),
                ("ffffff8000000000000000000000000000000000000000000000000000000000"));

        Int zero240 = new Int240(BigInteger.ZERO);

        assertEquals(
                TypeEncoder.encodeNumeric(zero240),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max240 =
                new Int240(
                        new BigInteger(
                                "883423532389192164791648750371459257913741948437809479060803100646309887"));
        assertEquals(
                TypeEncoder.encodeNumeric(max240),
                ("00007fffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));

        Int min240 =
                new Int240(
                        new BigInteger(
                                "-883423532389192164791648750371459257913741948437809479060803100646309888"));
        assertEquals(
                TypeEncoder.encodeNumeric(min240),
                ("ffff800000000000000000000000000000000000000000000000000000000000"));

        Int zero248 = new Int248(BigInteger.ZERO);

        assertEquals(
                TypeEncoder.encodeNumeric(zero248),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Int max248 =
                new Int248(
                        new BigInteger(
                                "226156424291633194186662080095093570025917938800079226639565593765455331327"));
        assertEquals(
                TypeEncoder.encodeNumeric(max248),
                ("007fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));

        Int min248 =
                new Int248(
                        new BigInteger(
                                "-226156424291633194186662080095093570025917938800079226639565593765455331328"));
        assertEquals(
                TypeEncoder.encodeNumeric(min248),
                ("ff80000000000000000000000000000000000000000000000000000000000000"));
        Int minusOne = new Int(BigInteger.valueOf(-1));
        assertEquals(
                TypeEncoder.encodeNumeric(minusOne),
                ("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));
    }

    /*
    TODO: Enable once Solidity supports fixed types - see
    https://github.com/ethereum/solidity/issues/409

    @Test
    public void testUfixedEncode() {
        Ufixed zero = new Ufixed24x40(BigInteger.ZERO);
        assertEquals(TypeEncoder.encodeNumeric(zero),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Ufixed maxLong = new Ufixed24x40(BigInteger.valueOf(Long.MAX_VALUE));
        assertEquals(TypeEncoder.encodeNumeric(maxLong),
                ("0000000000000000000000000000000000000000000000007fffffffffffffff"));

        Ufixed maxValue = new Ufixed(
                new BigInteger("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                        16));
        assertEquals(TypeEncoder.encodeNumeric(maxValue),
                ("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));
    }

    @Test
    public void testFixedEncode() {
        Fixed zero = new Fixed24x40(BigInteger.ZERO);
        assertEquals(TypeEncoder.encodeNumeric(zero),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Fixed maxLong = new Fixed24x40(BigInteger.valueOf(Long.MAX_VALUE));
        assertEquals(TypeEncoder.encodeNumeric(maxLong),
                ("0000000000000000000000000000000000000000000000007fffffffffffffff"));

        Fixed minLong = new Fixed24x40(BigInteger.valueOf(Long.MIN_VALUE));
        assertEquals(TypeEncoder.encodeNumeric(minLong),
                ("ffffffffffffffffffffffffffffffffffffffffffffffff8000000000000000"));

        Fixed minusOne = new Fixed24x40(BigInteger.valueOf(-1));
        assertEquals(TypeEncoder.encodeNumeric(minusOne),
                ("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));
    }
    */

    @Test
    public void testStaticBytes() {
        Bytes staticBytes = new Bytes6(new byte[] {0, 1, 2, 3, 4, 5});
        assertEquals(
                TypeEncoder.encodeBytes(staticBytes),
                ("0001020304050000000000000000000000000000000000000000000000000000"));

        Bytes empty = new Bytes1(new byte[] {0});
        assertEquals(
                TypeEncoder.encodeBytes(empty),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        Bytes ones = new Bytes1(new byte[] {127});
        assertEquals(
                TypeEncoder.encodeBytes(ones),
                ("7f00000000000000000000000000000000000000000000000000000000000000"));

        Bytes dave = new Bytes4("dave".getBytes());
        assertEquals(
                TypeEncoder.encodeBytes(dave),
                ("6461766500000000000000000000000000000000000000000000000000000000"));
    }

    @Test
    public void testDynamicBytes() {
        DynamicBytes dynamicBytes = new DynamicBytes(new byte[] {0, 1, 2, 3, 4, 5});
        assertEquals(
                TypeEncoder.encodeDynamicBytes(dynamicBytes),
                ("0000000000000000000000000000000000000000000000000000000000000006"
                        + "0001020304050000000000000000000000000000000000000000000000000000"));

        DynamicBytes empty = new DynamicBytes(new byte[] {0});
        assertEquals(
                TypeEncoder.encodeDynamicBytes(empty),
                ("0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000000"));

        DynamicBytes dave = new DynamicBytes("dave".getBytes());
        assertEquals(
                TypeEncoder.encodeDynamicBytes(dave),
                ("0000000000000000000000000000000000000000000000000000000000000004"
                        + "6461766500000000000000000000000000000000000000000000000000000000"));

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
                TypeEncoder.encodeDynamicBytes(loremIpsum),
                ("00000000000000000000000000000000000000000000000000000000000001bd"
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
                        + "74206d6f6c6c697420616e696d20696420657374206c61626f72756d2e000000"));
    }

    @Test
    public void testAddress() {
        Address address = new Address("0xbe5422d15f39373eb0a97ff8c10fbd0e40e29338");
        assertEquals(address.getTypeAsString(), ("address"));
        assertEquals(
                TypeEncoder.encodeAddress(address),
                ("000000000000000000000000be5422d15f39373eb0a97ff8c10fbd0e40e29338"));
    }

    @Test
    public void testInvalidAddress() {
        assertThrows(
                UnsupportedOperationException.class,
                () ->
                        new Address(
                                "0xa04462684b510796c186d19abfa6929742f79394583d6efb1243bbb473f21d9f"));
    }

    @Test
    public void testLongAddress() {
        Address address =
                new Address(
                        256, "0xa04462684b510796c186d19abfa6929742f79394583d6efb1243bbb473f21d9f");
        assertEquals(address.getTypeAsString(), ("address"));
        TypeEncoder.encodeAddress(address);
    }

    @Test
    public void testUtf8String() {
        Utf8String string = new Utf8String("Hello, world!");
        assertEquals(
                TypeEncoder.encodeString(string),
                ("000000000000000000000000000000000000000000000000000000000000000d"
                        + "48656c6c6f2c20776f726c642100000000000000000000000000000000000000"));
    }

    @Test
    public void testFixedArray() {
        StaticArray<Ufixed> array =
                new StaticArray2<>(
                        Ufixed.class,
                        new Ufixed(BigInteger.valueOf(0x2), BigInteger.valueOf(0x2)),
                        new Ufixed(BigInteger.valueOf(0x8), BigInteger.valueOf(0x8)));

        assertEquals(
                TypeEncoder.encodeArrayValues(array),
                ("0000000000000000000000000000000220000000000000000000000000000000"
                        + "0000000000000000000000000000000880000000000000000000000000000000"));
    }

    @Test
    public void testDynamicArray() {
        DynamicArray<Uint> array =
                new DynamicArray<>(
                        Uint.class,
                        new Uint(BigInteger.ONE),
                        new Uint(BigInteger.valueOf(2)),
                        new Uint(BigInteger.valueOf(3)));

        assertEquals(
                TypeEncoder.encodeDynamicArray(array),
                ("0000000000000000000000000000000000000000000000000000000000000003"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "0000000000000000000000000000000000000000000000000000000000000003"));
    }

    @Test
    public void testDynamicStringsArray() {
        DynamicArray<Utf8String> array =
                new DynamicArray<>(
                        Utf8String.class,
                        new Utf8String("web3j"),
                        new Utf8String("arrays"),
                        new Utf8String("encoding"));

        assertEquals(
                ("0000000000000000000000000000000000000000000000000000000000000003"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "00000000000000000000000000000000000000000000000000000000000000a0"
                        + "00000000000000000000000000000000000000000000000000000000000000e0"
                        + "0000000000000000000000000000000000000000000000000000000000000005"
                        + "776562336a000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000006"
                        + "6172726179730000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000008"
                        + "656e636f64696e67000000000000000000000000000000000000000000000000"),
                TypeEncoder.encodeDynamicArray(array));
    }

    @Test
    public void testDynamicUtf8StringsArray() {
        // Test for issue #1741: non-ASCII characters (Chinese, Korean, etc.) encoding
        DynamicArray<Utf8String> array =
                new DynamicArray<>(Utf8String.class, new Utf8String("你好"), new Utf8String("世界"));

        // "你好" UTF-8 = E4BDA0E5A5BD (6 bytes)
        // "世界" UTF-8 = E4B896E7958C (6 bytes)
        assertEquals(
                ("0000000000000000000000000000000000000000000000000000000000000002"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000006"
                        + "e4bda0e5a5bd0000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000006"
                        + "e4b896e7958c0000000000000000000000000000000000000000000000000000"),
                TypeEncoder.encodeDynamicArray(array));
    }

    @Test
    public void testDynamicArrayOfDynamicArraysOfStaticStructs() {
        DynamicArray<DynamicArray<Bar>> array =
                new DynamicArray(
                        DynamicArray.class,
                        Arrays.asList(
                                new DynamicArray(
                                        Bar.class,
                                        new Bar(
                                                new Uint256(BigInteger.ZERO),
                                                new Uint256(BigInteger.ZERO))),
                                new DynamicArray(
                                        Bar.class,
                                        new Bar(
                                                new Uint256(BigInteger.ONE),
                                                new Uint256(BigInteger.ZERO)))));
        assertEquals(
                ("0000000000000000000000000000000000000000000000000000000000000002"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "00000000000000000000000000000000000000000000000000000000000000a0"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000000"),
                TypeEncoder.encodeDynamicArray(array));
    }

    @Test
    public void testStructsDynamicArray() {
        DynamicArray<Foo> array =
                new DynamicArray<>(
                        Foo.class,
                        new Foo("id", "name"),
                        new Foo("id", "name"),
                        new Foo("id", "name"));

        assertEquals(
                TypeEncoder.encodeDynamicArray(array),
                ("0000000000000000000000000000000000000000000000000000000000000003"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000120"
                        + "00000000000000000000000000000000000000000000000000000000000001e0"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000"));
    }

    @Test
    public void testDynamicStructStaticArray() {
        StaticArray3<Foo> array =
                new StaticArray3<>(
                        Foo.class, new Foo("", ""), new Foo("id", "name"), new Foo("", ""));

        assertEquals(
                ("0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"),
                TypeEncoder.encodeArrayValues(array));
    }

    @Test
    public void testStaticStructStaticArray() {
        StaticArray3<Bar> array =
                new StaticArray3<>(
                        Bar.class,
                        new Bar(BigInteger.ONE, BigInteger.ZERO),
                        new Bar(BigInteger.ONE, BigInteger.ZERO),
                        new Bar(BigInteger.ONE, BigInteger.ZERO));

        assertEquals(
                TypeEncoder.encodeArrayValues(array),
                ("0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000000"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEmptyArray() {
        DynamicArray<Uint> array = new DynamicArray(Uint.class);
        assertEquals(
                TypeEncoder.encodeDynamicArray(array),
                ("0000000000000000000000000000000000000000000000000000000000000000"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testArrayOfBytes() {
        DynamicArray<DynamicBytes> array =
                new DynamicArray<>(
                        new DynamicBytes(
                                Numeric.hexStringToByteArray(
                                        "0x3c329ee8cd725a7f74f984cac52598eb170d731e7f3"
                                                + "80d59a18aa861d2c8d6c43c880b2bfe0f3cde4efcd7"
                                                + "11c010c2f1d8af5e796f06716539446f95420df4211c")),
                        new DynamicBytes(
                                Numeric.hexStringToByteArray("0xcafe0000cafe0000cafe0000")),
                        new DynamicBytes(
                                Numeric.hexStringToByteArray(
                                        "0x9215c928b97e0ebeeefd10003a4e3eea23f2eb3acba"
                                                + "b477eeb589d7a8874d7c5")));
        DynamicArray emptyArray = DynamicArray.empty("bytes");
        DynamicArray<DynamicBytes> arrayOfEmptyBytes =
                new DynamicArray<>(new DynamicBytes(new byte[0]), new DynamicBytes(new byte[0]));

        assertEquals(
                TypeEncoder.encodeDynamicArray(array),
                //  array length
                ("0000000000000000000000000000000000000000000000000000000000000003"
                        // offset first bytes
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        // offset second bytes
                        + "00000000000000000000000000000000000000000000000000000000000000e0"
                        // offset third bytes
                        + "0000000000000000000000000000000000000000000000000000000000000120"
                        // length first bytes
                        + "0000000000000000000000000000000000000000000000000000000000000041"
                        // first bytes
                        + "3c329ee8cd725a7f74f984cac52598eb170d731e7f380d59a18aa861d2c8d6c4"
                        // first bytes continued
                        + "3c880b2bfe0f3cde4efcd711c010c2f1d8af5e796f06716539446f95420df421"
                        // first bytes continued
                        + "1c00000000000000000000000000000000000000000000000000000000000000"
                        // length second bytes
                        + "000000000000000000000000000000000000000000000000000000000000000c"
                        // second bytes
                        + "cafe0000cafe0000cafe00000000000000000000000000000000000000000000"
                        // length third bytes
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        // third bytes
                        + "9215c928b97e0ebeeefd10003a4e3eea23f2eb3acbab477eeb589d7a8874d7c5"));
        assertEquals(
                TypeEncoder.encodeDynamicArray(emptyArray),
                //  array length
                ("0000000000000000000000000000000000000000000000000000000000000000"));
        assertEquals(
                TypeEncoder.encodeDynamicArray(arrayOfEmptyBytes),
                //  array length
                ("0000000000000000000000000000000000000000000000000000000000000002"
                        // offset first bytes
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        // offset second bytes
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        // length first bytes
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        // length second bytes
                        + "0000000000000000000000000000000000000000000000000000000000000000"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testArrayOfStrings() {
        DynamicArray<Utf8String> array =
                new DynamicArray<>(
                        new Utf8String(
                                "This string value is extra long so that it "
                                        + "requires more than 32 bytes"),
                        new Utf8String("abc"),
                        new Utf8String(""),
                        new Utf8String("web3j"));
        DynamicArray emptyArray = DynamicArray.empty("string");
        DynamicArray<Utf8String> arrayOfEmptyStrings =
                new DynamicArray<>(new Utf8String(""), new Utf8String(""));

        assertEquals(
                TypeEncoder.encodeDynamicArray(array),
                //  array length
                ("0000000000000000000000000000000000000000000000000000000000000004"
                        // offset first string
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        // offset second string
                        + "0000000000000000000000000000000000000000000000000000000000000100"
                        // offset third string
                        + "0000000000000000000000000000000000000000000000000000000000000140"
                        // offset fourth string
                        + "0000000000000000000000000000000000000000000000000000000000000160"
                        // length first string
                        + "0000000000000000000000000000000000000000000000000000000000000046"
                        // first string
                        + "5468697320737472696e672076616c7565206973206578747261206c6f6e6720"
                        // first string continued
                        + "736f2074686174206974207265717569726573206d6f7265207468616e203332"
                        // first string continued
                        + "2062797465730000000000000000000000000000000000000000000000000000"
                        // length second string
                        + "0000000000000000000000000000000000000000000000000000000000000003"
                        // second string
                        + "6162630000000000000000000000000000000000000000000000000000000000"
                        // length third string
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        // length fourth string
                        + "0000000000000000000000000000000000000000000000000000000000000005"
                        // fourth string
                        + "776562336a000000000000000000000000000000000000000000000000000000"));
        assertEquals(
                TypeEncoder.encodeDynamicArray(emptyArray),
                //  array length
                ("0000000000000000000000000000000000000000000000000000000000000000"));
        assertEquals(
                TypeEncoder.encodeDynamicArray(arrayOfEmptyStrings),
                //  array length
                ("0000000000000000000000000000000000000000000000000000000000000002"
                        // offset first string
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        // offset second string
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        // length first string
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        // length second string
                        + "0000000000000000000000000000000000000000000000000000000000000000"));
    }

    @Test
    public void testPrimitiveByte() {
        assertEquals(
                encode(new Byte((byte) 0)),
                ("0000000000000000000000000000000000000000000000000000000000000000"));
        assertEquals(
                encode(new Byte((byte) 127)),
                ("7f00000000000000000000000000000000000000000000000000000000000000"));
    }

    @Test
    public void testPrimitiveChar() {
        assertEquals(
                encode(new Char('a')),
                ("0000000000000000000000000000000000000000000000000000000000000001"
                        + "6100000000000000000000000000000000000000000000000000000000000000"));
        assertEquals(
                encode(new Char(' ')),
                ("0000000000000000000000000000000000000000000000000000000000000001"
                        + "2000000000000000000000000000000000000000000000000000000000000000"));
    }

    @Test
    public void testPrimitiveInt() {
        assertEquals(
                encode(new org.web3j.abi.datatypes.primitive.Int(0)),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        assertEquals(
                encode(new org.web3j.abi.datatypes.primitive.Int(Integer.MIN_VALUE)),
                ("ffffffffffffffffffffffffffffffffffffffffffffffffffffffff80000000"));

        assertEquals(
                encode(new org.web3j.abi.datatypes.primitive.Int(Integer.MAX_VALUE)),
                ("000000000000000000000000000000000000000000000000000000007fffffff"));
    }

    @Test
    public void testPrimitiveShort() {
        assertEquals(
                encode(new Short((short) 0)),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        assertEquals(
                encode(new Short(java.lang.Short.MIN_VALUE)),
                ("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8000"));

        assertEquals(
                encode(new Short(java.lang.Short.MAX_VALUE)),
                ("0000000000000000000000000000000000000000000000000000000000007fff"));
    }

    @Test
    public void testPrimitiveLong() {
        assertEquals(
                encode(new Long(0)),
                ("0000000000000000000000000000000000000000000000000000000000000000"));

        assertEquals(
                encode(new Long(java.lang.Long.MIN_VALUE)),
                ("ffffffffffffffffffffffffffffffffffffffffffffffff8000000000000000"));

        assertEquals(
                encode(new Long(java.lang.Long.MAX_VALUE)),
                ("0000000000000000000000000000000000000000000000007fffffffffffffff"));
    }

    @Test
    public void testPrimitiveFloat() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> encode(new org.web3j.abi.datatypes.primitive.Float(0)));
    }

    @Test
    public void testPrimitiveDouble() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> encode(new org.web3j.abi.datatypes.primitive.Double(0)));
    }

    @Test
    public void testStructContainingDynamicBytes() {
        String expectedEncoding =
                "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "00000000000000000000000000000000000000000000000000000000000000a0"
                        + "0000000000000000000000000000000000000000000000000000000000000007"
                        + "64796e616d696300000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000005"
                        + "4279746573000000000000000000000000000000000000000000000000000000";
        assertEquals(
                expectedEncoding,
                encode(AbiV2TestFixture.addDynamicBytesArrayFunction.getInputParameters().get(0)));
    }
}

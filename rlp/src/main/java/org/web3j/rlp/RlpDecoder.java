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

import java.util.ArrayList;

/**
 * Recursive Length Prefix (RLP) decoder.
 *
 * <p>For the specification, refer to p16 of the <a href="http://gavwood.com/paper.pdf">yellow
 * paper</a> and <a href="https://github.com/ethereum/wiki/wiki/RLP">here</a>.
 */
public class RlpDecoder {

    /**
     * [0x80] If a string is 0-55 bytes long, the RLP encoding consists of a single byte with value
     * 0x80 plus the length of the string followed by the string. The range of the first byte is
     * thus [0x80, 0xb7].
     */
    public static int OFFSET_SHORT_STRING = 0x80;

    /**
     * [0xb7] If a string is more than 55 bytes long, the RLP encoding consists of a single byte
     * with value 0xb7 plus the length of the length of the string in binary form, followed by the
     * length of the string, followed by the string. For example, a length-1024 string would be
     * encoded as \xb9\x04\x00 followed by the string. The range of the first byte is thus [0xb8,
     * 0xbf].
     */
    public static int OFFSET_LONG_STRING = 0xb7;

    /**
     * [0xc0] If the total payload of a list (i.e. the combined length of all its items) is 0-55
     * bytes long, the RLP encoding consists of a single byte with value 0xc0 plus the length of the
     * list followed by the concatenation of the RLP encodings of the items. The range of the first
     * byte is thus [0xc0, 0xf7].
     */
    public static int OFFSET_SHORT_LIST = 0xc0;

    /**
     * [0xf7] If the total payload of a list is more than 55 bytes long, the RLP encoding consists
     * of a single byte with value 0xf7 plus the length of the length of the list in binary form,
     * followed by the length of the list, followed by the concatenation of the RLP encodings of the
     * items. The range of the first byte is thus [0xf8, 0xff].
     */
    public static int OFFSET_LONG_LIST = 0xf7;

    /**
     * Parse wire byte[] message into RLP elements.
     *
     * @param rlpEncoded - RLP encoded byte-array
     * @return recursive RLP structure
     */
    public static RlpList decode(byte[] rlpEncoded) {
        RlpList rlpList = new RlpList(new ArrayList<>());
        traverse(rlpEncoded, 0, rlpEncoded.length, rlpList);
        return rlpList;
    }

    private static void traverse(byte[] data, int startPos, int endPos, RlpList rlpList) {

        try {
            if (data == null || data.length == 0) {
                return;
            }

            // endPos is derived from input data during recursion, so we must validate it
            if (endPos < 0 || endPos > data.length) {
                throw new RuntimeException("RLP invalid parameters while decoding");
            }

            while (startPos < endPos) {

                int prefix = data[startPos] & 0xff;

                if (prefix < OFFSET_SHORT_STRING) {

                    // 1. the data is a string if the range of the
                    // first byte(i.e. prefix) is [0x00, 0x7f],
                    // and the string is the first byte itself exactly;

                    byte[] rlpData = {(byte) prefix};
                    rlpList.getValues().add(RlpString.create(rlpData));
                    startPos += 1;

                } else if (prefix == OFFSET_SHORT_STRING) {

                    // null
                    rlpList.getValues().add(RlpString.create(new byte[0]));
                    startPos += 1;

                } else if (prefix > OFFSET_SHORT_STRING && prefix <= OFFSET_LONG_STRING) {

                    // 2. the data is a string if the range of the
                    // first byte is [0x80, 0xb7], and the string
                    // which length is equal to the first byte minus 0x80
                    // follows the first byte;

                    byte strLen = (byte) (prefix - OFFSET_SHORT_STRING);

                    // Input validation
                    if (strLen > endPos - (startPos + 1)) {
                        throw new RuntimeException("RLP length mismatch");
                    }

                    byte[] rlpData = new byte[strLen];
                    System.arraycopy(data, startPos + 1, rlpData, 0, strLen);

                    rlpList.getValues().add(RlpString.create(rlpData));
                    startPos += 1 + strLen;

                } else if (prefix > OFFSET_LONG_STRING && prefix < OFFSET_SHORT_LIST) {

                    // 3. the data is a string if the range of the
                    // first byte is [0xb8, 0xbf], and the length of the
                    // string which length in bytes is equal to the
                    // first byte minus 0xb7 follows the first byte,
                    // and the string follows the length of the string;

                    byte lenOfStrLen = (byte) (prefix - OFFSET_LONG_STRING);
                    int strLen = calcLength(lenOfStrLen, data, startPos);

                    // Input validation
                    if (strLen > endPos - (startPos + lenOfStrLen + 1)) {
                        throw new RuntimeException("RLP length mismatch");
                    }

                    // now we can parse an item for data[1]..data[length]
                    byte[] rlpData = new byte[strLen];
                    System.arraycopy(data, startPos + lenOfStrLen + 1, rlpData, 0, strLen);

                    rlpList.getValues().add(RlpString.create(rlpData));
                    startPos += lenOfStrLen + strLen + 1;

                } else if (prefix >= OFFSET_SHORT_LIST && prefix <= OFFSET_LONG_LIST) {

                    // 4. the data is a list if the range of the
                    // first byte is [0xc0, 0xf7], and the concatenation of
                    // the RLP encodings of all items of the list which the
                    // total payload is equal to the first byte minus 0xc0 follows the first byte;

                    byte listLen = (byte) (prefix - OFFSET_SHORT_LIST);

                    // Input validation
                    if (listLen > endPos - (startPos + 1)) {
                        throw new RuntimeException("RLP length mismatch");
                    }

                    RlpList newLevelList = new RlpList(new ArrayList<>());
                    traverse(data, startPos + 1, startPos + listLen + 1, newLevelList);
                    rlpList.getValues().add(newLevelList);

                    startPos += 1 + listLen;

                } else if (prefix > OFFSET_LONG_LIST) {

                    // 5. the data is a list if the range of the
                    // first byte is [0xf8, 0xff], and the total payload of the
                    // list which length is equal to the
                    // first byte minus 0xf7 follows the first byte,
                    // and the concatenation of the RLP encodings of all items of
                    // the list follows the total payload of the list;

                    byte lenOfListLen = (byte) (prefix - OFFSET_LONG_LIST);
                    int listLen = calcLength(lenOfListLen, data, startPos);

                    // Input validation
                    if (listLen > endPos - (startPos + lenOfListLen + 1)) {
                        throw new RuntimeException("RLP length mismatch");
                    }

                    RlpList newLevelList = new RlpList(new ArrayList<>());
                    traverse(
                            data,
                            startPos + lenOfListLen + 1,
                            startPos + lenOfListLen + listLen + 1,
                            newLevelList);
                    rlpList.getValues().add(newLevelList);

                    startPos += lenOfListLen + listLen + 1;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("RLP wrong encoding", e);
        }
    }

    private static int calcLength(int lengthOfLength, byte[] data, int pos) {
        byte pow = (byte) (lengthOfLength - 1);
        long length = 0;
        for (int i = 1; i <= lengthOfLength; ++i) {
            length += ((long) (data[pos + i] & 0xff)) << (8 * pow);
            pow--;
        }
        if (length < 0 || length > Integer.MAX_VALUE) {
            throw new RuntimeException("RLP too many bytes to decode");
        }
        return (int) length;
    }
}

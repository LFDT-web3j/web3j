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
package org.web3j.crypto;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WalletFileTest {

    @Test
    void equalsAndHashCodeTest() throws IOException {

        final String AES_128_CTR =
                "{\n"
                        + "    \"crypto\" : {\n"
                        + "        \"cipher\" : \"aes-128-ctr\",\n"
                        + "        \"cipherparams\" : {\n"
                        + "            \"iv\" : \"02ebc768684e5576900376114625ee6f\"\n"
                        + "        },\n"
                        + "        \"ciphertext\" : \"7ad5c9dd2c95f34a92ebb86740b92103a5d1cc4c2eabf3b9a59e1f83f3181216\",\n"
                        + "        \"kdf\" : \"pbkdf2\",\n"
                        + "        \"kdfparams\" : {\n"
                        + "            \"c\" : 262144,\n"
                        + "            \"dklen\" : 32,\n"
                        + "            \"prf\" : \"hmac-sha256\",\n"
                        + "            \"salt\" : \"0e4cf3893b25bb81efaae565728b5b7cde6a84e224cbf9aed3d69a31c981b702\"\n"
                        + "        },\n"
                        + "        \"mac\" : \"2b29e4641ec17f4dc8b86fc8592090b50109b372529c30b001d4d96249edaf62\"\n"
                        + "    },\n"
                        + "    \"id\" : \"af0451b4-6020-4ef0-91ec-794a5a965b01\",\n"
                        + "    \"version\" : 3\n"
                        + "}";
        ObjectMapper objectMapper = new ObjectMapper();
        WalletFile walletFile1 = objectMapper.readValue(AES_128_CTR, WalletFile.class);

        WalletFile.Crypto crypto = new WalletFile.Crypto();
        crypto.setCipher("aes-128-ctr");
        crypto.setCiphertext("7ad5c9dd2c95f34a92ebb86740b92103a5d1cc4c2eabf3b9a59e1f83f3181216");
        crypto.setKdf("pbkdf2");
        crypto.setMac("2b29e4641ec17f4dc8b86fc8592090b50109b372529c30b001d4d96249edaf62");

        WalletFile.CipherParams cipherParams = new WalletFile.CipherParams();
        cipherParams.setIv("02ebc768684e5576900376114625ee6f");
        crypto.setCipherparams(cipherParams);

        WalletFile.Aes128CtrKdfParams kdfParams = new WalletFile.Aes128CtrKdfParams();
        kdfParams.setC(262144);
        kdfParams.setDklen(32);
        kdfParams.setPrf("hmac-sha256");
        kdfParams.setSalt("0e4cf3893b25bb81efaae565728b5b7cde6a84e224cbf9aed3d69a31c981b702");
        crypto.setKdfparams(kdfParams);

        WalletFile walletFile2 = new WalletFile();
        walletFile2.setCrypto(crypto);
        walletFile2.setVersion(3);
        walletFile2.setId("af0451b4-6020-4ef0-91ec-794a5a965b01");

        assertEquals(walletFile1, walletFile2);
        assertEquals(walletFile1.hashCode(), walletFile2.hashCode());
    }
}

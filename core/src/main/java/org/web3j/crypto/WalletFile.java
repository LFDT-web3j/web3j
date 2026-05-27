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

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.node.ObjectNode;

/** Ethereum wallet file. */
public class WalletFile {
    private String address;
    private Crypto crypto;
    private String id;
    private int version;

    public WalletFile() {}

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Crypto getCrypto() {
        return crypto;
    }

    @JsonSetter("crypto")
    public void setCrypto(Crypto crypto) {
        this.crypto = crypto;
    }

    @JsonSetter("Crypto") // older wallet files may have this attribute name
    public void setCryptoV1(Crypto crypto) {
        setCrypto(crypto);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WalletFile)) {
            return false;
        }

        WalletFile that = (WalletFile) o;

        if (getAddress() != null
                ? !getAddress().equals(that.getAddress())
                : that.getAddress() != null) {
            return false;
        }
        if (getCrypto() != null
                ? !getCrypto().equals(that.getCrypto())
                : that.getCrypto() != null) {
            return false;
        }
        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) {
            return false;
        }
        return version == that.version;
    }

    @Override
    public int hashCode() {
        int result = getAddress() != null ? getAddress().hashCode() : 0;
        result = 31 * result + (getCrypto() != null ? getCrypto().hashCode() : 0);
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        result = 31 * result + version;
        return result;
    }

    public static class Crypto {
        private String cipher;
        private String ciphertext;
        private CipherParams cipherparams;

        private String kdf;
        private KdfParams kdfparams;

        private String mac;

        public Crypto() {}

        public String getCipher() {
            return cipher;
        }

        public void setCipher(String cipher) {
            this.cipher = cipher;
        }

        public String getCiphertext() {
            return ciphertext;
        }

        public void setCiphertext(String ciphertext) {
            this.ciphertext = ciphertext;
        }

        public CipherParams getCipherparams() {
            return cipherparams;
        }

        public void setCipherparams(CipherParams cipherparams) {
            this.cipherparams = cipherparams;
        }

        public String getKdf() {
            return kdf;
        }

        public void setKdf(String kdf) {
            this.kdf = kdf;
        }

        public KdfParams getKdfparams() {
            return kdfparams;
        }

        @JsonTypeInfo(
                use = JsonTypeInfo.Id.NAME,
                include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
                property = "kdf")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = Aes128CtrKdfParams.class, name = Wallet.AES_128_CTR),
            @JsonSubTypes.Type(value = ScryptKdfParams.class, name = Wallet.SCRYPT)
        })
        // To support my Ether Wallet keys uncomment this annotation & comment out the above
        //  @JsonDeserialize(using = KdfParamsDeserialiser.class)
        // Also add the following to the ObjectMapperFactory
        // objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        public void setKdfparams(KdfParams kdfparams) {
            this.kdfparams = kdfparams;
        }

        public String getMac() {
            return mac;
        }

        public void setMac(String mac) {
            this.mac = mac;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Crypto)) {
                return false;
            }

            Crypto that = (Crypto) o;

            if (getCipher() != null
                    ? !getCipher().equals(that.getCipher())
                    : that.getCipher() != null) {
                return false;
            }
            if (getCiphertext() != null
                    ? !getCiphertext().equals(that.getCiphertext())
                    : that.getCiphertext() != null) {
                return false;
            }
            if (getCipherparams() != null
                    ? !getCipherparams().equals(that.getCipherparams())
                    : that.getCipherparams() != null) {
                return false;
            }
            if (getKdf() != null ? !getKdf().equals(that.getKdf()) : that.getKdf() != null) {
                return false;
            }
            if (getKdfparams() != null
                    ? !getKdfparams().equals(that.getKdfparams())
                    : that.getKdfparams() != null) {
                return false;
            }
            return getMac() != null ? getMac().equals(that.getMac()) : that.getMac() == null;
        }

        @Override
        public int hashCode() {
            int result = getCipher() != null ? getCipher().hashCode() : 0;
            result = 31 * result + (getCiphertext() != null ? getCiphertext().hashCode() : 0);
            result = 31 * result + (getCipherparams() != null ? getCipherparams().hashCode() : 0);
            result = 31 * result + (getKdf() != null ? getKdf().hashCode() : 0);
            result = 31 * result + (getKdfparams() != null ? getKdfparams().hashCode() : 0);
            result = 31 * result + (getMac() != null ? getMac().hashCode() : 0);
            return result;
        }
    }

    public static class CipherParams {
        private String iv;

        public CipherParams() {}

        public String getIv() {
            return iv;
        }

        public void setIv(String iv) {
            this.iv = iv;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CipherParams)) {
                return false;
            }

            CipherParams that = (CipherParams) o;

            return getIv() != null ? getIv().equals(that.getIv()) : that.getIv() == null;
        }

        @Override
        public int hashCode() {
            int result = getIv() != null ? getIv().hashCode() : 0;
            return result;
        }
    }

    interface KdfParams {
        int getDklen();

        String getSalt();
    }

    public static class Aes128CtrKdfParams implements KdfParams {
        private int dklen;
        private int c;
        private String prf;
        private String salt;

        public Aes128CtrKdfParams() {}

        public int getDklen() {
            return dklen;
        }

        public void setDklen(int dklen) {
            this.dklen = dklen;
        }

        public int getC() {
            return c;
        }

        public void setC(int c) {
            this.c = c;
        }

        public String getPrf() {
            return prf;
        }

        public void setPrf(String prf) {
            this.prf = prf;
        }

        public String getSalt() {
            return salt;
        }

        public void setSalt(String salt) {
            this.salt = salt;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Aes128CtrKdfParams)) {
                return false;
            }

            Aes128CtrKdfParams that = (Aes128CtrKdfParams) o;

            if (dklen != that.dklen) {
                return false;
            }
            if (c != that.c) {
                return false;
            }
            if (getPrf() != null ? !getPrf().equals(that.getPrf()) : that.getPrf() != null) {
                return false;
            }
            return getSalt() != null ? getSalt().equals(that.getSalt()) : that.getSalt() == null;
        }

        @Override
        public int hashCode() {
            int result = dklen;
            result = 31 * result + c;
            result = 31 * result + (getPrf() != null ? getPrf().hashCode() : 0);
            result = 31 * result + (getSalt() != null ? getSalt().hashCode() : 0);
            return result;
        }
    }

    public static class ScryptKdfParams implements KdfParams {
        private int dklen;
        private int n;
        private int p;
        private int r;
        private String salt;

        public ScryptKdfParams() {}

        public int getDklen() {
            return dklen;
        }

        public void setDklen(int dklen) {
            this.dklen = dklen;
        }

        public int getN() {
            return n;
        }

        public void setN(int n) {
            this.n = n;
        }

        public int getP() {
            return p;
        }

        public void setP(int p) {
            this.p = p;
        }

        public int getR() {
            return r;
        }

        public void setR(int r) {
            this.r = r;
        }

        public String getSalt() {
            return salt;
        }

        public void setSalt(String salt) {
            this.salt = salt;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ScryptKdfParams)) {
                return false;
            }

            ScryptKdfParams that = (ScryptKdfParams) o;

            if (dklen != that.dklen) {
                return false;
            }
            if (n != that.n) {
                return false;
            }
            if (p != that.p) {
                return false;
            }
            if (r != that.r) {
                return false;
            }
            return getSalt() != null ? getSalt().equals(that.getSalt()) : that.getSalt() == null;
        }

        @Override
        public int hashCode() {
            int result = dklen;
            result = 31 * result + n;
            result = 31 * result + p;
            result = 31 * result + r;
            result = 31 * result + (getSalt() != null ? getSalt().hashCode() : 0);
            return result;
        }
    }

    // If we need to work with MyEtherWallet we'll need to use this deserializer, see the
    // following issue https://github.com/kvhnuke/etherwallet/issues/269
    static class KdfParamsDeserialiser extends ValueDeserializer<KdfParams> {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public KdfParams deserialize(
                JsonParser jsonParser, DeserializationContext deserializationContext) {

            ObjectNode root = jsonParser.readValueAsTree();
            KdfParams kdfParams;

            // it would be preferable to detect the class to use based on the kdf parameter in the
            // container object instance
            JsonNode n = root.get("n");
            if (n == null) {
                kdfParams = objectMapper.convertValue(root, Aes128CtrKdfParams.class);
            } else {
                kdfParams = objectMapper.convertValue(root, ScryptKdfParams.class);
            }

            return kdfParams;
        }
    }
}

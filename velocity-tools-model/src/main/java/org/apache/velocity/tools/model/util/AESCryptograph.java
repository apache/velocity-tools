package org.apache.velocity.tools.model.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.velocity.tools.config.ConfigurationException;

/**
 * TODO - document secure random requirement
 */

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;

import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;

/**
 * Basic AES encryption. Please note that it uses the ECB block mode, which has the advantage
 * to not require random bytes, thus providing some *persistence* for the encrypted data, but
 * at the expense of some security weaknesses. The purpose here is just to discourage editing
 * id values in URLs, not to protect state secrets.
 */

public class AESCryptograph implements Cryptograph
{
    @Override
    public void init(String key)
    {
        byte[] bytes = key.getBytes(Charset.defaultCharset());
        if (bytes.length < 16)
        {
            throw new ConfigurationException("not enough secret bytes");
        }
        SecretKey secret = new SecretKeySpec(bytes, 0, 16, ALGORITHM);
        try
        {
            encrypt = Cipher.getInstance(CIPHER);
            encrypt.init(ENCRYPT_MODE, secret);
            decrypt = Cipher.getInstance(CIPHER);
            decrypt.init(DECRYPT_MODE, secret);
        }
        catch (Exception e)
        {
            throw new RuntimeException("cyptograph initialization failed", e);
        }
    }

    @Override
    public byte[] encrypt(String str)
    {
        try
        {
            return encrypt.doFinal(str.getBytes(Charset.defaultCharset()));
        }
        catch (Exception e)
        {
            throw new RuntimeException("encryption failed failed", e);
        }
    }

    @Override
    public String decrypt(byte[] bytes)
    {
        try
        {
            return new String(decrypt.doFinal(bytes), Charset.defaultCharset());
        }
        catch (Exception e)
        {
            throw new RuntimeException("encryption failed failed", e);
        }
    }

    private final String CIPHER = "AES/ECB/PKCS5Padding";
    private final String ALGORITHM = "AES";
    private Cipher encrypt, decrypt;

}

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

import java.io.Serializable;

/**
 * Cryptograph - used to encrypt and decrypt strings.
 *
 *  @author <a href=mailto:claude.brisson@gmail.com>Claude Brisson</a>
 */

public interface Cryptograph extends Serializable
{
    /**
     * init.
     * @param random random string
     */
    void init(String random);

    /**
     * encrypt.
     * @param str string to encrypt
     * @return encrypted string
     */
    byte[] encrypt(String str);

    /**
     * decrypt.
     * @param bytes to decrypt
     * @return decrypted string
     */
    String decrypt(byte[] bytes);
}

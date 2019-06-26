/*
 * MIT License
 *
 * Copyright (c) 2019 Syswin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.syswin.temail.dispatcher.request.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtil {

  private static final String ALGORITHM_MD5 = "MD5";
  private static final String ALGORITHM_SHA224 = "SHA-224";
  private static final String ALGORITHM_SHA256 = "SHA-256";
  private static final String ALGORITHM_SHA384 = "SHA-384";
  private static final String ALGORITHM_SHA512 = "SHA-512";

  /**
   * 对数据进行md5签名
   *
   * @param data 原始数据
   * @return 数据的签名
   */
  public static byte[] md5(byte[] data) {
    return digest(ALGORITHM_MD5, data);
  }

  /**
   * 对数据进行sha224签名
   *
   * @param data 原始数据
   * @return 数据的签名
   */
  public static byte[] sha224(byte[] data) {
    return digest(ALGORITHM_SHA224, data);
  }

  /**
   * 对数据进行sha256签名
   *
   * @param data 原始数据
   * @return 数据的签名
   */
  public static byte[] sha256(byte[] data) {
    return digest(ALGORITHM_SHA256, data);
  }

  /**
   * 对数据进行sha384签名
   *
   * @param data 原始数据
   * @return 数据的签名
   */
  public static byte[] sha384(byte[] data) {
    return digest(ALGORITHM_SHA384, data);
  }

  /**
   * 对数据进行sha512签名
   *
   * @param data 原始数据
   * @return 数据的签名
   */
  public static byte[] sha512(byte[] data) {
    return digest(ALGORITHM_SHA512, data);
  }

  private static MessageDigest getDigest(String algorithm) {
    try {
      return MessageDigest.getInstance(algorithm);
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("Could not find MessageDigest with algorithm \"" + algorithm + "\"", ex);
    }
  }

  public static byte[] digest(String algorithm, byte[] data) {
    return getDigest(algorithm).digest(data);
  }

}

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

public class HexUtil {

  private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

  public static String encodeHex(byte[] bytes) {
    StringBuilder buf = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      buf.append(HEX_CHARS[(b >>> 4) & 0xf]).append(HEX_CHARS[b & 0xf]);
    }
    return buf.toString();
  }

  public static byte[] decodeHex(String data) {
    if ((data.length() & 1) == 1) {
      throw new RuntimeException("illegal hexadecimal number！" + data);
    }
    data = data.toUpperCase();
    byte[] bytes = new byte[data.length() >>> 1];
    for (int i = 0; i < data.length(); ) {
      int b = 0;
      for (int j = 0; j < 2; j++) {
        char c1 = data.charAt(i + j);
        if (c1 >= '0' && c1 <= '9') {
          b = b << 4 & (c1 - '0');
        } else if (c1 >= 'A' && c1 <= 'F') {
          b = b << 4 & (c1 - 'A' + 10);
        } else {
          throw new RuntimeException("illegal hexadecimal number！" + data);
        }
        i++;
      }
      bytes[i >>> 1] = (byte) (b & 0xf);
    }
    return bytes;
  }
}

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

package com.syswin.temail.dispatcher.valid.params;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class ValidParamsTest {

  private ValidParams validParams;

  @Before
  public void init() {
    validParams = new ValidParams();
  }

  @Test
  public void falseIfEmpty() {
    Assertions.assertThat(validParams.isAnValidParam()).isFalse();
  }

  @Test
  public void falseParamsEmpty() {
    validParams.setAuthUri("/uri");
    Assertions.assertThat(validParams.isAnValidParam()).isFalse();
  }

  @Test
  public void falseUriEmpty() {
    validParams.setParams(ImmutableMap.of("key", "value"));
    Assertions.assertThat(validParams.isAnValidParam()).isFalse();
  }


  @Test
  public void falseParamIsEmpty() {
    validParams.setAuthUri("uri");
    validParams.setParams(new HashMap<>());
    Assertions.assertThat(validParams.isAnValidParam()).isFalse();
  }


  @Test
  public void trueIfUriAndParamsIsNotEmpty() {
    validParams.setAuthUri("uri");
    validParams.setParams(ImmutableMap.of("key","value"));
    Assertions.assertThat(validParams.isAnValidParam()).isTrue();
  }



}
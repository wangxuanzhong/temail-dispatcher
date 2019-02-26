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
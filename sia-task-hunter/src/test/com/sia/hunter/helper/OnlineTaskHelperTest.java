package com.sia.hunter.helper;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Arrays;
import java.util.LinkedList;

@RunWith(PowerMockRunner.class)
public class OnlineTaskHelperTest {

  @PrepareForTest(RequestMethod.class)
  @Test
  public void testCheckRequestMethod() {
    final RequestMethod requestMethod1 = PowerMockito.mock(RequestMethod.class);

    Assert.assertFalse(OnlineTaskHelper.checkRequestMethod(null));
    Assert.assertFalse(OnlineTaskHelper.
            checkRequestMethod(new RequestMethod[]{requestMethod1}));

    Assert.assertTrue(OnlineTaskHelper.
            checkRequestMethod(new RequestMethod[]{}));
  }

  @Test
  public void testCheckReturnType() {
    Assert.assertFalse(OnlineTaskHelper.checkReturnType(null));
  }

  @Test
  public void testCheckParameterTypes() {
    Assert.assertEquals(-1, OnlineTaskHelper.checkParameterTypes(null));
    Assert.assertEquals(0,
            OnlineTaskHelper.checkParameterTypes(new Class[]{}));
    Assert.assertEquals(-1,
            OnlineTaskHelper.checkParameterTypes(new Class[]{null}));
    Assert.assertEquals(-1,
            OnlineTaskHelper.checkParameterTypes(new Class[]{null, null}));
  }

  @Test
  public void testCheckHttpPath() {
    Assert.assertFalse(OnlineTaskHelper.checkHttpPath(""));
    Assert.assertFalse(OnlineTaskHelper.checkHttpPath(" "));
  }

  @Test
  public void testEncodeHttpPath() {
    Assert.assertEquals("", OnlineTaskHelper.encodeHttpPath(""));
    Assert.assertEquals(" ", OnlineTaskHelper.encodeHttpPath(" "));
  }

  @Test
  public void testToList() {
    Assert.assertEquals(Arrays.asList("-100"),
            OnlineTaskHelper.toList(new Object[]{-100}));
    Assert.assertEquals(new LinkedList<String>(),
            OnlineTaskHelper.toList(new Object[]{}));
    Assert.assertEquals(new LinkedList<String>(),
            OnlineTaskHelper.toList(null));
  }
}

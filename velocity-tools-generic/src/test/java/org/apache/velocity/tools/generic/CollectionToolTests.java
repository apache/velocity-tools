package org.apache.velocity.tools.generic;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

public class CollectionToolTests
{
  public @Test
  void testNullElements()
  {
    CollectionTool tool = new CollectionTool();

    List lst = Arrays.asList("b", null, "a");
    List sorted = (List)tool.sort(lst);
    assertEquals(2, sorted.size());
    assertEquals("a", sorted.get(0));
    assertEquals("b", sorted.get(1));

    String arr[] = new String[] { "foo", null, "bar"};
    sorted = (List)tool.sort(arr);
    assertEquals(2, sorted.size());
    assertEquals("bar", sorted.get(0));
    assertEquals("foo", sorted.get(1));
  }
}

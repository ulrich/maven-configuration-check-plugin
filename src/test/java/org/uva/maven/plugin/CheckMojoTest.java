package org.uva.maven.plugin;

import junit.framework.Assert;

import org.apache.maven.settings.Server;
import org.junit.Before;
import org.junit.Test;
import org.uva.maven.plugin.CheckMojo;

public class CheckMojoTest {
   private CheckMojoForTest foo;

   @Before
   public void setUp() throws Exception {
      foo = new CheckMojoForTest();
   }

   @Test
   public void testCheckPatternWithoutAdminProfile() {
      Server server = new Server();
      server.setUsername("foo");
      server.setPassword("bar");

      Assert.assertFalse(foo.checkPattern(server));
   }

   @Test
   public void testCheckPatternWithAdminProfile() {
      Server server = new Server();
      server.setUsername("foo_adm");
      server.setPassword("bar");
      Assert.assertTrue(foo.checkPattern(server));
   }

   private class CheckMojoForTest extends CheckMojo {
   }
}

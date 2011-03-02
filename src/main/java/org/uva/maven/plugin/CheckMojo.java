package org.uva.maven.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.settings.Server;

/**
 * This mojo checks if any string pattern like:
 * <ul>
 * <li>adm</li>
 * <li>adm_</li>
 * <li>admin</li>
 * </ul>
 * found in Servers section of settings.xml Maven configuration file.
 * <p>
 * This is avoid to deploy/crush artifacts.
 * </p>
 * 
 * @author Ulrich VACHON
 * @goal check
 * @executionStrategy once-per-session <- this annotation doesn't work...
 */
public class CheckMojo extends AbstractMojo {
   private static final String DEFAULT_PATTERN = "^.*_adm$";

   /**
    * @parameter
    * @optional
    */
   private String[] patternArray;
   /**
    * @parameter default-value="${session}"
    */
   private MavenSession session;

   /**
    * Constructs mojo and initialize pattern array if no plugin configuration found.
    */
   public CheckMojo() {
      if (null == patternArray) {
         patternArray = new String[] {DEFAULT_PATTERN};
      }
   }

   /*
    * @see org.apache.maven.plugin.AbstractMojo#execute()
    */
   public void execute() throws MojoFailureException {
      if (!isNeeded()) {
         return;
      }
      getLog().info("Checking Maven configuration to find administrator login in server settings...");

      for (Object object : session.getSettings().getServers()) {
         if (checkPattern((Server) object)) {
            if (!"y".equalsIgnoreCase(readLine())) {
               throw new MojoFailureException("Unable to continue build execution, stopped by user!");
            }
            break;
         }
      }
   }

   /**
    * Tests if the plugin must run.
    * 
    * @return true if the plugin was never launched.
    */
   protected boolean isNeeded() {
      Properties properties = session.getExecutionProperties();
      String key = (String) properties.get(this.getClass());
      if ((null == key) || (key.length() == 0)) {
         properties.put(this.getClass(), session.getStartTime().toString());
         return true;
      }
      return false;
   }

   /**
    * Checks the existing in settings.xml file found.
    * 
    * @param server the server object where are storing the server properties.
    * @return true if an existing pattern was found.
    */
   protected boolean checkPattern(Server server) {
      for (String pattern : patternArray) {
         Matcher matcher = Pattern.compile(pattern).matcher((server).getUsername());
         if (matcher.find()) {
            return true;
         }
      }
      return false;
   }

   /**
    * This method returns the user-entered response.<br/>
    * It uses the old Java API because the plugin must be Java5 compliant.
    * 
    * @return the user-entered response.
    * @throws MojoFailureException is an error occurred.
    */
   public String readLine() throws MojoFailureException {
      String response = null;

      getLog().info("An existing administrator login was found in server username configuration, continue anyway (y/N)?");

      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      try {
         response = br.readLine();
      } catch (IOException ioe) {
         getLog().error(ioe);
         throw new MojoFailureException("An unexpected error occurred while reading user entry!");
      } finally {
         IOUtils.closeQuietly(br);
      }
      return response;
   }
}

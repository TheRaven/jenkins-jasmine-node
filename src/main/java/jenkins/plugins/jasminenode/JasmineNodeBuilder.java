package jenkins.plugins.jasminenode;

import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;

import java.io.IOException;
import java.util.Map;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class JasmineNodeBuilder extends Builder {

  private boolean useCoffee;
  private boolean useJunit;
  private String specsFolder;
  private String match;
  private String include;

  @DataBoundConstructor
  public JasmineNodeBuilder(boolean useCoffee, boolean useJunit, String specsFolder, String match, String include) {
    this.useCoffee = useCoffee;
    this.useJunit = useJunit;
    this.specsFolder = specsFolder;
    this.match = match;
    this.include = include;
  }

  @Override
  public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
    ArgumentListBuilder args = new ArgumentListBuilder();

    args.add("jasmine-node");
    if (useCoffee) {
      args.add("--coffee");
    }
    if (useJunit) {
      args.add("--junitreport");
    }
    if (match != null && !match.equals("")) {
      args.add("--match");
      args.add(match);
    }

    if (include != null && !include.equals("")) {
      args.add("--include");
      args.add(include);
    }

    if (specsFolder == null || specsFolder.equals("")) {
      specsFolder = "specs";
    }
    args.add(specsFolder);
    // Try to execute the command
    try {
      Map<String, String> env = build.getEnvironment(listener);
      int r = launcher.launch().cmds(args).envs(env).stdout(listener).pwd(build.getModuleRoot()).join();
      return r == 0;
    } catch (IOException e) {
      Util.displayIOException(e, listener);
      e.printStackTrace(listener.fatalError("command execution failed"));
    } catch (InterruptedException e) {
      e.printStackTrace(listener.fatalError("command execution interrupted"));
    }
    return false;
  }

  @Override
  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) super.getDescriptor();
  }

  /**
   * Descriptor for {@link JasmineNodeBuilder}. Used as a singleton. The class
   * is marked as public so that it can be accessed from views.
   * 
   * <p>
   * See
   * <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
   * for the actual HTML fragment for the configuration screen.
   */
  @Extension
  // This indicates to Jenkins that this is an implementation of an extension
  // point.
  public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

    /**
     * To persist global configuration information, simply store it in a field
     * and call save().
     * 
     * <p>
     * If you don't want fields to be persisted, use <tt>transient</tt>.
     */

    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
      // Indicates that this builder can be used with all kinds of project
      // types
      return true;
    }

    /**
     * This human readable name is used in the configuration screen.
     */
    public String getDisplayName() {
      return "run jasmine specs using jasmine-node";
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
      // To persist global configuration information,
      // set that to properties and call save().

      // ^Can also use req.bindJSON(this, formData);
      // (easier when there are many fields; need set* methods for this,
      // like setUseFrench)

      save();
      return super.configure(req, formData);
    }
  }

  public boolean useCoffee() {
    return useCoffee;
  }

  public boolean useJunit() {
    return useJunit;
  }

  public String getSpecsFolder() {
    return specsFolder;
  }

  public String getMatch() {
    return match;
  }

  public String getInclude() {
    return include;
  }

}

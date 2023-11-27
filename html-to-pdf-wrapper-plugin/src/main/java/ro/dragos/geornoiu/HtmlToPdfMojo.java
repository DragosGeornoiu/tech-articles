package ro.dragos.geornoiu;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import java.io.File;
import java.util.Collections;

@Mojo(name = "generatePdf")
public class HtmlToPdfMojo extends AbstractMojo {

  private static final String ERROR_MESSAGE_FAILED_TO_GENERATE_PDF =
      "Failed to generate PDF file [%s] from html file [%s].";
  private static final String ERROR_MESSAGE_NO_HTML_FILE_FOUND = "No HTML file found at [%s]";

  private static final String WRAPPED_PLUGIN_GROUP_ID = "au.net.causal.maven.plugins";
  private static final String WRAPPED_PLUGIN_ARTIFACT_ID = "html2pdf-maven-plugin";
  private static final String WRAPPED_PLUGIN_VERSION = "2.0";
  private static final String WRAPPED_PLUGIN_GOAL = "html2pdf";

  private static final String WRAPPER_PLUGIN_ID = "generate-pdf";
  private static final String WRAPPER_PLUGIN_PHASE = "generate-resources";

  @Parameter(property = "inputFile", required = true)
  private String inputFile;

  @Parameter(property = "outputFile", required = true)
  private String outputFile;

  @Component private MavenProject mavenProject;
  @Component private MavenSession mavenSession;
  @Component private BuildPluginManager pluginManager;

  public void execute() {
    File inputFilePath = new File(inputFile);

    if (!inputFilePath.exists()) {
      getLog().error(String.format(ERROR_MESSAGE_NO_HTML_FILE_FOUND, inputFile));
    } else {
      generatePdf(inputFilePath);
    }
  }

  private void generatePdf(File inputFilePath) {
    String inputDirectory = inputFilePath.getParent();
    String inputFileName = inputFilePath.getName();

    try {
      Plugin html2pdfPlugin = creteMvnPlugin(outputFile, inputFile, inputFileName);

      MojoExecutor.ExecutionEnvironment executionEnvironment =
          MojoExecutor.executionEnvironment(mavenProject, mavenSession, pluginManager);
      MojoExecutor.executeMojo(
          html2pdfPlugin,
          WRAPPED_PLUGIN_GOAL,
          computeWrappedPluginConfiguration(outputFile, inputDirectory, inputFileName),
          executionEnvironment);
    } catch (Exception ex) {
      getLog()
          .error(String.format(ERROR_MESSAGE_FAILED_TO_GENERATE_PDF, outputFile, inputFile), ex);
    }
  }

  private Plugin creteMvnPlugin(String outputFile, String inputDirectory, String inputFileName) {
    Plugin plugin = new Plugin();

    plugin.setGroupId(WRAPPED_PLUGIN_GROUP_ID);
    plugin.setArtifactId(WRAPPED_PLUGIN_ARTIFACT_ID);
    plugin.setVersion(WRAPPED_PLUGIN_VERSION);

    PluginExecution pluginExecution = createMvnPluginExecution();
    plugin.setExecutions(Collections.singletonList(pluginExecution));
    plugin.setConfiguration(
        computeWrappedPluginConfiguration(outputFile, inputDirectory, inputFileName));

    return plugin;
  }

  private PluginExecution createMvnPluginExecution() {
    PluginExecution pluginExecution = new PluginExecution();
    pluginExecution.setGoals(Collections.singletonList(WRAPPED_PLUGIN_GOAL));
    pluginExecution.setId(WRAPPER_PLUGIN_ID);
    pluginExecution.setPhase(WRAPPER_PLUGIN_PHASE);
    return pluginExecution;
  }

  private Xpp3Dom computeWrappedPluginConfiguration(
      String outputFile, String inputDirectory, String inputFileName) {
    MojoExecutor.Element outputFileElement = new MojoExecutor.Element("outputFile", outputFile);

    MojoExecutor.Element includeElement =
        new MojoExecutor.Element("include", "**/" + inputFileName);
    MojoExecutor.Element includesElement = new MojoExecutor.Element("includes", includeElement);
    MojoExecutor.Element directoryElement = new MojoExecutor.Element("directory", inputDirectory);
    MojoExecutor.Element htmlFileSetElement =
        new MojoExecutor.Element("htmlFileSet", directoryElement, includesElement);
    MojoExecutor.Element htmlFileSetsElement =
        new MojoExecutor.Element("htmlFileSets", htmlFileSetElement);

    return MojoExecutor.configuration(outputFileElement, htmlFileSetsElement);
  }
}

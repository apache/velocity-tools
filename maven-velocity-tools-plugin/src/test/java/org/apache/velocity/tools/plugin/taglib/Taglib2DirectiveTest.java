package org.apache.velocity.tools.plugin.taglib;

import static org.easymock.EasyMock.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

/**
 * Tests {@link Taglib2Directive}.
 *
 */
public class Taglib2DirectiveTest
{

    /**
     * Tests {@link Taglib2Directive#execute()}.
     *
     * @throws IOException If something goes wrong.
     * @throws MojoFailureException If something goes wrong.
     * @throws MojoExecutionException If something goes wrong.
     */
    @Test
    public void testExecute() throws IOException, MojoExecutionException,
            MojoFailureException
    {
        MavenProject mavenProject = createMock(MavenProject.class);

        Taglib2Directive mojo = new Taglib2Directive();
        File tempDir = File.createTempFile("velocity2directive", "tmp");
        tempDir.delete();
        tempDir.mkdirs();
        mojo.classesOutputDirectory = tempDir;
        List<String> classpathElements = new ArrayList<String>();
        String classesPath = System.getProperty("basedir")
                + "/target/test-classes";
        classpathElements.add(classesPath);
        mojo.classpathElements = classpathElements;
        mojo.packageName = "org.apache.velocity.tools.test";
        mojo.prefix = "sample";
        mojo.tld = "sample.tld";

        mavenProject.addCompileSourceRoot(tempDir.getAbsolutePath());

        replay(mavenProject);
        mojo.project = mavenProject;
        mojo.execute();

        FileUtils.contentEquals(new File(tempDir,
                "org/apache/velocity/tools/test/MyClassicTag.java"), new File(
                classesPath, "MyClassicTag.javat"));
        FileUtils.contentEquals(new File(tempDir,
                "org/apache/velocity/tools/test/MyBodylessClassicTag.java"),
                new File(classesPath, "MyBodylessClassicTag.javat"));
        FileUtils.contentEquals(new File(tempDir,
                "org/apache/velocity/tools/test/MySimpleTag.java"), new File(
                classesPath, "MySimpleTag.javat"));
        verify(mavenProject);

        FileUtils.deleteDirectory(tempDir);
    }

}

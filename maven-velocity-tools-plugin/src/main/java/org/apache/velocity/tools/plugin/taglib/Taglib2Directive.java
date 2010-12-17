package org.apache.velocity.tools.plugin.taglib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.annotations.DigesterLoader;
import org.apache.commons.digester.annotations.DigesterLoaderBuilder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.plugin.taglib.model.Tag;
import org.apache.velocity.tools.plugin.taglib.model.Taglib;
import org.xml.sax.SAXException;

/**
 * Creates directives from a tag library.
 *
 * @goal taglib2directive
 *
 * @phase generate-resources
 * @requiresDependencyResolution compile
 */
public class Taglib2Directive extends AbstractMojo
{

    /**
     * The project
     *
     * @parameter expression="${project.compileClasspathElements}"
     * @required
     * @readonly
     */
    List<String> classpathElements;

    /**
     * Location of the file.
     *
     * @parameter
     * expression="${project.build.directory}/veltools-generated-classes"
     * @required
     */
    File classesOutputDirectory;

    /**
     * Location of the file.
     *
     * @parameter
     * expression="${project.build.directory}/veltools-generated-resources"
     * @required
     */
    File resourcesOutputDirectory;

    /**
     * Name of the package.
     *
     * @parameter expression="sample"
     * @required
     */
    String packageName;

    /**
     * Name of the TLD prefix.
     *
     * @parameter expression="sample"
     * @required
     */
    String prefix;

    /**
     * Location of the TLD to parse.
     *
     * @parameter expression="sample"
     * @required
     */
    String tld;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    MavenProject project;

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        InputStream tldStream = null;
        try
        {
            tldStream = findTLD();
            DigesterLoader loader = new DigesterLoaderBuilder()
                    .useDefaultAnnotationRuleProviderFactory()
                    .useDefaultDigesterLoaderHandlerFactory();
            Digester digester = loader.createDigester(Taglib.class);
            digester.setValidating(false);
            digester.register(
                    "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.1//EN",
                    getClass().getResource("/web-jsptaglibrary_1_1.dtd"));
            digester.register(
                    "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN",
                    getClass().getResource("/web-jsptaglibrary_1_2.dtd"));
            Taglib taglib = (Taglib) digester.parse(tldStream);
            Properties props = new Properties();
            InputStream propsStream = getClass().getResourceAsStream(
                    "/velocity.properties");
            props.load(propsStream);
            propsStream.close();
            VelocityEngine engine = new VelocityEngine(props);
            String packageDirName = packageName.replaceAll("\\.", "/");
            File packageDir = new File(classesOutputDirectory, packageDirName);
            packageDir.mkdirs();
            for (Tag tag : taglib.getTags())
            {
                File sourceFile = new File(packageDir, tag
                        .getReflectedTagClass().getSimpleName()
                        + "Directive.java");
                VelocityContext context = new VelocityContext();
                context.put("packageName", packageName);
                context.put("prefix", prefix);
                context.put("tag", tag);
                Template template = engine.getTemplate("/jspDirective.vm");
                FileWriter writer = new FileWriter(sourceFile);
                template.merge(context, writer);
                writer.close();
            }
            project.addCompileSourceRoot(classesOutputDirectory.getAbsolutePath());
        } catch (IOException e)
        {
            throw new MojoExecutionException(
                    "I/O exception while executing taglib2directive", e);
        } catch (SAXException e)
        {
            throw new MojoExecutionException(
                    "Parsing exception while executing taglib2directive", e);
        } catch (RuntimeException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new MojoExecutionException(
                    "Generic exception while executing taglib2directive", e);
        } finally
        {
            if (tldStream != null)
            {
                try
                {
                    tldStream.close();
                } catch (IOException e)
                {
                    throw new MojoExecutionException(
                            "I/O exception while executing taglib2directive", e);
                }
            }
        }

    }

    private InputStream findTLD() throws IOException
    {
        for (String path : classpathElements)
        {
            File file = new File(path);
            if (file.isDirectory())
            {
                File candidate = new File(file, tld);
                if (candidate.exists())
                {
                    return new FileInputStream(candidate);
                }
            } else if (file.getPath().endsWith(".jar"))
            {
                JarFile jar = new JarFile(file);
                ZipEntry entry = jar.getEntry(tld);
                if (entry != null)
                {
                    return jar.getInputStream(entry);
                }
            }
        }

        throw new IOException("Cannot find tld resource: " + tld);
    }

}

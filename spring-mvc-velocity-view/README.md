# Spring Web MVC support for Apache Velocity Tools

`spring-mvc-velocity-view` lets you use Apache Velocity — with the Velocity Tools toolbox in
scope — as the view layer of a Spring Web MVC (Spring 6 / Spring 7) application.

It revives the tools-related part of the Velocity view support that Spring removed in 5.0,
rebased onto the modern Velocity Tools 3.x toolbox API and the engine-side
`org.apache.velocity:spring-velocity-support` factory.

## Requirements

- Java 17+
- Spring Framework 6.x or 7.x (`spring-webmvc`, supplied by your application)
- Jakarta Servlet 6.0 (Tomcat 10.1+)

## Dependency

    <dependency>
      <groupId>org.apache.velocity.tools</groupId>
      <artifactId>spring-mvc-velocity-view</artifactId>
      <version>4.0</version>
    </dependency>

It pulls in `velocity-tools-view` and the engine's `spring-velocity-support`. `spring-webmvc`
is `provided` — your application already supplies Spring.

## Configuration (Java)

    @Configuration
    @EnableWebMvc
    public class WebConfig {

      @Bean
      public VelocityConfigurer velocityConfigurer() {
        VelocityConfigurer cfg = new VelocityConfigurer();
        Properties props = new Properties();
        props.setProperty("resource.loaders", "webapp");
        props.setProperty("resource.loader.webapp.class",
            "org.apache.velocity.tools.view.WebappResourceLoader");
        props.setProperty("resource.loader.webapp.path", "/WEB-INF/templates/");
        cfg.setVelocityProperties(props);
        return cfg;
      }

      @Bean
      public VelocityViewResolver velocityViewResolver() {
        VelocityViewResolver resolver = new VelocityViewResolver();
        resolver.setSuffix(".vm");
        resolver.setContentType("text/html;charset=UTF-8");
        return resolver;
      }
    }

A controller returning view name `home` then renders `/WEB-INF/templates/home.vm`, with the
toolbox (`$esc`, `$date`, `$link`, …) and your model both in context.

## `VelocityConfigurer` properties

| property | default | meaning |
|---|---|---|
| `VelocityEngineFactory` setters (`velocityProperties`, `resourceLoaderPath`, `configLocation`, …) | — | configure the underlying `VelocityEngine`; or inject a ready one with `velocityEngine` |
| `includeDefaultToolbox` | `true` | load the bundled default tools |
| `toolboxConfigLocation` | — | an extra `tools.xml` / `tools.properties` to load |
| `autoConfigure` | `false` | also pick up a config from the `org.apache.velocity.tools` system property / servlet-context attribute |
| `createSession` | `false` | allow the views to create an `HttpSession` for session-scoped tools |

`createSession` is `false` by default (stateless-friendly): session-scoped tools (e.g. the
`BrowserTool`) are simply not published when there is no session. Set it `true` if you rely on
session-scoped tools persisting across requests.

## Not included

Layout support (the old Spring `VelocityLayoutView`) is not provided. If you need
screen-into-layout composition, use `velocity-tools-view`'s `VelocityLayoutServlet`, or open a
feature request for a Spring layout view.

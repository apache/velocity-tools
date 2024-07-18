package org.apache.velocity.tools.examples.showcase.sample;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class SampleListInitializer implements ServletContextListener
{

    public void contextInitialized(ServletContextEvent event)
    {
        List<Person> people = new ArrayList<Person>();
        people.add(new Person("Claude", "Brisson", "PMC Member"));
        people.add(new Person("Nathan", "Bubna", "PMC Member"));
        people.add(new Person("Will", "Glass-Husain", "PMC Chair"));
        people.add(new Person("Marinó A.", "Jónsson", "PMC Member"));
        people.add(new Person("Geir", "Magnusson Jr.", "PMC Member"));
        people.add(new Person("Daniel", "Rall", "PMC Member"));
        people.add(new Person("Henning P.", "Schmiedehausen", "PMC Member"));
        people.add(new Person("Jon S.", "Stevens", "Emeritus"));
        people.add(new Person("Jason", "van Zyl", "Emeritus"));
        people.add(new Person("Christopher", "Schultz", "Java developer"));
        people.add(new Person("Antonio", "Petrelli", "PMC Member"));
        event.getServletContext().setAttribute("people", people);
    }

    public void contextDestroyed(ServletContextEvent event)
    {
        // It does nothing.
    }

}

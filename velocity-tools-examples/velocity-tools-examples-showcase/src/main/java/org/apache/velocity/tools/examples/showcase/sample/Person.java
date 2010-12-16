package org.apache.velocity.tools.examples.showcase.sample;

public class Person
{

    private String name;

    private String surname;

    private String role;

    public Person(String name, String surname, String role)
    {
        this.name = name;
        this.surname = surname;
        this.role = role;
    }

    public String getName()
    {
        return name;
    }

    public String getSurname()
    {
        return surname;
    }

    public String getRole()
    {
        return role;
    }

}

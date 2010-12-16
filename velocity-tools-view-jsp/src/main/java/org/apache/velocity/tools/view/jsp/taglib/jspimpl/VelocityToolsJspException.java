package org.apache.velocity.tools.view.jsp.taglib.jspimpl;

public class VelocityToolsJspException extends RuntimeException
{

    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 7627800888526325645L;

    public VelocityToolsJspException()
    {
    }

    public VelocityToolsJspException(String message)
    {
        super(message);
    }

    public VelocityToolsJspException(Throwable cause)
    {
        super(cause);
    }

    public VelocityToolsJspException(String message, Throwable cause)
    {
        super(message, cause);
    }

}

public class ToyTool
{
    private String message = "Hello from ToyTool!";

	public String getMessage()
	{
        return message;
	}
    
    public void setMessage(String m)
    { 
        message = m;
    }

    /** To test exception handling in templates. */
    public boolean whine() {
        throw new IllegalArgumentException();
    }

}

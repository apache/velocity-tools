/*
 * Struts Example Application 4
 *
 * This demonstrates the use of the Velocity TilesTool with Struts Tiles.
 */


package examples.app4;

import org.apache.struts.tiles.Controller;
import org.apache.struts.tiles.ComponentContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * <p>A simple tile-controller that puts a string into the request scope.
*     Check out the tiles-defs to see the definition that uses the controller</p>
 *
 * @author <a href="mailto:marinoj@centrum.is"/>Marinó A. Jónsson</a>
 * @version $Id: MyTileController.java,v 1.1 2004/02/12 11:52:19 marino Exp $
 */

public class MyTileController implements Controller {

    public MyTileController() {
    }

    public void perform(ComponentContext tileContext,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        ServletContext servletContext)
        throws ServletException, IOException {

        request.setAttribute("foo", "bar");
    }

}

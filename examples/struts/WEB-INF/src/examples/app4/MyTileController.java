/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: MyTileController.java,v 1.3 2004/03/12 19:41:06 marino Exp $
 */

package examples.app4;

import org.apache.struts.tiles.ControllerSupport;
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
 * @version $Id: MyTileController.java,v 1.3 2004/03/12 19:41:06 marino Exp $
 */

public class MyTileController extends ControllerSupport {

    public MyTileController() {
    }

    /**
     * Method associated to a tile and called immediately before the tile
     * is included.
     * @param tileContext Current tile context.
     * @param request Current request
     * @param response Current response
     * @param servletContext Current servlet context
     */
    public void execute(ComponentContext tileContext,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        ServletContext servletContext)
        throws ServletException, IOException {

        request.setAttribute("foo", "bar");
    }

}

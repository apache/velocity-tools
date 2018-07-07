package org.apache.velocity.tools.test.blackbox;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestURLHandler
{
  static final String TEST_PROTOCOL = "veltest";
  static Map<String, String> contentMap;
  static
  {
    contentMap = new ConcurrentHashMap<String, String>();
    URLStreamHandlerFactory factory = new URLStreamHandlerFactory()
    {
      @Override
      public URLStreamHandler createURLStreamHandler(String protocol)
      {
        URLStreamHandler handler = null;
        if (TEST_PROTOCOL.equals(protocol))
        {
          handler = new URLStreamHandler()
          {
            @Override
            protected URLConnection openConnection(URL url) throws IOException
            {
              return new URLConnection(url)
              {
                @Override
                public void connect() throws IOException
                {
                }

                @Override
                public InputStream getInputStream() throws IOException
                {
                  InputStream inputStream = null;
                  String path = getURL().getPath();
                  String content = contentMap.get(path);
                  if (content != null)
                  {
                    inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
                  }
                  return inputStream;
                }
              };
            }
          };
        }
        return handler;
      }
    };
    // this call may fail in non-fork mode if someone made it sooner (it's allowed only once)
    URL.setURLStreamHandlerFactory(factory);
  }

  public static void registerTestURL(String path, String content)
  {
    if (path == null) path = "/";
    else if (!path.startsWith("/")) path = "/" + path;
    contentMap.put(path, content);
  }
}

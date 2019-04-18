package org.apache.velocity.tools.model.util;

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

import org.slf4j.Logger;
import org.slf4j.spi.LocationAwareLogger;

import java.io.OutputStream;
import java.util.function.Consumer;

public class LogOutputStream extends OutputStream
{
    private Logger logger;

    private StringBuffer buffer;

    /** slf4j levels */
    public static final int TRACE = LocationAwareLogger.TRACE_INT;
    public static final int DEBUG = LocationAwareLogger.DEBUG_INT;
    public static final int INFO = LocationAwareLogger.INFO_INT;
    public static final int WARN = LocationAwareLogger.WARN_INT;
    public static final int ERROR = LocationAwareLogger.ERROR_INT;

    public LogOutputStream(Logger logger, int level)
    {
        this.logger = logger;
        switch (level)
        {
            case TRACE: log = this::trace; break;
            case DEBUG: log = this::debug; break;
            case INFO : log = this::info; break;
            case WARN: log = this::warn; break;
            case ERROR: log = this::error; break;
        }
    }

    private Consumer<String> log;

    private final void trace(String log)
    {
        logger.trace(log);
    }

    private final void debug(String log)
    {
        logger.debug(log);
    }

    private final void info(String log)
    {
        logger.info(log);
    }

    private final void warn(String log)
    {
        logger.warn(log);
    }

    private final void error(String log)
    {
        logger.error(log);
    }

    @Override
    public void write (int c)
    {
        char ch = (char)(c & 0xFF);
        if (ch == '\n')
        {
            log.accept(buffer.toString());
            buffer = new StringBuffer();
        }
        else
        {
            buffer.append(ch);
        }
    }

    @Override
    public void flush()
    {
        // I prefer just waiting for \n
    }
}

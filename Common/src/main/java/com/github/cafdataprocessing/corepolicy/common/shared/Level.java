/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cafdataprocessing.corepolicy.common.shared;

/**
 * Logging levels definition
 */
public class Level {
    transient int level;
    transient String levelStr;
    transient int syslogEquivalent;

    public static final int OFF_INT = 2147483647;
    public static final int FATAL_INT = 50000;
    public static final int ERROR_INT = 40000;
    public static final int WARN_INT = 30000;
    public static final int INFO_INT = 20000;
    public static final int DEBUG_INT = 10000;
    public static final int TRACE_INT = 5000;
    public static final int ALL_INT = -2147483648;

    public static final Level OFF = new Level(2147483647, "OFF", 0);
    public static final Level FATAL = new Level('썐', "FATAL", 0);
    public static final Level ERROR = new Level('鱀', "ERROR", 3);
    public static final Level WARN = new Level(30000, "WARN", 4);
    public static final Level INFO = new Level(20000, "INFO", 6);
    public static final Level DEBUG = new Level(10000, "DEBUG", 7);
    public static final Level TRACE = new Level(5000, "TRACE", 7);
    public static final Level ALL = new Level(-2147483648, "ALL", 7);

    static final long serialVersionUID = 3491141966387921974L;

    protected Level() {
        this.level = TRACE_INT;
        this.levelStr = "TRACE";
        this.syslogEquivalent = 7;
    }

    protected Level(int level, String levelStr, int syslogEquivalent) {
        this.level = level;
        this.levelStr = levelStr;
        this.syslogEquivalent = syslogEquivalent;
    }

    public boolean equals(Object o) {
        if(o instanceof Level) {
            Level r = (Level)o;
            return this.level == r.level;
        } else {
            return false;
        }
    }


    public static Level toLevel(String sArg) {
        return toLevel(sArg, DEBUG);
    }

    public static Level toLevel(String sArg, Level defaultLevel) {
        if(sArg == null) {
            return defaultLevel;
        } else {
            String s = sArg.toUpperCase();
            return s.equals("ALL")?ALL:(s.equals("DEBUG")?DEBUG:(s.equals("INFO")?INFO:(s.equals("WARN")?WARN:(s.equals("ERROR")?ERROR:(s.equals("FATAL")?FATAL:(s.equals("OFF")?OFF:(s.equals("TRACE")?TRACE:(s.equals("İNFO")?INFO:defaultLevel))))))));
        }
    }

    public static Level toLevel(int val) {
        return toLevel(val, DEBUG);
    }

    public static Level toLevel(int val, Level defaultLevel) {
        switch(val) {
            case -2147483648:
                return ALL;
            case 5000:
                return TRACE;
            case 10000:
                return DEBUG;
            case 20000:
                return INFO;
            case 30000:
                return WARN;
            case 40000:
                return ERROR;
            case 50000:
                return FATAL;
            case 2147483647:
                return OFF;
            default:
                return defaultLevel;
        }
    }
}

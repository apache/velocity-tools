package org.apache.velocity.tools.view;

import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.shaded.commons.lang3.tuple.ImmutablePair;
import org.apache.velocity.shaded.commons.lang3.tuple.Pair;
import org.apache.velocity.tools.ClassUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UAParser
{
    public UAParser()
    {
    }

    public static class UAEntity
    {
        private String name = null;
        private int majorVersion = -1;
        private int minorVersion = -1;

        public UAEntity(String n, String maj, String min)
        {
            name = n;
            try
            {
                majorVersion = maj == null ? -1 : Integer.valueOf(maj);
                minorVersion = maj == null ? -1 : Integer.valueOf(min);
            }
            catch (NumberFormatException nfe)
            {
                majorVersion = minorVersion = -1;
            }
        }

        public String getName()
        {
            return name;
        }

        public int getMajorVersion()
        {
            return majorVersion;
        }

        public int getMinorVersion()
        {
            return minorVersion;
        }
    }

    /* device, in order of growing precedence */
    public enum DeviceType
    {
        UNKNOWN,
        DESKTOP,
        MOBILE,
        TABLET,
        TV,
        ROBOT
    };


    private static Map<String,String> browserTranslationMap = null;
    private static Map<String,String> osTranslationMap = null;
    static
    {
        browserTranslationMap = new HashMap<String,String>();
        browserTranslationMap.put("navigator","Netscape");
        browserTranslationMap.put("nokia5250", "Nokia Browser");

        osTranslationMap = new HashMap<String,String>();
        osTranslationMap.put("android", "Android");
        osTranslationMap.put("bada", "Bada");
        osTranslationMap.put("bb10", "BlackBerry");
        osTranslationMap.put("blackberry", "BlackBerry");
        osTranslationMap.put("cros", "Chrome OS");
        osTranslationMap.put("fxos", "Firefox OS");
        osTranslationMap.put("hpwos", "WebOS");
        osTranslationMap.put("ipad", "iOS");
        osTranslationMap.put("iphone", "iOS");
        osTranslationMap.put("ipod", "iOS");
        osTranslationMap.put("kfthwi", "Kindle");
        osTranslationMap.put("kftt", "Kindle");
        osTranslationMap.put("mac os x", "OS X");
        osTranslationMap.put("macos x", "OS X");
        osTranslationMap.put("nokiae", "Nokia");
        osTranslationMap.put("nokiax2", "Nokia");
        osTranslationMap.put("remi", "Fedora");
        osTranslationMap.put("rhel", "Red Hat");
        osTranslationMap.put("series40", "Symbian");
        osTranslationMap.put("series60", "Symbian");
        osTranslationMap.put("series80", "Symbian");
        osTranslationMap.put("series90", "Symbian");
        osTranslationMap.put("series 40", "Symbian");
        osTranslationMap.put("series 60", "Symbian");
        osTranslationMap.put("series 80", "Symbian");
        osTranslationMap.put("series 90", "Symbian");
        osTranslationMap.put("symbianos", "Symbian");
        osTranslationMap.put("symbos", "Symbian");
        osTranslationMap.put("tigeros", "OS X");
        osTranslationMap.put("tizen", "Tizen");
        osTranslationMap.put("tt", "Android");
        osTranslationMap.put("unix", "Unix");
        osTranslationMap.put("unix bsd", "BSD");
        osTranslationMap.put("unixware", "Unix");
        osTranslationMap.put("webos", "WebOS");
        osTranslationMap.put("windows nt", "Windows");
        osTranslationMap.put("win98", "Windows");
    }

    public static class UserAgent
    {
        private DeviceType deviceType = null;
        private UAEntity operatingSystem = null;
        private UAEntity browser = null;
        private UAEntity renderingEngine = null;
        private boolean isRobot = false;

        public DeviceType getDeviceType() { return deviceType; }
        public UAEntity getOperatingSystem() { return operatingSystem; }
        public UAEntity getBrowser() { return browser; }
        public UAEntity getRenderingEngine() { return renderingEngine; }

        protected void setOperatingSystem(String entity, String major, String minor)
        {
            if (entity.equals("Series") && major != null)
            {
                entity += major;
                major = minor = null;
            }
            String alternate = osTranslationMap.get(entity.toLowerCase());
            if (alternate != null) entity = alternate;
            if (entity.startsWith("BlackBerry")) { entity = "BlackBerry"; }
            operatingSystem = new UAEntity(entity, major, minor);
        }

        protected void setBrowser(String entity, String major, String minor)
        {
            String alternate = browserTranslationMap.get(entity.toLowerCase());
            if (alternate != null) { entity = alternate; }
            if ("Navigator".equals(entity)) { entity = "Netscape"; }
            browser = new UAEntity(entity, major, minor);
            if ("Edge".equals(entity) && renderingEngine == null) { renderingEngine = new UAEntity("EdgeHTML", major, minor); }
        }

        protected void setRenderingEngine(String entity, String major, String minor)
        {
            if (deviceType == DeviceType.ROBOT) return;
            renderingEngine = new UAEntity(entity, major, minor);
        }
        
        protected void setDeviceType(DeviceType deviceType)
        {
            this.deviceType = deviceType;
        }
    }

    private enum EntityType
    {
        BROWSER,
        BROWSER_OS,
        ENGINE,
        FORCE_BROWSER,
        FORCE_OS,
        IGNORE,
        MAYBE_BROWSER,
        MAYBE_OS,
        MAYBE_ROBOT,
        MERGE,
        MERGE_OR_BROWSER,
        MERGE_OR_OS,
        OS,
        ROBOT
    };

    private static final String UA_KEYWORDS = "/org/apache/velocity/tools/view/ua-keywords.txt";

    private static Map<String, Pair<EntityType, DeviceType>> entityMap = new HashMap<String, Pair<EntityType, DeviceType>>();

    static
    {
        try
        {
            Properties properties = new Properties();
            InputStream stream = ClassUtils.getResourceAsStream(UA_KEYWORDS, BrowserTool.class);
            if (stream == null) { throw new IOException("could not find org.apache.velocity.tools.view.ua-keywords.txt resource"); }
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line = null;
            int num = 1;
            while ((line = reader.readLine()) != null)
            {
                line = line.trim();
                if (line.length() == 0 || line.startsWith("#")) { ++num; continue; }
                int eq = line.indexOf('=');
                if (eq == -1) { throw new IOException("invalid line format in ua-keywords.txt at line " + num); }
                String key = line.substring(0, eq);
                String val = line.substring(eq + 1).toUpperCase();
                int coma = val.indexOf(',');
                DeviceType device = null;
                if (coma != -1)
                {
                    device = DeviceType.valueOf(val.substring(coma + 1));
                    val = val.substring(0, coma);
                }
                EntityType entity = val.length() > 0 ? EntityType.valueOf(val) : null;
                entityMap.put(key, new ImmutablePair<EntityType, DeviceType>(entity , device));
                ++num;
            }
        }
        catch(Exception e)
        {
            throw new VelocityException("BrowserTool: static initialization failed", e);
        }
    }

    private static final String nonMergeSep = "(;/)";

    private static Pattern versionPattern = Pattern.compile(
            /* entity name */
            "([a-z]+(?:(?=[;()@]|$)|(?:[0-9]+(?!\\.)[a-z]*)|(?:[!_+.\\-][a-z]+)+|(?=[/ ,\\-:0-9+!_=])))" +
            /* potential version */
                    "(?:([/ ,\\-:+_=])?(?:v?(\\d+)(?:\\.(\\d+))?[a-z+]*)?)",
            Pattern.CASE_INSENSITIVE);

    private static boolean isRobotToken(String token)
    {
        token = token.toLowerCase();
        return token.endsWith("bot") || token.endsWith("crawler") || token.endsWith("spider") || token.endsWith("agent") || token.endsWith("validator");
    }

    /* the big hairy parsing method */
    public static UserAgent parseUserAgent(String userAgentString, Logger log)
    {
        UserAgent ua = null;
        try
        {
            ua = new UserAgent();

            Matcher matcher = versionPattern.matcher(userAgentString);
            String merge = null;
            EntityType mergeTarget = null;
            boolean maybeBrowser = true;
            boolean maybeOS = true;
            boolean maybeRobot = false;
            boolean forcedBrowser = false;
            boolean forcedOS = false;

            while (matcher.find())
            {
                String entity = matcher.group(1);
                String separator = matcher.group(2);
                String major = matcher.group(3);
                String minor = matcher.group(4);
                char next = userAgentString.length() == matcher.end(1) ? ';' : userAgentString.charAt(matcher.end(1));
                if (entity != null)
                {
                    if (merge != null)
                    {
                        String merged = merge + " " + entity;
                        if (mergeTarget == null)
                        {
                            entity = merged;
                        }
                        else
                        {
                            Pair<EntityType,DeviceType> pair = entityMap.get(merged.toLowerCase());
                            EntityType mergedType = pair == null ? null : pair.getLeft();
                            if (mergedType != null && (
                                    mergeTarget == mergedType ||
                                            mergeTarget == EntityType.BROWSER && (mergedType == EntityType.MAYBE_BROWSER || mergedType == EntityType.FORCE_BROWSER) ||
                                            mergeTarget == EntityType.OS && (mergedType == EntityType.MAYBE_OS || mergedType == EntityType.FORCE_OS)
                            ))
                            {
                                entity = merged;
                            }
                            else
                            {
                                /* It means the merge failed, so revert it */
                                switch (mergeTarget)
                                {
                                    case BROWSER:
                                        ua.setBrowser(merge, null, null);
                                        break;
                                    case OS:
                                        ua.setOperatingSystem(merge, null, null);
                                        break;
                                    default:
                                        throw new VelocityException("BrowserTool: unhandled case!");
                                }
                            }
                        }
                        merge = null;
                        mergeTarget = null;
                    }
                    Pair<EntityType, DeviceType> identity = entityMap.get(entity.toLowerCase());
                    EntityType entityType = null;
                    DeviceType deviceType = null;
                    if (identity == null)
                    {
                        /* try again with major version appended */
                        String alternateEntity = entity + separator + major;
                        identity = entityMap.get((entity + separator + major).toLowerCase());
                        if (identity != null)
                        {
                            entity = alternateEntity;
                        }
                    }
                    if (identity != null)
                    {
                        entityType = identity.getLeft();
                        deviceType = identity.getRight();
                        DeviceType previousDeviceType = ua.getDeviceType();
                        /* only overwrite device types of lower precedence */
                        if (deviceType != null && (previousDeviceType == null || deviceType.compareTo(previousDeviceType) > 0))
                        {
                            ua.setDeviceType(deviceType); // may be overwritten by 'robot' device type
                        }
                    }
                    if (entityType != null)
                    {
                        switch (entityType)
                        {
                            case BROWSER:
                            {
                                if (ua.getBrowser() == null || !forcedBrowser)
                                {
                                    ua.setBrowser(entity, major, minor);
                                    maybeBrowser = false;
                                }
                                break;
                            }
                            case BROWSER_OS:
                            {
                                ua.setBrowser(entity, major, minor);
                                maybeBrowser = false;
                                ua.setOperatingSystem(entity, major, minor);
                                maybeOS = false;
                                break;
                            }
                            case ENGINE:
                            {
                                if (!"KHTML".equals(entity) || major != null || ua.getRenderingEngine() == null)
                                {
                                    ua.setRenderingEngine(entity, major, minor);
                                }
                                break;
                            }
                            case FORCE_BROWSER:
                            {
                                if (!forcedBrowser)
                                {
                                    ua.setBrowser(entity, major, minor);
                                    maybeBrowser = false;
                                    forcedBrowser = true;
                                }
                                break;
                            }
                            case FORCE_OS:
                            {
                                if (!forcedOS)
                                {
                                    ua.setOperatingSystem(entity, major, minor);
                                    maybeOS = false;
                                    forcedOS = true;
                                }
                                break;
                            }
                            case IGNORE:
                            {
                                break;
                            }
                            case MAYBE_BROWSER:
                            {
                                if (maybeBrowser)
                                {
                                    if ("rv".equals(entity))
                                    {
                                        if (ua.getBrowser() != null && ua.getBrowser().getName().equals("Mozilla"))
                                        {
                                            entity = "Mozilla";
                                        } else
                                        {
                                            entity = null;
                                        }
                                    } else if ("Version".equals(entity))
                                    {
                                        if (ua.getBrowser() != null && ua.getBrowser().getName().startsWith("Opera"))
                                        {
                                            entity = ua.getBrowser().getName();
                                        } else if (ua.getBrowser() != null && ua.getBrowser().getName().equals("Mozilla"))
                                        {
                                            entity = "Safari";
                                        } else
                                        {
                                            entity = null;
                                        }
                                    } else if ("Safari".equals(entity) && ua.getBrowser() != null && "Safari".equals(ua.getBrowser().getName()))
                                    {
                                        entity = null;
                                    }
                                    if (entity != null)
                                    {
                                        ua.setBrowser(entity, major, minor);
                                    }
                                }
                                break;
                            }
                            case MAYBE_OS:
                            {
                                if (maybeOS)
                                {
                                    ua.setOperatingSystem(entity, major, minor);
                                }
                                break;
                            }
                            case MAYBE_ROBOT:
                            {
                                maybeRobot = true;
                                break;
                            }
                            case MERGE:
                            {
                                if (major == null)
                                {
                                    if (nonMergeSep.indexOf(next) == -1)
                                    {
                                        merge = merge == null ? entity : merge + " " + entity;
                                    } else
                                    {
                                        if ("Mobile".equals(entity) && ua.getOperatingSystem() != null)
                                        {
                                            if (ua.getOperatingSystem().getName().equals("Ubuntu"))
                                            {
                                                ua.setOperatingSystem("Ubuntu Mobile", String.valueOf(ua.getOperatingSystem().getMajorVersion()), String.valueOf(ua.getOperatingSystem().getMinorVersion()));
                                            } else if (ua.getOperatingSystem().getName().equals("Linux"))
                                            {
                                                ua.setOperatingSystem("Android", null, null);
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                            case MERGE_OR_BROWSER:
                            {
                                if (!forcedBrowser)
                                {
                                    if (major != null || nonMergeSep.indexOf(next) != -1)
                                    {
                                        ua.setBrowser(entity, major, minor);
                                    } else
                                    {
                                        merge = entity;
                                        mergeTarget = EntityType.BROWSER;
                                    }
                                }
                                break;
                            }
                            case MERGE_OR_OS:
                            {
                                if (!forcedOS)
                                {
                                    if (major != null || nonMergeSep.indexOf(next) != -1)
                                    {
                                        ua.setOperatingSystem(entity, major, minor);
                                    } else
                                    {
                                        merge = entity;
                                        mergeTarget = EntityType.OS;
                                    }
                                }
                                break;
                            }
                            case OS:
                            {
                                if (ua.getOperatingSystem() == null || !forcedOS)
                                {
                                    ua.setOperatingSystem(entity, major, minor);
                                    maybeOS = false;
                                }
                                break;
                            }
                            case ROBOT:
                            {
                                ua.setDeviceType(DeviceType.ROBOT);
                                break;
                            }
                            default:
                            {
                                throw new VelocityException("BrowserTool: unhandled case: " + entityType);
                            }
                        }
                    }
                    else
                    {
                        if (entity.startsWith("Linux") && !forcedOS)
                        {
                            ua.setOperatingSystem("Linux", null, null);
                        }
                        else if (isRobotToken(entity))
                        {
                            ua.setDeviceType(DeviceType.ROBOT);
                        }
                        else if (entity.startsWith("MID") && !entity.startsWith("MIDP") && (ua.getDeviceType() == null || DeviceType.TABLET.compareTo(ua.getDeviceType()) > 0 ))
                        {
                            ua.setDeviceType(DeviceType.TABLET);
                        }
                        else if (entity.startsWith("CoolPad") && (ua.getDeviceType() == null || DeviceType.MOBILE.compareTo(ua.getDeviceType()) > 0 ))
                        {
                            ua.setDeviceType(DeviceType.MOBILE);
                        }
                        else if (entity.startsWith("LG-") && (ua.getDeviceType() == null || DeviceType.MOBILE.compareTo(ua.getDeviceType()) > 0 ))
                        {
                            ua.setDeviceType(DeviceType.MOBILE);
                        }
                        else if (entity.startsWith("SonyEricsson"))
                        {
                            ua.setDeviceType(DeviceType.MOBILE);
                        }
                    }
                }
            }
            if (ua.getOperatingSystem() != null && "Windows".equals(ua.getOperatingSystem().getName()) && (ua.getOperatingSystem().getMajorVersion() == 98 || ua.getOperatingSystem().getMajorVersion() == 2000))
            {
                if (ua.getOperatingSystem().getMajorVersion() == 98)
                {
                    ua.setOperatingSystem("Windows 98", "4", "90");
                }
                else if (ua.getOperatingSystem().getMajorVersion() == 2000)
                {
                    ua.setOperatingSystem("Windows 2000", "5", "0");
                }
            }
            if (ua.getBrowser() == null)
            {
                if (ua.getDeviceType() == DeviceType.ROBOT || maybeRobot)
                {
                    ua.setBrowser("robot", "0", "0");
                    ua.setDeviceType(DeviceType.ROBOT);
                }
                else if (ua.getOperatingSystem() != null && ua.getOperatingSystem().getName().equals("Symbian"))
                {
                    ua.setBrowser("Nokia Browser", String.valueOf(ua.getOperatingSystem().getMajorVersion()), String.valueOf(ua.getOperatingSystem().getMinorVersion()));
                }
                else
                {
                    ua.setBrowser("unknown", "0", "0");
                }
            }
            if (ua.getOperatingSystem() == null)
            {
                if (ua.getDeviceType() == DeviceType.ROBOT || maybeRobot)
                {
                    ua.setOperatingSystem("robot", "0", "0");
                    ua.setDeviceType(DeviceType.ROBOT);
                }
                else
                {
                    ua.setOperatingSystem("unknown", "0", "0");
                }
            }
            if (ua.getDeviceType() == null)
            {
                if (ua.getOperatingSystem() != null && "Android".equals(ua.getOperatingSystem().getName()))
                {
                    ua.setDeviceType(DeviceType.MOBILE);
                }
                else
                {
                    /* make Desktop the default device */
                    ua.setDeviceType(DeviceType.DESKTOP);
                }
            }
        }
        catch (Exception e)
        {
            log.error("Could not parse browser for User-Agent: {}", userAgentString, e);
            ua = null;
        }
        return ua;
    }
}

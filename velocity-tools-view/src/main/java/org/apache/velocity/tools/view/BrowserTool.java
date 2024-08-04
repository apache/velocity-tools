package org.apache.velocity.tools.view;

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

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

import org.apache.velocity.tools.ConversionUtils;
import static org.apache.velocity.tools.view.UAParser.*;

import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.InvalidScope;

import jakarta.servlet.http.HttpServletRequest;

/**
 *  <p>Browser sniffing tool (session or request scope requested, session scope advised).</p>
 * <p><b>Usage:</b></p>
 * <p>BrowserTool defines properties that are used to test the client browser, operating system, device, language...</p>
 * <p>All properties are boolean, excpet those in italic which are strings (and major/minor versions which are integers)</p>
 * <p>The following properties are available:</p>
 * <ul>
 * <li><b>Device: </b><i>device</i> robot mobile tablet desktop tv</li>
 * <li><b>Features:</b>css3 dom3</li>
 * <li><b>Browser:</b><i>browser.name browser.majorVersion browser.minorVersion</i></li>
 * <li><b>Rendering engine: </b><i>renderingEngine.name renderingEngine.minorVersion renderingEngine.majorVersion</i></li>
 * <li><b>Operating system: </b><i>operatingsystem.name operatingsystem.majorVersion operatingsystem.minorVersion</i></li>
 * <li><b>Specific browser tests:</b>netscape firefox safari MSIE opera links mozilla konqueror chrome</li>
 * <li><b>Specific rendering engine tests:</b>gecko webKit KHTML trident blink edgeHTML presto</li>
 * <li><b>Specific OS tests:</b>windows OSX linux unix BSD android iOS symbian</li>
 * <li><b>Languages</b>: <i>preferredLanguageTag</i> (a string like 'en', 'da', 'en-US', ...), <i>preferredLocale</i> (a java Locale)</li>
 * <li><b>IP address</b>: <i>IPAddress</i>
 * </ul>
 *
 * <p>Language properties are filtered by the languagesFilter tool param, if present, which is here to specify which languages are acceptable on the server side.
 * If no matching language is found, or if there is no
 * matching language, the tools defaut locale (or the first value of languagesFilter) is returned.
 * Their value is guarantied to belong to the set provided in languagesFilter, if any.</p>
 *
 * <p>Notes on implementation:</p>
 * <ul>
 *     <li>The parsing algorithm is mainly empirical. Used rules are rather generic, so shouldn't need recent updates to be accurate, but accuracy remains far from guaranteed for new devices.</li>
 *     <li>Parsing should be fast, as the parser only uses a single regex iteration on the user agent string.</li>
 *     <li>Game consoles, e-readers, etc... are for now classified as <i>mobile</i> devices (but can sometimes be identified by their operating system).</li>
 *     <li>Needless to say, the frontier between different device types can be very thin...</li>
 * </ul>
 *
 * <p>Thanks to Lee Semel (lee@semel.net), the author of the HTTP::BrowserDetect Perl module.</p>
 * <p>See also:</p>
 * <ul>
 *   <li>http://www.zytrax.com/tech/web/browser_ids.htm</li>
 *   <li>http://en.wikipedia.org/wiki/User_agent</li>
 *   <li>http://www.user-agents.org/</li>
 *   <li>https://github.com/OpenDDR</li>
 *   <li>https://devicemap.apache.org/</li>
 *   <li>http://www.useragentstring.com/pages/useragentstring.php?name=All</li>
 *   <li>https://en.wikipedia.org/wiki/Comparison_of_layout_engines_(Cascading_Style_Sheets)</li>
 *   <li>https://en.wikipedia.org/wiki/Comparison_of_layout_engines_(Document_Object_Model)</li>
 *   <li>http://www.webapps-online.com/online-tools/user-agent-strings</li>
 *   <li>https://whichbrowser.net/data/</li>
 * </ul>
 * <p>TODO:</p>
 * <ul>
 *     <li>parse X-Wap-Profile header if present</li>
 *     <li>parse X-Requested-With header if present</li>
 * </ul>
 *
 * @author <a href="mailto:claude@renegat.net">Claude Brisson</a>
 * @since VelocityTools 2.0
 * @version $Revision$ $Date$
 */

@DefaultKey("browser")
@InvalidScope(Scope.APPLICATION)
public class BrowserTool extends BrowserToolDeprecatedMethods implements Serializable
{
    private static final long serialVersionUID = 1734529350532353339L;

    /* IP */
    private String IPAddress = null;
    
    /* User-Agent */
    private String userAgentString = null;
    private String lowercaseUserAgentString = null;
    private UserAgent userAgent = null;

    /* Accept-Language header variables */
    private String acceptLanguage = null;
    private SortedMap<Float,List<String>> languageRangesByQuality = null;
    private String starLanguageRange = null;
    // pametrizable filter of retained laguages
    private List<String> languagesFilter = null;
    private String preferredLanguage = null;

    private static Pattern quality = Pattern.compile("^q\\s*=\\s*((?:0|1)(?:.\\d{0,3})?)$");

    /**
     * Retrieves the User-Agent header from the request (if any).
     * @param request servlet request
     * @see #setUserAgentString
     */
    public void setRequest(HttpServletRequest request)
    {
        if (request != null)
        {
            setUserAgentString(request.getHeader("User-Agent"));
            setAcceptLanguage(request.getHeader("Accept-Language"));

            /* Get IP Address */
            IPAddress = request.getHeader("X-FORWARDED-FOR");
            if (IPAddress == null)
            {
                IPAddress = request.getRemoteAddr();
            }
            int coma;
            if (IPAddress != null && (coma = IPAddress.indexOf(',')) != -1)
            {
                /* keep the leftmost address */
                IPAddress = IPAddress.substring(0, coma);
            }
        }
        else
        {
            setUserAgentString(null);
            setAcceptLanguage(null);
        }
    }

    /**
     * Sets the User-Agent string to be parsed for info.  If null, the string
     * will be empty and everything will return false or null.  Otherwise,
     * it will set the whole string to lower case before storing to simplify
     * parsing.
     * @param ua user agent string
     */
    public void setUserAgentString(String ua)
    {
        /* reset internal state */
        userAgentString = null;
        userAgent = null;
        acceptLanguage = preferredLanguage = null;
        languageRangesByQuality = null;
        starLanguageRange = null;

        if (ua == null)
        {
            lowercaseUserAgentString = "";
        }
        else
        {
            userAgentString = ua;
            lowercaseUserAgentString = ua.toLowerCase();
            userAgent = UAParser.parseUserAgent(ua, getLog());
        }
    }

    public void setAcceptLanguage(String al)
    {
        if(al == null)
        {
            acceptLanguage = "";
        }
        else
        {
            acceptLanguage = al.toLowerCase();
        }
    }

    public void setLanguagesFilter(String filter)
    {
        if(filter == null || filter.length() == 0)
        {
            languagesFilter = null;
        }
        else
        {
            languagesFilter = Arrays.asList(filter.split(","));
        }
        // clear preferred language cache
        preferredLanguage = null;
    }

    public String getLanguagesFilter()
    {
        return languagesFilter.toString();
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName()+"[ua="+ userAgentString +"]";
    }


    /* Generic getter for custom tests
     */
    public boolean get(String key)
    {
        return test(key);
    }

    public String getUserAgentString()
    {
	    return userAgentString;
    }

    public String getAcceptLanguage()
    {
        return acceptLanguage;
    }

    /**
     * <p>
     *   Get the client browser IP address. In the session scope, which is the default,
     *   it corresponds to the <i>first</i> seen IP adress.
     * </p>
     * <p>
     *   The tool tries to get the real IP address whenever the request has been proxied.
     * </p>
     * <p>
     *   Please note that the result may be null.
     * </p>
     * @return the IP address as a string
     * @since VelocityTools 3.0
     */
    public String getIPAddress()
    {
        return IPAddress;
    }

    /* device type */

    /**
     * @return found device
     * @since VelocityTools 3.0
     */
    public String getDevice()
    {
        return userAgent == null ? null : userAgent.getDeviceType().toString().toLowerCase();
    }

    public boolean isRobot()
    {
        return userAgent != null && userAgent.getDeviceType() == DeviceType.ROBOT;
    }

    /**
     * @return whether found device is a tablet
     * @since VelocityTools 3.0
     */
    public boolean isTablet()
    {
        return userAgent == null && userAgent.getDeviceType() == DeviceType.TABLET;
    }

    /**
     * @return whether found device is a mobile device
     * @since VelocityTools 3.0
     */
    public boolean isMobile()
    {
        return userAgent == null && userAgent.getDeviceType() == DeviceType.MOBILE;
    }

    /**
     * @return whether found device is a desktop device
     * @since VelocityTools 3.0
     */
    public boolean isDesktop()
    {
        return userAgent == null && userAgent.getDeviceType() == DeviceType.DESKTOP;
    }

    /**
     * @return whether found device is a TV
     * @since VelocityTools 3.0
     */
    public boolean isTV()
    {
        return userAgent == null && userAgent.getDeviceType() == DeviceType.TV;
    }

    /**
     * @return parsed browser
     * @since VelocityTools 3.0
     */
    public UAEntity getBrowser()
    {
        return userAgent == null ? null : userAgent.getBrowser();
    }

    /**
     * @return parsed rendering engine
     * @since VelocityTools 3.0
     */
    public UAEntity getRenderingEngine()
    {
        return userAgent == null ? null : userAgent.getRenderingEngine();
    }

    /**
     * @return parsed operating system
     * @since VelocityTools 3.0
     */
    public UAEntity getOperatingSystem()
    {
        return userAgent == null ? null : userAgent.getOperatingSystem();
    }

    /* Specific rendering engines */

    public boolean isGecko()
    {
        return getRenderingEngine() != null && "Gecko".equals(getRenderingEngine().getName());
    }

    public boolean isWebKit()
    {
        return getRenderingEngine() != null && "AppleWebKit".equals(getRenderingEngine().getName());
    }

    public boolean isKHTML()
    {
        return getRenderingEngine() != null && "KHTML".equals(getRenderingEngine().getName());
    }

    public boolean isTrident()
    {
        return getRenderingEngine() != null && "Trident".equals(getRenderingEngine().getName());
    }

    public boolean isBlink()
    {
        return getRenderingEngine() != null && "Blink".equals(getRenderingEngine().getName());
    }

    public boolean isEdgeHTML()
    {
        return getRenderingEngine() != null && "EdgeHTML".equals(getRenderingEngine().getName());
    }

    public boolean isPresto()
    {
        return getRenderingEngine() != null && "Presto".equals(getRenderingEngine().getName());
    }

    /* Specific browsers */

    public boolean isChrome()
    {
        return getBrowser() != null && ("Chrome".equals(getBrowser().getName()) || "Chromium".equals(getBrowser().getName()));
    }

    public boolean isMSIE()
    {
        return getBrowser() != null && "MSIE".equals(getBrowser().getName());
    }

    public boolean isFirefox()
    {
        return getBrowser() != null && ("Firefox".equals(getBrowser().getName()) || "Iceweasel".equals(getBrowser().getName()));
    }

    public boolean isOpera()
    {
        return getBrowser() != null && ("Opera".equals(getBrowser().getName()) || "Opera Mobile".equals(getBrowser().getName()));
    }

    public boolean isSafari()
    {
        return getBrowser() != null && "Safari".equals(getBrowser().getName());
    }

    public boolean isNetscape()
    {
        return getBrowser() != null && "Netscape".equals(getBrowser().getName());
    }

    public boolean isKonqueror()
    {
        return getBrowser() != null && "Konqueror".equals(getBrowser().getName());
    }

    public boolean isLinks()
    {
        return getBrowser() != null && "Links".equals(getBrowser().getName());
    }

    public boolean isMozilla()
    {
        return getBrowser() != null && "Mozilla".equals(getBrowser().getName());
    }

    /* Operating System */

    public boolean isWindows()
    {
        return getOperatingSystem() != null && getOperatingSystem().getName().startsWith("Windows");
    }

    public boolean isOSX()
    {
        return getOperatingSystem() != null && (getOperatingSystem().getName().equals("OS X") || getOperatingSystem().getName().equals("iOS"));
    }

    private static Set<String> linuxDistros = null;
    static
    {
        linuxDistros = new HashSet<String>();
        linuxDistros.add("Ubuntu");
        linuxDistros.add("Debian");
        linuxDistros.add("Red Hat");
        linuxDistros.add("Fedora");
        linuxDistros.add("Slackware");
        linuxDistros.add("SUSE");
        linuxDistros.add("ArchLinux");
        linuxDistros.add("Gentoo");
        linuxDistros.add("openSUSE");
        linuxDistros.add("Manjaro");
        linuxDistros.add("Mandriva");
        linuxDistros.add("PCLinuxOS");
        linuxDistros.add("CentOS");
        linuxDistros.add("Tizen");
        linuxDistros.add("Mint");
        linuxDistros.add("StartOS");
    }

    public boolean isLinux()
    {
        return getOperatingSystem() != null && (getOperatingSystem().getName().startsWith("Linux") || linuxDistros.contains(getOperatingSystem().getName()));
    }

    public boolean isBSD()
    {
        return getOperatingSystem() != null && getOperatingSystem().getName().endsWith("BSD");
    }

    public boolean isUnix()
    {
        if (getOperatingSystem() != null)
        {
            String osname = getOperatingSystem().getName().toLowerCase();
            return osname.indexOf("unix") != -1 || osname.equals("bsd") || osname.equals("sunos");
        }
        return false;
    }

    public boolean isAndroid()
    {
        return getOperatingSystem() != null && getOperatingSystem().getName().startsWith("Android");
    }

    public boolean isIOS()
    {
        if (getOperatingSystem() != null)
        {
            String osName = getOperatingSystem().getName();
            return osName.startsWith("iOS") || osName.startsWith("iPhone") || osName.startsWith("iPad");
        }
        return false;
    }

    public boolean isSymbian()
    {
        return getOperatingSystem() != null && getOperatingSystem().getName().startsWith("Symb");
    }

    public boolean isBlackberry()
    {
        return getOperatingSystem() != null &&
                (getOperatingSystem().getName().startsWith("BlackBerry") ||
                        getOperatingSystem().getName().equals("PlayBook"));
    }

    /* Features */

    /* Since support of those features is often partial, the sniffer returns true
        when a consequent subset is supported. */

    public boolean getCss3()
    {
        return isTrident() && getRenderingEngine().getMajorVersion() >= 9 ||
                isEdgeHTML() ||
                isGecko() && (getRenderingEngine().getMajorVersion() >=2 || getRenderingEngine().getMinorVersion() >= 9) ||
                isWebKit() && getRenderingEngine().getMajorVersion() >= 85 ||
                isKHTML() && (getRenderingEngine().getMajorVersion() >= 4 || getRenderingEngine().getMajorVersion() == 3 && getRenderingEngine().getMinorVersion() >= 4) ||
                isPresto() && getRenderingEngine().getMajorVersion() >= 2;
    }

    public boolean getDom3()
    {
        return isEdgeHTML() ||
                isTrident() && getRenderingEngine().getMajorVersion() >= 9 ||
                isGecko() && (getRenderingEngine().getMajorVersion() >=2 || getRenderingEngine().getMinorVersion() >= 7) ||
                isWebKit() && getRenderingEngine().getMajorVersion() >= 601;
    }

    /* Languages */

    public String getPreferredLanguage()
    {
        if(preferredLanguage != null) return preferredLanguage;

        parseAcceptLanguage();
        if(languageRangesByQuality.size() == 0)
        {
            preferredLanguage = starLanguageRange; // may be null
        }
        else
        {
            List<List<String>> lists = new ArrayList<List<String>>(languageRangesByQuality.values());
            Collections.reverse(lists);
            for(List<String> lst : lists) // sorted by quality (treemap)
            {
                for(String l : lst)
                {
                    preferredLanguage = filterLanguageTag(l);
                    if(preferredLanguage != null) break;
                }
                if(preferredLanguage != null) break;
            }
        }
        // fallback
        if(preferredLanguage == null)
        {
            preferredLanguage = filterLanguageTag(languagesFilter == null ? getLocale().getLanguage() : languagesFilter.get(0));
        }
        // preferredLanguage should now never be null
        assert(preferredLanguage != null);
        return preferredLanguage;
    }

    public Locale getPreferredLocale()
    {
        return ConversionUtils.toLocale(getPreferredLanguage());
    }

    /* Helpers */

    protected boolean test(String key)
    {
        return key == null ? null : lowercaseUserAgentString.indexOf(key.toLowerCase()) != -1;
    }

    private void parseAcceptLanguage()
    {
        if(languageRangesByQuality != null)
        {
            // already done
            return;
        }

        languageRangesByQuality = new TreeMap<Float,List<String>>();
        StringTokenizer languageTokenizer = new StringTokenizer(acceptLanguage, ",");
        while (languageTokenizer.hasMoreTokens())
        {
            String language = languageTokenizer.nextToken().trim();
            int qValueIndex = language.indexOf(';');
            if(qValueIndex == -1)
            {
                language = language.replace('-','_');
                List<String> l = languageRangesByQuality.get(1.0f);
                if(l == null)
                {
                    l = new ArrayList<String>();
                    languageRangesByQuality.put(1.0f,l);
                }
                l.add(language);
            }
            else
            {
                String code = language.substring(0,qValueIndex).trim().replace('-','_');
                String qval = language.substring(qValueIndex + 1).trim();
                if("*".equals(qval))
                {
                    starLanguageRange = code;
                }
                else
                {
                    Matcher m = quality.matcher(qval);
                    if(m.matches())
                    {
                        Float q = Float.valueOf(m.group(1));
                        List<String> al = languageRangesByQuality.get(q);
                        if(al == null)
                        {
                            al = new ArrayList<String>();
                            languageRangesByQuality.put(q,al);
                        }
                        al.add(code);
                    }
                    else
                    {
                        getLog().error("BrowserTool: could not parse language quality value: {}", language);
                    }
                }
            }
        }
    }

    private String filterLanguageTag(String languageTag)
    {
        languageTag = languageTag.replace('-','_');
        if(languagesFilter == null) return languageTag;
        if(languagesFilter.contains(languageTag)) return languageTag;
        if(languageTag.contains("_"))
        {
            String[] parts = languageTag.split("_");
            if(languagesFilter.contains(parts[0])) return parts[0];
        }
        return null;
    }
}

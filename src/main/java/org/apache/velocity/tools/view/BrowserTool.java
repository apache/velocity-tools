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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.ConversionUtils;
import org.apache.velocity.tools.generic.FormatConfig;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.InvalidScope;

/**
 *  <p>browser-sniffing tool (session or request scope requested, session scope advised).</p>
 *  <p></p>
 * <p><b>Usage:</b></p>
 * <p>BrowserTool defines properties that are used to test the client browser, operating system, device, language...
 * Apart from properties related to browser version and language, all properties are booleans.</p>
 * <p>The following properties are available:</p>
 * <ul>
 * <li><i>Versioning:</i>version majorVersion minorVersion geckoVersion</li>
 * <li><i>Browser:</i>mosaic netscape nav2 nav3 nav4 nav4up nav45 nav45up nav6 nav6up navgold firefox safari
 * ie ie3 ie4 ie4up ie5 ie5up ie55 ie55up ie6 ie6up ie7 ie7up ie8 ie8up opera opera3 opera4 opera5 opera6 opera7 opera8 opera9 lynx links w3m
 * aol aol3 aol4 aol5 aol6 neoplanet neoplanet2 amaya icab avantgo emacs mozilla gecko webtv staroffice java hotjava httpclient lobo
 * lotusnotes konqueror galeon kmeleon chrome</li>
 * <li><i>Operating systems:</i>win16 win3x win31 win95 win98 winnt windows win32 winme win2k winxp vista dotnet
 * mac macosx mac68k macppc os2 unix sun sun4 sun5 suni86 irix irix5 irix6 hpux hpux9 hpux10 aix aix1 aix2 aix3 aix4
 * linux sco unixware mpras reliant dec sinix freebsd bsd vms x11 amiga</li>
 * <li><i>Devices:</i>palm audrey iopener wap blackberry</li>
 * <li><i>Features:</i>javascript css css1 css2 dom0 dom1 dom2</li>
 * <li><i>Special:</i>robot (true if the page is requested by a robot, i.e. when one of the following properties is true:
 * wget getright yahoo altavista lycos infoseek lwp webcrawler linkexchange slurp google java)</li>
 * <li>Language: preferredLanguageTag (a string like 'en', 'da', 'en-US', ...), preferredLocale (a java Locale)</li>
 * </ul>
 *
 * <p>Language properties are filtered by the languagesFilter tool param, if present. If no matching language is found, or if there is no
 * matching language, the tools defaut locale (or the first value of languagesFilter) is returned.
 * Their value is guarantied to belong to the set provided in languagesFilter, if any.</p>
 *
 * Thanks to Lee Semel (lee@semel.net), the author of the HTTP::BrowserDetect Perl module.
 * See also:
 * * http://www.zytrax.com/tech/web/browser_ids.htm
 * * http://en.wikipedia.org/wiki/User_agent
 * * http://www.user-agents.org/
 *
 * @author <a href="mailto:claude@renegat.net">Claude Brisson</a>
 * @since VelocityTools 2.0
 * @version $Revision$ $Date$
 */
@DefaultKey("browser")
@InvalidScope(Scope.APPLICATION)
public class BrowserTool extends FormatConfig implements java.io.Serializable
{
    private static final long serialVersionUID = 1734529350532353339L;

    protected Log LOG;

    /* User-Agent header variables */
    private String userAgent = null;
    private String version = null;
    private int majorVersion = -1;
    private int minorVersion = -1;
    private String geckoVersion = null;
    private int geckoMajorVersion = -1;
    private int geckoMinorVersion = -1;

    private static Pattern genericVersion = Pattern.compile(
            "/"
            /* Version starts with a slash */
            +
            "([A-Za-z]*"
            /* Eat any letters before the major version */
            +
            "( [\\d]* )"
            /* Major version number is every digit before the first dot */
            + "\\." /* The first dot */
            +
            "( [\\d]* )"
            /* Minor version number is every digit after the first dot */
            + "[^\\s]*)" /* Throw away the remaining */
            , Pattern.COMMENTS);
    private static Pattern firefoxVersion = Pattern.compile(
            "/"
            +
            "(( [\\d]* )"
            /* Major version number is every digit before the first dot */
            + "\\." /* The first dot */
            +
            "( [\\d]* )"
            /* Minor version number is every digit after the first dot */
            + "[^\\s]*)" /* Throw away the remaining */
            , Pattern.COMMENTS);
    private static Pattern ieVersion = Pattern.compile(
            "compatible;"
            + "\\s*"
            + "\\w*" /* Browser name */
            + "[\\s|/]"
            +
            "([A-Za-z]*"
            /* Eat any letters before the major version */
            +
            "( [\\d]* )"
            /* Major version number is every digit before first dot */
            + "\\." /* The first dot */
            +
            "( [\\d]* )"
            /* Minor version number is digits after first dot */
            + "[^\\s]*)" /* Throw away remaining dots and digits */
            , Pattern.COMMENTS);
    private static Pattern safariVersion = Pattern.compile(
            "safari/"
            +
            "(( [\\d]* )"
            /* Major version number is every digit before first dot */
            + "(?:"
            + "\\." /* The first dot */
            +
            " [\\d]* )?)"
            /* Minor version number is digits after first dot */
            , Pattern.COMMENTS);
    private static Pattern mozillaVersion = Pattern.compile(
            "netscape/"
            +
            "(( [\\d]* )"
            /* Major version number is every digit before the first dot */
            + "\\." /* The first dot */
            +
            "( [\\d]* )"
            /* Minor version number is every digit after the first dot */
            + "[^\\s]*)" /* Throw away the remaining */
            , Pattern.COMMENTS);
    private static Pattern fallbackVersion = Pattern.compile(
            "[\\w]+/"
            +
            "( [\\d]+ );"
            /* Major version number is every digit before the first dot */
            , Pattern.COMMENTS);


    /* Accept-Language header variables */
    private String acceptLanguage = null;
    private SortedMap<Float,List<String>> languageRangesByQuality = null;
    private String starLanguageRange = null;
    // pametrizable filter of retained laguages
    private List<String> languagesFilter = null;
    private String preferredLanguage = null;

    private static Pattern quality = Pattern.compile("^q\\s*=\\s*(\\d(?:0(?:.\\d{0,3})?|1(?:.0{0,3}))?)$");

    /**
     * Retrieves the User-Agent header from the request (if any).
     * @see #setUserAgent
     */
    public void setRequest(HttpServletRequest request)
    {
        if (request != null)
        {
            setUserAgent(request.getHeader("User-Agent"));
            setAcceptLanguage(request.getHeader("Accept-Language"));
        }
        else
        {
            setUserAgent(null);
            setAcceptLanguage(null);
        }
    }

    /**
     * Set log.
     */
    public void setLog(Log log)
    {
        if (log == null)
        {
            throw new NullPointerException("log should not be set to null");
        }
        this.LOG = log;
    }


    /**
     * Sets the User-Agent string to be parsed for info.  If null, the string
     * will be empty and everything will return false or null.  Otherwise,
     * it will set the whole string to lower case before storing to simplify
     * parsing.
     */
    public void setUserAgent(String ua)
    {
        if (ua == null)
        {
            userAgent = "";
        }
        else
        {
            userAgent = ua.toLowerCase();
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
        return this.getClass().getSimpleName()+"[ua="+userAgent+"]";
    }


    /* Generic getter for unknown tests
     */
    public boolean get(String key)
    {
        return test(key);
    }

    public String getUserAgent()
    {
	    return userAgent;
    }

    public String getAcceptLanguage()
    {
        return acceptLanguage;
    }

    /* Versioning */

    public String getVersion()
    {
        parseVersion();
        return version;
    }

    public int getMajorVersion()
    {
        parseVersion();
        return majorVersion;
    }

    public int getMinorVersion()
    {
        parseVersion();
        return minorVersion;
    }

    public String getGeckoVersion()
    {
        parseVersion();
        return geckoVersion;
    }

    public int getGeckoMajorVersion()
    {
        parseVersion();
        return geckoMajorVersion;
    }

    public int getGeckoMinorVersion()
    {
        parseVersion();
        return geckoMinorVersion;
    }

    /* Browsers */

    public boolean getGecko()
    {
        return test("gecko") && !test("like gecko");
    }

    public boolean getFirefox()
    {
        return test("firefox") || test("firebird") || test("phoenix") || test("iceweasel");
    }

    public boolean getIceweasel()
    {
        return test("iceweasel");
    }

    public boolean getGaleon()
    {
        return test("galeon");
    }

    public boolean getKmeleon()
    {
        return test("k-meleon");
    }

    public boolean getEpiphany()
    {
        return test("epiphany");
    }

    public boolean getSafari()
    {
        return (test("safari") || test("applewebkit")) && !test("chrome");
    }

    public boolean getChrome() {
        return test("chrome");
    }

    public boolean getDillo()
    {
        return test("dillo");
    }

    public boolean getNetscape()
    {
        return test("netscape") || !getFirefox() && !getSafari() && test("mozilla") &&
               !test("spoofer") && !test("compatible") && !test("opera") &&
               !test("webtv") && !test("hotjava");
    }

    public boolean getNav2()
    {
        return getNetscape() && getMajorVersion() == 2;
    }

    public boolean getNav3()
    {
        return getNetscape() && getMajorVersion() == 3;
    }

    public boolean getNav4()
    {
        return getNetscape() && getMajorVersion() == 4;
    }

    public boolean getNav4up()
    {
        return getNetscape() && getMajorVersion() >= 4;
    }

    public boolean getNav45()
    {
        return getNetscape() && getMajorVersion() == 4 &&
               getMinorVersion() == 5;
    }

    public boolean getNav45up()
    {
        return getNetscape() && getMajorVersion() >= 5 ||
               getNav4() && getMinorVersion() >= 5;
    }

    public boolean getNavgold()
    {
        return test("gold");
    }

    public boolean getNav6()
    {
        return getNetscape() && getMajorVersion() == 5; /* sic */
    }

    public boolean getNav6up()
    {
        return getNetscape() && getMajorVersion() >= 5;
    }

    public boolean getMozilla()
    {
        return getNetscape() && getGecko();
    }

    public boolean getIe()
    {
        return test("msie") && !test("opera") ||
               test("microsoft internet explorer");
    }

    public boolean getIe3()
    {
        return getIe() && getMajorVersion() < 4;
    }

    public boolean getIe4()
    {
        return getIe() && getMajorVersion() == 4;
    }

    public boolean getIe4up()
    {
        return getIe() && getMajorVersion() >= 4;
    }

    public boolean getIe5()
    {
        return getIe() && getMajorVersion() == 5;
    }

    public boolean getIe5up()
    {
        return getIe() && getMajorVersion() >= 5;
    }

    public boolean getIe55()
    {
        return getIe() && getMajorVersion() == 5 && getMinorVersion() >= 5;
    }

    public boolean getIe55up()
    {
        return (getIe5() && getMinorVersion() >= 5) ||
               (getIe() && getMajorVersion() >= 6);
    }

    public boolean getIe6()
    {
        return getIe() && getMajorVersion() == 6;
    }

    public boolean getIe6up()
    {
        return getIe() && getMajorVersion() >= 6;
    }

    public boolean getIe7()
    {
        return getIe() && getMajorVersion() == 7;
    }

    public boolean getIe7up()
    {
        return getIe() && getMajorVersion() >= 7;
    }

    public boolean getIe8()
    {
        return getIe() && getMajorVersion() == 8;
    }

    public boolean getIe8up()
    {
        return getIe() && getMajorVersion() >= 8;
    }

    public boolean getNeoplanet()
    {
        return test("neoplanet");
    }

    public boolean getNeoplanet2()
    {
        return getNeoplanet() && test("2.");
    }

    public boolean getAol()
    {
        return test("aol");
    }

    public boolean getAol3()
    {
        return test("aol 3.0") || getAol() && getIe3();
    }

    public boolean getAol4()
    {
        return test("aol 4.0") || getAol() && getIe4();
    }

    public boolean getAol5()
    {
        return test("aol 5.0");
    }

    public boolean getAol6()
    {
        return test("aol 6.0");
    }

    public boolean getAolTV()
    {
        return test("navio") || test("navio_aoltv");
    }

    public boolean getOpera()
    {
        return test("opera");
    }

    public boolean getOpera3()
    {
        return test("opera 3") || test("opera/3");
    }

    public boolean getOpera4()
    {
        return test("opera 4") || test("opera/4");
    }

    public boolean getOpera5()
    {
        return test("opera 5") || test("opera/5");
    }

    public boolean getOpera6()
    {
        return test("opera 6") || test("opera/6");
    }

    public boolean getOpera7()
    {
        return test("opera 7") || test("opera/7");
    }

    public boolean getOpera8()
    {
        return test("opera 8") || test("opera/8");
    }

    public boolean getOpera9()
    {
        return test("opera/9");
    }

    public boolean getHotjava()
    {
        return test("hotjava");
    }

    public boolean getHotjava3()
    {
        return getHotjava() && getMajorVersion() == 3;
    }

    public boolean getHotjava3up()
    {
        return getHotjava() && getMajorVersion() >= 3;
    }

    public boolean getLobo()
    {
        return test("lobo");
    }

    public boolean getHttpclient()
    {
        return test("httpclient");
    }

    public boolean getAmaya()
    {
        return test("amaya");
    }

    public boolean getCurl()
    {
        return test("libcurl");
    }

    public boolean getStaroffice()
    {
        return test("staroffice");
    }

    public boolean getIcab()
    {
        return test("icab");
    }

    public boolean getLotusnotes()
    {
        return test("lotus-notes");
    }

    public boolean getKonqueror()
    {
        return test("konqueror");
    }

    public boolean getLynx()
    {
        return test("lynx");
    }

    public boolean getLinks()
    {
        return test("links");
    }

    public boolean getW3m()
    {
        return test("w3m");
    }

    public boolean getWebTV()
    {
        return test("webtv");
    }

    public boolean getMosaic()
    {
        return test("mosaic");
    }

    public boolean getWget()
    {
        return test("wget");
    }

    public boolean getGetright()
    {
        return test("getright");
    }

    public boolean getLwp()
    {
        return test("libwww-perl") || test("lwp-");
    }

    public boolean getYahoo()
    {
        return test("yahoo");
    }

    public boolean getGoogle()
    {
        return test("google");
    }

    public boolean getJava()
    {
        return test("java") || test("jdk") || test("httpunit") || test("httpclient") || test("lobo");
    }

    public boolean getAltavista()
    {
        return test("altavista");
    }

    public boolean getScooter()
    {
        return test("scooter");
    }

    public boolean getLycos()
    {
        return test("lycos");
    }

    public boolean getInfoseek()
    {
        return test("infoseek");
    }

    public boolean getWebcrawler()
    {
        return test("webcrawler");
    }

    public boolean getLinkexchange()
    {
        return test("lecodechecker");
    }

    public boolean getSlurp()
    {
        return test("slurp");
    }

    public boolean getRobot()
    {
        return getWget() || getGetright() || getLwp() || getYahoo() ||
               getGoogle() || getAltavista() || getScooter() || getLycos() ||
               getInfoseek() || getWebcrawler() || getLinkexchange() ||
               test("bot") || test("spider") || test("crawl") ||
               test("agent") || test("seek") || test("search") ||
               test("reap") || test("worm") || test("find") || test("index") ||
               test("copy") || test("fetch") || test("ia_archive") ||
               test("zyborg");
    }

    /* Devices */

    public boolean getBlackberry()
    {
        return test("blackberry");
    }

    public boolean getAudrey()
    {
        return test("audrey");
    }

    public boolean getIopener()
    {
        return test("i-opener");
    }

    public boolean getAvantgo()
    {
        return test("avantgo");
    }

    public boolean getPalm()
    {
        return getAvantgo() || test("palmos");
    }

    public boolean getWap()
    {
        return test("up.browser") || test("nokia") || test("alcatel") ||
               test("ericsson") || userAgent.indexOf("sie-") == 0 ||
               test("wmlib") || test(" wap") || test("wap ") ||
               test("wap/") || test("-wap") || test("wap-") ||
               userAgent.indexOf("wap") == 0 ||
               test("wapper") || test("zetor");
    }

    /* Operating System */

    public boolean getWin16()
    {
        return test("win16") || test("16bit") || test("windows 3") ||
               test("windows 16-bit");
    }

    public boolean getWin3x()
    {
        return test("win16") || test("windows 3") || test("windows 16-bit");
    }

    public boolean getWin31()
    {
        return test("win16") || test("windows 3.1") || test("windows 16-bit");
    }

    public boolean getWin95()
    {
        return test("win95") || test("windows 95");
    }

    public boolean getWin98()
    {
        return test("win98") || test("windows 98");
    }

    public boolean getWinnt()
    {
        return test("winnt") || test("windows nt") || test("nt4") || test("nt3");
    }

    public boolean getWin2k()
    {
        return test("nt 5.0") || test("nt5");
    }

    public boolean getWinxp()
    {
        return test("nt 5.1");
    }

    public boolean getVista()
    {
        return test("nt 6.0");
    }

    public boolean getDotnet()
    {
        return test(".net clr");
    }

    public boolean getWinme()
    {
        return test("win 9x 4.90");
    }

    public boolean getWin32()
    {
        return getWin95() || getWin98() || getWinnt() || getWin2k() ||
               getWinxp() || getWinme() || test("win32");
    }

    public boolean getWindows()
    {
        return getWin16() || getWin31() || getWin95() || getWin98() ||
               getWinnt() || getWin32() || getWin2k() || getWinme() ||
               test("win");
    }

    public boolean getMac()
    {
        return test("macintosh") || test("mac_");
    }

    public boolean getMacosx()
    {
        return test("macintosh") || test("mac os x");
    }

    public boolean getMac68k()
    {
        return getMac() && (test("68k") || test("68000"));
    }

    public boolean getMacppc()
    {
        return getMac() && (test("ppc") || test("powerpc"));
    }

    public boolean getAmiga()
    {
        return test("amiga");
    }

    public boolean getEmacs()
    {
        return test("emacs");
    }

    public boolean getOs2()
    {
        return test("os/2");
    }

    public boolean getSun()
    {
        return test("sun");
    }

    public boolean getSun4()
    {
        return test("sunos 4");
    }

    public boolean getSun5()
    {
        return test("sunos 5");
    }

    public boolean getSuni86()
    {
        return getSun() && test("i86");
    }

    public boolean getIrix()
    {
        return test("irix");
    }

    public boolean getIrix5()
    {
        return test("irix5");
    }

    public boolean getIrix6()
    {
        return test("irix6");
    }

    public boolean getHpux()
    {
        return test("hp-ux");
    }

    public boolean getHpux9()
    {
        return getHpux() && test("09.");
    }

    public boolean getHpux10()
    {
        return getHpux() && test("10.");
    }

    public boolean getAix()
    {
        return test("aix");
    }

    public boolean getAix1()
    {
        return test("aix 1");
    }

    public boolean getAix2()
    {
        return test("aix 2");
    }

    public boolean getAix3()
    {
        return test("aix 3");
    }

    public boolean getAix4()
    {
        return test("aix 4");
    }

    public boolean getLinux()
    {
        return test("linux");
    }

    public boolean getSco()
    {
        return test("sco") || test("unix_sv");
    }

    public boolean getUnixware()
    {
        return test("unix_system_v");
    }

    public boolean getMpras()
    {
        return test("ncr");
    }

    public boolean getReliant()
    {
        return test("reliantunix");
    }

    public boolean getDec()
    {
        return test("dec") || test("osf1") || test("delalpha") ||
               test("alphaserver") || test("ultrix") || test("alphastation");
    }

    public boolean getSinix()
    {
        return test("sinix");
    }

    public boolean getFreebsd()
    {
        return test("freebsd");
    }

    public boolean getBsd()
    {
        return test("bsd");
    }

    public boolean getX11()
    {
        return test("x11");
    }

    public boolean getUnix()
    {
        return getX11() || getSun() || getIrix() || getHpux() || getSco() ||
               getUnixware() || getMpras() || getReliant() || getDec() ||
               getLinux() || getBsd() || test("unix");
    }

    public boolean getVMS()
    {
        return test("vax") || test("openvms");
    }

    /* Features */

    /* Since support of those features is often partial, the sniffer returns true
        when a consequent subset is supported. */

    public boolean getCss()
    {
        return (getIe() && getMajorVersion() >= 4) ||
               (getNetscape() && getMajorVersion() >= 4) ||
               getGecko() ||
               getKonqueror() ||
               (getOpera() && getMajorVersion() >= 3) ||
               getSafari() ||
               getChrome() ||
               getLinks();
    }

    public boolean getCss1()
    {
        return getCss();
    }

    public boolean getCss2()
    {
        return getIe() &&
               (getMac() && getMajorVersion() >= 5) ||
               (getWin32() && getMajorVersion() >= 6) ||
               getGecko() || // && version >= ?
               (getOpera() && getMajorVersion() >= 4) ||
               (getSafari() && getMajorVersion() >= 2) ||
               (getKonqueror() && getMajorVersion() >= 2) ||
               getChrome();
    }

    public boolean getDom0()
    {
        return (getIe() && getMajorVersion() >= 3) ||
               (getNetscape() && getMajorVersion() >= 2) ||
               (getOpera() && getMajorVersion() >= 3) ||
               getGecko() ||
               getSafari() ||
               getChrome() ||
               getKonqueror();
    }

    public boolean getDom1()
    {
        return (getIe() && getMajorVersion() >= 5) ||
               getGecko() ||
               (getSafari() && getMajorVersion() >= 2) ||
               (getOpera() && getMajorVersion() >= 4) ||
               (getKonqueror() && getMajorVersion() >= 2)
               || getChrome();
    }

    public boolean getDom2()
    {
        return (getIe() && getMajorVersion() >= 6) ||
               (getMozilla() && getMajorVersion() >= 5.0) ||
               (getOpera() && getMajorVersion() >= 7) ||
               getFirefox() ||
               getChrome();
    }

    public boolean getJavascript()
    {
        return getDom0(); // good approximation
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
            preferredLanguage = filterLanguageTag(languagesFilter == null ? getLocale().getDisplayName() : languagesFilter.get(0));
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

    private boolean test(String key)
    {
        return userAgent.indexOf(key) != -1;
    }

    private void parseVersion()
    {
        try
        {
            if (version != null)
            {
                return; /* parsing of version already done */
            }

            /* generic versionning */
            Matcher v = genericVersion.matcher(userAgent);

            if (v.find())
            {
                version = v.group(1);
                if(version.endsWith(";"))
                {
                    version = version.substring(0,version.length()-1);
                }
                try
                {
                    majorVersion = Integer.valueOf(v.group(2));
                    String minor = v.group(3);
                    if (minor.startsWith("0"))
                    {
                        minorVersion = 0;
                    }
                    else
                    {
                        minorVersion = Integer.valueOf(minor);
                    }
                }
                catch (NumberFormatException nfe)
                {
                    LOG.error("BrowserTool: Could not parse browser version for User-Agent: "+userAgent,nfe);
                }
            }

            /* Firefox versionning */
            if (test("firefox"))
            {
                Matcher fx = firefoxVersion.matcher(userAgent);
                if (fx.find())
                {
                    version = fx.group(1);
                    try
                    {
                        majorVersion = Integer.valueOf(fx.group(2));
                        String minor = fx.group(3);
                        if (minor.startsWith("0"))
                        {
                            minorVersion = 0;
                        }
                        else
                        {
                            minorVersion = Integer.valueOf(minor);
                        }
                    }
                    catch (NumberFormatException nfe)
                    {
                        LOG.error("BrowserTool: Could not parse browser version for User-Agent: "+userAgent,nfe);
                    }
                }
            }

            /* IE versionning */
            else if (test("compatible"))
            {
                Matcher ie = ieVersion.matcher(userAgent);

                if (ie.find())
                {
                    version = ie.group(1);
                    try
                    {
                        majorVersion = Integer.valueOf(ie.group(2));
                        String minor = ie.group(3);
                        if (minor.startsWith("0"))
                        {
                            minorVersion = 0;
                        }
                        else
                        {
                            minorVersion = Integer.valueOf(minor);
                        }
                    }
                    catch (NumberFormatException nfe)
                    {
                        LOG.error("BrowserTool: Could not parse browser version for User-Agent: "+userAgent,nfe);
                    }
                }
            }

            /* Safari versionning*/
            else if (getSafari())
            {
                Matcher safari = safariVersion.matcher(userAgent);
                if (safari.find())
                {
                    version = safari.group(1);
                    try
                    {
                        int sv = Integer.valueOf(safari.group(2));
                        majorVersion = sv / 100;
                        minorVersion = sv % 100;
                    }
                    catch (NumberFormatException nfe)
                    {
                        LOG.error("BrowserTool: Could not parse browser version for User-Agent: "+userAgent,nfe);
                    }
                }
            }

            /* Gecko-powered Netscape (i.e. Mozilla) versions */
            else if (getGecko() && getNetscape() && test("netscape"))
            {
                Matcher netscape = mozillaVersion.matcher(userAgent);
                if (netscape.find())
                {
                    version = netscape.group(1);
                    try
                    {
                        majorVersion = Integer.valueOf(netscape.group(2));
                        String minor = netscape.group(3);
                        if (minor.startsWith("0"))
                        {
                            minorVersion = 0;
                        }
                        else
                        {
                            minorVersion = Integer.valueOf(minor);
                        }
                    }
                    catch (NumberFormatException nfe)
                    {
                        LOG.error("BrowserTool: Could not parse browser version for User-Agent: "+userAgent,nfe);
                    }
                }
            }

            /* last try if version not found */
            if (version == null)
            {
                Matcher mv = fallbackVersion.matcher(userAgent);
                if (mv.find())
                {
                    version = mv.group(1);
                    try
                    {
                        majorVersion = Integer.valueOf(version);
                        minorVersion = 0;
                    }
                    catch (NumberFormatException nfe)
                    {
                        LOG.error("BrowserTool: Could not parse browser version for User-Agent: "+userAgent,nfe);
                    }
                }
            }

            /* gecko engine version */
            if (getGecko())
            {
            	Matcher g = Pattern.compile(
                        "\\([^)]*rv:(([\\d]*)\\.([\\d]*).*?)\\)"
                        ).matcher(userAgent);
                if (g.find())
                {
                    geckoVersion = g.group(1);
                    try
                    {
                    	geckoMajorVersion = Integer.valueOf(g.group(2));
                    	String minor = g.group(3);
                        if (minor.startsWith("0"))
                        {
                            geckoMinorVersion = 0;
                        }
                        else
                        {
                            geckoMinorVersion = Integer.valueOf(minor);
                        }
                    }
                    catch (NumberFormatException nfe)
                    {
                        LOG.error("BrowserTool: Could not parse browser version for User-Agent: "+userAgent,nfe);
                    }
                }
            }
        }
        catch (PatternSyntaxException pse)
        {
            LOG.error("BrowserTool: Could not parse browser version for User-Agent: "+userAgent,pse);
        }
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
                        LOG.error("BrowserTool: could not parse language quality value: "+language);
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

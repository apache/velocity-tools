package org.apache.velocity.tools.view;

import org.apache.velocity.tools.generic.FormatConfig;
import static org.apache.velocity.tools.view.UAParser.UAEntity;


@Deprecated
public abstract class BrowserToolDeprecatedMethods extends FormatConfig
{
    public abstract UAEntity getBrowser();
    public abstract UAEntity getRenderingEngine();
    public abstract UAEntity getOperatingSystem();
    public abstract boolean isMSIE();
    public abstract boolean isNetscape();
    public abstract boolean isOpera();
    public abstract boolean isOSX();
    public abstract boolean isGecko();
    public abstract boolean isKonqueror();
    public abstract boolean isSafari();
    public abstract boolean isChrome();
    public abstract boolean isLinks();
    public abstract boolean isWindows();
    public abstract boolean isMozilla();
    public abstract boolean isFirefox();
    public abstract boolean isLinux();
    protected abstract boolean test(String str);

    /**
     * @deprecated use {@link #getBrowser()} version getters
     */
    @Deprecated
    public String getVersion()
    {
        return getBrowser().getMajorVersion() + "." + getBrowser().getMinorVersion();
    }

    /**
     * @deprecated use {@link #getBrowser()}.getMajorVersion()
     */
    @Deprecated
    public int getMajorVersion()
    {
        return getBrowser().getMajorVersion();
    }

    /**
     * @deprecated use {@link #getBrowser()}.getMinorVersion()
     */
    @Deprecated
    public int getMinorVersion()
    {
        return getBrowser().getMinorVersion();
    }

    /**
     * @deprecated use {@link #getRenderingEngine()} and version getters
     */
    @Deprecated
    public String getGeckoVersion()
    {
        UAEntity renderingEngine = getRenderingEngine();
        return
                renderingEngine != null && "Gecko".equals(renderingEngine.getName()) ?
                        renderingEngine.getMajorVersion() + "." + renderingEngine.getMinorVersion() :
                        null;
    }

    /**
     * @deprecated use {@link #getRenderingEngine()} and version getters
     */
    public int getGeckoMajorVersion()
    {
        UAEntity renderingEngine = getRenderingEngine();
        return
                renderingEngine != null && "Gecko".equals(renderingEngine.getName()) ?
                        renderingEngine.getMajorVersion() :
                        0;
    }

    /**
     * @deprecated use {@link #getRenderingEngine()} version getters
     */
    public int getGeckoMinorVersion()
    {
        UAEntity renderingEngine = getRenderingEngine();
        return
                renderingEngine != null && "Gecko".equals(renderingEngine.getName()) ?
                        renderingEngine.getMajorVersion() :
                        0;
    }

    /**
     * @deprecated
     */
    public boolean getNav2()
    {
        return isNetscape() && getBrowser().getMajorVersion() == 2;
    }

    /**
     * @deprecated
     */
    public boolean getNav3()
    {
        return isNetscape() && getMajorVersion() == 3;
    }

    /**
     * @deprecated
     */
    public boolean getNav4()
    {
        return isNetscape() && getMajorVersion() == 4;
    }

    /**
     * @deprecated
     */
    public boolean getNav4up()
    {
        return isNetscape() && getMajorVersion() >= 4;
    }

    /**
     * @deprecated
     */
    public boolean getNav45()
    {
        return isNetscape() && getMajorVersion() == 4 &&
                getMinorVersion() == 5;
    }

    /**
     * @deprecated
     */
    public boolean getNav45up()
    {
        return isNetscape() && getMajorVersion() >= 5 ||
                getNav4() && getMinorVersion() >= 5;
    }

    /**
     * @deprecated
     */
    public boolean getNavgold()
    {
        return test("gold");
    }

    /**
     * @deprecated
     */
    public boolean getNav6()
    {
        return isNetscape() && getMajorVersion() == 5; /* sic */
    }

    /**
     * @deprecated
     */
    public boolean getNav6up()
    {
        return isNetscape() && getMajorVersion() >= 5;
    }

    /**
     * @deprecated
     */
    public boolean getIe()
    {
        return isMSIE();
    }

    /**
     * @deprecated
     */
    public boolean getIe3()
    {
        return isMSIE() && getBrowser().getMajorVersion() < 4;
    }

    /**
     * @deprecated
     */
    public boolean getIe4()
    {
        return isMSIE() && getBrowser().getMajorVersion() == 4;
    }

    /**
     * @deprecated
     */
    public boolean getIe4up()
    {
        return isMSIE() && getBrowser().getMajorVersion() >= 4;
    }

    /**
     * @deprecated
     */
    public boolean getIe5()
    {
        return isMSIE() && getBrowser().getMajorVersion() == 5;
    }

    /**
     * @deprecated
     */
    public boolean getIe5up()
    {
        return isMSIE() && getBrowser().getMajorVersion() >= 5;
    }

    /**
     * @deprecated
     */
    public boolean getIe55()
    {
        return isMSIE() && getBrowser().getMajorVersion() == 5 && getBrowser().getMinorVersion() >= 5;
    }

    /**
     * @deprecated
     */
    public boolean getIe55up()
    {
        return (getIe5() && getBrowser().getMinorVersion() >= 5) ||
                (isMSIE() && getBrowser().getMajorVersion() >= 6);
    }

    /**
     * @deprecated
     */
    public boolean getIe6()
    {
        return isMSIE() && getBrowser().getMajorVersion() == 6;
    }

    /**
     * @deprecated
     */
    public boolean getIe6up()
    {
        return isMSIE() && getBrowser().getMajorVersion() >= 6;
    }

    /**
     * @deprecated
     */
    public boolean getIe7()
    {
        return isMSIE() && getBrowser().getMajorVersion() == 7;
    }

    /**
     * @deprecated
     */
    public boolean getIe7up()
    {
        return isMSIE() && getBrowser().getMajorVersion() >= 7;
    }

    /**
     * @deprecated
     */
    public boolean getIe8()
    {
        return isMSIE() && getBrowser().getMajorVersion() == 8;
    }

    /**
     * @deprecated
     */
    public boolean getIe8up()
    {
        return isMSIE() && getBrowser().getMajorVersion() >= 8;
    }

    /**
     * @deprecated
     */
    public boolean getOpera3()
    {
        return isOpera() && getBrowser().getMajorVersion() == 3;
    }

    /**
     * @deprecated
     */
    public boolean getOpera4()
    {
        return isOpera() && getBrowser().getMajorVersion() == 4;
    }

    /**
     * @deprecated
     */
    public boolean getOpera5()
    {
        return isOpera() && getBrowser().getMajorVersion() == 5;
    }

    /**
     * @deprecated
     */
    public boolean getOpera6()
    {
        return isOpera() && getBrowser().getMajorVersion() == 6;
    }

    /**
     * @deprecated
     */
    public boolean getOpera7()
    {
        return isOpera() && getBrowser().getMajorVersion() == 7;
    }

    /**
     * @deprecated
     */
    public boolean getOpera8()
    {
        return isOpera() && getBrowser().getMajorVersion() == 8;
    }

    /**
     * @deprecated
     */
    public boolean getOpera9()
    {
        return test("opera/9");
    }

    /**
     * @deprecated
     */
    public boolean getWin16()
    {
        return test("win16") || test("16bit") || test("windows 3") ||
                test("windows 16-bit");
    }

    /**
     * @deprecated
     */
    public boolean getWin3x()
    {
        return test("win16") || test("windows 3") || test("windows 16-bit");
    }

    /**
     * @deprecated
     */
    public boolean getWin31()
    {
        return test("win16") || test("windows 3.1") || test("windows 16-bit");
    }

    /**
     * @deprecated
     */
    public boolean getWin95()
    {
        return test("win95") || test("windows 95");
    }

    /**
     * @deprecated
     */
    public boolean getWin98()
    {
        return test("win98") || test("windows 98");
    }

    /**
     * @deprecated
     */
    public boolean getWinnt()
    {
        return test("winnt") || test("windows nt") || test("nt4") || test("nt3");
    }

    /**
     * @deprecated
     */
    public boolean getWin2k()
    {
        return test("nt 5.0") || test("nt5");
    }

    /**
     * @deprecated
     */
    public boolean getWinxp()
    {
        return test("nt 5.1");
    }

    /**
     * @deprecated
     */
    public boolean getVista()
    {
        return test("nt 6.0");
    }

    /**
     * @deprecated
     */
    public boolean getDotnet()
    {
        return test(".net clr");
    }

    /**
     * @deprecated
     */
    public boolean getWinme()
    {
        return test("win 9x 4.90");
    }

    /**
     * @deprecated
     */
    public boolean getWin32()
    {
        return getWin95() || getWin98() || getWinnt() || getWin2k() ||
                getWinxp() || getWinme() || test("win32");
    }

    /**
     * @deprecated use isOSX()
     */
    @Deprecated
    public boolean isMac()
    {
        return isOSX();
    }

    @Deprecated
    public boolean isMac68k()
    {
        return isMac() && (test("68k") || test("68000"));
    }

    @Deprecated
    public boolean isMacppc()
    {
        return isMac() && (test("ppc") || test("powerpc"));
    }

    @Deprecated
    public boolean isAmiga()
    {
        return test("amiga");
    }

    @Deprecated
    public boolean isEmacs()
    {
        return test("emacs");
    }

    @Deprecated
    public boolean isOs2()
    {
        return test("os/2");
    }

    @Deprecated
    public boolean isSun()
    {
        return test("sun");
    }

    @Deprecated
    public boolean isSun4()
    {
        return test("sunos 4");
    }

    @Deprecated
    public boolean isSun5()
    {
        return test("sunos 5");
    }

    @Deprecated
    public boolean isSuni86()
    {
        return isSun() && test("i86");
    }

    @Deprecated
    public boolean isIrix()
    {
        return test("irix");
    }

    @Deprecated
    public boolean isIrix5()
    {
        return test("irix5");
    }

    @Deprecated
    public boolean isIrix6()
    {
        return test("irix6");
    }

    @Deprecated
    public boolean isHpux()
    {
        return test("hp-ux");
    }

    @Deprecated
    public boolean isHpux9()
    {
        return isHpux() && test("09.");
    }

    @Deprecated
    public boolean isHpux10()
    {
        return isHpux() && test("10.");
    }

    @Deprecated
    public boolean isAix()
    {
        return test("aix");
    }

    @Deprecated
    public boolean isAix1()
    {
        return test("aix 1");
    }

    @Deprecated
    public boolean isAix2()
    {
        return test("aix 2");
    }

    @Deprecated
    public boolean isAix3()
    {
        return test("aix 3");
    }

    @Deprecated
    public boolean isAix4()
    {
        return test("aix 4");
    }

    @Deprecated
    public boolean isSco()
    {
        return test("sco") || test("unix_sv");
    }

    @Deprecated
    public boolean isUnixware()
    {
        return test("unix_system_v");
    }

    @Deprecated
    public boolean isMpras()
    {
        return test("ncr");
    }

    @Deprecated
    public boolean isReliant()
    {
        return test("reliantunix");
    }

    @Deprecated
    public boolean isDec()
    {
        return test("dec") || test("osf1") || test("delalpha") ||
                test("alphaserver") || test("ultrix") || test("alphastation");
    }

    @Deprecated
    public boolean isSinix()
    {
        return test("sinix");
    }

    @Deprecated
    public boolean isFreebsd()
    {
        return test("freebsd");
    }

    @Deprecated
    public boolean isBsd()
    {
        return test("bsd");
    }

    @Deprecated
    public boolean isX11()
    {
        return test("x11");
    }

    @Deprecated
    public boolean isVMS()
    {
        return test("vax") || test("openvms");
    }

    @Deprecated
    public boolean getCss()
    {
        return (isMSIE() && getBrowser().getMajorVersion() >= 4) ||
                (isNetscape() && getBrowser().getMajorVersion() >= 4) ||
                isGecko() ||
                isKonqueror() ||
                (isOpera() && getBrowser().getMajorVersion() >= 3) ||
                isSafari() ||
                isChrome() ||
                isLinks();
    }

    @Deprecated
    public boolean getCss1()
    {
        return getCss();
    }

    @Deprecated
    public boolean getCss2()
    {
        int maj = getBrowser() != null ? getBrowser().getMajorVersion() : 0;
        return
                (isOSX() && maj >= 5) ||
                        (isWindows() && getOperatingSystem().getMajorVersion() >= 6) ||
                        isGecko() || // && version >= ?
                        (isOpera() && maj >= 4) ||
                        (isSafari() && maj >= 2) ||
                        (isKonqueror() && maj >= 2) ||
                        isChrome();
    }

    @Deprecated
    public boolean getDom0()
    {
        int maj = getBrowser() != null ? getBrowser().getMajorVersion() : 0;
        return (isMSIE() && maj >= 3) ||
                (isNetscape() && maj >= 2) ||
                (isOpera() && maj >= 3) ||
                isGecko() ||
                isSafari() ||
                isChrome() ||
                isKonqueror();
    }

    @Deprecated
    public boolean getDom1()
    {
        int maj = getBrowser() != null ? getBrowser().getMajorVersion() : 0;
        return (isMSIE() && getBrowser().getMajorVersion() >= 5) ||
                isGecko() ||
                (isSafari() && maj >= 2) ||
                (isOpera() && maj >= 4) ||
                (isKonqueror() && maj >= 2)
                || isChrome();
    }

    @Deprecated
    public boolean getDom2()
    {
        int maj = getBrowser() != null ? getBrowser().getMajorVersion() : 0;
        return (isMSIE() && maj >= 6) ||
                (isMozilla() && maj >= 5.0) ||
                (isOpera() && maj >= 7) ||
                isFirefox() ||
                isChrome();
    }

    @Deprecated
    public boolean getJavascript()
    {
        return getDom0(); // good approximation
    }
}

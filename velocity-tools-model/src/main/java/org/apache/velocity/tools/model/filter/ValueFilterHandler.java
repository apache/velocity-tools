package org.apache.velocity.tools.model.filter;

import org.apache.velocity.tools.model.util.Cryptograph;
import org.apache.velocity.tools.model.util.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;

public class ValueFilterHandler extends FilterHandler<Serializable>
{
    protected static Logger logger = LoggerFactory.getLogger(ValueFilterHandler.class);

    public ValueFilterHandler(String configurationPrefix)
    {
        super(configurationPrefix);
        addStockObject("lowercase", x -> String.valueOf(x).toLowerCase(Locale.ROOT));
        addStockObject("uppercase", x -> String.valueOf(x).toUpperCase(Locale.ROOT));
        addStockObject("calendar_to_date", x -> x != null && x instanceof Calendar ? TypeUtils.toDate(x) : x);
        addStockObject("date_to_calendar", x -> x != null && x instanceof java.sql.Date ? TypeUtils.toCalendar(x) : x);
        addStockObject("number_to_boolean", x -> x != null && x instanceof Number ? ((Number)x).longValue() != 0 : x);
        addStockObject("raw_obfuscate", x -> x != null ? cryptograph.encrypt(TypeUtils.toString(x)) : null);
        addStockObject("raw_deobfuscate", x -> x != null ? cryptograph.decrypt(TypeUtils.toBytes(x)) : null);
        addStockObject("obfuscate", x -> x != null ? TypeUtils.base64Encode(cryptograph.encrypt(String.valueOf(x))) : null);
        addStockObject("deobfuscate", x -> x != null ? cryptograph.decrypt(TypeUtils.base64Decode(x)) : null);
        addStockObject("deobfuscate_strings", x -> x != null && x instanceof String ? cryptograph.decrypt(TypeUtils.base64Decode(x)) : x);
        addStockObject("base64_encode", x -> TypeUtils.base64Encode(x));
        addStockObject("base64_decode", x -> TypeUtils.base64Decode(x));
    }

    @Override
    protected Filter<Serializable> getStockObject(String key)
    {
        if (key.contains("obfuscate"))
        {
            needsCryptograph = true;
        }
        return super.getStockObject(key);
    }


    @Override
    protected Logger getLogger()
    {
        return logger;
    }

    public void setCryptograph(Cryptograph cryptograph)
    {
        this.cryptograph = cryptograph;
    }

    public boolean needsCryptograph()
    {
        return needsCryptograph;
    }

    private Cryptograph cryptograph = null;
    private boolean needsCryptograph = false;
}

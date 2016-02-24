package org.bitcoin.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class FiatConverter {
    private static final Logger LOG = LoggerFactory.getLogger(FiatConverter.class);
    private final static double USD2MXN = 18.19;  //esto debe ser dinamico y calculado al momento no fijo

    public static double toUsd(Double mxn) {
        return toUsd(mxn, new Date());
    }

    public static double toUsd(Double mxn, Date date) {
        if (mxn == null || mxn == 0.0) {
            return 0.0;
        }
        double usd = mxn / getRate(date);
        return DoubleUtils.toFourDecimal(usd);
    }

    private static Double getRate(Date date) {
        return USD2MXN;
    }


    public static Double toMXN(Double usd) {
        if (usd == null || usd == 0.0) {
            return 0.0;
        }
        double mxn = usd * getRate(new Date());
        return DoubleUtils.toFourDecimal(mxn);
    }
}

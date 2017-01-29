package org.bitcoin.market;

import android.content.Context;

import org.bitcoin.market.bean.Market;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Sebastian Acosta Checa on 14-2-26.
 */

public class MarketApiFactory {

    private Context context;

    private static MarketApiFactory factory = new MarketApiFactory(); //<--- null? wtf???

    private MarketApiFactory() {

       // this.context = context;
    }

    public static MarketApiFactory getInstance() {
        return factory;
    }

    static Map<String, AbstractMarketApi> marketMap = new LinkedHashMap<String, AbstractMarketApi>();

    static {
       // AbstractMarketApi isbit = new IsbitMXNApi(this.context);
        //marketMap.put(isbit.getMarket().name(), isbit);
    }


    public AbstractMarketApi getMarket(String name) {
        AbstractMarketApi market = marketMap.get(name);
        if (market == null) {
            throw new RuntimeException("name:" + name + " is null");
        }
        return market;
    }

    public AbstractMarketApi getMarket(Market market) {
        return getMarket(market.name());
    }


}

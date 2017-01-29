package org.bitcoin.market;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Throwables;
import com.isbit.m.DS;
import com.isbit.m.ISO8601;

import org.bitcoin.common.FiatConverter;
import org.bitcoin.common.HttpUtils;
import org.bitcoin.market.bean.*;
import org.bitcoin.market.utils.MarketErrorCode;
import org.bitcoin.market.utils.MarketUtils;
import org.bitcoin.market.utils.TradeException;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.*;


public class IsbitMXNApi extends AbstractMarketApi {
    private static final Logger LOG = LoggerFactory.getLogger(IsbitMXNApi.class);

    private static  String ISBIT_URL = "http://isbit.co";
      //private static  String ISBIT_URL = "http://isbit.ddns.net:3006";

    private static final long DURATION = 1000;
    private static final int TIME_OUT = 15000;

    public IsbitMXNApi(Context context) {
        super(context, org.bitcoin.market.bean.Currency.MXN, Market.IsbitMXN);
        DS ds = new DS(context);
        ds.open();
        ISBIT_URL = ds.get_isbit_url();
        ds.close();

    }

    @Override
    Long createNonce() {
        return System.currentTimeMillis();
    }

    @Override
    public Long buy(AppAccount appAccount, double amount, double price, SymbolPair symbolPair, OrderType orderType) {

       // price = FiatConverter.toMXN(price);
        TreeMap<String, String> params = new TreeMap<String, String>();
        params.put("side", "buy");
        JSONObject response = trade(appAccount, amount, price, params, symbolPair, orderType);
        if (response.containsKey("id")) {
            return response.getLong("id");
        }
        return -1L;
    }

    @Override
    public Long sell(AppAccount appAccount, double amount, double price, SymbolPair symbolPair, OrderType orderType) {
       // price = FiatConverter.toMXN(price); //<--- this modfies the price... it increases it distoring everything... BAG BUG. removed!
        TreeMap<String, String> params = new TreeMap<String, String>();
        params.put("side", "sell");
        JSONObject response = trade(appAccount, amount, price, params, symbolPair, orderType);
        if (response.containsKey("id")) {
            return response.getLong("id");
        }
        return -1L;
    }

    private JSONObject trade(AppAccount appAccount, Double amount, Double price, TreeMap<String, String> params, SymbolPair symbolPair, OrderType orderType) {
        /*if (AppConfig.isDebug()) { todo
            LOG.info("AppConfig is debug,can't trade at isbit");
            return new JSONObject();
        }*/

        if (!appAccount.getEnable()) {
            LOG.info("appAccount is disable {}", appAccount);
            return new JSONObject();
        }

        if (orderType.isMargin()) {
            throw new UnsupportedOperationException();
        }

        params.put("volume", amount.toString());
        params.put("price", price.toString());
        params.put("market", getSymbolPairDescFromUsd2Mxn(symbolPair));
        params.put("canonical_verb", "POST");
        params.put("canonical_uri", "/api/v2/orders");
        JSONObject response = send_request(appAccount, params, TIME_OUT, false);
        if (response.containsKey("error")) {
            throw new TradeException(MarketErrorCode.getForIsbitMXN(response));
        }
        return response;
    }

    @Override
    public void cancel(AppAccount appAccount, Long orderId, SymbolPair symbolPair) {
        /*if (AppConfig.isDebug()) { todo
                    LOG.info("AppConfig is debug,can't cancel at isbit");

            return;
        }*/

        TreeMap<String, String> params = new TreeMap<String, String>();
        params.put("id", orderId.toString());
        params.put("canonical_verb", "POST");
        params.put("canonical_uri", "/api/v2/order/delete");

        JSONObject response = send_request(appAccount, params, TIME_OUT, true);
        if (response.containsKey("error")) {
            throw new TradeException(MarketErrorCode.getForIsbitMXN(response));
        }
    }

    private String getSymbolPairDescFromUsd2Mxn(SymbolPair symbolPair) {
        if (symbolPair.getSecond().isUsd()) {
            SymbolPair symbolPair1 = new SymbolPair(symbolPair.getFirst(), Symbol.mxn);
            return symbolPair1.getDesc(false);
        } else if (symbolPair.getSecond().isMxn()) {
            return symbolPair.getDesc(false);
        }
        throw new RuntimeException("symbolPair not contain usd " + symbolPair.getDesc(false));
    }

    @Override
    public Double getTransactionFee() {
        return 0.0;
    }

    @Override
    public Double getWithdrawalFee() {
        return 0.0;
    }

    @Override
    public Double getDepositFee() {
        return 0.0;
    }


    @Override
    public Asset getInfo(AppAccount appAccount) {

        TreeMap<String, String> params = new TreeMap<String, String>();
        JSONObject response;
        try {
            params.put("canonical_verb", "GET");
            params.put("canonical_uri", "/api/v2/members/me");

            response = send_request(appAccount, params, TIME_OUT, true);
        } catch (Exception e) {
            params.put("canonical_verb", "GET");
            params.put("canonical_uri", "/api/v2/members/me");
            response = send_request(appAccount, params, TIME_OUT, true);
        }
        if (response == null) {
            throw new RuntimeException("Can't get_info");
        }

        Asset asset = new Asset();
        asset.setAppAccountId(appAccount.getId());
        asset.setMarket(getMarket());

        JSONArray accounts = response.getJSONArray("accounts");
        for (int i = 0; i < accounts.size(); i++) {
            JSONObject balance = accounts.getJSONObject(i);
            String currency1 = balance.getString("currency");
            if (currency1.equals("btc")) {
                asset.setAvailableBtc(balance.getDouble("balance"));
                asset.setFrozenBtc(balance.getDouble("locked"));
            }
            if (currency1.equals("mxn")) {
                asset.setAvailableMxn(balance.getDouble("balance"));
               // asset.setAvailableMxn(FiatConverter.toUsd(asset.getAvailableMxn()));
                asset.setFrozenMxn(balance.getDouble("locked"));
                //asset.setFrozenMxn(FiatConverter.toUsd(asset.getFrozenMxn()));
            }
            if (currency1.equals("usd")) {
                asset.setAvailableUsd(balance.getDouble("balance"));
                asset.setFrozenUsd(balance.getDouble("locked"));
                //asset.setAvailableUsd(FiatConverter.toUsd(asset.getAvailableMxn()));
                //asset.setFrozenUsd(FiatConverter.toUsd(asset.getFrozenMxn()));
            }
        }
        return asset;

    }

    @Override
    public BitOrder getOrder(AppAccount appAccount, Long orderId, SymbolPair symbolPair) {
        TreeMap<String, String> params = new TreeMap<String, String>();
        params.put("canonical_verb", "GET");
        params.put("canonical_uri", "/api/v2/order");
        params.put("id", orderId.toString());
        JSONObject response = send_request(appAccount, params, TIME_OUT, true);
        return getOrder(response);
    }

    @Override
    public List<BitOrder> getRunningOrders(AppAccount appAccount) {
        TreeMap<String, String> params = new TreeMap<String, String>();
        params.put("canonical_verb", "GET");
        params.put("canonical_uri", "/api/v2/orders");
        params.put("market", getSymbolPairDescFromUsd2Mxn(new SymbolPair(Symbol.btc, Symbol.mxn)));
        params.put("limit", "100");
        List<BitOrder> orders = new ArrayList<BitOrder>();
        JSONArray ordersResponse = send_requests(appAccount, params, TIME_OUT, true);
        Log.d("getRunningOrders()", ordersResponse.toString());
        for (Object anOrdersResponse : ordersResponse) {
            JSONObject orderResponse = (JSONObject) anOrdersResponse;
            orders.add(getOrder(orderResponse));
        }
        return orders;

    }


    @Override
    public JSONObject getDepositAddress(AppAccount appAccount,Symbol currency) {
        TreeMap<String, String> params = new TreeMap<String, String>();
        params.put("canonical_verb", "GET");
        params.put("canonical_uri", "/api/v2/deposit_address.json");
        params.put("currency",currency.toString());

        JSONObject resp = send_request(appAccount, params, TIME_OUT, true);
        Log.d("getDepositAddress()", resp.toString());


        String addr = "address goes here";
        return resp;

    }

    private BitOrder getOrder(JSONObject jsonObject) {

        BitOrder bitOrder = new BitOrder();
        bitOrder.setOrderId(jsonObject.getLong("id"));
        bitOrder.setOrderAmount(jsonObject.getDouble("volume"));
        bitOrder.setOrderMxnPrice(jsonObject.getDouble("price"));
        bitOrder.setOrderPrice(FiatConverter.toUsd(bitOrder.getOrderMxnPrice()));
        bitOrder.setProcessedAmount(jsonObject.getDouble("executed_volume"));
        bitOrder.setProcessedMxnPrice(jsonObject.getDouble("avg_price"));
        bitOrder.setProcessedPrice(FiatConverter.toUsd(jsonObject.getDouble("avg_price")));

        // 	"created_at": "2017-01-02T15:41:25-06:00",

       // DateTimeFormatter formatter = ISODateTimeFormat.ordinalDate();
        //        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
      //  DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//          LocalDateTime ps = formatter.parseLocalDateTime(jsonObject.getString("created_at"));


        try {
            Calendar cal = ISO8601.toCalendar(jsonObject.getString("created_at"));
            Date dt = cal.getTime();
             bitOrder.setCreateTime(dt);

        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("IsbitMXNApi",e.toString());
        }






        if ("sell".equals(jsonObject.getString("side"))) {
            bitOrder.setOrderSide(OrderSide.buy.sell);
        }else{
            bitOrder.setOrderSide(OrderSide.buy.buy);
        }

        bitOrder.setFee(getTransactionFee());
        String orderStatusStr = jsonObject.getString("state");
        OrderStatus orderStatus = OrderStatus.none;
        if ("wait".equals(orderStatusStr)) {
            orderStatus = OrderStatus.none;
        } else if ("done".equals(orderStatusStr)) {
            orderStatus = OrderStatus.complete;
        } else if ("cancel".equals(orderStatusStr)) {
            orderStatus = OrderStatus.cancelled;
        }
        bitOrder.setStatus(orderStatus);

        return bitOrder;
    }

    private String getSign(AppAccount appAccount, TreeMap<String, String> parameters) {
        if (parameters.containsKey("signature")) {
            parameters.remove("signature");
        }

        StringBuilder parameter = new StringBuilder();
        for (Map.Entry entry : parameters.entrySet()) {
            if (entry.getKey().equals("canonical_verb") || entry.getKey().equals("canonical_uri")) {
                continue;
            }

            parameter.append("&").append(entry.getKey()).append("=").append(entry.getValue());
        }
        if (parameter.length() > 0) {
            parameter.deleteCharAt(0);
        }
        String canonical_verb = parameters.get("canonical_verb");
        String canonical_uri = parameters.get("canonical_uri");

        String signStr = String.format("%s|%s|%s", canonical_verb, canonical_uri, parameter.toString());
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keyspec = new SecretKeySpec(appAccount.getSecretKey().getBytes("UTF-8"), "HmacSHA256");
            mac.init(keyspec);
            mac.update(signStr.getBytes("UTF-8"));
            return String.format("%064x", new BigInteger(1, mac.doFinal()));
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private String internal_send_request(AppAccount appAccount, TreeMap<String, String> params, int timeout) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdate < DURATION) {
            try {
                Thread.sleep(DURATION - (currentTime - lastUpdate));
            } catch (InterruptedException e) {
                // ignore
            }
        }
        params.put("access_key", appAccount.getAccessKey());
        params.put("tonce", createNonce().toString());

        params.put("signature", getSign(appAccount, params));

        String canonical_verb = params.get("canonical_verb");
        params.remove("canonical_verb");
        String canonical_uri = params.get("canonical_uri");
        params.remove("canonical_uri");
        LOG.info("send_request params:{}", params);
        Document doc;
        String response = null;
        try {
            String url = ISBIT_URL + canonical_uri;
            Connection connection = HttpUtils.getConnectionForPost(url, params).timeout(timeout).ignoreContentType(true);
            connection.ignoreHttpErrors(true);
            if ("post".equalsIgnoreCase(canonical_verb)) {
                doc = connection.post();
            } else {
                doc = connection.get();
            }
            lastUpdate = System.currentTimeMillis();
            response = doc.body().text();

            return response;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        } finally {
            LOG.info("send_request result:{}", response);
            try {
                DS ds = new DS(context);
                ds.open();
                com.alibaba.fastjson.JSONObject  json_resp = com.alibaba.fastjson.JSONObject.parseObject(response);
                String sn = json_resp.getString("sn");
                String email = json_resp.getString("email");
                String name = json_resp.getString("name");
                String activated = json_resp.getBoolean("activated").toString();
                ds.save_key_value_pair( "sn", sn);
                ds.save_key_value_pair( "email", email);
                ds.save_key_value_pair( "name", name);
                ds.save_key_value_pair( "activated", activated);
                ds.close();

            }catch (Exception e){
                Log.e("IsbitMXNApi", e.toString() );
            }


        }
    }

    private JSONObject send_request(AppAccount appAccount, TreeMap<String, String> params, int timeout, boolean isThrow) {
        String body = internal_send_request(appAccount, params, timeout);
        JSONObject response = JSON.parseObject(body);
        if (response == null) {
            throw new RuntimeException("send_request response is null");
        }
        if (isThrow && response.containsKey("error")) {
            throw new RuntimeException("send_request:" + MarketErrorCode.getForIsbitMXN(response));
        }
        return response;
    }


    private JSONArray send_requests(AppAccount appAccount, TreeMap<String, String> params, int timeout, boolean isThrow) {
        String body = internal_send_request(appAccount, params, timeout);
        JSONArray response = JSONObject.parseArray(body);
        if (response == null) {
            throw new RuntimeException("send_request response is null");
        }
        if (isThrow && body.contains("error")) {
            throw new RuntimeException("send_request:" + body);
        }
        return response;
    }

    @Override
    public Double ticker(SymbolPair symbol) throws IOException {
        String ticker_url = ISBIT_URL + "/api/v2/tickers/" + getSymbolPairDescFromUsd2Mxn(symbol);
        String text = HttpUtils.getContentForGet(ticker_url, 5000);
        JSONObject jsonObject = JSONArray.parseObject(text);
        JSONObject ticker = jsonObject.getJSONObject("ticker");
        return FiatConverter.toUsd(ticker.getDouble("last"));
    }

    @Override
    public JSONObject update_depth(SymbolPair symbolPair) {
        JSONObject data = this.get_json(symbolPair);
        if (data.containsKey("asks")) {
            JSONObject jsonObject = this.format_depth(data);
           // convert_to_usd(jsonObject);
            return jsonObject;
        }
        throw new RuntimeException("update_depth error");
    }

    private JSONObject get_json(SymbolPair symbolPair) {
        String url = ISBIT_URL + "/api/v2/order_book?market=" + getSymbolPairDescFromUsd2Mxn(symbolPair) +
                "&asks_limit=100&bids_limit=100";

        Log.i("JSONObject get_json url",url);
        JSONObject data = new JSONObject();
        try {
            String jsonstr = this.parse_json_str(url);
            data = JSON.parseObject(jsonstr);
        } catch (Exception e) {
            LOG.info("{} - Can't parse json message:{}", this.getMarket(), e.getMessage());

            sleep(3000);
            try {
                String jsonstr = this.parse_json_str(url);
                data = JSON.parseObject(jsonstr);
            } catch (Exception e2) {
                LOG.error("{} - Can't parse json message:{}", getMarket(), e2.getMessage());
            }
        }

        return data;

    }


    protected JSONObject format_depth(JSONObject data) {
        JSONArray bids = this.sort_and_format(data.getJSONArray("bids"), true);
        JSONArray asks = this.sort_and_format(data.getJSONArray("asks"), false);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("asks", asks);
        jsonObject.put("bids", bids);
        return jsonObject;
    }

    protected JSONArray sort_and_format(JSONArray jsonArray, boolean reverse) {

        if (reverse) {
            Collections.sort(jsonArray, new ReversePriceComparator());
        } else {
            Collections.sort(jsonArray, new PriceComparator());

        }
        JSONArray jsonArray1 = new JSONArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("price", jsonObject.getDouble("price"));
            jsonObject1.put("amount", jsonObject.getDouble("remaining_volume"));
            jsonArray1.add(jsonObject1);

        }

        Log.d("sort_and_format array", jsonArray1.toString() );

        return jsonArray1;
    }

    class PriceComparator implements Comparator<Object> {
        public int compare(Object member1, Object member2) {
            //compareTo，两个对象属性之间的比较，返回负数，0和正数
            return ((JSONObject) member1).getDouble("price").compareTo(((JSONObject) member2).getDouble("price"));
        }
    }

    class ReversePriceComparator implements Comparator<Object> {
        public int compare(Object member1, Object member2) {
            //compareTo，两个对象属性之间的比较，返回负数，0和正数
            return ((JSONObject) member2).getDouble("price").compareTo(((JSONObject) member1).getDouble("price"));
        }
    }

    @Override
    public List<Kline> getKline1Min(Symbol symbol) throws IOException, ParseException {
        String url = getKlineUrl(symbol, 1);
        return getKlines(url, symbol);
    }

    @Override
    public List<Kline> getKline5Min(Symbol symbol) throws IOException, ParseException {
        String url = getKlineUrl(symbol, 5);

        return getKlines(url, symbol);
    }

    @Override
    public List<Kline> getKlineDate(Symbol symbol) throws IOException, ParseException {

        String url = getKlineUrl(symbol, 1440);
        List<Kline> klines = getKlines(url, symbol);
        for (Kline kline : klines) {
            kline.setTimestamp(kline.getTimestamp());
        }
        return klines;
    }

    private String getKlineUrl(Symbol symbol, int period) {
        return ISBIT_URL + "/api/v2/k?market=" + getSymbolPairDescFromUsd2Mxn(new SymbolPair(symbol, Symbol.mxn))
                + "&period=" + period + "&limit=100";
    }

    private List<Kline> getKlines(String url, Symbol symbol) throws ParseException, IOException {
        String text = HttpUtils.getContentForGet(url, TIME_OUT);
        JSONArray lines = JSONArray.parseArray(text);
        List<Kline> klines = new ArrayList<Kline>();
        for (int i = 0; i < lines.size(); i++) {
            Kline kline = getKline(JSONArray.parseArray(lines.getString(i)), symbol);
            if (kline == null) {
                continue;
            }

            klines.add(kline);

        }
        return klines;
    }

    private Kline getKline(JSONArray params, Symbol symbol) throws ParseException {

        Kline kline = new Kline();
        kline.setMarket(getMarket());
        long timestamp = params.getLong(0);
        kline.setTimestamp(timestamp);
        Date date = new Date(timestamp * 1000);
        kline.setDatetime(date);
        kline.setOpen(params.getDouble(1));
        kline.setHigh(params.getDouble(2));
        kline.setLow(params.getDouble(3));
        kline.setClose(params.getDouble(4));
        kline.setVwap(MarketUtils.avgPrice(kline));
        kline.setVolume(params.getDouble(5));
        kline.setSymbol(symbol);

        return kline;
    }


}

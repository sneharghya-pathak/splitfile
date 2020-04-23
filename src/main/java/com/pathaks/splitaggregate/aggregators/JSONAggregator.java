package com.pathaks.splitaggregate.aggregators;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.json.JSONArray;
import org.json.JSONException;


public class JSONAggregator implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        Exchange exchange = oldExchange == null ? newExchange : oldExchange;
		String body = null;
		JSONArray bodyJsonArray = new JSONArray();
		try {
			body = newExchange.getIn().getBody(String.class);
			bodyJsonArray = new JSONArray(body);
		} catch (JSONException e) {
			exchange.setProperty("ErrorOccured", "true");
		}
        
		if (oldExchange == null) {
			newExchange.getIn().setBody(bodyJsonArray);
		} else {
			JSONArray list = oldExchange.getIn().getBody(JSONArray.class);
			for (int i = 0; i < bodyJsonArray.length(); i++) {
				list.put(bodyJsonArray.getJSONObject(i));
			}
			oldExchange.getIn().setBody(list);
		}

		return exchange;
    }

}
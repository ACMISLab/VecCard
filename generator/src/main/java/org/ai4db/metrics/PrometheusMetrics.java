package org.ai4db.metrics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class PrometheusMetrics {
    public static void main(String[] args) throws MalformedURLException {
        String prometheusUrl = System.getenv().getOrDefault(
                "VECCARD_PROMETHEUS_URL",
                "http://127.0.0.1:19090/prometheus/api/v1/query_range"
        );
        String instance = System.getenv().getOrDefault("VECCARD_PROM_INSTANCE", "127.0.0.1:19100");
        String query = "avg(rate(node_cpu_seconds_total{instance=\"" + instance + "\", mode=\"user\"}[5m])) by (instance) * 100";
        long endTime = System.currentTimeMillis() / 1000;
        long startTime = endTime - 3600;
        String step = "15s";
        System.out.println(prometheusUrl + "?query=" + query + "&start=" + startTime + "&end=" + endTime + "&step=" + step);

        try{
            URL url = new URL(prometheusUrl + "?query=" + query + "&start=" + startTime + "&end=" + endTime + "&step=" + step);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                System.out.println(response.toString());


//                JSONObject data = new JSONObject(response.toString());

//                if (data.has("data") && data.getJSONObject("data").has("result")) {
//                    JSONArray results = data.getJSONObject("data").getJSONArray("result");
//                    for (int i = 0; i < results.length(); i++) {
//                        JSONObject result = results.getJSONObject(i);
//                        String instance = result.getJSONObject("metric").optString("instance", "unknown");
//                        JSONArray values = result.getJSONArray("values");
//
//                        for (int j = 0; j < values.length(); j++) {
//                            JSONArray valuePair = values.getJSONArray(j);
//                            long timestamp = valuePair.getLong(0);
//                            double value = valuePair.getDouble(1);
//
//                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                            String formattedTimestamp = sdf.format(new Date(timestamp * 1000));
//
//                            HashMap<String, HashMap<String, Double>> instanceData = new HashMap<>();
//                            if (!instanceData.containsKey(instance)) {
//                                instanceData.put(instance, new HashMap<>());
//                            }
//                            if (!instanceData.get(instance).containsKey(formattedTimestamp)) {
//                                instanceData.get(instance).put(formattedTimestamp, new HashMap<>());
//                            }
//                            instanceData.get(instance).get(formattedTimestamp).put(query, value);
//                        }
//                    }
//                } else {
//                    System.out.println("No data found for query: " + query);
//                }
            } else {
                System.out.println("Failed to fetch data: HTTP error code : " + responseCode);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}

/* BSD 3-Clause License
 *
 * Copyright (c) 2017, Louis Jenkins <LouisJenkinsCS@hotmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *     - Neither the name of Louis Jenkins, Bloomsburg University nor the names of its 
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package async;

import io.reactivex.Observable;
import io.reactivex.observables.GroupedObservable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import org.javatuples.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import utilities.FileUtils;

/**
 *
 * @author Louis Jenkins
 * 
 * DataReceiver's purpose is to handle all API calls for data, as well as temporarily
 * (until a specific class is built for generation) generating the HTML required
 * for the chart, table, and descriptions.
 */
public class DataReceiver {
    
    // TODO: Quit being lazy and create a single map which holds the data needed
    // The keys are the exact same, and only differ in values.
    private static final Map<String, String> descriptionMap = new HashMap<>();
    private static final Map<String, Long> idMap = new HashMap<>();
    
    // Read-in and fill map of descriptions.
    static {
        // The file 'descriptions.json' contains the descriptions for parameters,
        // which need to be displayed when selected. This is also used to filter
        // out any API data that we do not have a description of.
        String filename = "resources/descriptions.json";
        
        Observable
                .just(filename)
                .map(FileUtils::readAll)
                .map(str -> (JSONObject) new JSONParser().parse(str))
                .map(obj -> (JSONArray) obj.get("descriptions"))
                .flatMap(Observable::fromIterable)
                .blockingSubscribe(obj -> descriptionMap.put((String) ((JSONObject) obj).get("name"), ((String) ((JSONObject) obj).get("description")).replaceAll("\\P{Print}", "")));
    }
    
    static {
        // Fill the map so we only need to obtain it once on startup.
        // The map will contain a list of identifiers (being the very same
        // identifier displayed in dashboard.jsp) to their actual identifier. This
        // MUST be refactored before actual release, but should be sufficient for now.
        
        // This is the URL used to obtain ALL data for the sensor within the past
        // 15 minutes. This is important as, again, it holds ALL of the data. So
        // we can use this to construct our map.
        String url = "https://ienvironet.com/api/data/last/0A178632?auth_token=avfzf6dn7xgv48qnpdhqzvlkz5ke7184";
        
        // We want to make some API calls next. Given the above URL, we can then
        // make a connection and obtain the JSON data sent.
        getData(url)
                // For an example of the format given see: https://gist.github.com/LouisJenkinsCS/cca0069178f194329d55aabf33c28418
                // We need to obtain the "data" parameter, which a JSONArray.
                .map((JSONObject obj) -> (JSONArray) obj.get("data"))
                // A JSONArray implements the Iterable interface, just like any Collection such as an ArrayList would.
                // Because of this we can go from the Collection itself to the items it contains. Hence, a collection
                // containing ({1, 2, 3}) will be converted into the respective elements, (1, 2, 3).
                .flatMap(Observable::fromIterable)
                .filter(obj -> descriptionMap.containsKey(((JSONObject) obj).get("name")))
                // With the JSONObjects being the data we need, we can easily parse it into tuples
                // of (Name, Id) pairs, or rather Pair<String, Long>
                .map(obj -> Pair.with(((JSONObject) obj).get("name") + " (" + ((JSONObject)obj).get("unit") + ")", (Long) ((JSONObject)obj).get("id")))
                .subscribe(pair -> idMap.put(((Pair<String, Long>) pair).getValue0(), ((Pair<String, Long>)pair).getValue1()));
    };
    
    /**
     * Obtain all sensor parameter names.
     * @return All sensor parameter names.
     */
    public static Observable<String> getParameters() {
        return Observable.fromIterable(idMap.keySet());
    }
    
    /**
     * Obtain DataValues from a query for all supplied keys that are within the
     * time frame of start to end. The data is returned as Data, which can be passed
     * to other DataReceiver methods, or even used by itself. For information on
     * how Data can be used, see the JavaDoc.
     * @param start Beginning of time frame
     * @param end End of time frame.
     * @param keys Selected data.
     * @return Data containing data from query.
     */
    public static Data getData(Instant start, Instant end, String ...keys) {
        return new Data(Observable
                // For each key
                .fromArray(keys)
                .flatMap((String key) ->
                    getData(getParameterURL(start, end, idMap.get(key)))
                            // For an example of the format given see: https://gist.github.com/LouisJenkinsCS/cca0069178f194329d55aabf33c28418
                            // We need to obtain the "data" parameter, which a JSONArray.
                            .map((JSONObject obj) -> (JSONArray) obj.get("data"))
                            // A JSONArray implements the Iterable interface, just like any Collection such as an ArrayList would.
                            // Because of this we can go from the Collection itself to the items it contains. Hence, a collection
                            // containing ({1, 2, 3}) will be converted into the respective elements, (1, 2, 3).
                            .flatMap(Observable::fromIterable)
                            // We take both the timestamp (X-Axis) and the value (Y-Axis).
                            // This data is what is returned as a DataValue.
                            .map(obj -> new DataValue(key, (String)((JSONObject) obj).get("timestamp"), (Double) ((JSONObject) obj).get("value")))
                // 'replay' is a way to say that we want to take ALL items up to this point (being the DataValues), cache it, and then
                // resend it each and every time it is subscribed to (pretty much meaning this becomes reusable).
                ).replay());
    }
    
    /**
     * Generates HTML for the descriptions for each unique parameter of the underlying source.
     * The parameters are bolded and centered on their own line, while the descriptions
     * are displayed below them.
     * @param source Data.
     * @return Generated HTML of descriptions.
     */
    public static String generateDescriptions(Data source) {
        StringBuilder descriptions = new StringBuilder();
        source.getData()
                .map(DataValue::getParameter)
                .distinct()
                .map((String parameter) -> "\n<center><h1>" + parameter + "</center></h1>\n" + descriptionMap.get(parameter.substring(0, parameter.indexOf("(") - 1)))
                .blockingSubscribe(descriptions::append);
        
        return descriptions.toString();
    }
    
    /**
     * Generates HTML for the Chart.js. This assumes that the receiving HTML document
     * contains a canvas with the element 'myChart'. The chart is constructed with
     * it's timestamp as the X-Axis, and it's values as the Y-Axis. The color of each
     * parameter held by the underlying source are given a randomized color.
     * @param source Data.
     * @return Generated HTML for Chart.js.
     */
    public static String generateChartJS(Data source) {
        StringBuilder chartJS =  new StringBuilder("<script>" +
                "var ctx = document.getElementById('myChart').getContext('2d');\n" + 
                "var myChart = new Chart(ctx, {\n" +
                 "  type: 'line',\n" +
                 "  data: {\n");
        
        StringBuilder labels = new StringBuilder("    labels: [");
        source.getData()
                .map(DataValue::getTimestamp)
                .distinct()
                .sorted()
                .map((Instant ts) -> "\"" + ts.toString().replace("T", " ").replace("Z", "") + "\",")
                .blockingSubscribe(labels::append);
        
        // Replace the last ',' for a end bracket ']'
        labels.replace(labels.length()-1, labels.length(), "]");
        chartJS.append(labels.toString());
        chartJS.append(",\n    datasets: [");
        
        // Add all data to the dataset for each element
        source.getData()
                .groupBy((DataValue dv) -> dv.parameter)
                .sorted((GroupedObservable<String, DataValue> group1, GroupedObservable<String, DataValue> group2) -> 
                        group1.getKey().compareTo(group2.getKey())
                )
                .flatMap((GroupedObservable<String, DataValue> group) -> {
                    int rgb[] = new Random().ints(0, 256).limit(3).toArray();
                    return group
                            .buffer(Integer.MAX_VALUE)
                            .map((List<DataValue> data) -> "{\n" +
                                    "      label: '" + group.getKey() + "',\n" +
                                    "      data: [" + data.stream().map(DataValue::getValue).map(Object::toString).collect(Collectors.joining(",")) + "],\n" +
                                    "      backgroundColor: 'transparent', borderColor: 'rgb(" + rgb[0] + "," + rgb[1] + "," + rgb[2] + ")'\n" +
                                    "},"
                            );
                })
                .blockingSubscribe(chartJS::append);
        
        // Replace the last ',' with a ']' and finish off chartJS
        chartJS.replace(labels.length()-1, labels.length(), "]");
        chartJS.append("]\n  }\n});</script>");
        
        return chartJS.toString();
    }
    
    /**
     * Generates an HTML Table for the underlying data source. The HTML table is generated
     * with a column for it's timestamp and one for each of the unique parameters.
     * @param source Data.
     * @return Generated HTML table.
     */
    public static String generateTable(Data source) {
        StringBuilder table = new StringBuilder("<table border='1'>\n\t<tr>\n\t\t<th>Timestamp</th>");
        
        // Generate the headers first
        source.getData()
                .map(DataValue::getParameter)
                .distinct()
                .sorted()
                .map((String parameter) -> "\n\t\t<th>\n\t\t" + parameter + "\n\t\t</th>")
                .blockingSubscribe(table::append);
        
        // Generate the body next
        source.getData()
                .groupBy(DataValue::getTimestamp)
                .sorted((GroupedObservable<Instant, DataValue> group1, GroupedObservable<Instant, DataValue> group2) -> 
                        group1.getKey().compareTo(group2.getKey())
                )
                .flatMap((GroupedObservable<Instant, DataValue> group) -> {
                    return Observable
                            .just("\n\t<tr>\n\t\t<td>" + group.getKey().toString().replace("T", " ").replace("Z", "") + "</td>")
                            .flatMap(str -> group.map((DataValue dv) -> "\n\t\t<td>" + dv.getValue() + "</td>")
                                    .buffer(Integer.MAX_VALUE)
                                    .map(list -> str + list
                                            .stream()
                                            .collect(Collectors.joining())
                                    )
                            )
                            .map(str -> str + "\n\t</tr>");
                }).blockingSubscribe(table::append);
        table.append("</table>");
        
        return table.toString();
    }
    
    /**
     * Obtain the API URL from the given time frame and parameter ID.
     * @param start Start.
     * @param end End.
     * @param id Id.
     * @return API URL.
     */
    private static String getParameterURL(Instant start, Instant end, long id) {
       return "https://ienvironet.com/api/data/" + start.getEpochSecond() + ":" + end.getEpochSecond() + "/" + id + ".json?auth_token=avfzf6dn7xgv48qnpdhqzvlkz5ke7184";
    }
    
    /**
     * Obtain the JSON from the passed URL. The URL SHOULD be one that corresponds to
     * the environet API. The data is taken as JSON.
     * @param url
     * @return Data from URL as JSON.
     */
    public static Observable<JSONObject> getData(String url) {
        return Observable.just(url)
                // From the URL, construct an actual URL (remember, the above is a String)
                .map(URL::new)
                // From the actual URL, form a connection with the endpoint (URL -> URLConnection)
                .map(URL::openConnection)
                // Need to set this or else API will refuse our request. We need to act like a web browser
                .doOnNext(conn -> conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"))
                // Attempt to form actual connection
                .doOnNext(URLConnection::connect)
                // Obtain the input stream, of which we can obtain the data it is sending
                .map(URLConnection::getInputStream)
                // Obtain the data from the Stream as a String
                .map(stream -> {
                    BufferedReader r  = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));

                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        sb.append(line);
                    }
                    
                    return sb.toString();
                })
                // Remove any and all unicode characters
                .map(str -> str.replaceAll("\\P{Print}", ""))
                // From that received string, since it is in JSON, we can parse it into a JSONObject.
                .map(str -> (JSONObject) new JSONParser().parse(str));
    }
}

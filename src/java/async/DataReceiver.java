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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.javatuples.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import utilities.FileUtils;
import utilities.JSONUtils;

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
    private static final Map<Long, DataParameter> PARAMETER_MAP = new HashMap<>();
    
    // Read-in and fill map of descriptions.
    static {
        // The file 'descriptions.json' contains the descriptions for parameters,
        // which need to be displayed when selected. This is also used to filter
        // out any API data that we do not have a description of.
        String filename = "resources/descriptions.json";
        List<DataParameter> parameters = new ArrayList<>();
        
        Observable
                .just(filename)
                .map(FileUtils::readAll)
                .map(str -> (JSONObject) new JSONParser().parse(str))
                .map(obj -> (JSONArray) obj.get("descriptions"))
                .flatMap(JSONUtils::toData)
                .blockingSubscribe((JSONObject obj) -> parameters.add(new DataParameter((String) obj.get("name"), (String) obj.get("description"))));
        
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
                .flatMap(JSONUtils::toData)
                // Filter out any parameters we do not contain a description for
                .filter((JSONObject obj) -> parameters
                        .stream()
                        .map(DataParameter::getName)
                        .anyMatch((name -> name.equals(obj.get("name"))))
                )
                .doOnNext(obj -> System.out.println("Parameter: " + obj.toJSONString()))
                // Update the current DataParameters loaded with the data from the JSON and add it
                // to our map.
                .blockingSubscribe((JSONObject obj) -> {
                    List<DataParameter> parameter = parameters
                            .stream()
                            .filter((DataParameter param) -> param.getName().equals(obj.get("name")))
                            .collect(Collectors.toList());
                    
                    // NOTE: This will be refactored, but for now, Dr. Rier's description
                    // contains two "Temperature" fields, and they are literally indistinguishable from each other.
                    // Because of this, when we find "Temperature", we assign them randomly to their description
                    // as a temporary workaround.
                    DataParameter param = parameter.get(0);
                    param.fromJSON(obj);
                    PARAMETER_MAP.put(param.getId(), param);
                    parameters.remove(param);
                });
    }
    
    /**
     * Obtain all sensor parameter names.
     * @return All sensor parameter names.
     */
    public static Observable<DataParameter> getParameters() {
        return Observable.fromIterable(PARAMETER_MAP.values());
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
    public static Data getData(Instant start, Instant end, Long ...keys) {
        return new Data(Observable
                // For each key
                .fromArray(keys)
                .flatMap((Long key) ->
                    getData(getParameterURL(start, end, key))
                            // For an example of the format given see: https://gist.github.com/LouisJenkinsCS/cca0069178f194329d55aabf33c28418
                            // We need to obtain the "data" parameter, which a JSONArray.
                            .map((JSONObject obj) -> (JSONArray) obj.get("data"))
                            // A JSONArray implements the Iterable interface, just like any Collection such as an ArrayList would.
                            // Because of this we can go from the Collection itself to the items it contains. Hence, a collection
                            // containing ({1, 2, 3}) will be converted into the respective elements, (1, 2, 3).
                            .flatMap(JSONUtils::toData)
                            // We take both the timestamp (X-Axis) and the value (Y-Axis).
                            // This data is what is returned as a DataValue.
                            .map((JSONObject obj) -> new DataValue(key, (String) obj.get("timestamp"), (Double) obj.get("value")))
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
                .map(DataValue::getId)
                .distinct()
                .map(PARAMETER_MAP::get)
                .map((DataParameter parameter) -> "\n<center><h1>" + parameter.getName() + "</center></h1>\n" + parameter.getDescription())
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
                "var chart = Highcharts.chart('Graph', {\n" + 
                "title: { text: 'Water Quality Graph', x: -20}");
        
        StringBuilder labels = new StringBuilder(", xAxis: { categories: [");
        source.getData()
                .map(DataValue::getTimestamp)
                .distinct()
                .sorted()
                .map((Instant ts) -> "\"" + ts.toString().replace("T", " ").replace("Z", "") + "\",")
                .blockingSubscribe(labels::append);
        
        // Replace the last ',' for a end bracket ']'
        labels.replace(labels.length()-1, labels.length(), "]");
        chartJS.append(labels.toString());
        chartJS.append("}, yAxis: { title: { text: 'Values' }, plotLines: [{ value: 0, width: 1, color: '#808080' }] },");
        chartJS.append("legend: {\n" +
"        layout: 'vertical',\n" +
"        align: 'right',\n" +
"        verticalAlign: 'middle',\n" +
"        borderWidth: 0\n" +
"    },\nseries: [");
        AtomicInteger yAxis = new AtomicInteger(0);
        
        // Add all data to the dataset for each element
        source.getData()
                .groupBy(DataValue::getId)
                .sorted((GroupedObservable<Long, DataValue> group1, GroupedObservable<Long, DataValue> group2) -> 
                        group1.getKey().compareTo(group2.getKey())
                )
                .flatMap((GroupedObservable<Long, DataValue> group) -> {
                    int axis = yAxis.getAndIncrement();
                    int rgb[] = new Random().ints(0, 256).limit(3).toArray();
                    return group
                            .buffer(Integer.MAX_VALUE)
                            .map((List<DataValue> data) -> "{\n" +
                                    "      name: '" + PARAMETER_MAP.get(group.getKey()).getName() + "',\n" +
                                    "      data: [" + data.stream().map(DataValue::getValue).map(Object::toString).collect(Collectors.joining(",")) + "],\n" +
                                    "},"
                            );
                })
                .blockingSubscribe(chartJS::append);
        
        // Replace the last ',' with a ']' and finish off chartJS
        chartJS.replace(chartJS.length()-1, chartJS.length(), "]");
        chartJS.append("\n  });$('#Graph').resizable({\n" +
"    // On resize, set the chart size to that of the\n" +
"    // resizer minus padding. If your chart has a lot of data or other\n" +
"    // content, the redrawing might be slow. In that case, we recommend\n" +
"    // that you use the 'stop' event instead of 'resize'.\n" +
"    resize: function () {\n" +
"        chart.setSize(\n" +
"            this.offsetWidth - 20,\n" +
"            this.offsetHeight - 20,\n" +
"            false\n" +
"        );\n" +
"    }\n" +
"});</script>");
        
        return chartJS.toString();
    }
    
    public static String generateSeries(Data source) {
        StringBuilder series = new StringBuilder();
        // Add all data to the dataset for each element
        source.getData()
                .groupBy(DataValue::getId)
                .sorted((GroupedObservable<Long, DataValue> group1, GroupedObservable<Long, DataValue> group2) -> 
                        group1.getKey().compareTo(group2.getKey())
                )
                .flatMap((GroupedObservable<Long, DataValue> group) -> {
                    return group
                            .buffer(Integer.MAX_VALUE)
                            .map((List<DataValue> data) -> "{\n" +
                                    "      name: '" + PARAMETER_MAP.get(group.getKey()).getName() + "',\n" +
                                    "      data: [" + data.stream().map(DataValue::getValue).map(Object::toString).collect(Collectors.joining(",")) + "],\n" +
                                    "},"
                            );
                })
                .blockingSubscribe(series::append);
        series.delete(series.length()-1, series.length());
        
        return series.toString();
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
                .map(DataValue::getId)
                .distinct()
                .sorted()
                .map(PARAMETER_MAP::get)
                .map((DataParameter parameter) -> "\n\t\t<th>\n\t\t" + parameter.getName() + "\n\t\t</th>")
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
                .doOnNext((URLConnection conn) -> conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"))
                // Attempt to form actual connection
                .doOnNext(URLConnection::connect)
                // Obtain the input stream, of which we can obtain the data it is sending
                .map(URLConnection::getInputStream)
                // Obtain the data from the Stream as a String
                .map((InputStream stream) -> {
                    BufferedReader r  = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));

                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        sb.append(line);
                    }
                    
                    return sb.toString();
                })
                // Remove any and all unicode characters
                .map((String json) -> json.replaceAll("\\P{Print}", ""))
                // From that received string, since it is in JSON, we can parse it into a JSONObject.
                .map((String json) -> (JSONObject) new JSONParser().parse(json));
    }
}

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
package protocol;

import async.Data;
import async.DataReceiver;
import async.DataValue;
import io.reactivex.Observable;
import io.reactivex.observables.GroupedObservable;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utilities.JSONUtils;

/**
 *
 * Process AJAX requests sent following this protocol. The protocol can be seen
 * here: https://gist.github.com/LouisJenkinsCS/54bdb64628a478bfc57d77dd64c6bb82
 * 
 * @author Louis Jenkins
 */
public class JSONProtocol implements Protocol<JSONObject, JSONObject> {

    public JSONProtocol() {
        
    }

    @Override
    public Observable<JSONObject> processUsing(Data source) {
        return source.getData()
                .groupBy(DataValue::getId)
                .flatMap((GroupedObservable<Long, DataValue> group) -> {
                    JSONObject obj = new JSONObject();
                    // The key is the actual 'id' for the parameter.
                    obj.put("param", group.getKey());
                    obj.put("name", DataReceiver.getParameterName(group.getKey()));

                    // Create a JSONArray filled with the DataValues
                    return group.sorted()
                            .map((DataValue dv) -> {
                                JSONObject dataField = new JSONObject();
                                dataField.put("timestamp", dv.getTimestamp().getEpochSecond() * 1000);
                                dataField.put("value", dv.getValue());
                                return dataField;
                            })
                            .buffer(Integer.MAX_VALUE)
                            .map((List<JSONObject> list) -> {
                                JSONArray arr = new JSONArray();
                                arr.addAll(list);
                                return arr;
                            })
                            .map((JSONArray arr) -> {
                                obj.put("data", arr);
                                return obj;
                            });
                })
                .buffer(Integer.MAX_VALUE)
                .map((List<JSONObject> list) -> {
                    JSONArray arr = new JSONArray();
                    arr.addAll(list);
                    return arr;
                })
                .map((JSONArray arr) -> {
                    JSONObject response = new JSONObject();
                    response.put("resp", arr);
                    return response;
                });
    }

    @Override
    public Observable<JSONObject> process(JSONObject request) { 
        return Observable.just(request)
                .map((JSONObject queryData) -> {
                    Long startTime = (Long) queryData.get("startTime");
                    Long endTime = (Long) queryData.get("endTime");
                    JSONArray arr = (JSONArray) queryData.get("params");
                    Long id[] = (Long[]) arr.toArray(new Long[0]);
                    
                    System.out.println("startTime: " + startTime + ", endTime: " + endTime + ", ids: " + Arrays.deepToString(id));
                    return DataReceiver.getData(Instant.ofEpochMilli(startTime), Instant.ofEpochMilli(endTime), id);
                })
                .flatMap(this::processUsing);
    }
    
    
}

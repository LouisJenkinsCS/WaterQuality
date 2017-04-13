package utilities;

import async.Data;
import async.DataReceiver;
import async.DataValue;
import bayesian.RunBayesianModel;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.internal.functions.Functions;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
/**
 *
 * @author Louis Jenkins
 */
public class DataToCSV {
    
    private static Observable<DataValue> fillForDay(GroupedObservable<Long, DataValue> values) {
        return values
                .buffer(Integer.MAX_VALUE)
                .flatMap((List<DataValue> allDataValues) -> {
                    allDataValues.sort(DataValue::compareTo);
                    System.out.println("Processing list of size: " + allDataValues.size());
                    if (allDataValues.size() == 95) {
                        return Observable.fromIterable(allDataValues);
                    }
                    // Already sorted by timestamp in ascending order.
                    Instant time = allDataValues.get(0).getTimestamp().truncatedTo(ChronoUnit.DAYS);
                    Instant endTime = allDataValues.get(0).getTimestamp().truncatedTo(ChronoUnit.DAYS).plus(Period.ofDays(1));
                    List<DataValue> graphData = new ArrayList<>();
                    for (DataValue value : allDataValues) {
                        while (value.getTimestamp().compareTo(time) != 0 && time.compareTo(endTime) <= 0) {
                            System.out.println("Added for " + values.getKey() + " because " + value.getTimestamp() + " < " + time);
                            graphData.add(new DataValue(values.getKey(), time, value.getValue()));
                            time = time.plus(Duration.ofMinutes(15));
                        }
                        
                        time = time.plus(Duration.ofMinutes(15));
                        graphData.add(value);
                    }
                    
                    List<DataValue> c = new ArrayList<>(graphData);
                    c.removeAll(allDataValues);
                    System.out.println("Filling for: " + values.getKey() + ", # of Values: " 
                            + graphData.size() + ", Delta: " + (graphData.size() - allDataValues.size()) + "\n" + c);

                    return Observable.fromIterable(graphData);
                });
        
    }
    
    /**
     * Fill days for the entire stream.
     * @param dataValues DataValues.
     * @return Modified stream with filled days.
     */
    private static Observable<DataValue> fillForDays(Observable<DataValue> dataValues) {
        return dataValues
                .groupBy((DataValue dv) -> dv.getTimestamp().truncatedTo(ChronoUnit.DAYS))
                .flatMap((GroupedObservable<Instant, DataValue> groupedByDay) -> 
                        groupedByDay
                                .groupBy(DataValue::getId)
                                .flatMap(DataToCSV::fillForDay)
                );
    }

    /**
     * Formats an entire row. Assumes that data contains DataValues for a single
     * row.
     *
     * @param groupedData DataValue for a single row.
     * @return Stringified CSV row.
     */
    private static Observable<String> formatRow(GroupedObservable<Instant, DataValue> groupedData) {
        return groupedData
                .sorted((dv1, dv2) -> Long.compare(dv1.getId(), dv2.getId()))
                .map(DataValue::getValue)
                .buffer(Integer.MAX_VALUE)
                .map(list -> list.stream().map(Object::toString).collect(Collectors.joining(",")))
                .map(line -> {
                    LocalDateTime ldt = LocalDateTime.ofInstant(groupedData.getKey(), ZoneId.of("Z"));
                    String date = ldt.getMonthValue() + "/" + ldt.getDayOfMonth() + "/" + ldt.getYear();
                    String time = ldt.getHour() + ":" + ldt.getMinute() + ":" + ldt.getSecond();
                    return date + "," + time + "," + line;
                });
    }
    
    /**
     * Ensures that, when the day is missing data for a day, the entire day is dropped. If the
     * day is not empty,
     * @param dataValues DataValues
     * @return 
     */
    private static Observable<DataValue> dropEmptyDays(Observable<DataValue> dataValues) {
        return dataValues
                .groupBy((DataValue dv) -> dv.getTimestamp().truncatedTo(ChronoUnit.DAYS))
                .flatMap((GroupedObservable<Instant, DataValue> groupedByDay) -> {
                    Observable<DataValue> cachedDataValues = groupedByDay.cache();
                    return cachedDataValues
                            .groupBy(DataValue::getId)
                            .count()
                            .toObservable()
                            .flatMap((Long numberOfParameters) -> {
                                if (numberOfParameters != 4) {
                                    System.out.println("Dropped for day: " + groupedByDay.getKey() + " with #Params: " + numberOfParameters);
                                    return Observable.empty();
                                } else {
                                    System.out.println("Approved for day: " + groupedByDay.getKey());
                                    return cachedDataValues;
                                }
                            });
                });
    }

    /**
     * Converts a data source to a CSV format. If any values are missing for a
     * given 15-minute interval, it will reuse the previous value for that day;
     * if the previous value for that day is not available, it will instead take
     * the next. If no values are available, it will skip the entire day.
     *
     * @param source Data source.
     * @return The CSV as a string.
     */
    public static Observable<String> dataToCSV(Data source) {
        return source.getData()
                .compose(DataToCSV::dropEmptyDays)
                .compose(DataToCSV::fillForDays)
                // Format row
                .groupBy(DataValue::getTimestamp)
                .sorted((g1, g2) -> g1.getKey().compareTo(g2.getKey()))
                .flatMap(DataToCSV::formatRow)
                .buffer(Integer.MAX_VALUE)
                .map(list -> list.stream().collect(Collectors.joining("\n")))
                .flatMap(values -> source.getData()
                        .sorted()
                        .map(DataValue::getId)
                        .sorted()
                        .distinct()
                        .map(DataReceiver::getParameterName)
                        .buffer(Integer.MAX_VALUE)
                        .map(list -> list.stream().collect(Collectors.joining(",")))
                        .map(header -> "Date,Time," + header)
                        .map(header -> header + "\n" + values)
                );
    }

    public static void main(String[] args) {

        long PAR = 637957793;
        long HDO = 1050296639;
        long Temp = 1050296629;
        long Pressure = 639121405;

        dataToCSV(
                DataReceiver
                        .getRemoteData(
                                Instant.now().minus(Period.ofDays(3)).truncatedTo(ChronoUnit.DAYS), 
                                Instant.now().truncatedTo(ChronoUnit.DAYS).minusSeconds(15 * 60), 
                                PAR, HDO, Temp, Pressure
                        )
        )
                .blockingSubscribe(System.out::println);
    }

}

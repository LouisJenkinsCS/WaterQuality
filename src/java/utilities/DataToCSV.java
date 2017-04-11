package utilities;


import async.Data;
import async.DataReceiver;
import async.DataValue;
import bayesian.RunBayesianModel;
import io.reactivex.schedulers.Schedulers;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

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
    
    public static String dataToCSV(Data source, boolean bayesian) {
        return source.getData()
                .map(dv -> bayesian && dv.getId() == RunBayesianModel.Pressure ? new DataValue(dv.getId(), dv.getTimestamp(), dv.getValue() * RunBayesianModel.ATMOSPHERIC_CONVERSION_FACTOR) : dv)
                .subscribeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .groupBy(DataValue::getTimestamp)
                .sorted((g1, g2) -> g1.getKey().compareTo(g2.getKey()))
                .flatMap(group -> 
                        group
                            .sorted()
                            .map(DataValue::getValue)
                            .buffer(Integer.MAX_VALUE)
                            .map(list -> list.stream().map(Object::toString).collect(Collectors.joining(",")))
                            .map(line -> {
                                LocalDateTime ldt = LocalDateTime.ofInstant(group.getKey(), ZoneId.of("Z"));
                                String date = ldt.getMonthValue() + "/" + ldt.getDayOfMonth() + "/" + ldt.getYear();
                                String time = ldt.getHour() + ":" + ldt.getMinute() + ":" + ldt.getSecond();
                                return date + "," + time + "," + line;
                            })
                )
                .buffer(Integer.MAX_VALUE)
                .map(list -> list.stream().collect(Collectors.joining("\n")))
                .flatMap(values -> source.getData()
                        .sorted()
                        .map(DataValue::getId)
                        .distinct()
                        .map(DataReceiver::getParameterName)
                        .buffer(Integer.MAX_VALUE)
                        .map(list -> list.stream().collect(Collectors.joining(",")))
                        .map(header -> "Date,Time," + header)
                        .map(header -> header + "\n" + values)
                )
                .blockingFirst();
                
    }
    
    
    public static void main(String[] args) {
        
    long PAR = 637957793;
    long HDO = 1050296639;
    long Temp = 1050296629;
    long Pressure = 639121405;

    String dataString = dataToCSV(DataReceiver.getRemoteData(Instant.now().minus(Period.ofDays(3)).truncatedTo(ChronoUnit.DAYS), Instant.now().truncatedTo(ChronoUnit.DAYS), PAR, HDO, Temp, Pressure), true);
        System.out.println(dataString);
    }
    
}

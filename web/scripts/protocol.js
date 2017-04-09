/*  BSD 3-Clause License
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


/*
 * This file contains types for the protocol used to communicate between JSP and Servlet.
 */



function Request() {}

Request.prototype.toString = function () {
    return "";
}

Request.prototype.post = function (destination, action) {
    post(destination, {action: action, data: this.toString()}, function () {});
}


function DataRequest(startTime, endTime, parameters) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.params = parameters;
}

DataRequest.prototype = new Request();

DataRequest.prototype.toString = function () {
    return this.value;
}

function ParameterRequest(dataMask)
{
    this.action = "";
    this.data = dataMask;
}

ParameterRequest.prototype = new Request();
ParameterRequest.prototype.toString = function () {
    return this.value;
}

function ParameterResponse(json)
{
    if (typeof json !== "object") {
        //console.log("json: " + json);
        json = JSON.parse(json);
    }
    // Obtain data from response as JSONArray
    this.data = json["data"];
//    console.log("this.data: " + JSON.stringify(this.data));
//    this.descriptors = this.data[0]["descriptors"];
//    console.log("this.descriptors length: " + this.descriptors.length);
//    this.names = [];
//
//    for (var i = 0; i < this.descriptors.length; i++) {
//        this.piece = this.descriptors[i];
//        console.log("this.piece: " + JSON.stringify(this.piece));
//        this.names.push(this.piece["name"]);
//        console.log("this.names contains:" + JSON.stringify(this.names))
//    }
}


function DeleteDataRange(start, end) {
    this.start = start;
    this.end = end;
}

DeleteDataRange.prototype = {}

function DeleteDataRequest() {
    this.data = [];
}

DeleteDataRequest.prototype = new Request();

DeleteDataRequest.prototype.toString = function () {
    return this.data;
}

DeleteDataRequest.prototype.queueDeletion = function (name, range) {
    // Append if already present
    for (i = 0; i < this.data.length; i++) {
        if (data[i].name === name) {
            data[i].timeRange.push(range);
            return;
        }
    }

    // Create new...
    this.data.push({name: name, timeRange: [range]});
}


class InsertDataValue{
    constructor(timestamp, value) {
        this.timestamp = timestamp;
        this.value = value;
    }
}

//function InsertDataValue(timestamp, value) {
//    this.timestamp = timestamp;
//    this.value = value;
//}
//
//InsertDataValue.prototype = {}


class InsertDataRequest {
    constructor() {
        this.action = "insertData";
        this.data = [];
    }

    queueInsertion(name, valueObject)
    {
        // Append if already present
        for (var i = 0; i < this.data.length; i++) {
            if (this.data[i].name === name) {
                this.data[i].values.push(valueObject);
                return;
            }
        }
        // Create a new object
        this.data.push({name: name, values: [valueObject]});
    }
}

//function InsertDataRequest(){
//    this.action = "insertData";
//    this.data = [];
//}
//
//
//InsertDataRequest.prototype.constructor()
//{
//    action = "insertData",
//            data = []
//}
//
//InsertDataRequest.prototype = {}
//
//InsertDataRequest.prototype.queueInsertion = function (name, valueObject) {
//    // Append if already present
//    for (var i = 0; i < this.data.length; i++) {
//        if (data[i].name === name) {
//            data[i].values.push(valueObject);
//            return;
//        }
//    }
//    // Create a new object
//    this.data.push({name: name, values: [valueObject]});
//}



function DataResponse(json) {
    if (typeof json != "object") {
        console.log(json);
        json = JSON.parse(json);
    }


    // Obtain data from response as JSONArray
    this.data = json["resp"];
    this.table = json["table"];
    this.description = json["descriptions"];
    console.log(this.data);
    for (var i = 0; i < this.data.length; i++) {
        console.log("Parsed: " + this.data[i]["name"] + " with " + this.data[i]["data"].length + " items...");
    }
}

DataResponse.prototype = {

}

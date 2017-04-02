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

function post(destination, data, callback) {
    console.log("Sent to " + destination + ": " + JSON.stringify(data));
    callback("");
}

function Request() {}

Request.prototype.toString = function() {
    return "";
}

Request.prototype.post = function(destination, action) {
    post(destination, {action: action, data: this.toString()}, function() {});
}


function DataRequest(startTime, endTime, parameters) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.params = parameters;
}

DataRequest.prototype = {}

DataRequest.prototype.toString = function() {
    return this.value;
}

function ParameterRequest(dataMask)
{
    data = dataMask;
}

ParameterRequest.prototype = {data : 1}
ParameterRequest.prototype.toString = function(){return this.value;}

function ParameterResponse(json)
{
    if (typeof json != "object") {
        json = JSON.parse(json);
    } 
    console.log(json);
    // Obtain data from response as JSONArray
    this.data = json["data"];
    console.log(this.data);
    for (var i = 0; i < this.data.length; i++) {
        this.piece = data[i];
        this.descriptors = piece["descriptors"];
        this.names = [];
        for (var j = 0; j < this.descriptors.length; j++)
        {
            this.names[j] = this.descriptors["name"];
        }
    }
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

DeleteDataRequest.prototype.toString = function() {
    return this.data;
}

DeleteDataRequest.prototype.queueDeletion = function(name, range) {
    // Append if already present
    for (i = 0; i < this.data.length; i++) {
        if (data[i].name === name) {
            data[i].timeRange.push(range);
            return;
        }
    }

    // Create new...
    this.data.push({ name: name, timeRange: [range]});
}



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

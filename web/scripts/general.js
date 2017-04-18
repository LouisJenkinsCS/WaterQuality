
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


/**Sets the default dates for the date selectors
 * @param {type} date
 * @param {type} id
 */
function setDate(date, id) {
    var dateStr = date.getFullYear() + "-" + pad(date.getMonth() + 1, 2) + "-" + pad(date.getDate(), 2) + "T" + pad((date.getHours() + 1) % 24, 2) + ":" + pad((date.getMinutes() + 1) % 60, 2) + ":" + pad(0, 2);
    document.getElementById(id).value = dateStr;
    console.log("id: " + id + ", date: " + date, ", datestr: " + dateStr);
}

function pad(num, size) {
    var s = num + "";
    while (s.length < size)
        s = "0" + s;
    return s;
}

function createDateAsUTC(date) {
    return new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate(), date.getHours(), date.getMinutes(), date.getSeconds()));
}

function convertDateToUTC(date) {
    return new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), date.getUTCHours(), date.getUTCMinutes(), date.getUTCSeconds());
}

function formatDate(date) {
    date = convertDateToUTC(date);
    var day = date.getDate();
    var month = date.getMonth() + 1;
    var year = date.getFullYear();
    var hour = pad((date.getHours()) % 24, 2);
    var minute = pad((date.getMinutes()) % 60, 2);
    var am_pm = hour < 12 ? "AM" : "PM";
    if (hour > 12) {
        hour -= 12;
    }

    return month + "/" + day + "/" + year + " " + hour + ":" + minute + " " + am_pm;
}

function formatHiddenDate(date) {
    //date = convertDateToUTC(date);
    var day = pad(date.getDate(), 2);
    var month = pad(date.getMonth() + 1, 2);
    var year = date.getFullYear();
    var hour = pad((date.getHours()) % 24, 2);
    var minute = pad((date.getMinutes()) % 60, 2);

    return year + "" + month + "" + day + " " + hour + ":" + minute;
}

function formatDateSimple(date) {
    var day = date.substring(8, 10);
    var month = date.substring(5, 7);
    var year = date.substring(0, 4);
    var hour = date.substring(11, 13);
    var minute = date.substring(14, 16);
    var second = date.substring(17, 19);

    return month + "/" + day + "/" + year + " " + hour + ":" + minute + ":" + second;
}

function formatDateSimple2(date2) {
    var date = date2.toString();
    var date = date.substring(4,24);
    return date;
}

Date.prototype.dst = function () {
    return this.getTimezoneOffset() < this.stdTimezoneOffset();
};

Date.prototype.stdTimezoneOffset = function () {
    var jan = new Date(this.getFullYear(), 0, 1);
    var jul = new Date(this.getFullYear(), 6, 1);
    return Math.max(jan.getTimezoneOffset(), jul.getTimezoneOffset());
};

function sleep(milliseconds) {
    var start = new Date().getTime();
    for (var i = 0; i < 1e7; i++) {
        if ((new Date().getTime() - start) > milliseconds) {
            break;
        }
    }
}

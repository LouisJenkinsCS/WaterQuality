/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var checkedBoxes = 0;
var selected = [];
/**
 * The <code>fullCheck</code> function limits the number of data
 * checkboxes checked at a time to 3 by unchecking <coe>id</code>
 * if <code>checkedBoxes</code> equals 3
 * @param {type} id the current data type the user is trying to check
 */
function fullCheck(id) {
    var item = document.getElementById(id);
    var it;
    var startTime = new Date(document.getElementById("startdate").value).getTime();
    var endTime = new Date(document.getElementById("enddate").value).getTime();
    console.log("Start: " + startTime + " end: " + endTime);
    if (item.checked == true) {
        if (checkedBoxes < 2) {
            checkedBoxes++;
            selected.push(id);
        } else {
            it = selected.shift();
            document.getElementById(it).checked = false;
            selected.push(id);
        }
    } else {
        checkedBoxes--;
        it = selected.shift();
        if (it != id) {
            selected.push(it);
            selected.shift();
        }
    }
}

/**
 * The <code>toggle</code> function checks or unchecks
 * all of the checkboxes in the given <code>source</code> 
 * @param {type} source
 */
function toggle(source) {
    var checkboxes = document.getElementById("Table_form").querySelectorAll('input[type="checkbox"]');
    for (var i = 0; i < checkboxes.length; i++) {
        if (checkboxes[i] != source)
            checkboxes[i].checked = source.checked;
    }
}

function setCookie(name, value, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
    var expires = "expires=" + d.toUTCString();
    document.cookie = name + "=" + value + ";" + expires + ";path=/";
}

function getCookie(cname) {
    var name = cname + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

var current;
/**
 * The <code>openTab</code> function activates a certain event
 * based on the provided <code>tabName</code> parameter
 * @param {type} evt
 * @param {type} tabName the tab that the user is switching to
 */
function openTab(evt, tabName) {
    var i, tabcontent, tablinks, form;
    tabcontent = document.getElementsByClassName("tabcontent");


    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }

    tablinks = document.getElementsByClassName("tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }
    document.getElementById(tabName).style.display = "block";
    evt.currentTarget.className += " active";

    //<code>current</code>holds the current <code>tabName</code>
    //This is done because we need to limit the number of boxes checked
    //for the Graph tab and not the Table tab
    current = tabName;

    form = document.getElementsByClassName("data_type_form");
    for (i = 0; i < form.length; i++) {
        form[i].style.display = "none";
    }
    document.getElementById(current + "_form").style.display = "block";
    setCookie("id", current, 1);
}

function fetchData(json) {
    var resp = new DataResponse(json);

    var table = resp.table;
    var description = resp.description;
    document.getElementById("description").innerHTML = description;
    // This is new: Once we get data via AJAX, it's as easy as plugging it into DataResponse.
    var data = new DataResponse(json);
    var timeStamps = getTimeStamps(data);
    var timeStampStr = [];
    var values = getDataValues(data);
    // Convert timestamps to string; HighCharts already defines a nice formatting one.
    for (var i = 0; i < values.length; i++) {
        var arr = [];
        for (var j = 0; j < timeStamps.length; j++) {
            arr.push([new Date(timeStamps[j]), values[i][j]]);
            console.log("Pushed: " + values[i][j]);
        }
        timeStampStr.push(arr);
        console.log("Pushed: " + arr);
    }
    if(getCookie("id") == "Table")
        document.getElementById("Table").innerHTML = table;
        //fillTable(resp);
    else{
    // Remove all series data
    while (chart.series.length > 0)
        chart.series[0].remove(true);

    for (var i = 0; i < data.data.length; i++) {
        chart.addSeries({
            yAxis: i,
            name: data.data[i]["name"],
            data: timeStampStr[i]
        }, false);
        chart.yAxis[i].setTitle({text: data.data[i]["name"]});
    }
    chart.redraw();
    }
}

function handleClick(cb)
{
    if (current == 'Graph') {
        fullCheck(cb.id);
    }
//                post("ControlServlet", {key: 'control', control: 'getDesc'});
}

function fetch() {
    var startTime = new Date(document.getElementById("startdate").value).getTime();
    var endTime = new Date(document.getElementById("enddate").value).getTime();
    var selected = [];
    if(current=="Graph")
        var checkboxes = document.getElementById("Graph_form").querySelectorAll('input[type="checkbox"]');
    else
        var checkboxes = document.getElementById("Table_form").querySelectorAll('input[type="checkbox"]');
    console.log("Start: " + startTime + " end: " + endTime);
    for (var i = 0; i < checkboxes.length; i++) {
        if (checkboxes[i].checked == true) {
            selected.push(Number(checkboxes[i].name));
        }
    }

    var request = new DataRequest(startTime, endTime, selected);
    post("ControlServlet", {action: "fetchQuery", query: JSON.stringify(request)}, fetchData);

}

function setDate(date, id) {
    var dateStr = date.getFullYear() + "-" + pad(date.getMonth() + 1, 2) + "-" + pad(date.getDate(), 2) + "T" + pad((date.getHours() + 1) % 24, 2) + ":" + pad((date.getMinutes() + 1)%60, 2) + ":" + pad(0, 2);
    document.getElementById(id).value = dateStr;
    console.log("id: " + id + ", date: " + date, ", datestr: " + dateStr);
}

/**
 * Makes it so the date input fields can not be chosen for furture
 * dates. Also sets makes sure the <code>enddate</code> can not be a
 * date that is earlier than <code>startdate</code>
 */
function dateLimits() {
    var date = new Date();
    var dateStr = date.getFullYear() + "-" + pad(date.getMonth() + 1, 2) + "-" + pad(date.getDate(), 2) + "T" + pad(date.getHours() + 1, 2) + ":" + pad(date.getMinutes() + 1, 2) + ":" + pad(0, 2);
    document.getElementById("enddate").setAttribute("max", dateStr);
    document.getElementById("startdate").setAttribute("max", document.getElementById("enddate").value);
    document.getElementById("enddate").setAttribute("min", document.getElementById("startdate").value);
}

function pad(num, size) {
    var s = num + "";
    while (s.length < size)
        s = "0" + s;
    return s;
}

function fillTable(dataResp) {
    var table = document.getElementById("dataTable");
    table.innerHTML = "";
    var row, cell;
    var html = [];
    html.push("<table><tr><th>TimeStamp</th>");

    for (var i = 0; i < dataResp.data.length; i++) {
        html.push("<th>" + dataResp.data[i]["name"] + "</th>");
    }
    html.push("</tr>");
    var d = dataResp.data[0]["data"];
    console.log(d);
    for (var i = 0; i < d.length; i++) {
        html.push("<tr>");
        var ts_val = d[i];
        console.log("Date: " + new Date(ts_val["timestamp"]));
        html.push("<td>" + new Date(ts_val["timestamp"]).toUTCString() + "</td>");
        for (var j = 0; j < dataResp.data.length; j++) {
            var dl=dataResp.data[j]["data"];
            ts_val=dl[i];
            if(ts_val["value"]==null)
                html.push("<td> N/A </td>");
            else
                html.push("<td>" + ts_val["value"] + "</td>");
        }
        html.push("</tr>");
    }
    var finalHtml = "";
    for (i = 0; i < html.length; i++) {
        var str = html[i];
        console.log(str);
        finalHtml += str;
    }
    console.log(finalHtml);
    table.innerHTML = finalHtml;
}
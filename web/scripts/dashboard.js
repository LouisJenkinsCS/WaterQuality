$.getScript("scripts/datetimepicker.js", function () {});
$.getScript("scripts/general.js", function () {});

var checkedBoxes = 0;
var selected = [];
var descriptions = [];

/**
 * The <code>fullCheck</code> function limits the number of data
 * checkboxes checked at a time to 3 by unchecking <coe>id</code>
 * if <code>checkedBoxes</code> equals 3
 * @param {type} id the current data type the user is trying to check
 */
function fullCheck(id) {
    var item = document.getElementById(id);
    var it;
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

var bayesianMapping = [];

function bayesianSetData() {
             var selectBox = document.getElementById("bayesian_options");
            var selectedValue = selectBox.options[selectBox.selectedIndex].value;
            var arr = bayesianMapping[selectedValue].data;
            var datasets = bayesianMapping[selectedValue].datasets;
            while (bayesianChart.series.length > 0) {
                bayesianChart.series[0].remove(true);
            }
            for (var i = 0; i < arr.length; i++) {
                bayesianChart.addSeries({
    //                yAxis: i,
                    name: datasets[i].name,
                    data: arr[i]
                }, false);
    //            bayesianChart.yAxis[i].setTitle({text: dataSets[i].name});
            }
            bayesianChart.redraw();
         }

function bayesianRequest() {
    //makes the cursor show loading when graph/table is being generated 
    document.getElementById("loader").style.cursor = "progress";
    // Proof of Concept: Only obtains for a valid day
    var date = $('#bayesian_date').datepicker("getDate");
    post("ControlServlet", {action: "getBayesian", data: date.getTime() }, function(resp) {
        while (bayesianChart.series.length > 0)
            bayesianChart.series[0].remove(true);
        
        document.getElementById("loader").style.cursor = "default";
        var response = JSON.parse(resp);
        
        var dataSets;
        for (i = 0; i < response.data.length; i++) {
            document.getElementById("bayesian_options").innerHTML += "<option value='" + response.data[i].name + "'>" + response.data[i].name + "</option>";
            dataSets = response.data[i].dataSets;
            
            var timestamp = response.date;
            var arr = [];
            var idx = 0;
            console.log(timestamp);
            for (j = 0; j < dataSets.length; j++) {
                 var subArr = [];

                for (timestamp = response.date; timestamp < (response.date + 24 * 60 * 60 * 1000); timestamp += 15 * 60 * 1000) {
                    subArr.push([timestamp, dataSets[j].dataValues[idx]]);

                    idx++;
    //                console.log(subArr);
                }
                arr.push(subArr);
                idx = 0;
            }
            
            bayesianMapping[response.data[i].name] = { data: arr, datasets: dataSets };
        }
        
         document.getElementById("bayesian_options").onchange = bayesianSetData;
        
        document.getElementById("bayesian_options").style.display = "inline-block";
        $('#bayesian_options').val("DO Model");
        
        bayesianSetData();
//        var timestamp = response.date;
//        var arr = [];
//        var idx = 0;
//        console.log(timestamp);
//        for (i = 0; i < dataSets.length; i++) {
//             var subArr = [];
//             
//            for (timestamp = response.date; timestamp < (response.date + 24 * 60 * 60 * 1000); timestamp += 15 * 60 * 1000) {
//                subArr.push([timestamp, dataSets[i].dataValues[idx]]);
//                
//                idx++;
////                console.log(subArr);
//            }
//            arr.push(subArr);
//            idx = 0;
//        }
//        
//        
//        console.log(arr);
//        for (var i = 0; i < arr.length; i++) {
//            bayesianChart.addSeries({
////                yAxis: i,
//                name: dataSets[i].name,
//                data: arr[i]
//            }, false);
////            bayesianChart.yAxis[i].setTitle({text: dataSets[i].name});
//        }
//        
//        bayesianChart.redraw();
    })
}

//<code>doCheck</code> tells the checkboxes if they need to check or uncheck
//The default value is set to true so that first time it is clicked it resets
var doCheck = true;
/**
 * The <code>toggle</code> function checks or unchecks
 * all of the checkboxes in the table tab depending on the state of <code>doCheck</code>
 */
function toggle() {
    var checkboxes = document.getElementById("Table_form").querySelectorAll('input[type="checkbox"]');
    for (var i = 0; i < checkboxes.length; i++) {
        checkboxes[i].checked = doCheck;
    }
    //Sets to the opposite of itself so that the next click will be the revers
    doCheck = !doCheck;
    //makes sure the <code>select_all_box</code>
    //document.getElementById("select_all_box").checked=false;
}

/**Sets a cookie so that the current tab name can remembered for reloading the page
 * 
 * @param {type} CookieName the name of the cookie 
 * @param {type} tab is the current tab that is being loaded into the cookie
 * @param {type} exdays the number of days till the cookie expires
 */
function setCookie(CookieName, tab, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
    var expires = "expires=" + d.toUTCString();
    document.cookie = CookieName + "=" + tab + ";" + expires + ";path=/";
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

    //Makes all tabs not display anything
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

    //Switches between the forms depending on which tab is open
    form = document.getElementsByClassName("data_type_form");
    for (i = 0; i < form.length; i++) {
        form[i].style.display = "none";
    }
    document.getElementById(current + "_form").style.display = "block";
    //Sets a cookie so that the current tab can be remembered
    setCookie("id", current, 1);
}

function fetchData(json) {
    var resp = new DataResponse(json);



    // This is new: Once we get data via AJAX, it's as easy as plugging it into DataResponse.
    var data = new DataResponse(json);
    document.getElementById("description").innerHTML = "";
    for (i = 0; i < data.data.length; i++) {
        document.getElementById("description").innerHTML += "<center><h1>" + data.data[i].name + "</h1></center>";
        document.getElementById("description").innerHTML += descriptions[data.data[i].name];
    }
    var timeStamps = getTimeStamps(data);
    var timeStampStr = [];
    var values = getDataValues(data);
    // Convert timestamps to string; HighCharts already defines a nice formatting one.
    for (var i = 0; i < values.length; i++) {
        var arr = [];
        for (var j = 0; j < timeStamps.length; j++) {
            arr.push([timeStamps[j], values[i][j]]);
            //console.log("Pushed: " + values[i][j]);
        }
        timeStampStr.push(arr);
        //console.log("Pushed: " + arr);
    }

    //If this page is being loaded/refreshed it will run through the if
    //otherwise it goes to the else for normal page operation
    if (load == true) {
        load = false;//Sets <code>load</code> to false to continue normal operations
        fillTable(resp);

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
        if (data.data.length == 1)
            chart.yAxis[i].setTitle({text: ""});
        chart.redraw();
    } else {
        if (getCookie("id") == "Table")
            //document.getElementById("Table").innerHTML = table;
            fillTable(resp);
        else {
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
            if (data.data.length == 1)
                chart.yAxis[i].setTitle({text: ""});
            chart.redraw();
        }
    }
    //sets the cursor back to default after the graph/table is done being generated
    document.getElementById("loader").style.cursor = "default";
}

/**The <code>handleClick</code> function handles any and hall actions that need
 * to be done upon clicking of checkbox <code>cb</code>
 * 
 * @param {type} cb
 * @returns {undefined}
 */
function handleClick(cb)
{
    //If the current tab is the graph then it limits the number of boxes checked
    if (current == 'Graph') {
        fullCheck(cb.id);
    }
//                post("ControlServlet", {key: 'control', control: 'getDesc'});
}

function fetch() {
    //makes the cursor show loading when graph/table is being generated 
    document.getElementById("loader").style.cursor = "progress";
    if (current == "Graph") {
        var startTime = new Date(document.getElementById("graph_start_date").value).getTime();
        var endTime = new Date(document.getElementById("graph_end_date").value).getTime();
    }
    if (current == "Table") {
        var startTime = new Date(document.getElementById("table_start_date").value).getTime();
        var endTime = new Date(document.getElementById("table_end_date").value).getTime();
    }
    var selected = [];
    if (current == "Graph")
        var checkboxes = document.getElementById("Graph_form").querySelectorAll('input[type="checkbox"]');
    if (current == "Table")
        var checkboxes = document.getElementById("Table_form").querySelectorAll('input[type="checkbox"]:not([id="select_all_box"])');
    //console.log("Start: " + startTime + " end: " + endTime);
    var numChecked = 0;
    for (var i = 0; i < checkboxes.length; i++) {
        if (checkboxes[i].checked == true) {
            numChecked++;
            selected.push(Number(checkboxes[i].name));
        }
    }

    //checks if there are no data points selected
    if (numChecked == 0) {
        //if in the table tab the table will be set to null
        if (current == "Table")
            document.getElementById("data_table").innerHTML = null;
        //Returns the cursor to default so it doesnt get stuck on loading
        document.getElementById("loader").style.cursor = "default";
        return;
    }

    var request = new DataRequest(startTime, endTime, selected);
    post("ControlServlet", {action: "fetchQuery", query: JSON.stringify(request)}, fetchData);
}

/**Sets the default dates for the date selectors
 * @param {type} date
 * @param {type} id
 */
function setDate(date, id) {
    var dateStr = date.getFullYear() + "-" + pad(date.getMonth() + 1, 2) + "-" + pad(date.getDate(), 2) + "T" + pad((date.getHours() + 1) % 24, 2) + ":" + pad((date.getMinutes() + 1) % 60, 2) + ":" + pad(0, 2);
    document.getElementById(id).value = dateStr;
    //console.log("id: " + id + ", date: " + date, ", datestr: " + dateStr);
}

/**
 * Makes it so the date input fields can not be chosen for furture
 * dates. Also sets makes sure the ending dates can not be a
 * date that is earlier than ending dates
 */
function dateLimits() {
    var date = new Date();
    /*var dateStr = date.getFullYear() + "-" + pad(date.getMonth() + 1, 2) + "-" + pad(date.getDate(), 2) + "T" + pad(date.getHours() + 1, 2) + ":" + pad(date.getMinutes() + 1, 2) + ":" + pad(0, 2);
     document.getElementById("graph_end_date").setAttribute("maxDate", dateStr);
     document.getElementById("graph_start_date").setAttribute("maxDate", document.getElementById("graph_end_date").value);
     document.getElementById("graph_end_date").setAttribute("minDate", document.getElementById("graph_start_date").value);
     document.getElementById("table_end_date").setAttribute("maxDate", dateStr);
     document.getElementById("table_start_date").setAttribute("maxDate", document.getElementById("table_end_date").value);
     document.getElementById("table_end_date").setAttribute("minDate", document.getElementById("table_start_date").value);*/
    var selected = document.activeElement;
    if ($("#graph_end_date").data("datepicker") != null) {
        $("#graph_end_date").datetimepicker("option", "maxDate", date);
    }
    if ($("#graph_start_date").data("datepicker") != null) {
        $("#graph_start_date").datetimepicker("option", "maxDate", $("#graph_end_date").datepicker("getDate"));
        $("#graph_end_date").datetimepicker("option", "minDate", $("#graph_start_date").datepicker("getDate"));
    }
    if ($("#table_end_date").data("datepicker") != null) {
        $("#table_end_date").datetimepicker("option", "maxDate", date);
    }
    if ($("#table_start_date").data("datepicker") != null) {
        $("#table_start_date").datetimepicker("option", "maxDate", $("#table_end_date").datepicker("getDate"));
        $("#table_end_date").datetimepicker("option", "minDate", $("#table_start_date").datepicker("getDate"));
    }
    $(selected).datepicker("show");
}

function pad(num, size) {
    var s = num + "";
    while (s.length < size)
        s = "0" + s;
    return s;
}

/**The <code>fillTable</code> function recieves the selected data and
 * outputs the corresponding table within the table tab of the webpage
 * @param {type} dataResp
 */
function fillTable(dataResp) {
    var table = document.getElementById("data_table");

    $("#data_table").DataTable().destroy();
    table.innerHTML = "";

    var html = [];//Holds the table that will be created 
    var dates = [];//holds the array of all dates from all parameters 
    html.push("<table><thead><tr><th>TimeStamp</th>");
    //Adds the names to the header of the table 
    for (var i = 0; i < dataResp.data.length; i++) {
        html.push("<th>" + dataResp.data[i]["name"] + "</th>");
    }
    html.push("</tr></thead><tbody>");
    //adds one of every date to the <code>dates</code> array
    //This ensures that every date that is used can be accounted for
    //also allows the handling of missing data
    for (var j = 0; j < dataResp.data.length; j++) {
        var d = dataResp.data[j]["data"];
        for (var i = 0; i < d.length; i++) {
            var ts_val = d[i];
            if (dates.indexOf(ts_val["timestamp"]) == -1) {
                dates.push(ts_val["timestamp"]);
            }
        }
    }
    //since the dates are stored as epoch miliseconds this make sure the dates
    //are in the correct order
    dates.sort(function (a, b) {
        return a - b
    });
    //Adds all the values to the <code>html</code> array for the table
    for (var i = 0; i < dates.length; i++) {
        html.push("<tr>");
        var rowData = [];
        html.push("<td>" + formatDate(new Date(dates[i])) + "</td>");
        rowData.push(formatDate(new Date(dates[i])));
        for (var j = 0; j < dataResp.data.length; j++) {
            var d = dataResp.data[j]["data"];
            if (i >= d.length) {
                html.push("<td> N/A </td>");
                continue;
            }
            var ts_val = d[i];
            if (ts_val["timestamp"] != dates[i]) {
                html.push("<td> N/A </td>");
                d.splice(i, 0, null);
            } else {
                html.push("<td>" + ts_val["value"] + "</td>");
            }
        }
        html.push("</tr>");
    }
    html.push("</tbody></table>")
    //setting the innerHTML allows the table to be visible on the page
    var finalHtml = "";
    for (i = 0; i < html.length; i++) {
        var str = html[i];
        //console.log(str);
        finalHtml += str;
    }
    //console.log(finalHtml);
    table.innerHTML = finalHtml;
    $("#data_table").DataTable({
        dom: 'l<"#table_exports"B>frtip',
        buttons: [
            'excel',
            'csv',
            'pdf'
        ]
    });
}

/**The <code>openPoppup()</code> function simply opens a popped up
 * version of the data table when <code>data_table</code> is clicked 
 * so that the user can more easily see the data 
 */
function openPopup() {
    var modal = document.getElementById("myModal");
    var span = document.getElementsByClassName("close")[0];
    var table = document.getElementById("data_table");
    var popup = document.getElementById("popup");


    popup.innerHTML = table.innerHTML;
    modal.style.display = "block";

    $(popup).DataTable();
    span.onclick = function () {
        modal.style.display = "none";
        $(popup).DataTable().destroy();
    }
}

//<code>load</code> makes sure that when the page is newly loaded it will do a
//special action in the <code>fetchDataFunction</code> allowing it to generate
//both the table and the graph
var load = true;
/**The <code>startingData()</code> function generates both the table and the graph
 * on load/refresh of a page by using setting them to Dewpoint
 */
function startingData() {
    post("AdminServlet", {action: "getParameters", data: 3}, function (resp) {

        console.log(JSON.parse(resp));
        var data = JSON.parse(resp)["data"][0]["descriptors"];
        console.log(data);
        for (i = 0; i < data.length; i++) {
            descriptions[data[i].name] = data[i].description;
            var param = "<input type='checkbox' name='" + data[i].id + "' onclick='handleClick(this); fetch();' class='sensor_data' id='" + data[i].id + "' value='data'>" + data[i].name + "<br>\n";
            document.getElementById("graph_sensor_parameters").innerHTML += param;
            document.getElementById("table_sensor_parameters").innerHTML += param;
        }
        data = JSON.parse(resp)["data"][1]["descriptors"];
        for (i = 0; i < data.length; i++) {
            descriptions[data[i].name] = data[i].description;
            var param = "<input type='checkbox' name='" + data[i].id + "' onclick='handleClick(this); fetch();' class='manual_data' id='" + data[i].id + "' value='data'>" + data[i].name + "<br>\n";
            document.getElementById("graph_manual_parameters").innerHTML += param;
            document.getElementById("table_manual_parameters").innerHTML += param;
        }
        current = "Graph";
        var graphcheckboxes = document.getElementById("Graph_form").querySelectorAll('input[type="checkbox"]');
        graphcheckboxes[3].click();

        var tablecheckboxes = document.getElementById("Table_form").querySelectorAll('input[type="checkbox"]');
        tablecheckboxes[4].checked = true;
        current = getCookie("id");
        if (getCookie("id") == "Table")
            document.getElementById("TableTab").click();
        else {
            if (getCookie("id") == "Bayesian")
                document.getElementById("BayesianTab").click();
            else
                document.getElementById("GraphTab").click();
        }
    });
}

function setOnSelect(){
    $("#graph_end_date").datetimepicker("option","onSelect",fetch);
    $("#graph_start_date").datetimepicker("option","onSelect",fetch);
    $("#table_end_date").datetimepicker("option","onSelect",fetch);
    $("#table_start_date").datetimepicker("option","onSelect",fetch);
}

$(function () {
    var date = new Date();
//            $( "#delete_endtime" ).timepicker();
//            $( "#delete_starttime" ).timepicker();
    $("#graph_end_date").datetimepicker({
        controlType: 'select',
        oneLine: true,
        timeFormat: 'hh:mm tt',
        altField: "#graph_end_time"
    })
            .datepicker("setDate", date);
    $("#table_end_date").datetimepicker({
        controlType: 'select',
        oneLine: true,
        timeFormat: 'hh:mm tt',
        altField: "#table_end_time"
    })
            .datepicker("setDate", date);

    date.setMonth(date.getMonth() - 1);
    $("#graph_start_date").datetimepicker({
        controlType: 'select',
        oneLine: true,
        timeFormat: 'hh:mm tt',
        altField: "#graph_start_time"
    })
            .datepicker("setDate", date);
    $("#table_start_date").datetimepicker({
        controlType: 'select',
        oneLine: true,
        timeFormat: 'hh:mm tt',
        altField: "#table_start_time"
    })
            .datepicker("setDate", date);
    
    var bayesian_date=new Date();
    bayesian_date.setDate(bayesian_date.getDate()-1);
    $("#bayesian_date").datepicker({
        controlType: 'select',
        oneLine: true,
        timeFormat: 'hh:mm tt',
        maxDate:bayesian_date
    })
            .datepicker("setDate",bayesian_date);
    setOnSelect();
});
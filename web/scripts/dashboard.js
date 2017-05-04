$.getScript("scripts/datetimepicker.js", function () {});
$.getScript("scripts/general.js", function () {});

var checkedBoxes = 0;
var selected = [];
var tableSelected = [];
// Descriptions for DataParameters; (id -> description)
var descriptions = [];
var units = [];
// Names for DataParameters; (id -> name)
var names = [];
//creates the interval to call getMostRecent every 5 minutes
var interval=setInterval(getMostRecent(),1000*60*5);
var isFirstLoad = true;

/**
 * The <code>fullCheck</code> function limits the number of data
 * checkboxes checked at a time to 3 by unchecking <coe>id</code>
 * if <code>checkedBoxes</code> equals 3
 * @param {type} id the current data type the user is trying to check
 */
function fullCheck(id) {
    var item = document.getElementById(id);
    if (item.nodeName === "SELECT") {
        return;
    }
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

function tableChecked(id) {
    var item = document.getElementById(id);
    var it;
    if (item.checked === true) {
        tableSelected.push(id);
    } else {
        if (tableSelected.indexOf(id) != -1) {
            tableSelected.splice(tableSelected.indexOf(id), 1);
        }
    }
}

/**
 * The <code>toggle</code> function checks or unchecks
 * all of the checkboxes in the table tab depending on the state of <code>source</code>
 * checkbox
 */
function toggle(id, source) {
    var checkboxes = document.getElementById(id).querySelectorAll('input[type="checkbox"]');
    for (var i = 0; i < checkboxes.length; i++) {
        checkboxes[i].checked = source.checked;
    }
    if (source.checked == false)
        tableSelected = [];
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
    if (tabName === current) {
        if (isFirstLoad === true) {
            isFirstLoad = false;
        } else {
            return;
        }
    }
    var i, tabcontent, tablinks, form, descriptions;
    tabcontent = document.getElementsByClassName("tabcontent");

    //Makes all tabs not display anything
    for (i = 0; i < tabcontent.length; i++) {
        $(tabcontent[i]).finish();
        $(tabcontent[i]).hide("slow");
//        tabcontent[i].style.display = "none";
    }


    tablinks = document.getElementsByClassName("tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }
    $('#' + tabName).finish().show({
        duration: "slow",
        complete: () =>
            $('#' + tabName).css("display", "block").promise().done(() => {
                if (tabName === "Graph") {
                    chart.redraw();
                    chart.reflow();
                } 
            }),
        step: (n, tween) => {
                if ((n % 25) === 0) {
                    chart.redraw();
                    chart.reflow();
                }
            }
            
        });
//    document.getElementById(tabName).style.display = "block";
    evt.currentTarget.className += " active";
    
    //<code>current</code>holds the current <code>tabName</code>
    //This is done because we need to limit the number of boxes checked
    //for the Graph tab and not the Table tab
    current = tabName;

    //Switches between the forms depending on which tab is open
    form = document.getElementsByClassName("data_type_form");
    for (i = 0; i < form.length; i++) {
        $(form[i]).finish();
        $(form[i]).hide("slow");
        //form[i].style.display = "none";
    }
    $('#' + tabName + '_form').finish().show("slow",() => {
                $('#' + tabName+'_form').css("display", "block");
            });
    //document.getElementById(current + "_form").style.display = "block";

    descriptions = document.getElementsByClassName("description");
    for (i = 0; i < descriptions.length; i++) {
        descriptions[i].style.display = "none";
    }
    document.getElementById(current + "_description").style.display = "block";
    //Sets a cookie so that the current tab can be remembered
    setCookie("id", current, 1);
    
    //fixes the issue that the chart doesnt show when page is rezied
    chart.reflow();
    chart.redraw();
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
    } else {
        tableChecked(cb.id);
    }
    fetch();
    //updates the most recent data for all parameters and then clears the
    //interval so it resets the time til the next call
    getMostRecent();
    clearInterval(interval);
}

function fetch() {
    if (current === null || current === undefined) {
        current = "Graph";
    }
    //makes the cursor show loading when graph/table is being generated 
    document.getElementById("loader").style.cursor = "progress";
    var startTime, endTime;
    if (current === "Graph") {
        var startTime = new Date(document.getElementById("graph_start_date").value).getTime();
        var endTime = new Date(document.getElementById("graph_end_date").value).getTime();
        
        var graphStartTime = document.getElementById("graph_start_time").value;
        var graphEndTime = document.getElementById("graph_end_time").value;
        
        var tempstart = graphStartTime.split(':');
        var tempend = graphEndTime.split(':');
        
        startTime = new Date(startTime + tempstart[0] * 3600000 + tempstart[1] * 60000).getTime();
        endTime = new Date(endTime + tempend[0] * 3600000 + tempend[1] * 60000).getTime();
    }
    if (current == "Table") {
        var startTime = new Date(document.getElementById("table_start_date").value).getTime();
        var endTime = new Date(document.getElementById("table_end_date").value).getTime();
        
        var tableStartTime = document.getElementById("table_start_time").value;
        var tableEndTime = document.getElementById("table_end_time").value;
        
        var tempstart = tableStartTime.split(':');
        var tempend = tableEndTime.split(':');
        
        startTime = new Date(startTime + tempstart[0] * 3600000 + tempstart[1] * 60000).getTime();
        endTime = new Date(endTime + tempend[0] * 3600000 + tempend[1] * 60000).getTime();
    }
    var selecteddata = [];
    if (current == "Graph")
        var checkboxes = document.getElementById("Graph_form").querySelectorAll('input[type="checkbox"]');
    if (current == "Table")
        var checkboxes = document.getElementById("Table_form").querySelectorAll('input[type="checkbox"]:not([class="select_all_box"])');
    //console.log("Start: " + startTime + " end: " + endTime);
    var numChecked = 0;
    for (var i = 0; i < checkboxes.length; i++) {
        if (checkboxes[i].checked == true) {
            numChecked++;
            var name=checkboxes[i].name.substring(6);
            selecteddata.push(Number(name));
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
    var request = new DataRequest(startTime, endTime, selecteddata);
    post("DataServlet", {action: "fetchQuery", query: JSON.stringify(request)}, fetchData);
}

function fetchData(json) {
    var data = new DataResponse(json);

    // New data: Clear descriptions

    //document.getElementById("graph_description").innerHTML = "";
    //document.getElementById("table_description").innerHTML = "";

    // If there is a missing description for something selected, fill it ourselves...
    if (current == "Graph") {
        for (j = 0; j < selected.length; j++) {
            var contains = false;
            for (i = 0; i < data.data.length; i++) {
                if (selected[j].substring(6) == data.data[i].id) {
                    contains = true;
                    break;
                }
            }

            if (!contains) {
                data.data.push({id: selected[j].substring(6), dataValues: []});
            }
        }
    }

    // Empty?
    if (data.data.length == 0) {
        if (current == "Table") {
            fillTable(data);
        } else
            return;
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
        fillTable(data);

        while (chart.series.length > 0)
            chart.series[0].remove(true);

        for (var i = 0; i < data.data.length; i++) {
            var currentSelectedUnit = document.getElementById("graph_unit_selection_" + data.data[i].id).selectedIndex;
            chart.addSeries({
                yAxis: i,
                name: names[data.data[i].id],
                data: getDataValuesFor(data.data[i]["dataValues"], units[data.data[i].id][currentSelectedUnit].conversion)
            }, false);
            if (names[data.data[i].id] === "pH")
                chart.yAxis[i].setTitle({text: names[data.data[i].id]});
            else
                chart.yAxis[i].setTitle({text: names[data.data[i].id] + " (" + units[data.data[i].id][currentSelectedUnit].unit + ")"});
        }
        if (data.data.length == 1)
            chart.yAxis[i].setTitle({text: ""});
        chart.redraw();
        
//        document.getElementById("Graph_description").innerHTML="";
//        document.getElementById("Table_description").innerHTML="";
        var description = "";
        for (i = 0; i < data.data.length; i++) {
            
            // The server gives us the identifier, not the name, and so we need to do a lookup in our own map.
            description += "## " + names[data.data[i].id] + "\n" + descriptions[data.data[i].id] + "\n";
            
//            document.getElementById("Graph_description").innerHTML += "# " + names[data.data[i].id] + "\n";
//            document.getElementById("Graph_description").innerHTML += descriptions[data.data[i].id];
//            document.getElementById("Table_description").innerHTML += "# " + names[data.data[i].id] + "\n";
//            document.getElementById("Table_description").innerHTML += descriptions[data.data[i].id];
        }
        $('#Graph_description', '#Table_description')
                .fadeOut("slow", () => {
                    $('#Graph_description', '#Table_description').html(marked(description));
                    $('#Graph_description', '#Table_description').fadeIn("slow");
                });
        document.getElementById("Graph_description").innerHTML = document.getElementById("Table_description").innerHTML = 
                "<div class='markdown-preview' data-use-github-style>" + marked(description) + "</div>";
    } else {
        var description = "";
        if (getCookie("id") == "Table") {
            //document.getElementById("Table").innerHTML = table;
            fillTable(data);
            
//            document.getElementById("Table_description").innerHTML = "";
            for (i = 0; i < data.data.length; i++) {
                description += "## " + names[data.data[i].id] + "\n" + descriptions[data.data[i].id] + "\n";
                
//                // The server gives us the identifier, not the name, and so we need to do a lookup in our own map.
//                document.getElementById("Table_description").innerHTML += "<center><h1>" + names[data.data[i].id] + "</h1></center>";
//                document.getElementById("Table_description").innerHTML += descriptions[data.data[i].id];
            }
            $('#Table_description')
                .fadeOut("slow", () => {
                    $('#Table_description').html("<div class='markdown-preview' data-use-github-style>" + marked(description) + "</div>");
                    $('#Table_description').fadeIn("slow");
                });
        }
        else {
            // Remove all series data
            while (chart.series.length > 0)
                chart.series[0].remove(true);

            for (var i = 0; i < data.data.length; i++) {
                var currentSelectedUnit = document.getElementById("graph_unit_selection_" + data.data[i].id).selectedIndex;
                chart.addSeries({
                    yAxis: i,
                    name: names[data.data[i].id],
                    data: getDataValuesFor(data.data[i]["dataValues"], units[data.data[i].id][currentSelectedUnit].conversion)
                }, false);
                if (names[data.data[i].id] === "pH")
                    chart.yAxis[i].setTitle({text: names[data.data[i].id]});
                else
                    chart.yAxis[i].setTitle({text: names[data.data[i].id] + " (" + units[data.data[i].id][currentSelectedUnit].unit + ")"});
            }
            if (data.data.length == 1)
                chart.yAxis[i].setTitle({text: ""});
            chart.redraw();
//            document.getElementById("Graph_description").innerHTML = "";
            for (i = 0; i < data.data.length; i++) {
                description += "## " + names[data.data[i].id] + "\n" + descriptions[data.data[i].id] + "\n";
                
                // The server gives us the identifier, not the name, and so we need to do a lookup in our own map.
//                document.getElementById("Graph_description").innerHTML += "<center><h1>" + names[data.data[i].id] + "</h1></center>";
//                document.getElementById("Graph_description").innerHTML += descriptions[data.data[i].id];
            }
            $('#Graph_description')
                .fadeOut("slow", () => {
                    $('#Graph_description').html("<div class='markdown-preview' data-use-github-style>" + marked(description) + "</div>");
                    $('#Graph_description').fadeIn("slow");
                });
        }
    }

    //sets the cursor back to default after the graph/table is done being generated
    document.getElementById("loader").style.cursor = "default";
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
    var active = document.activeElement;
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
    $(active).datepicker("show");
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
    //if(dataResp.data.length)
    // If there is a missing description for something selected, fill it ourselves...
    for (j = 0; j < tableSelected.length; j++) {
        var contains = false;
        for (i = 0; i < dataResp.data.length; i++) {
            if (tableSelected[j].substring(6) == dataResp.data[i].id) {
                contains = true;
                break;
            }
        }

        if (!contains) {
            dataResp.data.push({id: tableSelected[j].substring(6), dataValues: []});
        }
    }

    var html = [];//Holds the table that will be created 
    var dates = [];//holds the array of all dates from all parameters 
    html.push("<table><thead><tr><th>TimeStamp</th>");
    //Adds the names to the header of the table 
    for (var i = 0; i < dataResp.data.length; i++) {
        html.push("<th>" + names[dataResp.data[i]["id"]] + "</th>");
    }
    html.push("</tr></thead><tbody>");
    //adds one of every date to the <code>dates</code> array
    //This ensures that every date that is used can be accounted for
    //also allows the handling of missing data
    for (var j = 0; j < dataResp.data.length; j++) {
        var d = dataResp.data[j]["dataValues"];
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
        return a - b;
    });
    //Adds all the values to the <code>html</code> array for the table
    for (var i = 0; i < dates.length; i++) {
        html.push("<tr>");
        var tempDate = dates[i];
        if(new Date(tempDate).dst())
            tempDate = tempDate - 14400000;
        else
            tempDate = tempDate - 18000000;
        html.push("<td><span style='display: none'>" + formatHiddenDate(new Date(tempDate))
                + "</span>" + formatDate(new Date(tempDate)) + "</td>");
        for (var j = 0; j < dataResp.data.length; j++) {
            var currentSelectedUnit = document.getElementById("table_unit_selection_" + dataResp.data[j].id).selectedIndex;
            var conversion = units[dataResp.data[j].id][currentSelectedUnit].conversion;
            var d = dataResp.data[j]["dataValues"];
            if (i >= d.length) {
                html.push("<td> N/A </td>");
                continue;
            }
            var ts_val = d[i];
            if (ts_val["timestamp"] !== dates[i]) {
                html.push("<td> N/A </td>");
                d.splice(i, 0, null);
            } else {
                html.push("<td>" + conversion(ts_val["value"]) + "</td>");
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
    
    table.innerHTML = finalHtml;
    //creates the datatables api for the data in the table
    $("#data_table").DataTable({
        //the options for the API chooses what to display
        //l- number of rows displayed selector
        //B- the export buttons
        //f- filtering of the input
        //t- the actual table
        //i- information about how many entries are in the table
        //p- control for switching to the next page of rows
        dom: 'l<"#table_exports"B>ftip',
        //displays the buttons for exporting
        buttons: [
            'excel',
            'csv',
            'pdf'
        ]
    });
}

//<code>load</code> makes sure that when the page is newly loaded it will do a
//special action in the <code>fetchDataFunction</code> allowing it to generate
//both the table and the graph
var load = true;
/**The <code>startingData()</code> function generates both the table and the graph
 * on load/refresh of a page by using setting them to Dewpoint
 */
function startingData() {
    $("#Graph").resize(() => {
        chart.redraw();
        chart.reflow();
    })
    // Stickies the admin notes to top of screen...
    new Waypoint.Sticky({
        element: $("#myNav")
    });
    $("#admin_expand_button").click(() => {
        // Expand to about half page...
        $("#myNav").css("z-index", "1");
        $("#myNav").height(($(window).height() / 2) + "px");
        $("#admin_notes").show();
    })
    post("AdminServlet", {action: "getNotes"}, function (resp) {
        if (resp.hasOwnProperty("status")) {
            window.alert("Error getting notes");
        }
        else{
            var respData = JSON.parse(resp);
            $("#overlayNote").html(marked(respData["note"]));
        }
    });
    post("AdminServlet", {action: "getParameters", data: 3}, function (resp) {

//        console.log(JSON.parse(resp));
        var data = JSON.parse(resp)["data"][0]["descriptors"];
//        console.log(data);
        for (i = 0; i < data.length; i++) {
            // Cache parameter descriptors
            descriptions[data[i].id] = data[i].description;
            names[data[i].id] = data[i].name;
            units[data[i].id] = [ { unit: data[i].unit, conversion: x => x } ];
            
            if (data[i].name.includes("Temperature") || data[i].name === "Dewpoint") {
                units[data[i].id].push({ unit: "F", conversion: x => x * 1.8 + 32 });
            } else if (data[i].name === "Depth") {
                units[data[i].id].push({ unit: "ft", conversion: x => x * 3.28084 });
            }
            
            var tableRef = document.getElementById('sensor_formatted_table').getElementsByTagName('tbody')[0];
            var table_tableRef = document.getElementById('table_sensor_formatted_table').getElementsByTagName('tbody')[0];
            // Insert a row in the table at the last row
            var newRow   = tableRef.insertRow(tableRef.rows.length);
            var table_newRow = table_tableRef.insertRow(table_tableRef.rows.length);

            // Insert a cell in the row at index 0
            var newCell  = newRow.insertCell(0);
            var table_newCell = table_newRow.insertCell(0);
            newCell.innerHTML = "<input type='checkbox' name='graph_" + data[i].id + "' onclick='handleClick(this);' class='sensor_data' id='graph_" + data[i].id + "' value='data'>" + data[i].name;
            table_newCell.innerHTML = "<input type='checkbox' name='table_" + data[i].id + "' onclick='handleClick(this);' class='sensor_data' id='table_" + data[i].id + "' value='data'>" + data[i].name;
                    
            var unit_selection = "<select onchange='handleClick(this);' id='graph_unit_selection_" + data[i].id + "'>";
            var table_unit_selection = "<select onchange='handleClick(this);' id='table_unit_selection_" + data[i].id + "'>";
            for (k = 0; k < units[data[i].id].length; k++) {
                var unitObj = units[data[i].id][k];
                unit_selection += "<option value='" + unitObj.unit + "'" + (k === 0 ? "selected='selected'" : "") + ">" + unitObj.unit + "</option>";
                table_unit_selection += "<option value='" + unitObj.unit + "'" + (k === 0 ? "selected='selected'" : "") + ">" + unitObj.unit + "</option>";
            }
            newCell = newRow.insertCell(1).innerHTML = unit_selection;
            table_newCell = table_newRow.insertCell(1).innerHTML = table_unit_selection;
            newCell = newRow.insertCell(2);
            table_newCell = table_newRow.insertCell(2);
            newCell.innerHTML = "<span class='recent_sensor_"+data[i].id+"'></span>";
            table_newCell.innerHTML = "<span class='recent_sensor_"+data[i].id+"'></span>";

            var param = "<input type='checkbox' name='graph_" + data[i].id + "' onclick='handleClick(this);' class='sensor_data' id='graph_" + data[i].id + "' value='data'>" + data[i].name 
                    + "<span class='recent_sensor_"+data[i].id+"'></span><br>\n";
            var tableparam = "<input type='checkbox' name='table_" + data[i].id + "' onclick='handleClick(this);' class='sensor_data' id='table_" + data[i].id + "' value='data'>" + data[i].name 
                    + "<span class='recent_sensor_"+data[i].id+"'></span><br>\n";
            
//            document.getElementById("graph_sensor_parameters").innerHTML += param;
//            document.getElementById("table_sensor_parameters").innerHTML += tableparam;
        }
        data = JSON.parse(resp)["data"][1]["descriptors"];
        for (i = 0; i < data.length; i++) {
            descriptions[data[i].id] = data[i].description;
            names[data[i].id] = data[i].name;
            units[data[i].id] = [ { unit: data[i].unit, conversion: x => x } ];
            
            var tableRef = document.getElementById('manual_formatted_table').getElementsByTagName('tbody')[0];
            var table_tableRef = document.getElementById('table_manual_formatted_table').getElementsByTagName('tbody')[0];
            // Insert a row in the table at the last row
            var newRow   = tableRef.insertRow(tableRef.rows.length);
            var table_newRow = table_tableRef.insertRow(table_tableRef.rows.length);
            

            // Insert a cell in the row at index 0
            var newCell  = newRow.insertCell(0);
            var table_newCell  = table_newRow.insertCell(0);
            newCell.innerHTML = "<input type='checkbox' name='graph_" + data[i].id + "' onclick='handleClick(this);' class='manual_data' id='graph_" + data[i].id + "' value='data'>" + data[i].name;
            table_newCell.innerHTML = "<input type='checkbox' name='table_" + data[i].id + "' onclick='handleClick(this);' class='manual_data' id='table_" + data[i].id + "' value='data'>" + data[i].name;
                    
            var unit_selection = "<select onchange='handleClick(this);' id='graph_unit_selection_" + data[i].id + "'>";
            var table_unit_selection = "<select onchange='handleClick(this);' id='table_unit_selection_" + data[i].id + "'>";
            for (k = 0; k < units[data[i].id].length; k++) {
                var unitObj = units[data[i].id][k];
                unit_selection += "<option value='" + unitObj.unit + "'" + (k === 0 ? "selected='selected'" : "") + ">" + unitObj.unit + "</option>";
                table_unit_selection += "<option value='" + unitObj.unit + "'" + (k === 0 ? "selected='selected'" : "") + ">" + unitObj.unit + "</option>";
            }
            newCell = newRow.insertCell(1).innerHTML = unit_selection;
            table_newCell = table_newRow.insertCell(1).innerHTML = table_unit_selection;
          
            
            var param = "<input type='checkbox' name='graph_" + data[i].id + "' onclick='handleClick(this);' class='manual_data' id='graph_" + data[i].id + "' value='data'>" + data[i].name 
                    + "<span class='graph_recent_manual_"+data[i].id+"'></span><br>\n";
            var tableparam = "<input type='checkbox' name='table_" + data[i].id + "' onclick='handleClick(this);' class='manual_data' id='table_" + data[i].id + "' value='data'>" + data[i].name 
                    + "<span class='table_recent_manual_"+data[i].id+"'></span><br>\n";
          
//            document.getElementById("graph_manual_parameters").innerHTML += param;
//            document.getElementById("table_manual_parameters").innerHTML += tableparam;
        }
        current = "Graph";
        var graphcheckboxes = document.getElementById("Graph_form").querySelectorAll('input[type="checkbox"]');
        graphcheckboxes[3].click();

        var tablecheckboxes = document.getElementById("Table_form").querySelectorAll('input[type="checkbox"]');
        tablecheckboxes[5].checked = true;

        if(getCookie("id")=="")
            setCookie("id", current, 1);
        else
            current = getCookie("id");
        if (getCookie("id") == "Table")
            document.getElementById("TableTab").click();
        else {
            if (getCookie("id") == "Graph")
                document.getElementById("GraphTab").click();
        }
        checkuser();
    });
}

/**Sets the <code>onClose</code> property of each datpicker to call fetch
 */
function setOnClose() {
    $("#graph_end_date").datetimepicker("option", "onClose", fetch);
    $("#graph_start_date").datetimepicker("option", "onClose", fetch);
    $("#table_end_date").datetimepicker("option", "onClose", fetch);
    $("#table_start_date").datetimepicker("option", "onClose", fetch);
}

$(function () {
    var date = new Date();
//            $( "#delete_endtime" ).timepicker();
//            $( "#delete_starttime" ).timepicker();
    $("#graph_end_date").datetimepicker({
        controlType: 'select',
        oneLine: true,
        altField: "#graph_end_time"
    })
            .datepicker("setDate", date);
    $("#table_end_date").datetimepicker({
        controlType: 'select',
        oneLine: true,
        altField: "#table_end_time"
    })
            .datepicker("setDate", date);

    date.setDate(date.getDate() - 7);
    $("#graph_start_date").datetimepicker({
        controlType: 'select',
        oneLine: true,
        altField: "#graph_start_time"
    })
            .datepicker("setDate", date);
    $("#table_start_date").datetimepicker({
        controlType: 'select',
        oneLine: true,
        altField: "#table_start_time"
    })
            .datepicker("setDate", date);
    setOnClose();
});

function getMostRecent(){
    post("DataServlet", {action: "getMostRecent"}, function (resp) {
        var data=JSON.parse(resp)["data"];
        /*
         * getMostRecent
         * {data:[{
         *  id:
         *  time:
         *  value:
         *  }]
         *  }
         */
        //var html="";
        for(var i=0; i < data.length; i++){
            var sensorrecent=document.getElementsByClassName("recent_sensor_"+data[i].id);
//            var html= formatDate(new Date(data[i].time));
            var html = data[i].value;
            if(html!=null){
                for(var j=0; j < sensorrecent.length; j++){
                    sensorrecent[j].innerHTML=html;
                }
            }
        }
        var recentdate = document.getElementsByClassName("last_updated");
        
        var tempDate = new Date(data[0].time);

        //8 hours difference to accommodate formatting of
        //formatDate() in general.js and timezone change
        tempDate -= 28800000;
        
        var displayTime = new Date(tempDate);
        
        if(displayTime !== null)
            for(var j=0; j < recentdate.length; j++)
                recentdate[j].innerHTML = formatDate(displayTime);
                
    });
}

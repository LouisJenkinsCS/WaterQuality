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

//<code>doCheck</code> tells the checkboxes if they need to check or uncheck
//The default value is set to true so that first time it is clicked it resets
var doCheck=true;
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
    doCheck=!doCheck;
    //makes sure the <code>select_all_box</code>
    document.getElementById("select_all_box").checked=false;
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
    if(current == "Table")
        document.getElementById("exportbutton").style.display="block";
    else
        document.getElementById("exportbutton").style.display="none";
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
            arr.push([timeStamps[j], values[i][j]]);
            console.log("Pushed: " + values[i][j]);
        }
        timeStampStr.push(arr);
        console.log("Pushed: " + arr);
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
    document.getElementById("loader").style.cursor="default";
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
    document.getElementById("loader").style.cursor="progress";
    if(current=="Graph"){
        var startTime = new Date(document.getElementById("graph_start_date").value).getTime();
        var endTime = new Date(document.getElementById("graph_end_date").value).getTime();
    }
    if(current=="Table"){
        var startTime = new Date(document.getElementById("table_start_date").value).getTime();
        var endTime = new Date(document.getElementById("table_end_date").value).getTime();
    }
    var selected = [];
    if(current=="Graph")
        var checkboxes = document.getElementById("Graph_form").querySelectorAll('input[type="checkbox"]');
    if(current=="Table")
        var checkboxes = document.getElementById("Table_form").querySelectorAll('input[type="checkbox"]');
    console.log("Start: " + startTime + " end: " + endTime);
    var numChecked=0;
    for (var i = 0; i < checkboxes.length; i++) {
        if (checkboxes[i].checked == true) {
            numChecked++;
            selected.push(Number(checkboxes[i].name));
        }
    }
    
    //checks if there are no data points selected
    if(numChecked==0){
        //if in the table tab the table will be set to null
        if(current=="Table")
            document.getElementById("dataTable").innerHTML=null;
        //Returns the cursor to default so it doesnt get stuck on loading
        document.getElementById("loader").style.cursor="default";
        return;
    }
    
    var request = new DataRequest(startTime, endTime, selected);
    post("ControlServlet", {action: "fetchQuery", query: JSON.stringify(request)}, fetchData);
    //document.getElementById("loader").style.cursor="default";
}

/**Sets the default dates for the date selectors
 * @param {type} date
 * @param {type} id
 */
function setDate(date, id) {
    var dateStr = date.getFullYear() + "-" + pad(date.getMonth() + 1, 2) + "-" + pad(date.getDate(), 2) + "T" + pad((date.getHours() + 1) % 24, 2) + ":" + pad((date.getMinutes() + 1)%60, 2) + ":" + pad(0, 2);
    document.getElementById(id).value = dateStr;
    console.log("id: " + id + ", date: " + date, ", datestr: " + dateStr);
}

/**
 * Makes it so the date input fields can not be chosen for furture
 * dates. Also sets makes sure the ending dates can not be a
 * date that is earlier than ending dates
 */
function dateLimits() {
    var date = new Date();
    var dateStr = date.getFullYear() + "-" + pad(date.getMonth() + 1, 2) + "-" + pad(date.getDate(), 2) + "T" + pad(date.getHours() + 1, 2) + ":" + pad(date.getMinutes() + 1, 2) + ":" + pad(0, 2);
    document.getElementById("graph_end_date").setAttribute("max", dateStr);
    document.getElementById("graph_start_date").setAttribute("max", document.getElementById("graph_end_date").value);
    document.getElementById("graph_end_date").setAttribute("min", document.getElementById("graph_start_date").value);
    document.getElementById("table_end_date").setAttribute("max", dateStr);
    document.getElementById("table_start_date").setAttribute("max", document.getElementById("table_end_date").value);
    document.getElementById("table_end_date").setAttribute("min", document.getElementById("table_start_date").value);
}

function setBayesianDate(date,id){
    var dateStr = date.getFullYear() + "-" + pad(date.getMonth() + 1, 2) + "-" + pad(date.getDate()-1, 2);
    document.getElementById(id).value = dateStr;
}

function bayesianDateLimits(){
    var date = new Date();
    var dateStr = date.getFullYear() + "-" + pad(date.getMonth() + 1, 2) + "-" + pad(date.getDate()-1, 2);
    
    document.getElementById("bayesian_day").setAttribute("max",dateStr);
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
    var table = document.getElementById("dataTable");
    table.innerHTML = "";
    var html = [];//Holds the table that will be created 
    var dates=[];//holds the array of all dates from all parameters 
    html.push("<table><tr><th>TimeStamp</th>");
    //Adds the names to the header of the table 
    for (var i = 0; i < dataResp.data.length; i++) {
        html.push("<th>" + dataResp.data[i]["name"] + "</th>");
    }
    html.push("</tr>");
    //adds one of every date to the <code>dates</code> array
    //This ensures that every date that is used can be accounted for
    //also allows the handling of missing data
    for (var j = 0; j < dataResp.data.length; j++) {
        var d = dataResp.data[j]["data"];
        for(var i=0; i<d.length; i++){
            var ts_val = d[i];
            if(dates.indexOf(ts_val["timestamp"])==-1){
               dates.push(ts_val["timestamp"]);
            }
        }
    }
    //since the dates are stored as epoch miliseconds this make sure the dates
    //are in the correct order
    dates.sort(function(a, b){return a - b});
    //Adds all the values to the <code>html</code> array for the table
    for (var i = 0; i < dates.length; i++) {
        html.push("<tr>");
        html.push("<td>" + new Date(dates[i]).toUTCString() + "</td>");
        for (var j = 0; j < dataResp.data.length; j++) {
            var d=dataResp.data[j]["data"];
            if(i>=d.length){
                html.push("<td> N/A </td>");
                continue;
            }
            var ts_val=d[i];
            if(ts_val["timestamp"]!=dates[i]){
                html.push("<td> N/A </td>");
                    d.splice(i,0,null);
            }
            else
                html.push("<td>" + ts_val["value"] + "</td>");
        }
        html.push("</tr>");
    }
    //setting the innerHTML allows the table to be visible on the page
    var finalHtml = "";
    for (i = 0; i < html.length; i++) {
        var str = html[i];
        console.log(str);
        finalHtml += str;
    }
    console.log(finalHtml);
    table.innerHTML = finalHtml;
}

/**The <code>openPoppup()</code> function simply opens a popped up
 * version of the data table when <code>dataTable</code> is clicked 
 * so that the user can more easily see the data 
 */
function openPopup() {
    var modal = document.getElementById("myModal");
    var span = document.getElementsByClassName("close")[0];
    var table = document.getElementById("dataTable");
    var popup = document.getElementById("popup");


    popup.innerHTML = table.innerHTML;
    modal.style.display = "block";
    span.onclick = function () {
        modal.style.display = "none";
    }
}

/**The <code>exportTable()</function> taakes the innerHTML from the given 
 * <code>tableId</code> paramter and converts it to csv format. Then initiates
 * a download of a csv file
 * @param {type} tableId the id of the table being exported
 */
function exportTable(tableId) {
    var table = document.getElementById(tableId).innerHTML;
    //converts the innerHTML of table into csv format
    var data = table.replace(/<thead>/g, '').replace(/<\/thead>/g, '')
            .replace(/<tbody>/g, '').replace(/<\/tbody>/g, '')
            .replace(/<tr>/g, '').replace(/<\/tr>/g, '\r\n')
            .replace(/,/g, '')
            .replace(/<th>/g, '').replace(/<\/th>/g, ',')
            .replace(/<td>/g, '').replace(/<\/td>/g, ',')
            .replace(/\t/g, '')
            .replace(/\n/g, '');
    //creates a link to initiate a download of the csv formated data in a csv file
    var downloadLink = document.createElement("a");
    downloadLink.download = "tabledata.csv";
    downloadLink.href = "data:application/csv,"+encodeURI(data);
    downloadLink.click();
}

//<code>load</code> makes sure that when the page is newly loaded it will do a
//special action in the <code>fetchDataFunction</code> allowing it to generate
//botht he table and the graph
var load=true;
/**The <code>startingData()</code> function generates both the table and the graph
 * on load/refresh of a page by using the same randomly generated data type
 */
function startingData(){
    current="Graph";
    var graphcheckboxes = document.getElementById("Graph_form").querySelectorAll('input[type="checkbox"]');
    var startingNumber=Math.floor((Math.random() * graphcheckboxes.length));
    graphcheckboxes[startingNumber].click();
    
    var tablecheckboxes = document.getElementById("Table_form").querySelectorAll('input[type="checkbox"]');
    tablecheckboxes[startingNumber+1].checked=true;
    current=getCookie("id");
    if (getCookie("id") == "Table")
        document.getElementById("TableTab").click();
    else{
        if(getCookie("id") == "Bayesian")
            document.getElementById("BayesianTab").click();
        else
            document.getElementById("GraphTab").click();
    }
}
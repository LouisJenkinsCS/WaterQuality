/*
 * Deletion requests are handled here.
 * 
 */

$.getScript("scripts/AJAX_magic.js", function () {});

//del_options will hold the retrieved data names from
//the table ManualDataNames
var del_options = "";

//Defines how AdminServlet responds
var dataRequest = {action: 'getManualItems'};

//Called in admin.jsp to load this script
function deleteTheStuff()
{
    get("AdminServlet", dataRequest, function (response)
    {
        console.log(response);
        console.log("Connection made!" + response);
        var param_names = JSON.parse(response)["data"];
        for (var i = 0; i < param_names.length; i++)
        {
            del_options += '<option>';
            var entry_name = param_names[i];
            console.log(entry_name["name"]);
            del_options += entry_name["name"];
            del_options += '</option>';
        }

        console.log("Parameter names: " + param_names);
        console.log("Entry name: " + entry_name["name"]);

        $('#Delete_Data').append(
                '<div class="large_text">Time Frame:</div>' +
                '<div id="dateInstructDiv">Start Date to End Date (Format: yyyy-mm-ddThh:mm:ss)</div>' +
                '<div id="dateselectordiv" onclick="dateLimits();">' +
                '<input class="dateselector" id="delete_startdate" type="date" min="2016-01-01" max="" value="2017-03-15T08:15:00"> to ' +
                '<input class="dateselector" id="delete_enddate" type="date" min="2016-01-01" max="" value="2017-03-20T08:15:00"></div>' +
                '<div class="large_text">Parameter to delete:</div>' +
                '<select id="delete_param">' + del_options +
                '</select><br/><br/>' +
                '<button type="button" onclick="filterData()">Filter</button><br/><br/>' +
                '<div class="large_text">Please select the data entry from below:</div>' +
                '<table id="deletion_space">' +
                '<tr><th>Date/Time</th><th>Name</th><th>Value</th><th>Author</th></tr>' +
                '</table><br/>' +
                '<button type="button" onclick="deleteData()">Delete</button><br/><br/>'
                );
    });
}

function filterData() {
    
    //To store the string to append to the document
    var htmlstring = "";
    
    //The entered/selected parameters are stored
    var $paramName = $('#delete_param').val();
    var $deleteStart = $('#delete_startdate').val();
    var $deleteEnd = $('#delete_enddate').val();

    var filterRequest = {action: 'getFilteredData',
        parameter: '$paramName',
        startTime: '$deleteStart',
        endTime: '$deleteEnd'};

    /*
     * Sample desired JSON response
     * data: [
     *  {
     *      entryID : "1",
     *      name : "Soluble Reactive Phosphorus",
     *      units : "ug P/L",
     *      submittedBy : "Test User",
     *      time : "2017-03-20T09:15:00",
     *      value : "4.0"
     *  }
     * ]
     */
    post("AdminServlet", filterRequest, function(resp) {
        var data = JSON.parse(resp)["data"];
        for(var i = 0; i < data.length; i++)
        {
            var item = data[i];
            htmlstring += '<tr id = deletion_row class = datadeletion>';
            htmlstring += '<td><input type="text" name="data_time" id="time" value=' + item["time"] + '></td>';
            htmlstring += '<td><input type="text" name="data_name" id="name" value=' + item["name"] + '></td>';
            htmlstring += '<td><input type="text" name="data_value" id="value" value=' + item["value"] + '></td>';
            htmlstring += '<td><input type="text" name="data_author" id="author" value=' + item["submittedBy"] + '></td>';
            htmlstring += '<td><input type="checkbox" name="data_select" id="checkbox" value=' + item["entryID"] + '></td>';
            htmlstring += '</tr>';
            htmlstring += '<br/>';
        }
        
        console.log('Stringified' + JSON.stringify(items));
    });
    
    $('#deletion_space').append(htmlstring);
}

function deleteData() {
    
    //TODO loop through listed entries, pass entryID of each selected
    //entry to a POST that calls the DatabaseManager method manualDeletion
    alert("Beep boop bop it's gonna happen!");
    
    var $idList;// = Array? of IDs
    
    var deleteRequest = {
        action : "RemoveData",
        idList : $idList
    };
    
    //Not much needed in terms of feedback, except a confirmation
    //before firing off the request for sure
    post("AdminServlet", deleteRequest, function(resp) {
        alert("Successful deletion of data");
    });
    
    alert("It happened. Beep beep boop.");
}
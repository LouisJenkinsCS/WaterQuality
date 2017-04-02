/* TODO: Change hardcoded times to the current time / whatever makes sense
 *       Change the filter date format to date and time
 *       Handle loop in deleteData()
 */

//This function simply pulls the AJAX_magic.js script
//to allow the current script to use AJAX functions
$.getScript("scripts/AJAX_magic.js", function () {});
$.getScript("scripts/general.js", function () {});
$.getScript("scripts/datetimepicker.js", function () {});

//del_options will hold the retrieved data names from
//the table ManualDataNames
var del_options = "";

//Defines how AdminServlet responds
var dataRequest = {action: "getManualItems"};

//Called in admin.jsp to load this script
function loadDelete()
{
    //A request to the servlet is made to retrieve all parameter names

    /*
     * GET request: {action : 'getManualItems'} (as seen on line 10)
     * GET response:
     *  
     * data: [
     *  {
     *      name : 'parameter name'
     *  },
     *  {
     *      name : 'parameter name'
     *  },
     *  ...
     * ]
     * 
     */

    get("AdminServlet", dataRequest, function (response)
    {
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
        
        del_options += '<option disabled>----------</option>';
        
        get("AdminServlet", {action: "getSensorItems"}, function (response)
        {
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
        
        

        //console.log("Parameter names: " + param_names);
        //console.log("Entry name: " + entry_name["name"]);
        console.log("del_options" + del_options);
        
        
        var today = new Date();
        var date = (today.getMonth() + 1) + '/' + today.getDate() + '/' + today.getFullYear();
        var time = today.toLocaleTimeString();
        //This contains the bulk of the HTML which will be shown to the user,
        //providing the inputs for the user to fill which will filter the data
        //shown to them, from which they can choose to delete.
        //Uses the global variable del_options to show the user which parameters
        //they may choose from.
        $('#Delete_Data').append(
                '<div class="large_text">Time Frame:</div>' +
                '<div id="dateInstructDiv">Start Date:</div>' +
                '<input  id="delete_startdate" type="text"> ' +
                '<input id="delete_starttime" type="text"></div>' +
                '<div id="dateInstructDiv">End Date:</div>' +
                '<input class="dateselector" id="delete_enddate" type="text"> ' +
                '<input class="dateselector" id="delete_endtime" type="text"></div>' +
                '<div class="large_text">Parameter to delete:</div>' +
                '<select id="delete_param">' + del_options +
                '</select><br/><br/>' +
                '<button type="button" onclick="filterData()">Filter</button><br/><br/>' +
                '<div class="large_text">Please select the data entry from below:</div>' +
                '<table id="delete_table">' +
                '<thead><tr><th>Date-Time</th><th>Name</th><th>Value</th></tr></thead>' +
                '</table><br/>' +
                '<button type="button" onclick="deleteData()">Delete</button><br/><br/>'
                );
        $('#delete_table').DataTable({
            columns: [
                { title: "Date-Time" },
                { title: "Name" },
                { title: "Value" }
            ]
        });

        $( function() {
            var date = new Date();
//            $( "#delete_endtime" ).timepicker();
//            $( "#delete_starttime" ).timepicker();
            $( "#delete_enddate" ).datetimepicker({
                controlType: 'select',
                oneLine: true,
                timeFormat: 'hh:mm tt',
                altField: "#delete_endtime"
            })
            .datepicker("setDate", date);
            
            date.setMonth(date.getMonth() - 1);
            $( "#delete_startdate" ).datetimepicker({
                    controlType: 'select',
                    oneLine: true,
                    timeFormat: 'hh:mm tt',
                    altField: "#delete_starttime"
            })
            .datepicker("setDate", date);
        });
    });
});
}

/**
 * Input declaring the range of time to be filtered from, and which
 * parameter(s) the user wishes to see data from is stored here to be sent
 * in a POST request. After retrieving the data, it is displayed in a
 * table where the user may select individual pieces of data to be deleted.
 */
function filterData() {

    //To store the string to append to the document
    var htmlstring = "";

    //The entered/selected parameters are stored
    var $paramName = $('#delete_param').val();
    var $deleteStartDate = new Date($('#delete_startdate').val()).getTime();
    var $deleteEndDate = new Date($('#delete_enddate').val()).getTime();
    var $deleteStartTime = $('#delete_starttime').val();
    var $deleteEndTime = $('#delete_endtime').val();
    

    var filterRequest = {action: 'getData',
        parameter: $paramName,
        start: $deleteStartDate,
        end: $deleteEndDate,
    };

    /*
     * Dr. Jones has requested that the user be shown the date and time
     * in a format more user-friendly than our LocalDateTime format, so
     * that is reflected in the sample response.
     * 
     * POST request:
     * {
     *  action: 'getFilteredData',
     *  parameter : 'Soluble Reactive Phosphorus',
     *  startDate : '3/19/2017',
     *  endDate : '3/20/2017',
     *  startTime : '08:00',
     *  endTime : '18:00'
     * }
     * 
     * POST response:
     * data: [
     *  {
     *      entryID : '1',
     *      name : 'Soluble Reactive Phosphorus',
     *      submittedBy : 'Test User',
     *      date : '3/20/2017',
     *      time : '08:30'
     *      value : '4.0'
     *  }
     * ]
     * 
     */
    post("AdminServlet", filterRequest, function (resp) {
        if (resp.hasOwnProperty("status")) {
            window.alert("Error Fetching Data from AdminServlet...\nError: \"" + resp.status + "\"");
            return;
        }
        
        var dataTable = $('#delete_table').DataTable();
        
        dataTable.clear();
        
        var data = JSON.parse(resp)["data"];
        var htmlstring = '<thead><tr><th>Date-Time</th><th>Name</th><th>Value</th></tr></thead>';
        for (var i = 0; i < data.length; i++)
        {
            var item = data[i];
            var name = item.name;
            var dataValues = item.dataValues;
            for (var j = 0; j < dataValues.length; j++) {
                item = dataValues[j];
                dataTable.rows.add([ [ formatDate(new Date(item["timestamp"])), name, item["value"]]]);
            }
        }
        
        dataTable.draw();
//        console.log(htmlstring);
//         
//        $('#delete_table').DataTable().destroy(true);
//        $('#delete_table').append(htmlstring);
//        $('#delete_table').DataTable();
       
    });

    
}

/**
 * Upon submission, the user is prompted to confirm their selection. If
 * confirmation is received, the POST request to delete the data is sent.
 * 
 * POST request:
 * {
 *  action: 'RemoveData',
 *  entryIDs : {'3', '4', '6'}
 * }
 * 
 * 
 */
function deleteData() {

    var $idList;// = Array of IDs

    //TODO loop through listed entries, pass $(#entryID) of each selected
    //entry to variable entryIDs

    var deleteRequest = {
        action: "RemoveData",
        entryIDs: $idList
    };

    //TODO confirm with the user that they're sure the selections
    //are correct - on OK, continue to POST request below

    //Not much needed in terms of feedback, except a confirmation
    //before firing off the request for sure
    post("AdminServlet", deleteRequest, function (resp) {
        alert(resp);
    });

}

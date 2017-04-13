//This function simply pulls the AJAX_magic.js script
//to allow the current script to use AJAX functions
$.getScript("scripts/AJAX_magic.js", function () {});
$.getScript("scripts/general.js", function () {});
$.getScript("scripts/datetimepicker.js", function () {});

//del_options will hold the retrieved data names from
//the table ManualDataNames
var del_options = "";
var firstLoad = true;
var listenOK = false;
var epochMs;

//Called in admin.jsp to load this script
function loadDelete()
{


    //A request to the servlet is made to retrieve all parameter names

    /*
     * request: {action : "getParameters", data : 1}
     * response:
     {
     "data" : [
     {
     "mask" : 1,
     "descriptors" : [
     {
     "id" : 001,
     "name" : "DO",
     "description" : "Dissolved oxygen is..."
     },
     {
     "id" : 002,
     "name" : "Water Temperature",
     "description" : "The temperature of the water..."
     }
     ]
     }
     ]
     }
     * 
     */

    //Defines how AdminServlet responds
    var ALL_MASK = 3;
    var parameterRequest = new ParameterRequest(ALL_MASK);
    parameterRequest.action = "getParameters";


    post("AdminServlet", parameterRequest, function (response) {
//        response = {
//            "data": [
//                {
//                    "descriptors": [
//                        {
//                            "id": "001",
//                            "name": "DO",
//                            "description": "Dissolved oxygen is blah blah..."
//                        },
//                        {
//                            "id": "005",
//                            "name": "Water Temperature",
//                            "description": "The temperature of the water..."
//                        }
//                    ],
//                    "mask": 1
//                },
//                {
//                    "descriptors": [
//                        {
//                            "id": "021",
//                            "name": "Algae Cover",
//                            "description": "Amount of algae on a cool lookin' rock..."
//                        },
//                        {
//                            "id": "022",
//                            "name": "Nitrate+Nitrite-Nitrous",
//                            "description": "This is a confusing one..."
//                        }
//                    ],
//                    "mask": 2
//                }
//            ]
//        }

        //console.log("Response from admin_deletion: " + response);
        var resp = new ParameterResponse(response);
        //console.log("resp.data: " + JSON.stringify(resp.data));
        for (var k = 0; k < resp.data.length; k++)
        {
            if (resp.data[k]["mask"] === 1) {
                del_options += '<option disabled selected hidden>Please Choose...</option>';
                del_options += '<option disabled>-----Sensor Parameters-----</option>';
            } else
                del_options += '<option disabled>-----Manual Parameters-----</option>';

            resp.descriptors = resp.data[k]["descriptors"];
            //console.log("resp.descriptors length: " + resp.descriptors.length);
            resp.names = [];

            for (var i = 0; i < resp.descriptors.length; i++) {
                resp.piece = resp.descriptors[i];
                //console.log("resp.piece: " + JSON.stringify(resp.piece));
                resp.names.push(resp.piece["name"]);
                //console.log("resp.names contains:" + JSON.stringify(resp.names))
            }

            (resp.names).forEach(function (item) {
                del_options += '<option>';
                del_options += item;
                del_options += '</option>';
            });
        }

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
                '<br/><br/><div class="large_text">Parameter to delete:</div>' +
                '<select id="delete_param" onchange="filterData(); setListenOK();" selected="-">' + del_options +
                '</select><br/><br/>' +
                //'<button type="button" onclick="filterData()">Filter</button><br/><br/>' +
                '<div class="large_text">Please select the data entry from below:</div>' +
                '<table id="delete_table">' +
                '<thead><tr><th>Date-Time</th><th>Name</th><th>Value</th></tr></thead>' +
                '</table><br/>' +
                '<button type="button" onclick="deleteData()">Delete</button><br/><br/>'
                );

        $('#delete_table').DataTable({
            columns: [
                {title: "Date-Time"},
                {title: "Name"},
                {title: "Value"}
            ],
            "order": [[0, "desc"]]
        });

        createDatePickers();
//        $(function () {
//            var date = new Date();
////            $( "#delete_endtime" ).timepicker();
////            $( "#delete_starttime" ).timepicker();
//            $("#delete_enddate").datetimepicker({
//                controlType: 'select',
//                oneLine: true,
//                altField: "#delete_endtime"
//            })
//                    .datepicker("setDate", date);
//
//            date.setMonth(date.getMonth() - 1);
//            $("#delete_startdate").datetimepicker({
//                controlType: 'select',
//                oneLine: true,
//                altField: "#delete_starttime"
//            })
//                    .datepicker("setDate", date);
//        });

        //console.log("del_options: " + del_options);
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
    var deleteStartDate = new Date($('#delete_startdate').val());

    /*
     * Due to difference in local time and UTC, we need to do some
     * hard-coding changes to the times...
     */
    if (deleteStartDate.dst())
        deleteStartDate = deleteStartDate.getTime() - 14400000;
    else
        deleteStartDate = deleteStartDate.getTime() - 18000000;

    var deleteStartTime = $('#delete_starttime').val();

    var deleteEndDate = new Date($('#delete_enddate').val());
    if (deleteEndDate.dst())
        deleteEndDate = deleteEndDate.getTime() - 14400000;
    else
        deleteEndDate = deleteEndDate.getTime() - 18000000;

    var deleteEndTime = $('#delete_endtime').val();

    var starttime = deleteStartTime.split(':');
    var endtime = deleteEndTime.split(':');

    var startDateTime = new Date(deleteStartDate + starttime[0] * 3600000 + starttime[1] * 60000).getTime();
    var endDateTime = new Date(deleteEndDate + endtime[0] * 3600000 + endtime[1] * 60000).getTime();


    var filterRequest = {
        action: 'getDataDeletion',
        parameter: $paramName,
        start: startDateTime,
        end: endDateTime
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

        // Rows are added to the DataTable in a loop
        var data = JSON.parse(resp)["data"];
        var htmlstring = '<thead><tr><th>Date-Time</th><th>Name</th><th>Value</th></tr></thead>';
        for (var i = 0; i < data.length; i++) {
            var item = data[i];
            var name = item.name;
            var dataValues = item.dataValues;
            for (var j = 0; j < dataValues.length; j++) {
                item = dataValues[j];
                dataTable.rows.add([[formatDateSimple(item["timestamp"]), name, item["value"]]]);
            }
        }


        dataTable.draw();

        var ddr = new DeleteDataRequest();

        // If the DataTable is being generated for the first time
        // and the condition for listenOK is still true,
        // set onClick listener
        if (firstLoad && listenOK) {

            $(document).ready(function () {
                $('#delete_table').DataTable({
                    columnDefs: [{
                            orderable: false,
                            className: 'select-checkbox',
                            targets: 0
                        }],
                    select: {
                        style: 'os',
                        selector: 'td:first-child'
                    },
                    order: [[1, 'asc']]
                });
            });

            $('#delete_table tbody').on('click', 'tr', function () {
                var timestamp = dataTable.row(this).data()[0];
                var paramName = dataTable.row(this).data()[1];

                var dateString = new Date(timestamp);
                epochMs = dateString.getTime();

                //if(!(dataTable.row(this).classList.contains('highlight'))) {       
                //highlight row
                var datarow = dataTable.row(this).index().row;
                console.log("datarow: " + datarow);
                var nodes = $(dataTable.row(datarow).nodes());
                nodes.css("background-color", "gray");
                //clicked = true;
                ddr.queueDeletion(paramName, epochMs);
                //} else {
                //ddr.popDeletion(ts, pn);
                //unhighlight row
                //clicked = false;
                //}

//                console.log("Time: " + timestamp);
//                console.log("In ms: " + epochMs);
//                console.log("Parameter: " + paramName);
                firstLoad = false;

            });
        }
    });
}


/**
 * Upon submission, the user is prompted to confirm their selection. If
 * confirmation is received, the POST request to delete the data is sent.
 * 
 * POST request:
 * {
 *  action: "deleteData"
 {
 "data" : [
 {
 name : "PAR",
 timeRange : [
 {
 start : epoch_milliseconds,
 end : epoch_milliseconds,
 "//If selecting one piece of data, start and end will be the same"
 }]
 }]
 }
 * }
 * 
 * 
 */
function deleteData() {

    var $idList;// = Array of IDs
    var ddr = new DeleteDataRequest();
    var $param = $('#deleteparam').val();
    var epoch = epochMs;

    //TODO loop through listed entries, pass $(#entryID) of each selected
    //entry to variable entryIDs

    var deleteRequest = {
        action: "RemoveData",
        parameter: $param,
        time: epoch
    };

    //ddr.queueDeletion($param, epoch);

    //TODO confirm with the user that they're sure the selections
    //are correct - on OK, continue to POST request

    //Not much needed in terms of feedback, except a confirmation
    //before firing off the request for sure
    post("AdminServlet", deleteRequest, function (resp) {
        alert(resp);
        //Data shown has to be refreshed after deletion occurs
        filterData();
    });
}

/*
 * The DatePickers are initialized here
 */
function createDatePickers() {

    var date = new Date();

    // The default time range is a month before
    // the current date to today's date
    $("#delete_enddate").datetimepicker({
        controlType: 'select',
        oneLine: true,
        altField: "#delete_endtime"
    })
            .datepicker("setDate", date);


    date.setMonth(date.getMonth() - 1);
    $("#delete_startdate").datetimepicker({
        controlType: 'select',
        oneLine: true,
        altField: "#delete_starttime"
    })
            .datepicker("setDate", date);

    changeOnSelect();
}

/*
 * If a new date or time is selected in the DatePicker
 * we want the table to reflect that immediately. This function
 * sets an onSelect listener to call filterData() whenever a
 * new date or time is selected.
 */
function changeOnSelect() {

    $("#delete_enddate").datetimepicker("option", "onSelect", filterData);
    $("#delete_startdate").datetimepicker("option", "onSelect", filterData);
}

/*
 * Global variable ListenOK is set to true here to indicate
 * that a parameter has been selected. If no parameter is selected,
 * the onClick listener should NOT be set.
 */
function setListenOK() {
    listenOK = true;
}
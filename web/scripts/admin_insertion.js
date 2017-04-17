//This function simply pulls the AJAX_magic.js script
//to allow the current script to use AJAX functions
$.getScript("scripts/AJAX_magic.js", function () {});

////insertionid changes with each new field created
//for unique entries
var $insertionid = 0;

//remember the list for the parameter dropdown
var options_params = "";

/**
 * The actions to be performed are defined here. This method is called
 * upon loading the admin.jsp page.
 */
function loadInsert() {
    //A request to the servlet is made to retrieve all parameter names

    /*
     * GET request: {action : 'getManualItems'} (as seen on line 9)
     * GET response:
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

    var ALL_MASK = 3;
    var parameterRequest = new ParameterRequest(ALL_MASK);
    parameterRequest.action = "getParameters";

    get("AdminServlet", parameterRequest, function (response) {
        //console.log("Response from admin_insertion: " + response);
        var resp = new ParameterResponse(response);
        //console.log("resp.data: " + JSON.stringify(resp.data));
        for (var k = 0; k < resp.data.length; k++) {
            if (resp.data[k]["mask"] === 1)
                options_params += '<option disabled=true>-----Sensor Parameters-----</option>';
            else
                options_params += '<option disabled=true>-----Manual Parameters-----</option>';

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
                options_params += '<option>';
                options_params += item;
                options_params += '</option>';
            });
        }
    });

//This creates the browse area, then fires off the function createNewInput,
//then puts a button below for adding more data entry areas
    $('#Input_Data').append(
            '<div class="large_text">Upload .CSV File</div>'
            + '<input type="file" id="csv" value="Browse..."><br/>'
            + '<button type="button" onclick="sendCSV()">Submit</button><br/>'
            + '<h3>Please refer to the example below to format your .csv file '
            + 'appropriately.</h3>'
            + '<img  id="csv_image" border="0" src="images/ExampleInput.JPG">'
            + '<h5>Please do not include units of measurement in '
            + 'your file. Metric units are used, and are displayed on the Dashboard.</h5>'
            )
}


function sendCSV() {

    console.log("Attempted to send");
    var lines;
    var file = $('#csv')[0].files[0];
    var fr = new FileReader();
    fr.readAsText(file);
    fr.onload = loadHandler;

    function loadHandler(event) {
        var csv = event.target.result;
        processData(csv);
    }

    function processData(csv) {

        var allTextLines = csv.split(/\r\n|\n/);
        lines = [];
        while (allTextLines.length) {
            lines.push(allTextLines.shift().split(','));
        }
        //console.log("Lines:" + lines);
        useThese();
    }
    function useThese() {

        var headerArray = lines[0];
        var NUM_OF_FIELDS = headerArray.length;
        var paramList = [];
        var timestamp;
        var idr = new InsertDataRequest();

        for (var i = 2; i < NUM_OF_FIELDS; i++)
        {
            paramList.push(headerArray[i]);
            console.log(paramList);
        }

        for (var i = 1; i < (lines.length - 1); i++) {

            timestamp = convertToEpochMs(lines[i][0], lines[i][1]);

            for (var j = 2; j < paramList.length + 2; j++) {

                if (lines[i][j] !== "") {
                    var idv = new InsertDataValue(timestamp, lines[i][j]);
                    idr.queueInsertion(paramList[j - 2], idv);
                } else {
                    lines[i][j] = Number.NaN;
                }

            }

            var obj = {action: "insertData", data: JSON.stringify(idr.data)};

            //On every 10th iteration, a post is sent, use below..
            //check length of JSON.stringify(idr.data) > 4kb, send
//            if ((i % 10 === 0) && (i !== paramList.length + 1))
            if (JSON.stringify(obj).length > 8 * 1024) {

                post("AdminServlet", {action: "insertData", data: JSON.stringify(idr.data)}, function (resp) {
                    //console.log("idr chunk " + i + ": " + JSON.stringify(idr));
                });

                idr = new InsertDataRequest();
            }
        }

        //If there was any leftover piece it is sent here
        if (idr.data.length !== 0) {

            post("AdminServlet", {action: "insertData", data: JSON.stringify(idr.data)}, function (resp) {
                //console.log("idr leftover " + i + ": " + JSON.stringify(idr));
            });
        }

        alert("Your insertion was successful!");
    }

    function convertToEpochMs(date, time) {

        var dateline = "";
        var epochMs;

        dateline += "" + date + " " + time;
        var d = new Date(dateline);
        epochMs = d.getTime();

        return epochMs;
    }

}

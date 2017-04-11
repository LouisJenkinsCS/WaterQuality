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
function loadInsert()
{
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

    get("AdminServlet", parameterRequest, function (response)
    {
        //console.log("Response from admin_insertion: " + response);
        var resp = new ParameterResponse(response);
        //console.log("resp.data: " + JSON.stringify(resp.data));
        for (var k = 0; k < resp.data.length; k++)
        {
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
        //createNewInput();//get() is non-blocking, so moving createNewInput()
        //outside of this block {} of code will cause it to display before
        //options_params has been initialilzed.
    });

//This creates the browse area, then fires off the function createNewInput,
//then puts a button below for adding more data entry areas
    $('#Input_Data').append(
            '<div class="large_text">Upload .CSV File</div>'
            + '<input type="file" id="csv" value="Browse..."><br/>'
            + '<button type="button" onclick="sendCSV()">Submit</button><br/>'
            + '<br>'
            + '<div class="large_text">Enter Data Manually</div>'
            + '<table id="input_space">'
            + '<tr><th>Date</th><th>Time Collected</th><th>Parameter</th><th>Value</th></tr>'
            + '</table>'
            + '<button type="button" onclick="createNewInput()">+</button>'
            + '<button type="button" onclick="removeLastInput()">x</button>'
            + '<button type="button" onclick="submitInput()">Submit</button>\n');

}
;

/**
 * Creates a row of input fields for the user to enter data into,
 * and uses the global variable options_params for the parameter name selection.
 * $insertionid stays unique and also allows the number of entries to be monitored.
 */
function createNewInput()
{
    var today = new Date();
    var date = (today.getMonth() + 1) + '/' + today.getDate() + '/' + today.getFullYear();
    var time = today.toLocaleTimeString();

    $('#input_space').append(
            '  <tr data-insertion_id=' + $insertionid++ + ' class=datainsertion>'
            + '     <td><input type="date" name="data_date" id="date" placeholder="' + date + '"></td>'
            + '     <td><input type="time" name="data_time" id="time" placeholder="' + time + '"></td>'
            + '     <td><select id="select_param">' + options_params + '</select></td>'
            + '     <td><input type="text" name="value" id="value" placeholder="Do not include units"></td>'
            + '  </tr>'
            );
}
;

/**
 * If there is more than one row of information, the function will
 * fadeout the last entry, and then remove it from the table.
 */
function removeLastInput()
{
    if ((document.getElementById("input_space")).rows.length > 2)
    {
        //fadeOut is more visually pleasing when removing items
        $('#input_space tr:last').fadeOut(200, function () {
            $(this).remove();
        });
        $insertionid--;
    }
}
;

/**
 * POST request: 
 * {
 *  action : 'InputData',
 *  dataName : 'Temperature'
 *  time : '1490525115000' <-- epoch milliseconds
 *  value : '13.0'
 * }
 * 
 * POST response:
 *  Because this post is to insert, no response is necessary. If
 *  the post is unsuccessful, the anonymous callback method within
 *  the post function is not called. The post() definition in AJAX_magic
 *  handles unsuccessful cases.
 * 
 */
function submitInput()
{
    //jQuery function .each executes the following anonymous function
    //for each selection
    $('#input_space .datainsertion').each(function () {

        var time = "";

        var inputRequest = {
            action: 'InputData',
            dataName: '',
            time: '',
            value: ''
        };

        var $dataName = $(this).find("select").val();
        inputRequest['dataName'] = $dataName;

        //Stores each input element found in an array, to be traversed below
        var $items = $(this).find("input");

        //$items is iterated through, storing the appropriate information in order
        $.each($items, function () {
            //console.log($(this).attr("name"));
            //console.log("value: " + $(this).val());
            var name = $(this).attr("name");

            if (name === "data_date")
                time = $(this).val();
            else if (name === "data_time")
                time += ' ' + $(this).val();
            else
                inputRequest['value'] = $(this).val();

        });

        var date = new Date(time);
        var ms = date.getTime();

        inputRequest['time'] = ms;

        post("AdminServlet", inputRequest, function (resp) {
            //console.log("Entry: " + JSON.stringify(inputRequest));
            //console.log("inputStatus: " + inputRequest['inputStatus']);
        });
    });
}
;

function sendCSV()
{
    console.log("Attempted to send");
    var lines;
    var file = $('#csv')[0].files[0];
    var fr = new FileReader();
    fr.readAsText(file);
    fr.onload = loadHandler;

    function loadHandler(event)
    {
        var csv = event.target.result;
        processData(csv);
    }

    function processData(csv)
    {
        var allTextLines = csv.split(/\r\n|\n/);
        lines = [];
        while (allTextLines.length) {
            lines.push(allTextLines.shift().split(','));
        }
        console.log("Lines:" + lines);
        useThese();
    }
    function useThese()
    {
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

        for (var i = 1; i < (lines.length - 1); i++)
        {
            timestamp = convertToEpochMs(lines[i][0], lines[i][1]);

            for (var j = 2; j < paramList.length + 2; j++)
            {
                if (lines[i][j] !== "")
                {
                    var idv = new InsertDataValue(timestamp, lines[i][j]);
                    //console.log("Lines["+i+"]["+j+"]: " + lines[i][j]);
                    idr.queueInsertion(paramList[j - 2], idv);
                }
                else
                {
                    lines[i][j] = Number.NaN;
                    console.log("NaN?" + lines[i][j]);
                }
                
            }

            var obj = {action: "insertData", data: JSON.stringify(idr.data)};

            //On every 10th iteration, a post is sent, use below..
            //check length of JSON.stringify(idr.data) > 4kb, send
//            if ((i % 10 === 0) && (i !== paramList.length + 1))
            if (JSON.stringify(obj).length > 8 * 1024)
            {
                //console.log(idr);

                post("AdminServlet", {action: "insertData", data: JSON.stringify(idr.data)}, function (resp) {
                    console.log("idr chunk " + i + ": " + JSON.stringify(idr));
                });
                idr = new InsertDataRequest();
            }
        }

        //If there was any leftover piece it is sent here
        if (idr.data.length !== 0)
        {
            console.log(idr);
            post("AdminServlet", {action: "insertData", data: JSON.stringify(idr.data)}, function (resp) {
                console.log("idr leftover " + i + ": " + JSON.stringify(idr));
            });
        }
        
        alert("Your insertion was successful!");

    }
    function convertToEpochMs(date, time)
    {
        var dateline = "";
        var epochMs;

        dateline += "" + date + " " + time;
        var d = new Date(dateline);
        epochMs = d.getTime();

        return epochMs;
    }

}

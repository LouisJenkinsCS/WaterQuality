//This function simply pulls the AJAX_magic.js script
//to allow the current script to use AJAX functions
$.getScript("scripts/AJAX_magic.js", function () {});

////insertionid changes with each new field created
//for unique entries
var $insertionid = 0;

//remember the list for the parameter dropdown
var options_params = "";

//defines the request action to be read by AdminServlet
var getRequest = {action: 'getManualItems'};

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

    get("AdminServlet", getRequest, function (response)
    {
        //console.log(response);
        //console.log("Connection made!" + response);
        var parameter_names = JSON.parse(response)["data"];
        for (var i = 0; i < parameter_names.length; i++)
        {
            options_params += '<option>';
            var item = parameter_names[i];
            //console.log(item["name"]);
            options_params += item["name"];
            options_params += '</option>';
        }

        //console.log("Parameter names: " + parameter_names);
        //console.log("Item: " + item["name"]);

        //console.log("Parameter names: " + options_params);

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
        console.log(lines);
    }  
    
    var sendRequest = {action: 'insertCSV', csvfile: lines};

    post("AdminServlet", sendRequest, function (resp) {
        alert("Posted successfully");
    });
}

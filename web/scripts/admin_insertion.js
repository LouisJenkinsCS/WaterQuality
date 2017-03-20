//insertionid changes with each new field created
//for unique entries
var $insertionid = 0;

//remember the list for the parameter dropdown
var options_params = "";

//Sample of successful data format for input into the database - tested and approved
var inputData = {action: 'getManualItems'};

//This function simply pulls the AJAX_magic.js script
//to allow the current script to use AJAX functions
$.getScript("scripts/AJAX_magic.js", function () {});

function doTheThing()
{
    //This will hold the array of names? Maybe?
    get("AdminServlet", inputData, function (response)
    {
        console.log(response);
        console.log("Connection made!" + response);
        var thing = JSON.parse(response)["data"];
        for (var i = 0; i < thing.length; i++)
        {
            options_params += '<option>';
            var subthing = thing[i];
            console.log(subthing["name"]);
            options_params += subthing["name"];
            options_params += '</option>';
        }

        console.log("Thing: " + thing);
        console.log("Subthing: " + subthing["name"]);

        createNewInput();//get() is non-blocking, so moving createNewInput()
        //outside of this block {} of code will cause it to display before
        //options_params has been initialilzed.
    });

//This creates the browse area, then fires off the function createNewInput,
//then puts a button below for adding more data entry areas
    $('#Input_Data').append(
            '<div class="large_text">Upload .CSV File</div>'
            + '<input type="file" value="Browse..."><br/>'
            + '<input type="submit" value="Submit"><br/>'
            + '<br>'
            + '<div class="large_text">Enter Data Manually</div>'
            + '<table id="input_space">'
            + '<tr><th>Date</th><th>Time</th><th>Parameter</th><th>Value</th></tr>'
            + '</table>'
            + '<button type="button" onclick="createNewInput()">+</button>'
            + '<button type="button" onclick="removeLastInput()">x</button>'
            + '<button type="button" onclick="submitInput()">Submit</button>\n');
}
;

/**
 * relies on global variable options_params.
 */
function createNewInput()
{
    $('#input_space').append(
            '  <tr data-insertion_id=' + $insertionid++ + ' class=datainsertion>'
            + '     <td><input type="date" name="data_date" id = "date" value=2007-12-03></td>'
            + '     <td><input type="time" name="data_time" id = "time" value=10:15:30></td>'
            + '     <td><select id="select_param">' + options_params + '</select></td>'
            + '     <td><input type="text" name="value" id = "value" value=3.0></td>'
            + '  </tr>'
            );
}
;

function removeLastInput()
{
    if ((document.getElementById("input_space")).rows.length > 2)
    {
        //Gettin fancy, fadeout looks nicer :thumbsup:
        $('#input_space tr:last').fadeOut(500, function(){$(this).remove();});
        $insertionid--;
    }
}
;

function submitInput()
{
    $('#input_space .datainsertion').each(function () {

        var inputData = {action: 'InputData', dataName: 'Temperature',
            units: 'C', time: '2007-12-03T10:15:30',
            value: '13.0', delta: '2.0', id: '126',
            inputStatus: ''};

        var $dataName = $(this).find("select").val();
        inputData['dataName'] = $dataName;

        var $items = $(this).find("input");

        $.each($items, function () {
            //console.log($(this).attr("name"));
            //console.log("value: " + $(this).val());
            var name = $(this).attr("name");

            if (name == "data_date")
                inputData['time'] = $(this).val();
            else if (name == "data_time")
                inputData['time'] += 'T' + $(this).val();
            else
                inputData['value'] = $(this).val();

        });
        console.log("Entry: " + JSON.stringify(inputData));
        post("AdminServlet", inputData, function (resp) {
            console.log("Entry: " + JSON.stringify(inputData));
            
        });

    });

    alert("(" + ($insertionid) + ") Successful data entry/entries!");
}
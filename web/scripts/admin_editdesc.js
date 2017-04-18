/*
 * Administrators may want to edit the description of a parameter which is
 * shown to the public on the dashboard, or even change the name of that
 * parameter. The functionality to do so is defined here.
 */

var edit_options = "";
var cached_names = [];
var cached_ids = [];
var cached_Descriptions = [];
var saved_id;
var saved_index;

/*
 * The HTML displayed to the user is built here. The data parameters are
 * put into a drop-down selection, and on selection of a parameter the
 * fields associated will change to display the current name and description
 * of that option.
 */
function fillPageEditParams() {
    // Using a mask of 3 indicates the we want sensor and manually inserted
    // parameter names. For future use, 1 is sensor data and 2 is manual data.
    var ALL_MASK = 3;
    var parametersRequest = new ParameterRequest(ALL_MASK);
    parametersRequest.action = "getParameters";

    /*
     * The JSON-formatted response is parsed appropriately, and the
     * drop-down options are created.
     */
    post("AdminServlet", parametersRequest, function (response) {
        var resp = new ParameterResponse(response);

        for (var k = 0; k < resp.data.length; k++) {
            if (resp.data[k]["mask"] === 1) {
                edit_options += '<option disabled selected hidden>Please Choose...</option>';
                edit_options += '<option disabled>-----Sensor Parameters-----</option>';
            } else
                edit_options += '<option disabled>-----Manual Parameters-----</option>';

            resp.descriptors = resp.data[k]["descriptors"];
            resp.names = [];

            for (var i = 0; i < resp.descriptors.length; i++) {
                resp.piece = resp.descriptors[i];
                resp.names.push(resp.piece["name"]);
                cached_ids.push(resp.piece["id"]);
                cached_names.push(resp.piece["name"]);
                cached_Descriptions.push(resp.piece["description"]);
            }

            (resp.names).forEach(function (item) {
                edit_options += '<option>';
                edit_options += item;
                edit_options += '</option>';
            });
        }

        var sample_desc = "Retrieved description goes here.";
        $('#Edit_Params').append(
                '<section class="section_edit_desc">' +
                '<div class="large_text">Parameter to Edit:</div>' +
                '<select id="edit_param" onchange="viewDescription()">' + edit_options +
                '</select><br/><br/>' +
                '<button type="button" onclick="editDesc()">Submit Changes</button><br/><br/>' +
                '</section>' +
                '<section class="section_edit_desc">' +
                '<div class=large_text>Name</div>' +
                '<input type="text" id="paramchange" size="30" value="Name will go here"><br/><br/>' +
                '<div class=large_text>Description</div>' +
                '<textarea name="desc" id="textarea_desc" form="form_edit_desc">' +
                sample_desc +
                '</textarea><br><br>' +
                '</section>'
                );
    });
}

/*
 * The associated description is pulled from the cached list of
 * descriptions. The displayed name is changed to reflect the selection, as well.
 */
function viewDescription() {
    var $paramName = $('#edit_param').val();

    for (var i = 0; i < cached_names.length; i++) {
        if (cached_names[i] === $paramName) {
            document.getElementById("textarea_desc").value = cached_Descriptions[i];
            document.getElementById("paramchange").value = cached_names[i];
            saved_id = cached_ids[i];
            saved_name = cached_names[i];
            saved_index = i;
            break;
        }
    }
}

/*
 * When the user submits their change, a request is formed
 * and sent to the servlet using the fields' current values.
 */
function editDesc() {
    cached_Descriptions[saved_index] = $('#textarea_desc').val();
    var newName = $('#paramchange').val();
    var conflictFlag = 0;

    // If the new name desired already exists, a flag is raised..
    for (var i = 0; i < cached_names.length; i++)
        if (cached_names[i] === newName && i !== saved_index)
            conflictFlag = 1;

    // ..and if that flag is not raised, the new name is saved..
    if (conflictFlag !== 1)
        cached_names[saved_index] = newName;

    // ..and passed along into the request.
    var editRequest = {action: 'editParamDesc',
        desc_id: saved_id,
        desc: $('#textarea_desc').val(),
        name: cached_names[saved_index]
    };

    post("AdminServlet", editRequest, function (resp) {
        var respData = JSON.parse(resp);
        if (respData["status"] === "Success") {
            window.alert("Description update successful. The page will now reload.");
            location.reload();
        } else {
            window.alert("Description Update Failed");
            alert(editRequest.editDescStatus);
        }
    });
}


$.getScript("scripts/AJAX_magic.js", function () {});
$.getScript("scripts/general.js", function () {});
$.getScript("scripts/protocol.js", function () {});

var edit_options = "";
var cached_names = [];
var cached_ids = [];
var cached_Descriptions = [];
var saved_id;
var saved_index;

function fillPageEditParams()
{
    var ALL_MASK = 3;
    var parametersRequest = new ParameterRequest(ALL_MASK);
    parametersRequest.action = "getParameters";
    var sample_desc = "Retrieved description goes here.";
    var param_change = "Parameter name will go here.";

    post("AdminServlet", parametersRequest, function (response)
    {
        var resp = new ParameterResponse(response);

        for (var k = 0; k < resp.data.length; k++)
        {
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

function viewDescription()
{
    var $paramName = $('#edit_param').val();
    for (var i = 0; i < cached_names.length; i++)
    {
        if (cached_names[i] === $paramName)
        {
            document.getElementById("textarea_desc").value = cached_Descriptions[i];
            document.getElementById("paramchange").value = cached_names[i];
            saved_id = cached_ids[i];
            saved_name = cached_names[i];
            saved_index = i;
            break;
        }
    }

}

function editDesc()
{
    var editRequest = {action: 'editParamDesc',
        desc_id: saved_id,
        desc: $('#textarea_desc').val(),
        name: $('#paramchange').val()
    };
    cached_Descriptions[saved_index] = $('#textarea_desc').val();
    cached_names[saved_index] = $('#paramchange').val();

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
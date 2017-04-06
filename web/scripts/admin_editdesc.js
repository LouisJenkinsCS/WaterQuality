

$.getScript("scripts/AJAX_magic.js", function () {});
$.getScript("scripts/general.js", function () {});
$.getScript("scripts/protocol.js", function () {});

var edit_options = "";
var cached_names = [];
var cached_ids = [];
var cached_Descriptions = [];
var saved_id;
var saved_index;

function fillPageEditDesc()
{
    var ALL_MASK = 3;
    var parametersRequest = new ParameterRequest(ALL_MASK);
    parametersRequest.action = "getParameters";
    var sample_desc = "Retrieved description goes here.";

    post("AdminServlet", parametersRequest, function (response)
    {
        var resp = new ParameterResponse(response);

        for (var k = 0; k < resp.data.length; k++)
        {
            if (resp.data[k]["mask"] === 1)
                edit_options += '<option disabled=true>-----Sensor Parameters-----</option>';
            else
                edit_options += '<option disabled=true>-----Manual Parameters-----</option>';

            resp.descriptors = resp.data[k]["descriptors"];

            for (var i = 0; i < resp.descriptors.length; i++) {
                resp.piece = resp.descriptors[i];
                cached_ids.push(resp.piece["id"]);
                cached_names.push(resp.piece["name"]);
                cached_Descriptions.push(resp.piece["description"]);
            }

            (cached_names).forEach(function (item) {
                edit_options += '<option>';
                edit_options += item;
                edit_options += '</option>';
            });
        }

        $('#Edit_Description').append(
                '<section class="section_edit_desc">' +
                '<div class="large_text">Parameter to Edit:</div>' +
                '<select id="edit_param" onchange="viewDescription()">' + edit_options +
                '</select><br/><br/>' +
                '<button type="button" onclick="editDesc()">Submit Changes</button><br/><br/>' +
                '</section>' +
                '<section class="section_edit_desc">' +
                '<div class=large_text>Description</div><br>' +
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
            saved_id = cached_ids[i];
            saved_index = i;
            break;
        }
    }

}

function editDesc()
{
    var editRequest = {action: 'editParamDesc',
        desc_id: saved_id,
        desc: $('#textarea_desc').val()
    };
    cached_Descriptions[saved_index] = $('#textarea_desc').val();
    
    post("AdminServlet", editRequest, function (resp) {
        var respData = JSON.parse(resp);
        if(respData["status"] === "Success")
            window.alert("Description Update Successful");
        else
            window.alert("Description Update Failed");
    });
}

/*


var SERVLET = "AdminServlet";
var PARAMS_SENSOR=1;
var PARAMS_MANUAL=2;
var PARAMS_BOTH=3;

function fillPageEditDesc(){
    var default_desc="Select a parameter from the left to edit its description.";
    var default_params='<option>[no parameters loaded]</option>';
    $('#Edit_Description').append(
            '<form method="POST" id="form_edit_desc"></form>'
            //left side
            +'<section class="section_edit_desc">'
            +'  <div class=large_text>Parameter List</div><br>'
            +'  <select id="select_param_list">'
                +default_params
            +'  </select>'
            +'</section>'
            //right side
            +'<section class="section_edit_desc">'
            +'  <div class=large_text>Description</div><br>'
            +'  <textarea id="textarea_desc">'
                +default_desc
            +'  </textarea><br><br>'
            +'  <input type="submit" value="Save Changes" onclick="requestEditDesc">'
            +'</section>'
    );
    var selectParamList = document.getElementById("select_param_list");
    selectParamList.multiple="multiple";
};

function requestParamList(){
    var reqObj={
        "action" : "getParameters",
        "data" : PARAMS_BOTH
    };
    post(SERVLET, reqObj, responseParamList);
}

function responseParamList(response){
    console.log(response.data);
    var selectParamList = document.getElementById("select_param_list");
    for(var i=0; i<response.data.length; ++i){
        var option=document.createElement("option");
        option.value=response.data[i].name;
        option.text=response.data[i].name;
        selectParamList.add(option);
    }
}

function requestParamDesc(){
    var selectParamList = document.getElementById("select_param_list");
    var paramName = selectParamList.options[selectParamList.selectedIndex].value;
    var reqObj={
        "action" : "getParamDesc",
        "data":[
            {
                "name" : paramName
            }
        ]
    };
    post(SERVLET, reqObj, responseParamDesc);
}

function responseParamDesc(response){
    console.log(response.status);
    document.getElementById("textarea_desc").textContent=response.data.desc;
}

function requestEditDesc(){
    var selectParamList = document.getElementById("select_param_list");
    var paramName = selectParamList.options[selectParamList.selectedIndex].value;
    var textAreaDesc = document.getElementById("textarea_desc");
    var newDesc=textAreaDesc.textContent;
    reqObj={
        "action" : "editParamDesc",
        "data" : [
            {
                "name" : paramName,
                "desc" : newDesc
            }
        ]
    };
    post(SERVLET, reqObj, responseEditDesc);
}

function responseEditDesc(response){
    console.log(response.status);
    alert("The description has been successfully updated.");
}
*/

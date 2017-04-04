/*things to add:
    add a pop-up warning when changing params with unsaved text edits.
    add a confirmation once the server successfully changes the desc.
 */

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
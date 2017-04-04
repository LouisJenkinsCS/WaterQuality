/*
 Things to add:
 add a pop-up that confirms who is getting removed.
 add a confirmation once the server successfully removes the users.
 */

//This function simply pulls the AJAX_magic.js script
//to allow the current script to use AJAX functions
$.getScript("scripts/AJAX_magic.js", function () {});

var SERVLET="AdminServlet";
var ACTION_GET_USERS='getUserList';
var ACTION_REMOVE_USERS='removeUsers';
var PAD_USERID=20;
var PAD_BETWEEN=2;
var PAD_ROLE=13;
var PAD_DATE=8;
var OPTION_LENGTH=PAD_USERID+PAD_BETWEEN*3+PAD_ROLE+PAD_DATE;
var PADDING = Array(OPTION_LENGTH).join(' ');//warning: assumes that Array is initially null/undefined (blank) in each position.
var arrSelectedUsernames = new Array();
var default_options='<option>[no users loaded]</option>';

function fillPageRemoveUser() {

    //maybe we could also display their role and date of creation.
    //this will have to be a formatted string with a set number of characters 
    //for each field so they appear to be in columns.
    
    //<form> tag might be removed when implementing AJAX (unsure).
    $('#Remove_User').append(
            '<div class=large_text>Select Users to Remove</div><br>'
            + '<select id="select_user_list">'
                +default_options
            +'</select><br><br>'
            + '<input type="submit" id="input_submit_remove_users" value="Remove Users" onclick="requestRemoveUsers">'
            );
    var userList = document.getElementById("select_user_list");
    userList.multiple = "multiple";
    //userList.size=15;
    
    requestGetUserList();//call to server
    
    document.getElementById('input_submit_remove_users').addEventListener("click", requestRemoveUsers, false);
}

//also updates select_user_list options upon receiving response.
function requestGetUserList(){
    var reqObj={action: ACTION_GET_USERS};
    post(SERVLET, reqObj, responseGetUserList);
}
function responseGetUserList(response) {
    //might first have to do response=JSON.parse(response);
    console.log(response.status);
    response=getDummyDataUserList();
    console.log(response.status);
    if(response.status[0].errorCode!=0) return;//error
    console.log("User List Received:\n"+response.userList+"\n");
    for(var i=0; i<response.userList.length; ++i){
        var option=document.createElement("option");
        option.value=response.userList[i].username;
        var option_text=
            padRight(PADDING,response.userList[i].username, PAD_USERID)
            +PAD_BETWEEN
            +padRight(PADDING,response.userList[i].role, PAD_ROLE)
            +PAD_BETWEEN
            +padRight(PADDING,response.userList[i].dateJoined, PAD_DATE)
            +PAD_BETWEEN;
        option.text=option_text;
        document.getElementById("select_user_list").add(option);
    }
}
function requestRemoveUsers(){
    //put selected usernames into an request object
    var reqObj={
	"action" : ACTION_REMOVE_USERS,
	"usersSelected" : []//to be filled dynamically below
    };
    var allOptions=document.getElementById("select_user_list").options;
    arrSelectedUsernames=new Array();
    allOptions.forEach(function(option) {
        if(option.selected){
            arrSelectedUsernames.push(option.value);
            reqObj.usersSelected.push({"userID":option.value});
        }
        //could use reqObj["usersSelected"].push() instead.
    });
    post(SERVLET, reqObj, responseRemoveUsers);//when to use post vs. get?
}

function responseRemoveUsers(response) {
    //might first have to do response=JSON.parse(response);
    console.log(response.status);
    getDummyDataRemoveUsers();
    console.log(response.status);
    if(response.status[0].errorCode!=0) return;//error
    alert("The selected users have been successfully removed.");
    requestGetUserList();//update the list.
};

/**
 * helper function for testing.
 * @returns {respObj} A object containing a sample response.
 */
function getDummyDataUserList(){
    var respObj={
        "status" :[
          {
            "errorCode" : "0",
            "errorMsg" : "[THIS IS A LOCAL DUMMY] User list received successfully."
          }
        ],
        "userList" : [
          {
            "username" : "User9403",
            "role" : "Administrator",
                "dateJoined" : "01/01/2017"
          },
          {
            "username" : "User0822",
            "role" : "Administrator",
                "dateJoined" : "01/02/2017"
          }
        ]
    };
    return respObj;
}
function getDummyDataRemoveUsers(){
    var respObj={"status" : [
        {
          "errorCode" : "0",
          "errorMsg" : "[LOCAL DUMMY DATA] Selected users were successfully removed."//this message could be client-side only.
        }
      ]
    };
    return respObj;
}

/**
 * A helper function for aligning columns.
 * Adds padding to the right of str with characters from pStr for a total length
 *  of len.
 * @param {string} pStr A string of filler-characters to use as padding.
 * @param {string} str The original non-padded string to be padded.
 * @param {int} len The width in units of characters.
 * @returns {string} The resulting padded string.
 */
function padRight(pStr, str, len){
    return (str+pStr).slice(0, len);
}

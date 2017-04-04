
var errors="No errors have been loaded from the server.";

function fillPageErrors(){
    $('#Errors').append(
             '  <div class=large_text>Error History</div>'
            +'  <textarea readonly name="errors" id="textarea_errors">'
                +errors
            +'  </textarea>'
    );
};


var errors="No errors have been loaded from the server.";

function fillPageErrors(){
    //<form> tag might be removed when implementing AJAX (unsure).
    $('#Errors').append(
             '  <div class=large_text>Error History</div>'
            +'  <textarea readonly name="errors" id="textarea_errors">'
                +errors
            +'  </textarea>'
    );
};

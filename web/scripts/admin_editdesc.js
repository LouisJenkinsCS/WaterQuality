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

        var sample_desc = "# Markdown Syntax Demo\n" +
                "---\n" +
                "# h1 Heading\n" +
                "## h2 Heading\n" +
                "### h3 Heading\n" +
                "#### h4 Heading\n" +
                "##### h5 Heading\n" +
                "###### h6 Heading\n" +
                "\n" +
                "\n" +
                "## Horizontal Rules\n" +
                "\n" +
                "___\n" +
                "\n" +
                "---\n" +
                "\n" +
                "***\n" +
                "\n" +
                "\n" +
                "## Emphasis\n" +
                "\n" +
                "**This is bold text**\n" +
                "\n" +
                "__This is bold text__\n" +
                "\n" +
                "*This is italic text*\n" +
                "\n" +
                "_This is italic text_\n" +
                "\n" +
                "~~Strikethrough~~\n" +
                "\n" +
                "\n" +
                "## Blockquotes\n" +
                "\n" +
                "\n" +
                "> Blockquotes can also be nested...\n" +
                ">> ...by using additional greater-than signs right next to each other...\n" +
                "> > > ...or with spaces between arrows.\n" +
                "\n" +
                "\n" +
                "## Lists\n" +
                "\n" +
                "Unordered\n" +
                "\n" +
                "+ Create a list by starting a line with `+`, `-`, or `*`\n" +
                "+ Sub-lists are made by indenting 2 spaces:\n" +
                "  - Marker character change forces new list start:\n" +
                "    * Ac tristique libero volutpat at\n" +
                "    + Facilisis in pretium nisl aliquet\n" +
                "    - Nulla volutpat aliquam velit\n" +
                "+ Very easy!\n" +
                "\n" +
                "Ordered\n" +
                "\n" +
                "1. Lorem ipsum dolor sit amet\n" +
                "2. Consectetur adipiscing elit\n" +
                "3. Integer molestie lorem at massa\n" +
                "\n" +
                "\n" +
                "1. You can use sequential numbers...\n" +
                "1. ...or keep all the numbers as `1.`\n" +
                "\n" +
                "Start numbering with offset:\n" +
                "\n" +
                "57. foo\n" +
                "1. bar\n" +
                "\n" +
                "\n" +
                "## Code\n" +
                "\n" +
                "Inline `code`\n" +
                "\n" +
                "Indented code\n" +
                "\n" +
                "    // Some comments\n" +
                "    line 1 of code\n" +
                "    line 2 of code\n" +
                "    line 3 of code\n" +
                "\n" +
                "\n" +
                "Block code \"fences\"\n" +
                "\n" +
                "```\n" +
                "Sample text here...\n" +
                "```\n" +
                "\n" +
                "Syntax highlighting\n" +
                "\n" +
                "``` js\n" +
                "var foo = function (bar) {\n" +
                "  return bar++;\n" +
                "};\n" +
                "\n" +
                "console.log(foo(5));\n" +
                "```\n" +
                "\n" +
                "## Tables\n" +
                "\n" +
                "| Option | Description |\n" +
                "| ------ | ----------- |\n" +
                "| data   | path to data files to supply the data that will be passed into templates. |\n" +
                "| engine | engine to be used for processing templates. Handlebars is the default. |\n" +
                "| ext    | extension to be used for dest files. |\n" +
                "\n" +
                "Right aligned columns\n" +
                "\n" +
                "| Option | Description |\n" +
                "| ------:| -----------:|\n" +
                "| data   | path to data files to supply the data that will be passed into templates. |\n" +
                "| engine | engine to be used for processing templates. Handlebars is the default. |\n" +
                "| ext    | extension to be used for dest files. |\n" +
                "\n" +
                "\n" +
                "## Links\n" +
                "\n" +
                "[link text](http://dev.nodeca.com)\n" +
                "\n" +
                "[link with title](http://nodeca.github.io/pica/demo/ \"title text!\")\n" +
                "\n" +
                "Autoconverted link https://github.com/nodeca/pica (enable linkify to see)\n" +
                "\n" +
                "\n" +
                "## Images\n" +
                "\n" +
                "![Minion](https://octodex.github.com/images/minion.png)\n" +
                "![Stormtroopocat](https://octodex.github.com/images/stormtroopocat.jpg \"The Stormtroopocat\")\n" +
                "\n" +
                "Like links, Images also have a footnote style syntax\n" +
                "\n" +
                "![Alt text][id]\n" +
                "\n" +
                "With a reference later in the document defining the URL location:\n" +
                "\n" +
                "[id]: https://octodex.github.com/images/dojocat.jpg  \"The Dojocat\"\n" +
                "\n" +
                "\n";
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
                '</textarea>' +
                '<button type="button" onclick="showPreview()">Preview</button>' +
                '<br><br>' +
                '</section>' +
                '<section style="width: 95%;" class="section_edit_desc">' +
                 '<div style="height:100%; width: 100%; border: 1px solid black;" class="modal" name="descr_preview" id="descr_modal_preview">' +
                '<div id="descr_preview" style="min-width: 49.5%; position: absolute; top: 1%; left: 0%; resize: none; width: 49.5%;">'+
                '<textarea style="resize:none; width:100%;" name="notes" id="textarea_descr_preview">' +
                '</textarea>'+
                '</div>'+
                '<div style="background-color:white; position: absolute; left: 50.5%; top: 1%; min-width: 49.5%; max-width: 49.5%; border: 1px solid black;"'+
                'name="notes_preview" class="markdown-preview" data-use-github-style id="descr_textarea_preview">' +
                '</div><br><br>' +
                '</section>'
                );
        
        
    });
}

function closeOnOutsideClick_descr(e) {
    var container = $("#descr_modal_preview");

    if (!container.is(e.target) // if the target of the click isn't the container...
        && container.has(e.target).length === 0) // ... nor a descendant of the container
    {
        $(document.getElementById('textarea_desc')).val($(document.getElementById('textarea_descr_preview')).val());
        container.hide("slow");
        $(document).unbind('mouseup', closeOnOutsideClick_descr);
    }
}

function showPreview() {
    $(document.getElementById('textarea_descr_preview')).val($(document.getElementById('textarea_desc')).val());
    $(document.getElementById('descr_textarea_preview')).html(marked($(document.getElementById('textarea_desc')).val()));
    $(document).mouseup(closeOnOutsideClick_descr);
    
    $(document.getElementById("descr_modal_preview")).show("slow", () => {
        var preview = $(document.getElementById('descr_textarea_preview'));
        var textarea = $(document.getElementById('textarea_descr_preview'));
        var previewPaddTop = parseInt(preview.css('padding-top').substring(0, 2));
        var textareaPaddTop = parseInt(textarea.css('padding-top').substring(0, 2));
        var previewPaddBott = parseInt(preview.css('padding-bottom').substring(0, 2));
        var textareaPaddBott = parseInt(textarea.css('padding-bottom').substring(0, 2))
        var previewPadding = previewPaddTop + previewPaddBott;
        var textareaPadding = textareaPaddTop + textareaPaddBott;
        $(document.getElementById('textarea_descr_preview')).css('height', ($(document.getElementById('descr_modal_preview')).outerHeight() - textareaPadding)+ 'px');
        $(document.getElementById('textarea_descr_preview')).css('max-height', ($(document.getElementById('descr_modal_preview')).outerHeight() - textareaPadding) + 'px');
        $(document.getElementById('descr_textarea_preview')).css('height', ($(document.getElementById('descr_modal_preview')).outerHeight() - previewPadding) + 'px');
        $(document.getElementById('descr_textarea_preview')).css('max-height', ($(document.getElementById('descr_modal_preview')).outerHeight() - previewPadding) + 'px');
    });
    
    
    $('#textarea_descr_preview').on('input propertychange paste', () => {
            $('#descr_textarea_preview').html(marked($('#textarea_descr_preview').val()));
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
            $(document.getElementById('textarea_desc')).val(cached_Descriptions[i]);
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
/* Load the deck and get stats */
function loadDeck() {
    let fd = new FormData(document.getElementById('deckForm'));

    $.ajax({
        url: '/deck',
        type: 'POST',
        data: fd,
        processData: false,
        cache: false,
        // contentType: 'multipart/form-data',
        contentType: false,

        success: function(data) {
            // console.log(data);
            // var json = $.parseJSON(data);
            // $('#response').append('<p>' + json.filesize + '</p>');
            $('#response').append('<p>' + data.avgManaCost + '</p>');
        },

        error: function(errorData) {
            // alert('Error loading deck: ' + errorData.responseText);
            $('#response').append('<p>' + errorData.responseText + '</p>');
        }
    });
}

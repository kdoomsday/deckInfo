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
            avgCosts(data);
            manaCurve(data);
        },

        error: function(errorData) {
            // alert('Error loading deck: ' + errorData.responseText);
            $('#response').append('<p>' + errorData.responseText + '</p>');
        }
    });
}

/** Display Avg costs */
function avgCosts(data) {
    $('#response').append('<p>Avg cmc: ' + data.avgCMC + '</p>');
    $('#response').append('<p>Avg nonLands: ' + data.avgCMCNonLands + '</p>');
}

/** Display the mana curve */
function manaCurve(data) {
    var ctx = document.getElementById('manaCurve').getContext('2d');
    var myChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['1', '2', '3', '4', '5'],
            datasets: [{
                label: 'Mana Curve',
                data: [data.manaCurve[0].amount,
                       data.manaCurve[1].amount,
                       data.manaCurve[2].amount,
                       data.manaCurve[3].amount,
                       data.manaCurve[4].amount],
                backgroundColor: 'rgba(255, 99, 132, 0.2)',
                borderColor: 'rgba(255, 99, 132, 1)',
                borderWidth: 1
            }]
        },
        options: {
            scales: {
                yAxes: [{
                    ticks: {
                        beginAtZero: true
                    }
                }]
            }
        }
    });
}

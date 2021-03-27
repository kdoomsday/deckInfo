/* Load the deck and get stats */
function loadDeck() {
    let fd = new FormData(document.getElementById('deckForm'));

    hideAll();
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
            showAll();
        },

        error: function(errorData) {
            // alert('Error loading deck: ' + errorData.responseText);
            // $('#response').append('<p>' + errorData.responseText + '</p>');
            $('#myalerts').append(errorData.responseText);
            $('#myalerts').show();
        }
    });
}

function hideAll() {
    // $('.deckdata').hide();
    $('#deckinfo').hide();
}
function showAll() {
    // $('.deckdata').show();
    $('#deckinfo').show();
}

/** Display Avg costs */
function avgCosts(data) {
    // $('#response').empty();
    // $('#response').append('<p>Avg cmc: ' + data.avgCMC + '</p>');
    // $('#response').append('<p>Avg nonLands: ' + data.avgCMCNonLands + '</p>');
    $('#avgCMC').val(data.avgCMC);
    $('#avgCMCNonLands').val(data.avgCMCNonLands);
}

/** Display the mana curve */
function manaCurve(data) {
    // Clear the canvas for reuploads
    $('#manaCurve').replaceWith('<canvas id="manaCurve"></canvas>');

    var ctx = document.getElementById('manaCurve').getContext('2d');
    var chartData = fixMCHoles(data.manaCurve);
    var myChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: chartData.labels,
            datasets: [{
                label: 'Mana Curve',
                data: chartData.curve,
                backgroundColor: 'rgba(100, 100, 100, 0.2)',
                borderColor: 'rgba(100, 100, 100, 1)',
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

/** Fill in holes in the mana curve
  * Returns the fixed mana curve and labels
  */
function fixMCHoles(curve) {
    var res    = [];
    var labels = [];
    var cp = 0;

    for (var i in curve) {
        while (curve[i].cost > cp) {
            res.push(0);
            labels.push(cp);
            cp += 1;
        }

        labels.push(cp);
        res.push(curve[i].amount);
        cp += 1;
    }

    return {
        "curve": res,
        "labels": labels
    };
}

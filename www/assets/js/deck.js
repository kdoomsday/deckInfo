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
        contentType: 'text/xml',
        // contentType: false,

        success: function(data) {
            displayInfo(data);
            manaCurve(data);
            countsChart(data);
            symbolsChart(data);
            showAll();
        },

        error: function(errorData) {
            $('#myalerts').append(errorData.responseText);
            $('#myalerts').show();
        }
    });
}

/* Load the deck and get stats
 * This will read the file and send the contents as a string, instead of sending
 * them as a 'file'
 */
function loadDeckContents() {
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
            displayInfo(data);
            manaCurve(data);
            countsChart(data);
            symbolsChart(data);
            showAll();
        },

        error: function(errorData) {
            $('#myalerts').append(errorData.responseText);
            $('#myalerts').show();
        }
    });
}

// Hide and show deck info
function hideAll() {
    $('#deckinfo').hide();
}
function showAll() {
    $('#deckinfo').show();
}

/** Display deck information */
function displayInfo(data) {
    $('#avgCMC').val(data.avgCMC.toFixed(2));
    $('#avgCMCNonLands').val(data.avgCMCNonLands.toFixed(2));
    $('#cardCount').val(data.cardCount);
    $('#deckName').val(data.deckName);

    $('#deckList').html("");
    $('#deckList').append("<ul id='deckList_inner'></ul>");
    for (card of data.cards) {
        $('#deckList_inner').append(`<li>${card.count} x ${cardLink(card.name)}</li>`);
    }
}

function cardLink(name) {
    // return "<a href='https://deckbox.org/mtg/" + card.name + "'>" + card.name + "</a>";
    return `<a href='https://deckbox.org/mtg/${encodeURIComponent(name)}' target='_blank'>${name}</a>`;
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
                data: chartData.curve,
                backgroundColor: 'rgba(50, 50, 240, 0.8)',
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
            },
            title: {
                display: true,
                position: 'bottom',
                text: 'Mana Curve'
            },
            legend: { display: false }
        }
    });
}

/** Transform a list of CountObjects into labels and counts */
function countsToChartData(countsObj) {
    const labs = [];
    const counts = [];
    for (co of countsObj) {
        labs.push(co.name);
        counts.push(co.count);
    }

    return {
        "labels": labs,
        "counts": counts
    };
}

/** Create the card type counts chart */
function countsChart(data) {
    $('#counts').replaceWith('<canvas id="counts"></canvas>');

    const cd = countsToChartData(data.counts);

    const colors = ["#703716", "#1b5918", "#bb0606", "#071084", "#a08924", "#8e8c83", "#669ae2"];

    const ctx = document.getElementById('counts').getContext('2d');
    const myChart = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: cd.labels,
            datasets: [{
                data: cd.counts,
                backgroundColor: colors
            }],
        },
        options: {
            title: {
                text: "Counts by Card Type",
                position: "bottom",
                display: true
            }
        }
    });
}

/** Fill in holes in the mana curve
  * Returns the fixed mana curve and labels
  */
function fixMCHoles(curve) {
    const res    = [];
    const labels = [];
    let   cp     = 0;

    for (var mc of curve) {
        while (mc.cost > cp) {
            res.push(0);
            labels.push(cp);
            cp += 1;
        }

        labels.push(cp);
        res.push(mc.amount);
        cp += 1;
    }

    return {
        "curve": res,
        "labels": labels
    };
}

/** Color for a mana symbol */
function symbolColor(context) {
    const index = context.dataIndex;
    const label = context.chart.data.labels[index];

    if (label == "W")
        return "#f4f395";
    else if (label == "U")
        return "#0000ff";
    else if (label == "B")
        return "#000000";
    else if (label == "R")
        return "#ff0000";
    else if (label == "G")
        return "#00ff00";
    else
        return "#888888";
}

/** Mana symbols chart */
function symbolsChart(data) {
    $('#symbols').replaceWith('<canvas id="symbols"></canvas>');

    const cd = countsToChartData(data.manaSymbols);

    const ctx = document.getElementById('symbols').getContext('2d');
    const myChart = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: cd.labels,
            datasets: [{
                data: cd.counts,
                backgroundColor: symbolColor
            }],
        },
        options: {
            title: {
                text: "Mana Symbol Counts",
                position: "bottom",
                display: true
            },
            legend: {
                position: "left"
            }
        }
    });
}

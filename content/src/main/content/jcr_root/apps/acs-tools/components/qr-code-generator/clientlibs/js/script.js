/*global JSON: false, angular: false */

var DEFAULT_PAGE_URL = "/etc/acs-tools/qr-code-config/jcr:content/config.json";

var qrElement = $(".qr-code-url")[0],
    publishHost, urlElement, isEnabled, url, mappingConfig, parsedResponse, host;

// Get the configurations for current hostname
$.ajax({
    url: DEFAULT_PAGE_URL,
    dataType: "json"
}).done(function (response) {
    parsedResponse = JSON.parse(response.config);
    isEnabled = parsedResponse.enable;

    if (isEnabled) {
        $('.qrcode-hidden').removeAttr('disabled');
        mappingConfig = parsedResponse.properties;
        var host;
        for (host in mappingConfig) {
            if (mappingConfig[host].name === window.location.host) {
                publishHost = mappingConfig[host].value;

            }

        }

    }

});

// Create QR code element on page if it does not exist
urlElement = document.createElement('div');
urlElement.id = "qrcodeTable";
$(".qr-code-url").append(urlElement);

$(qrElement).on("click", function () {
    url = publishHost + window.location.pathname;

    // Remove editor.html from URL
    url = url.replace("/editor.html", "");
    jQuery('#qrcodeTable').empty();
    jQuery('#qrcodeTable').qrcode(url);
    $("#qrcodeTable").toggle();

});

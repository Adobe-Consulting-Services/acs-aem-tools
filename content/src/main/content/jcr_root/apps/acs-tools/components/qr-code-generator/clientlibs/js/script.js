/* ---------------------------- Custom Code to show popup with QR Code -------------------------- */
var qrCode = {
    default_url :   "/etc/acs-tools/qr-code-config/jcr:content/config.json"
};
var DEFAULT_PAGE_URL = "/etc/acs-tools/qr-code-config/jcr:content/config.json";

var qrElement = $(".qr-code-url")[0],
    publishDomainURL, array, p, urlElement;

// Get the configurations for current hostname
$.ajax({
    url: DEFAULT_PAGE_URL,
    dataType: "json"
}).done(function (response) {

    parsedResponse = JSON.parse(response.config);
    isEnabled = parsedResponse.enable;
    console.log(isEnabled);

    if (isEnabled) {
        $('.qrcode-hidden').removeAttr('disabled');
        mappingConfig = parsedResponse.properties;

        console.log(mappingConfig);
        for (host in mappingConfig) {
            if (mappingConfig[host].name === window.location.host) {
                publishDomainURL = mappingConfig[host].value;
                console.log(publishDomainURL);
            }

        }

    }

});

// Create QR code element on page if it does not exist
urlElement = document.createElement('div');
urlElement.id = "qrcodeTable";
$(".qr-code-url").append(urlElement);

$(qrElement).on("click", function () {
    var url = "http://" + publishDomainURL + window.location.pathname;

    // Remove editor.html from URL
    url = url.replace("/editor.html", "");
    console.log(url);
    jQuery('#qrcodeTable').empty();
    jQuery('#qrcodeTable').qrcode(url);
    $("#qrcodeTable").toggle();
});

---
layout: feature
title: AEM LiveReload
description: Give Cmd-R a break
date: 2013-12-04
thumbnail: /images/aem-livereload/thumbnail.png
categories: features
tags: updated
---

LiveReload is a browser plugin which automatically reloads browser windows when files change. It uses WebSockets to create a persistent connection to the server. When used with AEM 5.6 or AEM 5.6.1, this feature supports reloading pages when JSPs change. When used with AEM 6.0, it will also listen for client library changes.

* **AEM 5.6.x** - Supports reloading pages when JSPs change.
* **AEM 6** - Supports reloading pages when JSPs change **AND** when client libraries change.

For more information on LiveReload, visit [http://livereload.com/](http://livereload.com/)

## Installation

Installation of the ACS AEM Tools LiveReload functionality requires:

* ACS AEM Tools package
* Netty OSGi Bundles (via the [Netty content package](https://github.com/Adobe-Consulting-Services/com.adobe.acs.bundles.netty/releases))
* LiveReload browser plugin

### Installing ACS AEM Tools

1. Download and install the latest ACS AEM Tools package and install via [AEM's Package Manager](http://localhost:4502/crx/packmgr)

### Installing Netty

1. Download the [Netty content package](https://github.com/Adobe-Consulting-Services/com.adobe.acs.bundles.netty/releases) and install via [AEM's Package Manager](http://localhost:4502/crx/packmgr)

This includes the necessary Netty OSGi bundles.

### Install LiveReload Browser Plugin

* [Chrome](https://chrome.google.com/webstore/detail/livereload/jnihajbhpnppcggbcgedagnkighmdlei)
* [Firefox](https://addons.mozilla.org/en-US/firefox/addon/livereload/)
* [Opera](https://addons.opera.com/en/extensions/details/livereload-201-beta/)
* [IE](https://github.com/dvdotsenko/livereload_ie_extension)


![Browser Plugin]({{ site.baseurl }}/images/aem-livereload/browser-plugin.png)

## How to Use

1. After installation steps outlined above are complete, in your Web browser, navigate to the page you will be developing against; in this example: `http://localhost:4502/content/geometrixx/en/products.html`
2. Ensure the LiveReload browser plugin is enabled
3. Push your changes to to AEM (on the AEM install with Netty and ACS AEM Tools LiveReload installed) for the Component or Page under development.
	
	* LiveReload can be leveraged when vlt'ing files into CRX, deploying via Maven builds or editting in CRXDE/CRXDE Lite.
5. The browser window opened in Step 1. will automatically refresh and display the changes.

![ACS AEM Tools LiveReload Example]({{ site.baseurl }}/images/aem-livereload/example.png)

***Tip: Append `?wcmmode=disabled` to the end of the URL in your address bar to avoid loading authoring scripts and reduce refresh times.***

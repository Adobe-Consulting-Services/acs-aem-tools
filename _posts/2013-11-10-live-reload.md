---
layout: feature
title: AEM LiveReload
description: Give Cmd-R a break
date: 2013-12-04
thumbnail: /images/aem-livereload/thumbnail.png
categories: features
tags: new
---

## Installation

Installation of the ACS AEM Tools LiveReload functionality requires:

* ACS AEM Tools Package
* Netty OSGi Bundles
* LiveReload browser plugin

### Installing ACS AEM Tools

1. Download and install the latest ACS AEM Tools Packagea and install via [AEM's Package Manager](http://localhost:4502/crx/packmgr)

### Installing Netty

1. [Download latest Netty 4.x](http://netty.io/downloads.html) (ex. netty-4.0.13.Final.tar.bz2)
2. Unzip, and install the following OSGi Bundles via the [AEM Web Console](http://localhost:4502/system/console/bundles).

* netty-buffer-4.x.x.Final.jar
* netty-codec-4.x.x.Final.jar
* netty-codec-http-4.x.x.Final.jar
* netty-codec-socks-4.x.x.Final.jar
* netty-common-4.x.x.Final.jar
* netty-handler-4.x.x.Final.jar
* netty-transport-4.x.x.Final.jar

Resulting in the following Active bundles. If any bundles are not active, press the ">" next to them to activate.

![Netty Bundles]({{ site.baseurl }}/images/aem-livereload/netty-bundles.png)

Verify the ACS AEM Tools Live Reload Bundle is Active. If not, press the ">" to activate the bundle.

![ACS AEM Tools LiveReload Bundle]({{ site.baseurl }}/images/aem-livereload/acs-aem-tools-livereload-bundle.png)

### Install LiveReload Browser Plugin

* [Chrome](https://chrome.google.com/webstore/detail/livereload/jnihajbhpnppcggbcgedagnkighmdlei)
* [Firefox](https://addons.mozilla.org/en-US/firefox/addon/livereload/)
* [Opera](https://addons.opera.com/en/extensions/details/livereload-201-beta/)
* [IE](https://github.com/dvdotsenko/livereload_ie_extension)
* [LiveReload Homepage](http://livereload.com/)

![Browser Plugin]({{ site.baseurl }}/images/aem-livereload/browser-plugin.png)

## How to Use

1. After installation steps outlined above are complete, in your Web browser, navigate to the page you will be developing against; in this example: `http://localhost:4502/content/geometrixx/en/products.html`
2. Ensure the LiveReload browser plugin is enabled
3. Push your changes to to AEM (on the AEM install with Netty and ACS AEM Tools LiveReload installed) for the Component or Page under development.
	
	* LiveReload can be leveraged when vlt'ing files into CRX, deploying via Maven builds or editting in CRXDE/CRXDE Lite.
5. The browser window opened in Step 1. will automatically refresh and display the changes.

![ACS AEM Tools LiveReload Example]({{ site.baseurl }}/images/aem-livereload/example.png)

***Tip: Append `?wcmmode=disabled` to the end of the URL in your address bar to avoid loading authoring scripts and reduce refresh times.***


(function () {
  /**
   * The global variable with the WebDAV server url.
   */
  webdavServerPluginUrl = null;

  var href = location.href;
  var endIndex = href.indexOf('/app/');
  if (endIndex == -1) {
    endIndex = href.indexOf('/static/');
  }

  var baseUrl = href.substring(0, endIndex + 1);
  webdavServerPluginUrl = baseUrl + 'plugins-dispatcher/webdav-server/'

  // load samples thumbails.
  goog.events.listen(
    workspace, sync.api.Workspace.EventType.DASHBOARD_LOADED, function() {
      // get options
      goog.net.XhrIo.send('../plugins-dispatcher/webdav-server-public-options/',
        handleOptions, 'GET', null, {
          'Accept': 'application/json'
        });

      function handleOptions(e) {
        var request = e.target;
        if (request.getStatus() == 200) {
          var options = JSON.parse(request.getResponseText());
          if (options.display_samples == "on") {
            displayWebdavSamples();
          }
        }
      };
    });

  /**
   * Display the webdav samples on the dashboard.
   */
  function displayWebdavSamples() {
    // icons sources
    var images = [sync.util.computeHdpiIcon("/plugin-resources/webdav-server/gerbera.png"),
      sync.util.computeHdpiIcon("plugin-resources/webdav-server/ditamap.png"),
      sync.util.computeHdpiIcon("plugin-resources/webdav-server/docbook.png"),
      sync.util.computeHdpiIcon("plugin-resources/webdav-server/xhtml.png"),
      sync.util.computeHdpiIcon("plugin-resources/webdav-server/tei.png")];

    // samples types.
    var samples = ["DITA", "DITA Map", "DocBook", "XHTML", "TEI"];
    // samples urls
    var sampleUrls = ['../../plugins-dispatcher/webdav-server/dita/flowers/topics/flowers/gardenia.dita',
      '../../plugins-dispatcher/webdav-server/dita/flowers/flowers.ditamap',
      '../../plugins-dispatcher/webdav-server/docbook/v5/sample.xml',
      '../../plugins-dispatcher/webdav-server/xhtml/sample.xml',
      '../../plugins-dispatcher/webdav-server/tei/TEI-P5.xml'];
    // samples ditamaps
    var ditamaps = ['../plugins-dispatcher/webdav-server/dita/flowers/flowers.ditamap', null, null, null, null];

    // construct the dom structure for the samples.
    var domHelper = new goog.dom.DomHelper();
    var samplesContainer = domHelper.createDom('div', 'dashboard-section');
    samplesContainer.id = 'samples-container';
    samplesContainer.style.display = 'none';
    var titleCss = '';

    for (var i = 0; i < samples.length; i++) {
      var sampleLink = domHelper.createDom('a', 'dashboard-sample');
      sampleLink.href = '#';
      var sampleName = samples[i];
      var sampleId = 'sample-title-' + sampleName.replace(' ', '-');
      sampleLink.id = sampleId;

      titleCss += '#' + sampleId + ':after{content:"' + sampleName + '";}\n';

      var image = domHelper.createDom('img', 'dashboard-sample-image');
      image.src = '../' + images[i];
      sampleLink.appendChild(image);
      samplesContainer.appendChild(sampleLink);

      var author = sync.util.getURLParameter('author') || 'John Doe';

      // open the sample document in a new tab when the sample image is clicked.
      var openUrl = getUrl(sampleUrls[i], ditamaps[i], author);
      goog.events.listen(image, goog.events.EventType.CLICK,
        goog.bind(function (openUrl) {
          window.open(openUrl)
        }, this, openUrl));
    };

    // add styles before adding the elements.
    addNewStylesheet(domHelper, titleCss);

    // the dashboard containing element.
    var container = document.getElementById('dashboard-container');
    container.appendChild(samplesContainer);
  }

  var baseUrl = null;

  /**
   * Compute the document url depending on params.
   *
   * @param docUrl the document url.
   * @param ditamapUrl the ditamap url.
   * @param authorName the author name.
   *
   * @return {string} the document url.
   */
  function getUrl(docUrl, ditamapUrl, authorName) {
    // compute the base url at first use.
    if (!baseUrl) {
      baseUrl = location.origin + location.pathname;
      var end = baseUrl.indexOf("/app/oxygen.html");
      if (end != -1) {
        baseUrl = baseUrl.substring(0, end);
      }
      if (baseUrl.charAt(baseUrl.length - 1) != '/') {
        baseUrl += '/';
      }
    }
    var urlStr = "oxygen.html?";
    urlStr += 'url=' + encodeURIComponent((baseUrl + docUrl).replace('\\', '/')) + '&';
    urlStr += 'showSave=true&';
    if (ditamapUrl) {
      urlStr += 'ditamap=' + encodeURIComponent((baseUrl + ditamapUrl).replace('\\', '/')) + '&';
    }
    urlStr += 'author=' + authorName;
    return urlStr;
  };

  function addNewStylesheet(domHelper, dynamicCss) {
    var cssFormating = domHelper.createDom('style');
    cssFormating.innerHTML =
      '#samples-container.dashboard-section a{' +
      'text-decoration:none;' +
      '}' +

      '#samples-container:before{' +
      'content:"Samples";' +
      '}' +

      '#samples-container {' +
      'display: block !important;' +
      'text-align: center;' +
      '}' +

      '#samples-container .dashboard-sample-image {' +
      'background-color: #FFFFFF;' +
      'display: inline-block;' +
      'height: 159px;' +
      'width: 125px;' +
      'margin:10px;' +
      '}' +

      '.dashboard-sample {' +
      'display: inline-block;margin: 10px;' +
      '}' +

      '.dashboard-sample:after {' +
      'background-color: transparent;' +
      'cursor:default;' +
      'font-family:"Helvetica Neue",Helvetica,Arial,sans-serif;' +
      'color: #595959;' +
      'display:block;' +
      'position: relative;' +
      '}' +
      (dynamicCss ? dynamicCss : '') +

      '#dashboard-container {' +
      'box-sizing: border-box;' +
      'border: 1px solid transparent;' +
      'position: relative;' +
      'min-height: 100%;' +
      'padding-bottom: 50px;' +
      '}';

    document.head.appendChild(cssFormating);
  };
})();
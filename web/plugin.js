(function () {
  var readonlySaveDialog = null;
  var href = location.href;
  var endIndex = href.indexOf('/app/');
  if (endIndex == -1) {
    endIndex = href.indexOf('/static/');
  }

  var baseUrl = href.substring(0, endIndex + 1);
  // global variable with the server url.
  window.webdavServerPluginUrl = baseUrl + 'plugins-dispatcher/webdav-server/';

  // load samples thumbails.
  goog.events.listen(
    workspace, sync.api.Workspace.EventType.DASHBOARD_LOADED, function() {
      if (sync.options.PluginsOptions.getClientOption('display_samples') == "on") {
        displayWebdavSamples();
      }
    });

  /**
   * Replace the save action when the the server is readonly.
   */
  if(decodeURIComponent(sync.util.getURLParameter('url'))
      .indexOf('plugins-dispatcher/webdav-server/') != -1) {
    goog.events.listen(workspace, sync.api.Workspace.EventType.EDITOR_LOADED, function(e) {
      // load the css rules used in the editor.
      loadEditorCSS();

      var readonlyMode = sync.options.PluginsOptions.getClientOption('webdav_server_plugin_readonly_mode');
      if(readonlyMode == 'on') {
        var editor = e.editor;
        goog.events.listen(editor, sync.api.Editor.EventTypes.ACTIONS_LOADED, function() {
          var saveAction = editor.getActionsManager().getActionById('Author/Save');
          // override save action
          saveAction.actionPerformed = function(callback) {
            // create the dialog
            if(!readonlySaveDialog) {
              readonlySaveDialog = workspace.createDialog();
              readonlySaveDialog.setTitle('Readonly document');
              readonlySaveDialog.getElement().innerHTML =
                '<div id="readonly-save-dialog">' +
                'The WebDAV server is in read-only mode.' +
                '<p>This means that you can edit and download the document but you cannot save it to the server</p>' +
                '<p>Use the <b>Download</b> action from the <b>More</b> submenu to save the document to your computer</p>' +
                '</div>';
              readonlySaveDialog.setButtonConfiguration(sync.api.Dialog.ButtonConfiguration.OK);
            }
            readonlySaveDialog.onSelect(callback);
            readonlySaveDialog.show();
          }
        });
      }
    });
  }

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
    var sampleUrls = ['dita/flowers/topics/flowers/gardenia.dita',
      'dita/flowers/flowers.ditamap',
      'docbook/v5/sample.xml',
      'xhtml/sample.xml',
      'tei/TEI-P5.xml'];
    // samples ditamaps
    var ditamaps = ['dita/flowers/flowers.ditamap', null, null, null, null];

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
    }

    // add styles before adding the elements.
    addNewStylesheet(domHelper, titleCss);

    // the dashboard containing element.
    var container = document.getElementById('dashboard-container');
    container.appendChild(samplesContainer);
  }

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
    var urlStr = "oxygen.html?";

    urlStr += 'url=webdav-' + encodeURIComponent((webdavServerPluginUrl + docUrl).replace('\\', '/'));

    urlStr += '&showSave=true';
    if (ditamapUrl) {
      urlStr += '&ditamap=webdav-' + encodeURIComponent((webdavServerPluginUrl + ditamapUrl).replace('\\', '/')) + '&';
    }
    urlStr += '&author=' + authorName;
    return urlStr;
  }

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
  }

  function loadEditorCSS() {
    var domHelper = new goog.dom.DomHelper();
    var cssFormating = domHelper.createDom('style');

    cssFormating.innerHTML =
      '#readonly-save-dialog {' +
        'content: " ";'+
        'width: 400px;' +
        'line-height: 1.2em;' +
      '}' +
      '#readonly-save-dialog {' +
        'text-indent: 10px;' +
      '}';
    document.head.appendChild(cssFormating);
  }
})();

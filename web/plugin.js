(function () {


  /**
   * A dashboard tab for sample files.
   *
   * @constructor
   */
  sync.ui.SamplesTab = function () {
  };
  goog.inherits(sync.ui.SamplesTab, sync.ui.AbstractDashboardTab);

  /** @override */
  sync.ui.SamplesTab.prototype.getTabHeaderImage = function () {
    return sync.util.computeHdpiIcon("../plugin-resources/webdav-server/Samples70.png");
  };

  /** @override */
  sync.ui.SamplesTab.prototype.getTabHeaderDisplayName = function () {
    return tr(msgs.SAMPLES_);
  };

  /** @override */
  sync.ui.SamplesTab.prototype.getTabContentElement = function () {
    if (!this.samplesContainer) {
      var cD = goog.dom.createDom;
      // construct the dom structure for the samples.
      var domHelper = new goog.dom.DomHelper();
      this.samplesContainer = cD('div', {id: 'dashboard-samples-container'});
      this.samplesContainer.style.display = 'none';
      var descriptor = retrieveSamplesDescriptor();
      if (descriptor) {
        var samples = descriptor['samples'];
        var titleCss = '';

        for (var i in samples) {
          var sample = samples[i];
          var path = sample['path'];
          var ditamap = sample['ditamap'];
          var imagePath = sample['image'];
          var labels = sample['labels'];
          var defaultImage = false;
          if (!imagePath) {
            // The default image (if no one is provided in the samples descriptor)
            imagePath = "plugin-resources/webdav-server/MissingImage.png";
            defaultImage = true;
          }
          imagePath = sync.util.computeHdpiIcon(imagePath);

          // Open the sample document in a new tab when the sample image is clicked.
          var author = sync.util.getURLParameter('author') || tr(msgs.ANONYMOUS_);
          var openUrl = getUrl(path, ditamap, author);
          var sampleName = sample['name'];
          var sampleId = 'sample-title-' + sampleName.replace(/ /g, '-');

          // Make label elements.
          var labelElements = [];
          if (labels) {
            var labelsTexts = labels.split(',');
            for (var j in labelsTexts) {
              var labelText = labelsTexts[j];
              if (labelText) {
                labelElements.push(cD('span', 'dashboard-sample-label', labelText.trim()));
              }
            }
          }

          goog.dom.appendChild(this.samplesContainer,
            cD('a', {
                className: 'dashboard-sample',
                target: '_blank',
                href: openUrl,
                id: sampleId
              },
              cD('img', {
                className: 'dashboard-sample-image',
                src: (defaultImage ? '../' : webdavServerPluginUrl) + imagePath}
              ),
              cD('div', 'dashboard-sample-name', sampleName),
              cD('div', 'dashboard-sample-labels',
                labelElements
              )
            )
          );
        }
        // add styles before adding the elements.
        addNewStylesheet(domHelper, titleCss);
      }
    }
    return this.samplesContainer;
  };

  /**
   * Returns a JSON containg information about all samples that must be rendered in the "Samples" tab.
   *
   * @param Object data JSON Object with details about samples
   * @return {boolean}
   */
  function retrieveSamplesDescriptor() {
    var descriptor = null;

    $.ajax({
      type: "GET",
      url: webdavServerPluginUrl + '.descriptor/samples.json',
      async: false,
      data: "",
      success: function (data_response) {
        descriptor = data_response;
      },
      error: function () {

      }
    });

    return descriptor;
  };

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

  /**
   * Add styles to the document.
   *
   * @param domHelper the document helper.
   * @param dynamicCss the dynamic CSS to be added to the document.
   */
  function addNewStylesheet(domHelper, dynamicCss) {
    var cssFormating = domHelper.createDom('style');
    cssFormating.innerHTML =
      '#dashboard-samples-container {' +
      'display: block !important;' +
      'text-align: center;' +
      '}' +

      '@media only screen and (min-width: 768px) {' +
      '#dashboard-samples-container {' +
      'padding: 30px 60px;' +
      '}' +
      '}' +

      '#dashboard-samples-container .dashboard-sample-image {' +
      'background-color: #FFFFFF;' +
      'display: inline-block;' +
      'height: 159px;' +
      'width: 125px;' +
      'margin:50px 30px 0px;' +
      'border: 2px solid #cccccc;' +
      '}' +

      '.dashboard-sample {' +
      'display: inline-block;' +
      'margin: 10px 60px 10px 60px;' +
      'text-decoration:none;' +
      '}' +

      '.dashboard-sample-labels {' +
      'display: block;' +
      'margin-bottom: 10px;' +
      ' }' +

      '.dashboard-sample-label {' +
      'font-size: 70%;' +
      ' border: 0px solid #ddd;' +
      'background-color: #b4b4b4;' +
      'border-radius: 4px;' +
      'padding: 1px 6px;' +
      'margin-left: 5px;' +
      'color: #fff;' +
      'cursor: pointer;' +
      ' }' +


      '#dashboard-samples-container .dashboard-sample-image:hover {' +
      'border-color: #34789d;' +
      '}' +

      '#dashboard-samples-container .dashboard-sample:hover .dashboard-sample-image {' +
      'border-color: #34789d;' +
      '}' +


    '.dashboard-sample:hover .dashboard-sample-label {' +
      'background-color: #34789d;' +
      ' }' +

      '.dashboard-sample-name {' +
      'background-color: transparent;' +
      'cursor:default;' +
      '    font-family: Roboto, Arial, Helvetica, sans-serif;' +
      'color: #595959;' +
      'display:block;' +
      'position: relative;' +
      'margin: 10px 0px 5px;' +
      '}' +
      (dynamicCss ? dynamicCss : '');

    document.head.appendChild(cssFormating);
  }

  var translations = {
    "WEBDAV_READ_ONLY_MODE_": {
      "en_US":"The WebDAV server is in read-only mode. " +
        "{$P_START}This means that you can edit and download the document but you cannot save it to the server.{$P_END}" +
        "{$P_START}Use the {$B_START}Download{$B_END} action from the {$B_START}More{$B_END} submenu to save the document to your computer{$P_END}",
      "de_DE":"Der WebDAV-Server befindet sich im schreibgeschützten Modus. " +
        "{$P_START}Das bedeutet, dass Sie das Dokument bearbeiten und herunterladen, aber nicht auf dem Server speichern können.{$P_END}" +
        "{$P_START}Verwenden Sie die {$B_START}Download{$B_END} Aktion aus dem {$B_START}Mehr{$B_END} Untermenü, um das Dokument auf Ihrem Computer zu speichern{$P_END}",
      "fr_FR":"Le serveur WebDAV est en mode lecture seule. " +
        "{$P_START}Cela signifie que vous pouvez éditer et télécharger le document mais vous ne pouvez pas l'enregistrer sur le serveur.{$P_END}" +
        "{$P_START}Utilisez l'action {$B_START}Télécharger{$B_END} depuis le sous-menu {$B_START}Plus{$B_END} pour enregistrer le document sur votre ordinateur{$P_END}",
      "ja_JP":"WebDAVサーバーは、読み取り専用モードです{$P_START}これは、ドキュメントを編集・ダウンロードできますが、サーバーに対して保存できないことを意味します。{$P_END}" +
        "{$P_START}{$B_START}その他{$B_END}サブメニューの{$B_START}ダウンロード{$B_END}操作を使用して、コンピュータ上にドキュメントを保存します{$P_END}",
      "nl_NL":"De WebDAV-server bevindt zich in de alleen-lezen modus. " +
        "{$P_START}Dit betekent dat u het document kunt bewerken en downloaden, maar niet kunt opslaan op de server.{$P_END}" +
        "{$P_START}Gebruik de actie {$B_START}Downloaden{$B_END} in het submenu {$B_START}Meer{$B_END} als u het document wilt opslaan op uw computer{$P_END}"
    },
    "READ_ONLY_DOCUMENT_": {
      "en_US":"Read-only document",
      "de_DE":"Schreibgeschütztes Dokument",
      "fr_FR":"Document en lecture seule",
      "ja_JP":"読み取り専用ドキュメント",
      "nl_NL":"Alleen-lezen document"
    },
    "ANONYMOUS_": {
      "en_US":"Anonymous",
      "de_DE":"Anonym",
      "fr_FR":"Anonyme",
      "ja_JP":"匿名",
      "nl_NL":"Anoniem"
    },
    "SAMPLES_": {
      "en_US":"Samples",
      "de_DE":"Beispiele",
      "fr_FR":"Exemples",
      "ja_JP":"例",
      "nl_NL":"Voorbeelden"
    }
  };
  sync.Translation.addTranslations(translations);

  var readonlySaveDialog = null;
  var href = location.href;
  var endIndex = href.indexOf('/app/');
  if (endIndex === -1) {
    endIndex = href.indexOf('/static/');
  }

  var baseUrl = href.substring(0, endIndex + 1) + 'plugins-dispatcher/webdav-server/';
  // global variable with the server url.
  window.webdavServerPluginUrl = baseUrl;

  // enforce the url if option enabled.
  if('on' == sync.options.PluginsOptions.getClientOption('webdav_server_plugin_enforce_url')) {
    window.addEnforcedWebdavUrl && window.addEnforcedWebdavUrl(baseUrl);
  }
  // load samples thumbails.
  goog.events.listen(
    workspace, sync.api.Workspace.EventType.BEFORE_DASHBOARD_LOADED, function() {
      if (sync.options.PluginsOptions.getClientOption('display_samples') == "on") {
        workspace.addDashboardAdditionalTabs(new sync.ui.SamplesTab());
      }
    });

  /**
   * Replace the save action when the the server is readonly.
   */
  if(decodeURIComponent(sync.util.getURLParameter('url'))
      .indexOf('plugins-dispatcher/webdav-server/') !== -1) {

    var readonlyMode = sync.options.PluginsOptions.getClientOption('webdav_server_plugin_readonly_mode') === 'on' ||
      sync.util.getURLParameter('webdav_disable_save') === 'true';
    if(readonlyMode) {
      // Disable autosave for the WebDAV server plugin.
      sync.options.PluginsOptions.clientOptions['webdav_autosave_interval'] = '0';

      goog.events.listen(workspace, sync.api.Workspace.EventType.EDITOR_LOADED, readOnlyEditorLoaded);
    }
  }

  /**
   * Editor Loaded callback.
   *
   * @param e the editor loaded event.
   */
  function readOnlyEditorLoaded(e) {
    // load the css rules used in the editor.
    loadEditorCSS();

    var editor = e.editor;
    goog.events.listen(editor, sync.api.Editor.EventTypes.ACTIONS_LOADED, function(e) {
      var saveAction = editor.getActionsManager().getActionById('Author/Save');
      if (saveAction) {
        // override save action
        saveAction.actionPerformed = function(callback) {
          // create the dialog
          if(!readonlySaveDialog) {
            readonlySaveDialog = workspace.createDialog();
            readonlySaveDialog.setTitle(tr(msgs.READ_ONLY_DOCUMENT_));
            // todo: add translation function for this.
            readonlySaveDialog.getElement().innerHTML =
              '<div id="readonly-save-dialog">' +
              tr(msgs.WEBDAV_READ_ONLY_MODE_, {'$P_START': '<p>', '$P_END': '</p>', '$B_START': '<b>', '$B_END': '</b>'}) +
              '</div>';
            readonlySaveDialog.setButtonConfiguration(sync.api.Dialog.ButtonConfiguration.OK);
          }
          readonlySaveDialog.onSelect(callback);
          readonlySaveDialog.show();
        }
      }

      // Disable the Ctrl+S shortcut.
      var noopAction = new sync.actions.NoopAction('M1 S');
      editor.getActionsManager().registerAction('DoNothing', noopAction);

      if(e.actionsConfiguration.toolbars.length) {
        var toolbarActions = e.actionsConfiguration.toolbars[0].children;
        var i = 0;
        for (i = 0; i < toolbarActions.length; i ++) {
          var currentAction = toolbarActions[i];
          if (currentAction.id == 'Author/Save') {
            // Replace the Save action withe download.
            var downloadActionDescriptor = {id: 'Author/SaveLocal', type: 'action'};
            toolbarActions.splice(i, 1, downloadActionDescriptor);
          }
        }
      }

      editor.getActionsManager().registerAction('Author/SaveLocal',
        new sync.actions.DownloadXMLAction(editor, ''));
    });
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

  setTimeout(function() {
    var createActions = workspace.getActionsManager().getCreateActions();
    for(var i = 0; i < createActions.length; i++) {
      var createAction = createActions[i];
      if(createAction.getActionId() == 'webdav-create-action') {
        var urlChooser = createAction.urlChooser;
        var originalRequestUrlInfo_ = goog.bind(urlChooser.requestUrlInfo_, urlChooser);
        urlChooser.requestUrlInfo_ = function(url, callback) {
          if(url && url.indexOf('/plugins-dispatcher/webdav-server/') !== -1) {
            callback(url, {
              rootUrl: url.substring(0, url.indexOf('/plugins-dispatcher/webdav-server/') + '/plugins-dispatcher/webdav-server/'.length),
              type: goog.string.endsWith(url, '/') ? 'COLLECTION' : 'FILE'
            });
          } else {
            originalRequestUrlInfo_(url, callback);
          }
        }.bind(urlChooser);

        break;
      }
    }
  }, 0);
})();

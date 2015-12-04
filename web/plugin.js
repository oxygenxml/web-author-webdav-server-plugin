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

  var baseUrl = endIndex != -1 ? href.substring(0, endIndex + 1) : null;
  if(baseUrl) {
    webdavServerPluginUrl = baseUrl + 'plugins-dispatcher/webdav-server/'
  }
})();
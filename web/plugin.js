(function () {
  var readonlySaveDialog = null;
  var href = location.href;
  var endIndex = href.indexOf('/app/');
  var baseUrl = href.substring(0, endIndex + 1) + 'plugins-dispatcher/webdav-server/';
  if('on' === sync.options.PluginsOptions.getClientOption('webdav_server_plugin_enforce_url')) {
    window.addEnforcedWebdavUrl && window.addEnforcedWebdavUrl(baseUrl);
  }
})();

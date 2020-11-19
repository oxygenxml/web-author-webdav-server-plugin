(function() {
  /**
   * Highlights actions specific in the sample.json, under "highlightedAction" property.
   * @constructor
   */
  sync.ui.FeaturesHighlighter = function() {
    /**
     * @type {sync.ui.FeatureHighlighter[]}
     */
    this.highlights = [];
  };

  /**
   * @type {string} loading option where the list of used promoted actions is stored.
   */
  sync.ui.FeaturesHighlighter.USED_PROMOTED_ACTIONS = "-oxy-demo-usedPromotedActions";

  /**
   * Attach the listeners.
   */
  sync.ui.FeaturesHighlighter.prototype.attach = function() {
    var highlights = [];
    goog.events.listen(
      workspace, sync.api.Workspace.EventType.EDITOR_LOADED, goog.bind(function(e) {
        var editor = e.editor;

        var highlightedActions = this.getActionsToHighlight(editor);
        if (!highlightedActions || highlightedActions.length === 0) {
          return;
        }

        this.addStyle();
        goog.events.listen(editor, sync.api.Editor.EventTypes.ACTIONS_LOADED, goog.bind(function() {
          var highlightedActionsNotYetRegistered = [];
          for (var i = 0; i < highlightedActions.length; i++) {
            var hlAction = highlightedActions[i];
            var button = document.querySelector('[name="'+ hlAction.id+ '"]');
            if (button) {
              var hl = new sync.ui.FeatureHighlighter(editor, button, goog.bind(this.markActionAsUsed, this, hlAction.id));
              highlights.push(hl);
            } else {
              highlightedActionsNotYetRegistered.push(hlAction);
            }
          }
          highlightedActions = highlightedActionsNotYetRegistered;
        }, this));
      }, this));

    goog.events.listen(
      workspace, sync.api.Workspace.EventType.EDITOR_DISPOSED, goog.bind(function(e) {
        for (var i = 0; i < highlights.length; i++) {
          highlights.pop().stopHighlightElement();
        }
      }, this));
  };

  /**
   * @param {sync.api.editor} editor
   * @return {string[]} actions to be highlighted
   */
  sync.ui.FeaturesHighlighter.prototype.getActionsToHighlight = function(editor) {
    var toReturn = [];

    var highlightedActionsParam = editor.options.highlightedActions;
    if (highlightedActionsParam) {
      var highlightedActions = JSON.parse(highlightedActionsParam);
      var usedPromotedActions = this.getAlreadyUsedActions();
      toReturn = highlightedActions.filter(hlAction => {
        var action = editor.getActionsManager().getActionById(hlAction.id);
        return usedPromotedActions.indexOf(hlAction.id) === -1 && action && action.isEnabled();
      });
    }

    return toReturn;
  };

  /**
   * @return {string[]} get list of already used actions.
   */
  sync.ui.FeaturesHighlighter.prototype.getAlreadyUsedActions = function() {
    return JSON.parse(localStorage.getItem(sync.ui.FeaturesHighlighter.USED_PROMOTED_ACTIONS) || "[]");
  };

  /**
   * @param actionId Action to be marked as used. It shouldn't be highlighted after reload.
   */
  sync.ui.FeaturesHighlighter.prototype.markActionAsUsed = function(actionId) {
    var alreadyUsedActions = this.getAlreadyUsedActions();
    alreadyUsedActions.push(actionId);
    localStorage.setItem(sync.ui.FeaturesHighlighter.USED_PROMOTED_ACTIONS, JSON.stringify(alreadyUsedActions));
  };

  /**
   * Add the 'style' element for highlights.
   */
  sync.ui.FeaturesHighlighter.prototype.addStyle = function() {
    var domHelper = new goog.dom.DomHelper();
    var cssFormating = domHelper.createDom('style');
    cssFormating.textContent =
      '@keyframes changewidth {'+
      ' from {'+
      '  transform: scale(1);'+
      ' }'+
      ' to {'+
      '  transform: scale(1.3);'+
      ' }'+
      '}'+
      '#actionHighlighted {'+
      ' box-sizing: border-box;'+
      ' position: absolute;'+
      ' border: 3px solid #b4b4b4;'+
      ' transition: 0.5s all;'+
      ' z-index: 20001;'+
      ' border-radius: 20px;'+
      ' opacity: 0.7;'+
      ' pointer-events: none;'+
      ' animation-duration: 0.5s;'+
      ' animation-name: changewidth;'+
      ' animation-iteration-count: infinite;'+
      ' animation-direction: alternate;'+
      '}';
    document.head.appendChild(cssFormating);
  };


  /**
   * The actual highlight for an action.
   * @param {sync.api.Editor} editor
   * @param {Object} highlightActionDescriptor
   * @param {Function} visitCallback Called when action is clicked.
   * @constructor
   */
  sync.ui.FeatureHighlighter = function (editor, element, visitCallback) {
    this.visitCallback = visitCallback;
    this.startHighlighting(element);
    goog.events.listenOnce(element, goog.events.EventType.CLICK, goog.bind(this.visitCallback, this));
  };

  /**
   * Create and add to the DOM the highlight element .
   * @param {HTMLElement} element The element to highlight.
   * @private
   */
  sync.ui.FeatureHighlighter.prototype.createHighlightElement = function(element) {
    var elementOffsets = goog.style.getPageOffset(element);
    this.highlightDiv_ = goog.dom.createDom(
      'div',
      {
        id: 'actionHighlighted',
        style:
          'left: '+ elementOffsets.x+ 'px;'+
          'top: '+ elementOffsets.y+ 'px;'+
          'height: '+ element.clientHeight+ 'px;'+
          'width: '+ element.clientWidth+ 'px'
      });
    goog.dom.appendChild(document.body, this.highlightDiv_);
  };

  /**
   * Stop the highlight.
   */
  sync.ui.FeatureHighlighter.prototype.stopHighlightElement = function() {
    goog.dom.removeNode(this.highlightDiv_);
  };

  /**
   * @param {HTMLElement} element Element to be highlighted.
   */
  sync.ui.FeatureHighlighter.prototype.startHighlighting = function (element) {
    this.createHighlightElement(element);
    goog.events.listenOnce(element, goog.events.EventType.CLICK, goog.bind(this.stopHighlightElement, this));
  };


  var featuresHighlighter = new sync.ui.FeaturesHighlighter();
  featuresHighlighter.attach();
})();
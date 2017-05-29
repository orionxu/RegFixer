!function(t,e,n){"use strict";function o(t,e){function n(){this.constructor=t}for(var o in e)e.hasOwnProperty(o)&&(t[o]=e[o]);t.prototype=null===e?Object.create(e):(n.prototype=e.prototype,new n)}function i(t,e){return t.line===e.line&&t.ch===e.ch}function r(t,e){return t.line===e.line?t.ch<e.ch:t.line<e.line}function s(t,e){return t.line===e.line?t.ch>e.ch:t.line>e.line}function h(t){return{line:t.firstLine(),ch:0}}function a(t){var e=t.lastLine();return{line:e,ch:t.getLine(e).length}}function c(t,e){if(e.ch>0)return{line:e.line,ch:e.ch-1};if(e.line>0)return{line:e.line-1,ch:t.getLine(e.line-1).length};throw new Error("cannot get position before (0:0)")}function l(t,e){if(e.ch<t.getLine(e.line).length-1)return{line:e.line,ch:e.ch+1};if(e.line<t.lastLine())return{line:e.line+1,ch:0};var n=t.lastLine(),o=t.getLine(n).length-1;throw new Error("cannot get position after ("+n+":"+o+")")}function p(t,e,n){t.addEventListener(e,n)}function u(t,e,n){var o=function(i){t.removeEventListener(e,o),n(i)};t.addEventListener(e,o)}function d(t,e,n){t.removeEventListener(e,n)}var g=function(t){function e(){t.apply(this,arguments)}return o(e,t),e.prototype.componentDidMount=function(){this.instance=window.CodeMirror.fromTextArea(this.textarea),this.instance.on("change",this.editorValueChanged.bind(this)),this.instance.on("focus",this.editorFocusChanged.bind(this,!0)),this.instance.on("blur",this.editorFocusChanged.bind(this,!1)),this.instance.on("scroll",this.editorScrollChanged.bind(this)),this.instance.setValue(this.props.value)},e.prototype.componentWillReceiveProps=function(t){this.instance.getValue()!==t.value&&this.instance.setValue(t.value)},e.prototype.editorValueChanged=function(t,e){"setValue"!==e.origin&&this.props.onChange(t.getValue())},e.prototype.editorFocusChanged=function(t){this.setState({isFocused:t}),t?this.props.onFocus():this.props.onBlur()},e.prototype.editorScrollChanged=function(t){console.log("scroll")},e.prototype.render=function(){var t=this;return React.createElement("textarea",{ref:function(e){t.textarea=e}})},e.defaultProps={onChange:function(){},onFocus:function(){},onBlur:function(){}},e}(n.Component),f=function(t){function e(){t.apply(this,arguments)}return o(e,t),e.prototype.render=function(){return React.createElement("div",{className:"regex-editor"},React.createElement(g,{value:this.props.regex,onChange:this.props.onRegexChange}),this.props.children)},e}(n.Component),v=function(t){function e(){t.apply(this,arguments)}return o(e,t),e.prototype.render=function(){return React.createElement("div",{className:"regex-editor-controls"},this.props.children)},e}(n.PureComponent),y=function(t){function e(){t.apply(this,arguments)}return o(e,t),e.prototype.render=function(){return React.createElement("div",{className:"regex-editor-status","data-error":this.props.inError},this.props.children)},e}(n.PureComponent),m=function(t){function e(){t.apply(this,arguments)}return o(e,t),e.prototype.render=function(){return React.createElement("div",{className:"fix-modal"},React.createElement("div",{className:"triangle"}),React.createElement("div",{className:"header"},React.createElement("div",{className:"regex"},this.props.regex),this.props.children))},e}(n.PureComponent),x=function(t){function e(){t.apply(this,arguments)}return o(e,t),e.prototype.render=function(){return React.createElement("div",{className:"corpus-editor-overlay"},this.props.children)},e}(n.Component),R=function(t){function e(){t.apply(this,arguments)}return o(e,t),e.prototype.componentDidMount=function(){this.ctx=this.canvas.getContext("2d")},e.prototype.componentWillReceiveProps=function(t){var e=this,n=0;this.ctx.clearRect(0,0,this.canvas.width,this.canvas.height),t.highlightList.forEach(function(t){e.ctx.fillStyle=e.props.colors[n],n=(n+1)%e.props.colors.length;var o=t.getStart().coords,i=t.getEnd().coords;if(o.top===i.top){var r=o.left,s=o.top,h=i.left-r,a=i.bottom-s;e.ctx.fillRect(r,s,h,a)}else{var r=o.left,s=o.top,h=e.canvas.width-r,a=o.bottom-s;e.ctx.fillRect(r,s,h,a),r=4,s=o.bottom,h=e.canvas.width,a=i.top-s,e.ctx.fillRect(r,s,h,a),r=4,s=i.top,h=i.left,a=i.bottom-s,e.ctx.fillRect(r,s,h,a)}})},e.prototype.render=function(){var t=this;return React.createElement("div",{className:"corpus-editor-underlay"},React.createElement("canvas",{className:"canvas",width:"1000",height:"500",ref:function(e){t.canvas=e}}))},e}(n.Component),E=function(t){function e(){t.apply(this,arguments)}return o(e,t),e.prototype.handleMouseDown=function(t,e){e.preventDefault(),e.stopPropagation(),this.props.onDragStart(t)},e}(n.PureComponent),C=function(t){function e(){t.apply(this,arguments)}return o(e,t),e.prototype.render=function(){var t=this.props.point.coords,e=t.right-t.left,n=t.bottom-t.top,o=-e/2,i=-n/2,r=this.handleMouseDown.bind(this,[o,i]);return React.createElement("div",{className:"grip start-grip",onMouseDown:r,style:{top:this.props.point.coords.top-4,left:this.props.point.coords.left-4}})},e}(E),w=function(t){function e(){t.apply(this,arguments)}return o(e,t),e.prototype.render=function(){var t=this.props.point.coords,e=t.right-t.left,n=t.bottom-t.top,o=+e/2,i=+n/2,r=this.handleMouseDown.bind(this,[o,i]);return React.createElement("div",{className:"grip end-grip",onMouseDown:r,style:{top:this.props.point.coords.bottom-4,left:this.props.point.coords.left-4}})},e}(E),P=function(t){function e(){t.apply(this,arguments)}return o(e,t),e.prototype.render=function(){var t=this.props.pair.start.coords.left,e=this.props.pair.end.coords.left-t,n=this.props.pair.end.coords.bottom,o=t+e/2-16;return React.createElement("div",{className:"popover",onMouseOver:this.props.onMouseOver,onMouseOut:this.props.onMouseOut,style:{top:n,left:o}},this.props.children)},e}(n.PureComponent),M=function(){function t(t,e,n,o){this.x=t,this.y=e,this.width=n,this.height=o,this.onMouseOver=[],this.onMouseOut=[]}return t.prototype.equals=function(t){return null!==t&&this.x===t.x&&this.y===t.y&&this.width===t.width&&this.height===t.height},t.prototype.contains=function(t,e){var n=this.x<=t&&t<=this.x+this.width,o=this.y<=e&&e<=this.y+this.height;return n&&o},t.prototype.on=function(t,e){switch(t){case"over":this.onMouseOver.push(e);break;case"out":this.onMouseOut.push(e)}},t.prototype.trigger=function(t){for(var e=[],n=1;n<arguments.length;n++)e[n-1]=arguments[n];var o=[];switch(t){case"over":o=this.onMouseOver;break;case"out":o=this.onMouseOut}o.forEach(function(t){t.apply(t,e)})},t}(),b=function(){function t(){this.onMouseOver=[],this.onMouseMove=[],this.onMouseOut=[]}return t.prototype.on=function(t,e){this.getListeners(t).push(e)},t.prototype.off=function(t,e){},t.prototype.trigger=function(t){for(var e=[],n=1;n<arguments.length;n++)e[n-1]=arguments[n];this.getListeners(t).forEach(function(t){t.apply(t,e)})},t.prototype.getListeners=function(t){switch(t){case"over":return this.onMouseOver;case"move":return this.onMouseMove;case"out":return this.onMouseOut;default:return[]}},t}(),S=function(t){function e(){t.call(this),this.clearZones(),this.on("move",this.defaultMouseMove.bind(this)),this.on("out",this.defaultMouseOut.bind(this))}return o(e,t),e.prototype.defaultMouseMove=function(t,e){var n=this,o=function(o){return!!o.contains(t,e)&&(null!==n.activeZone&&!1===n.activeZone.equals(o)&&(n.activeZone.trigger("out"),n.activeZone=null),n.activeZone=o,n.activeZone.trigger("over"),!0)};this.selectionZones.some(o)||this.highlightZones.some(o)||null!==this.activeZone&&(this.activeZone.trigger("out"),this.activeZone=null)},e.prototype.defaultMouseOut=function(t,e){null!==this.activeZone&&!1===this.activeZone.contains(t,e)&&(this.activeZone.trigger("out"),this.activeZone=null)},e.prototype.addZone=function(t,e){switch(t){case"highlight":this.highlightZones.push(e);break;case"selection":this.selectionZones.push(e)}},e.prototype.clearZones=function(){this.clearHighlightZones(),this.clearSelectionZones()},e.prototype.clearHighlightZones=function(){this.highlightZones=[],this.activeZone=null},e.prototype.clearSelectionZones=function(){this.selectionZones=[],this.activeZone=null},e}(b),N=function(t){function e(){t.apply(this,arguments)}return o(e,t),e.prototype.render=function(){return React.createElement("button",{className:"action"+(this.props.arrow?" arrow":""),"data-color":this.props.color,onClick:this.props.onClick},this.props.glyph)},e.defaultProps={arrow:!1},e}(n.PureComponent),A=function(){function t(t){this.prev=null,this.next=null,this.pair=t}return t.prototype.getPair=function(){return this.pair},t.prototype.getStart=function(){return this.pair.start},t.prototype.setStart=function(t){this.pair.start=t},t.prototype.getEnd=function(){return this.pair.end},t.prototype.setEnd=function(t){this.pair.end=t},t.prototype.getPrev=function(){return this.prev},t.prototype.setPrev=function(t){this.prev=t},t.prototype.getNext=function(){return this.next},t.prototype.setNext=function(t){this.next=t},t.prototype.toString=function(){return"("+this.pair.start.index+":"+this.pair.end.index+")"},t}(),Z=function(){function t(){this.head=null,this.tail=null}return t.prototype.clone=function(){var e=new t;return e.head=this.head,e.tail=this.tail,e},t.prototype.getMatches=function(){return this.map(function(t){return{start:t.getStart().index,end:t.getEnd().index}})},t.prototype.insert=function(t){if(null===this.head)return this.head=t,this.tail=t,this.clone();if(t.getEnd().index<=this.head.getStart().index)return this.head.setPrev(t),t.setPrev(null),t.setNext(this.head),this.head=t,this.clone();if(t.getStart().index>=this.tail.getEnd().index)return this.tail.setNext(t),t.setPrev(this.tail),t.setNext(null),this.tail=t,this.clone();for(var e=this.head;null!==e&&null!==e.getNext();){var n=e.getEnd().index,o=e.getNext().getStart().index;if(n<=t.getStart().index&&t.getEnd().index<=o)return t.setPrev(e),t.setNext(e.getNext()),e.setNext(t),e.getNext().getNext().setPrev(t),this.clone();e=e.getNext()}throw new Error("highlight does not fit in list")},t.prototype.remove=function(t){if(this.head===t)return this.head=this.head.getNext(),null!==this.head&&this.head.setPrev(null),this.clone();if(this.tail===t)return this.tail.getPrev().setNext(null),this.tail=this.tail.getPrev(),this.clone();for(var e=this.head;null!==e;){if(e.getNext()===t)return e.setNext(t.getNext()),t.getNext().setPrev(e),this.clone();e=e.getNext()}throw new Error("highlight not found in list")},t.prototype.forEach=function(t){this.map(t)},t.prototype.map=function(t){for(var e=[],n=this.head,o=0;null!==n;)e.push(t(n,o++)),n=n.getNext();return e},t.prototype.reduce=function(t,e){for(var n=this.head,o=0;null!==n;)e=t(e,n,o++),n=n.getNext();return e},t.prototype.toString=function(){return this.reduce(function(t,e,n){return t+(n>0?" -> ":"")+e.toString()},"")},t}(),F=function(t){function e(e){t.call(this,e),this.isDragging=!1,this.mouseoverField=new S,this.state={regex:e.regex,popover:null,highlights:new Z},this.handleEditorChange=this.handleEditorChange.bind(this),this.handleCursorActivity=this.handleCursorActivity.bind(this),this.handleMouseActivity=this.handleMouseActivity.bind(this),this.handleNewPopoverZone=this.handleNewPopoverZone.bind(this),this.delayHideAllPopovers=this.delayHideAllPopovers.bind(this),this.cancelHideAllPopovers=this.cancelHideAllPopovers.bind(this),this.hideAllPopovers=this.hideAllPopovers.bind(this)}return o(e,t),e.prototype.componentDidMount=function(){this.instance=window.CodeMirror.fromTextArea(this.textarea),this.instance.setValue(this.props.corpus),this.document=this.instance.getDoc(),this.instance.on("change",this.handleEditorChange),this.instance.on("cursorActivity",this.handleCursorActivity),this.resetHighlights()},e.prototype.componentWillReceiveProps=function(t){this.setState({regex:t.regex})},e.prototype.componentDidUpdate=function(t,e){e.regex!==this.state.regex&&this.resetHighlights()},e.prototype.isValidRegex=function(t){try{return new RegExp(t,"g"),!0}catch(t){return!1}},e.prototype.handleEditorChange=function(){this.resetHighlights(),this.props.onCorpusChange(this.instance.getValue())},e.prototype.clearSelection=function(){this.document.setCursor(this.document.getCursor())},e.prototype.handleCursorActivity=function(){var t=this;this.mouseoverField.clearSelectionZones(),this.hideAllPopovers(),this.document.somethingSelected()&&this.document.listSelections().forEach(function(e){var n=e.anchor,o=e.head;r(o,n)&&(n=e.head,o=e.anchor);var i=t.document.indexFromPos(n),s=t.instance.charCoords(n,"local"),h={index:i,pos:n,coords:s},a=t.document.indexFromPos(o),c=t.instance.charCoords(o,"local"),l={index:a,pos:o,coords:c},p={start:h,end:l},u=s.left,d=s.top,g=c.left-u,f=c.bottom-d,v=new M(u,d,g,f);v.on("over",t.showAddPopover.bind(t,p)),v.on("out",t.delayHideAllPopovers),t.mouseoverField.addZone("selection",v)})},e.prototype.handleMouseActivity=function(t){var e=t.clientX-this.root.offsetLeft,n=t.clientY-this.root.offsetTop;"mousemove"===t.type?this.mouseoverField.trigger("move",e,n):"mouseout"===t.type&&this.mouseoverField.trigger("out",e,n)},e.prototype.handleNewPopoverZone=function(t,e){t.on("over",this.showRemovePopover.bind(this,e)),t.on("out",this.delayHideAllPopovers),this.mouseoverField.addZone("highlight",t)},e.prototype.showPopover=function(t,e){!1===this.isDragging&&(this.cancelHideAllPopovers(),this.setState({popover:React.createElement(P,{pair:t,onMouseOver:this.cancelHideAllPopovers,onMouseOut:this.delayHideAllPopovers},e)}))},e.prototype.updateMouseoverZones=function(t){var e=this;this.mouseoverField.clearZones(),t.forEach(function(t){var n=t.getStart().coords,o=t.getEnd().coords;if(n.top===o.top){var i=n.left,r=n.top,s=o.left-i,h=o.bottom-r,a=new M(i,r,s,h);a.on("over",e.showRemovePopover.bind(e,t)),a.on("out",e.delayHideAllPopovers),e.mouseoverField.addZone("highlight",a)}else console.error("cannot support multi-line zones")})},e.prototype.handleHighlightsChange=function(t){this.updateMouseoverZones(t),this.props.onMatchesChange(t.getMatches())},e.prototype.showRemovePopover=function(t){var e=this;setTimeout(this.cancelHideAllPopovers,0),this.showPopover(t.getPair(),React.createElement(N,{glyph:"✗",color:"red",arrow:!0,onClick:function(){e.hideAllPopovers(),e.removeHighlight(t)}}))},e.prototype.showAddPopover=function(t){var e=this;this.showPopover(t,React.createElement(N,{glyph:"✓",color:"green",arrow:!0,onClick:function(){e.hideAllPopovers(),e.addHighlight(t),e.clearSelection()}}))},e.prototype.delayHideAllPopovers=function(){this.cancelHideAllPopovers(),this.popoverTimeout=setTimeout(this.hideAllPopovers,500)},e.prototype.cancelHideAllPopovers=function(){clearTimeout(this.popoverTimeout)},e.prototype.hideAllPopovers=function(){this.setState({popover:null}),this.cancelHideAllPopovers()},e.prototype.removeHighlight=function(t){if(this.state.highlights){var e=this.state.highlights.remove(t);this.setState({highlights:e}),this.handleHighlightsChange(e)}},e.prototype.addHighlight=function(t){this.state.highlights||this.setState({highlights:new Z});var e=new A(t),n=null;try{n=this.state.highlights.insert(e)}catch(t){return void alert("matches cannot overlap")}this.setState({highlights:n}),this.handleHighlightsChange(n)},e.prototype.clearHighlights=function(){this.setState({highlights:new Z}),this.handleHighlightsChange(new Z)},e.prototype.resetHighlights=function(){this.clearHighlights();var t=this.getMatchingPointPairs();if(void 0!==t){var e=t.reduce(function(t,e,n){var o=new A(e);return t.insert(o)},new Z);this.setState({highlights:e}),this.handleHighlightsChange(e)}},e.prototype.handleEmptyRegex=function(){this.props.onEmptyRegex()},e.prototype.handleInfiniteMatches=function(){this.props.onInfiniteMatches()},e.prototype.handleBrokenRegex=function(){this.props.onBrokenRegex()},e.prototype.getMatchingPointPairs=function(){var t,e=this;if(""===this.state.regex)return void this.handleEmptyRegex();try{t=new RegExp(this.state.regex,"g")}catch(t){return void this.handleBrokenRegex()}for(var n=[],o=0,i=null;;){if(null==(i=t.exec(this.instance.getValue())))break;if(t.global&&o===t.lastIndex)return void this.handleInfiniteMatches();o=i.index,n.push({start:o,end:i.index+i[0].length})}return n.map(function(t){var n=t.start,o=e.document.posFromIndex(n),i=e.instance.charCoords(o,"local"),r={index:n,pos:o,coords:i},s=t.end,h=e.document.posFromIndex(s);return{start:r,end:{index:s,pos:h,coords:e.instance.charCoords(h,"local")}}})},e.prototype.addDraggingClass=function(){window.document.body.classList.add("grabbing-cursor")},e.prototype.removeDraggingClass=function(){window.document.body.classList.remove("grabbing-cursor")},e.prototype.handleDragStart=function(t,e,n){var o=this;this.addDraggingClass(),this.clearSelection();var i=null,r=function(n){o.handleDrag(t,e,n.pageX,n.pageY)}.bind(this),s=function(t){o.removeDraggingClass(),o.handleDragStop(i,t.pageX,t.pageY),d(window.document.body,"mousemove",r)}.bind(this);this.isDragging=!0,this.hideAllPopovers(),i=this.setReadOnly(),p(window.document.body,"mousemove",r),u(window,"mouseup",s)},e.prototype.handleDrag=function(t,e,n,o){var p=!1===e?l(this.document,t.getStart().pos):null===t.getPrev()?h(this.document):t.getPrev().getEnd().pos,u=e?c(this.document,t.getEnd().pos):null===t.getNext()?a(this.document):t.getNext().getStart().pos,d=this.instance.coordsChar({left:n,top:o},"page");if(r(d,p)?d=p:s(d,u)&&(d=u),!1===i(e?t.getStart().pos:t.getEnd().pos,d)){var g=this.document.indexFromPos(d),f=this.instance.charCoords(d,"local");e?t.setStart({index:g,pos:d,coords:f}):t.setEnd({index:g,pos:d,coords:f})}this.setState({highlights:this.state.highlights.clone()})},e.prototype.handleDragStop=function(t,e,n){this.isDragging=!1,this.unsetReadOnly(t)},e.prototype.setReadOnly=function(){var t=null;return this.instance.hasFocus()&&(t=this.document.getCursor()),this.instance.setOption("readOnly","nocursor"),t},e.prototype.unsetReadOnly=function(t){this.instance.setOption("readOnly",!1),null!==t&&(this.instance.focus(),this.document.setCursor(t))},e.prototype.collectGrips=function(){var t=this;return this.state.highlights?this.state.highlights.reduce(function(e,n){return e.push(React.createElement(C,{key:e.length,point:n.getStart(),onDragStart:t.handleDragStart.bind(t,n,!0)})),e.push(React.createElement(w,{key:e.length,point:n.getEnd(),onDragStart:t.handleDragStart.bind(t,n,!1)})),e},[]):[]},e.prototype.render=function(){var t=this;return React.createElement("div",{className:"corpus-editor",ref:function(e){t.root=e},onMouseMove:this.handleMouseActivity,onMouseOut:this.handleMouseActivity},React.createElement(x,null,this.state.popover,this.collectGrips()),React.createElement("textarea",{ref:function(e){t.textarea=e}}),React.createElement(R,{highlightList:this.state.highlights,colors:this.props.colors,onNewPopoverZone:this.handleNewPopoverZone}))},e}(n.Component),H=function(t){function n(e){t.call(this,e),this.state={regex:this.props.regex,corpus:this.props.corpus,matches:[],hasFix:!1,fixedRegex:"",inError:!1,message:""},this.handleRegexChange=this.handleRegexChange.bind(this),this.handleRequestFix=this.handleRequestFix.bind(this),this.handleAcceptFix=this.handleAcceptFix.bind(this),this.handleRejectFix=this.handleRejectFix.bind(this),this.handleCorpusChange=this.handleCorpusChange.bind(this),this.handleMatchesChange=this.handleMatchesChange.bind(this),this.handleEmptyRegex=this.handleEmptyRegex.bind(this),this.handleInfiniteMatches=this.handleInfiniteMatches.bind(this),this.handleBrokenRegex=this.handleBrokenRegex.bind(this)}return o(n,t),n.prototype.handleRegexChange=function(t){this.setState({regex:t})},n.prototype.handleRequestFix=function(){var t=this,n=this.state.matches.map(function(t){return{left:t.start,right:t.end}});e.post("/api/fix").send({regex:this.state.regex,ranges:n,corpus:this.state.corpus}).end(function(e,n){if(null!=e||200!==n.status)throw new Error("did not receive fix from server");var o=JSON.parse(n.text).fix;if("string"!=typeof o)throw new Error("did not receive fix from server");o=o.replace(/\\\\/g,"\\"),t.setState({hasFix:!0,fixedRegex:o})})},n.prototype.handleAcceptFix=function(){this.setState({regex:this.state.fixedRegex,hasFix:!1,fixedRegex:""})},n.prototype.handleRejectFix=function(){this.setState({hasFix:!1,fixedRegex:""})},n.prototype.handleCorpusChange=function(t){this.setState({corpus:t})},n.prototype.handleMatchesChange=function(t){this.setState({matches:t,inError:!1,message:t.length+" matches"})},n.prototype.handleEmptyRegex=function(){this.setState({inError:!1,message:"Empty"})},n.prototype.handleInfiniteMatches=function(){this.setState({inError:!0,message:"Infinite"})},n.prototype.handleBrokenRegex=function(){this.setState({inError:!0,message:"Error"})},n.prototype.render=function(){return React.createElement("div",null,React.createElement(f,{regex:this.state.regex,onRegexChange:this.handleRegexChange},React.createElement(v,null,React.createElement(N,{glyph:"?",color:"blue",onClick:this.handleRequestFix}),React.createElement(y,{inError:this.state.inError},this.state.message)),this.state.hasFix&&React.createElement(m,{regex:this.state.fixedRegex},React.createElement(N,{glyph:"✓",color:"green",onClick:this.handleAcceptFix}),React.createElement(N,{glyph:"✗",color:"red",onClick:this.handleRejectFix}))),React.createElement(F,{regex:this.state.regex,corpus:this.state.corpus,colors:this.props.colors,onCorpusChange:this.handleCorpusChange,onMatchesChange:this.handleMatchesChange,onEmptyRegex:this.handleEmptyRegex,onInfiniteMatches:this.handleInfiniteMatches,onBrokenRegex:this.handleBrokenRegex}))},n}(n.Component);t.App=H}(this.frontend=this.frontend||{},superagent,React);
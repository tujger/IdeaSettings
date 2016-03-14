window.console.log("INSTALL ODYSSEY MOBILE API");
function OdysseyMobileUpdateView(newMode){

	OdysseyMobileAPI.increaseRequestsCounter();

	var getMode = function(newM){
	    if(newM)window.OdysseyMobileMode = newM*1;
	    return window.OdysseyMobileMode;
	};
	var mode = getMode(newMode);
	console.log("SET MODE TO "+mode);

	appPanelFadeIn = function(){};
	appPanelFadeOut = function(){};

    /*document.getElementById("dbBBButton").children[0].click();*/

    var header = document.getElementById("dbHeader");
    var central = document.getElementById("dbCentralContentPane");
        var toolbar = document.getElementById("dbNavToolbar");
        var apps = document.getElementById("dbContentNavContainer");
            var appsHeader = document.getElementById("dbNavHeaderMainContainer");
            var linksHeader = document.getElementById("dbMyLinks");
        var content = document.getElementById("dbContentPanel");
            var messages = document.getElementById("dbBBMsgPanel");
                var messagesHeader = document.getElementById("dbBBMsgPanelHeader");
            var inbox = document.getElementById("dbInboxMainContainer");
    var footer = document.getElementById("dbFooterContentPane");
    var popup = document.getElementById("modalLookupFrame");
    var timeout = document.getElementsByClassName("TimeoutMessageContainer");

	if(timeout && timeout.length>0){
        console.log("RELOGIN by timeout");
		OdysseyMobileAPI.login();
		return;
	}

	if(document.body.innerHTML.indexOf("Looks like you have arrived at the wrong gate")>0){
		console.log("REDIRECT BECAUSE OF WRONG GATE "+window.location.href);
        window.location.pathname = "/odyssey/index.ody";
        /*OdysseyMobileAPI.refresh();*/
		return;
	}

	try{
	    if(appsHeader)appsHeader.style.display = "none";
        else {
            console.log("CLICK apps");
            /*document.getElementById("dbAppButton").children[0].click();
            return;*/
        }

		console.log("HIDE header");
        header.style.display = "none";
		console.log("HIDE central");
	    central.style.display = "none";
		console.log("HIDE toolbar");
	    toolbar.style.display = "none";
		console.log("HIDE apps");
	    apps.style.display = "none";
	    if(linksHeader)linksHeader.style.width = "100%";

		console.log("HIDE content");
	    content.style.display = "none";
		console.log("HIDE messages");
	    messages.style.display = "none";
	    messages.style.overflow = "";
		console.log("HIDE messagesHeader");
	    messagesHeader.style.display = "none";
		console.log("HIDE inbox");
	    inbox.style.display = "none";
		console.log("HIDE footer");
	    footer.style.display = "none";

	    if(central){
	        central.className = "";
	        central.style.margin = "0px";
	    }
	    if(content){
	        content.style.display = "none";
	        content.style.margin = "0px";
	    }

	    if(mode == 100001){
			console.log("SHOW apps");
	        apps.style.width = "100%";
		    apps.style.margin = "0px";
	        apps.style.display = "block";
	        central.style.display = "block";
	    }else if(mode == 100002){
			console.log("SHOW messages");
	        messages.style.display = "block";
	        content.style.display = "block";
	        central.style.display = "block";
	    }else if(mode == 100003){
			console.log("SHOW inbox");
	        inbox.style.display = "block";
	        content.style.display = "block";
	        central.style.display = "block";
	    }else if(mode == 100004){
			console.log("SHOW profile");
	        var p = document.getElementsByClassName("dbHeaderProfile")[0];
	        p.onclick();
	    }else if(mode == 100006){
			console.log("SHOW info");
	        var p = document.getElementsByClassName("dbHeaderAbout")[0];
	        p.onclick();
	    }

	    /* adapt containers */
	    var p = document.getElementById("popupContainer");
	    if(p){
            if(p.style.display=="block"){
				console.log("SHOW from popupContainer");
			    p.style.position = "absolute";
	            p.style.top = "9999px";

	            p = document.getElementById("popupTitleBar");
	            if(p){
	                p.style.display = "none";
	            }
                p = document.getElementById("modalLookupFrame");
                if(p){
                    p.style.position = "fixed";
                    p.style.left = "0px";
                    p.style.top = "0px";
                    p.style.width = "100%";
                    p.style.height = "100%";
                }
                p = document.getElementById("modalLookupFrame");
                if(p){
                    p = p.contentDocument || p.contentWindow.document;
                    if(p){
                        p = p.body.getElementsByClassName("deployPropertyFormContainer")[0];
                        if(p){
		                    p.style.width = "100%";
		                    p.style.height = "100%";
                        }
                    }
                }
            }
	    }
		setTimeout(function(){OdysseyMobileAPI.show();},100)

	}catch(e){
		console.log("ERROR "+e);
        OdysseyMobileAPI.error(e.message);
	}

	OdysseyMobileAPI.resetRequestsCounter();

};
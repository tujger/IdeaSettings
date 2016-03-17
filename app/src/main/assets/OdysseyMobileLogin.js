function OdysseyMobileLogin(username,password){
	console.log("INSTALL ODYSSEY MOBILE LOGIN API");

	if(document.body.innerHTML.indexOf("This Odyssey Server has been accessed via an unlicensed IP or domain name")>0){
	    console.log("ERROR Trying to access Odyssey Server via an unlicensed IP or domain name");
		OdysseyMobileAPI.error("Trying to access Odyssey Server via an unlicensed IP or domain name.");
		return;
	}

    var isOdyssey = !!document.title.match("Odyssey");
    var hasError = document.getElementsByClassName("systemError") && document.getElementsByClassName("systemError")[0];
    var hasDocument = document.body.innerHTML.length > 10;
    var hasLoginForm = !!document.getElementById("frmAdmin");
    var hasContent = !!document.getElementById("dbContentPanel");
    console.log("FOUND isOdyssey:"+isOdyssey+" hasDocument:"+hasDocument+" hasError:"+hasError+" hasLoginForm:"+hasLoginForm+" hasContent:"+hasContent);
    if(isOdyssey && hasDocument){
        if(hasLoginForm){
            if(!hasError){
                try{
                    window.name = "";

                    console.log("TRY LOGIN AS "+username+":"+password);
	                var a = document.getElementById("OdysseyMobile");
	                if(a)a.value = true;
	                a = document.getElementById("userId");
	                console.log("SET VALUE userId");
	                a.autocomplete="off";
	                a.value = username;
	                a = document.getElementById("password");
	                console.log("SET VALUE password");
	                a.autocomplete="off";
	                a.value = password;
	                a = document.getElementById("loginBtn");
	                console.log("CLICK loginBtn");
	                OdysseyMobileAPI.showLoginScreen();
	                a.onclick();
	            }catch(e){
	                console.log("ERROR "+e);
                    OdysseyMobileAPI.error(e.message);
	            }
            }else{
                console.log("ERROR "+hasError.innerHTML);
                OdysseyMobileAPI.error(hasError.innerHTML);
            }
        }else if(hasContent){
            console.log("SUCCESS already logged in");
            OdysseyMobileAPI.loginSuccess();
        }else{
            console.log("ERROR The old version of Odyssey");
            OdysseyMobileAPI.error("The old version of Odyssey server.");
        }
    }
};

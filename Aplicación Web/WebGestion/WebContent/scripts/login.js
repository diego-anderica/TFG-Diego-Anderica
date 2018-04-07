function loginJSP() {
	var txtCorreo = document.getElementById("txtCorreo");
	var txtContrasena = document.getElementById("txtContrasena");
	var txtCifrada = null;
	txtCorreo.style.borderColor = "initial";
	txtContrasena.style.borderColor = "initial";
	
	if (comprobarCampos(txtCorreo, txtContrasena)) {
		var reqLogin = new XMLHttpRequest();
		
		reqLogin.open("post", "cifrarContrasena.jsp");
		reqLogin.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		
		reqLogin.onreadystatechange = function() {
			if (reqLogin.readyState == 4) {
				if (reqLogin.status == 200) {
					var result = JSON.parse(reqLogin.responseText);
					if (result.resultado == "OK") {
						txtCifrada = result.cifrada;
						accederFirestore(txtCorreo, txtCifrada, result.sesion)
					} else {
						alert(result.mensaje);
					}
				} else {
					alert("Se ha producido un error en la comunicación con el servidor.");
				}
			}
		};

		var p = {
			tipo : "LOGIN",
			correo : txtCorreo.value,
			contrasena : txtContrasena.value
		};

		reqLogin.send("p=" + JSON.stringify(p));
	}
}
			
function accederFirestore(txtCorreo, txtCifrada, sesion) {
	var docRef = firestore.collection("UsuariosWeb");
	var query = docRef.where("Correo", "==", txtCorreo.value.toString());
	var myData = null;
	var encontrado = false;
	document.body.style.cursor = "progress";
	
	query.get().then(function(querySnapshot){
		querySnapshot.forEach(function(doc) {
			myData = doc.data();
            if (myData.Correo == txtCorreo.value) {
            		encontrado = true;
            		if (txtCifrada == myData.Contrasena) {
            			document.body.style.cursor = "auto";
            			alert("Bienvenido, " + myData.Correo);
            			sessionStorage.setItem("sesion", sesion);
            			sessionStorage.setItem("correo", txtCorreo.value);
            			pasarVariable("index.html", "login");
            		} else {
            			alert("La contraseña para " + myData.Correo + " es incorrecta");
            			document.body.style.cursor = "auto";
            		}
            }
        })
        if (encontrado == false) {
			alert("El correo " + txtCorreo.value + " no se encuentra en la base de datos");
			document.body.style.cursor = "auto";
        }
	});				
}
			
function comprobarCampos(txtCorreo, txtContrasena) {
	var lblError = document.getElementById("lblError");
	
	if (txtCorreo.value == "") {
		alert("El campo de correo electrónico no puede estar vacío");
		txtCorreo.style.borderColor = "red";
	} else if (txtCorreo.value.indexOf('@') == -1) {
		alert("La dirección de correo introducida no es válida");
		txtCorreo.style.borderColor = "red";
	} else if (txtContrasena.value == "") {
		alert("El campo de contraseña no puede estar vacío");
		txtContrasena.style.borderColor = "red";
	} else {
		return true;
	}
	
	return false;
}

function pasarVariable(pagina) {
	window.location.replace("index.html");
}
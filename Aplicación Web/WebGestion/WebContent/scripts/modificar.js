window.onbeforeunload = function() {
	return "¿Desea realmente abandonar la página?";
}

function rellenarLista() {
	var select = document.getElementById("cbFamilias");
	var opt = null;
	
	firestore.collection("Usuarios").get().then(function(querySnapshot) {
	    querySnapshot.forEach(function(doc) {
	        opt = document.createElement('option');
	        opt.value = doc.id;
	        opt.innerHTML = doc.id;
	        select.appendChild(opt);
	        rellenarDatos(select);
	    });
	});
}

function rellenarDatos(cb) {
	var txtNombreT1 = document.getElementById("txtNombreT1");
	var txtApellido1T1 = document.getElementById("txtApellido1T1");
	var txtApellido2T1 = document.getElementById("txtApellido2T1");
	var txtTfnoT1 = document.getElementById("txtTfnoT1");
	var txtCorreoT1 = document.getElementById("txtCorreoT1");
	
	var txtNombreT2 = document.getElementById("txtNombreT2");
	var txtApellido1T2 = document.getElementById("txtApellido1T2");
	var txtApellido2T2 = document.getElementById("txtApellido2T2");
	var txtTfnoT2 = document.getElementById("txtTfnoT2");
	var txtCorreoT2 = document.getElementById("txtCorreoT2");
	
	var usuarioBBDD = cb.options[cb.selectedIndex].text;
	
	var docRef = firestore.collection("Usuarios").doc(usuarioBBDD);

	docRef.get().then(function(doc) {
	    if (doc.exists) {
			datos = doc.data();
			txtNombreT1.value = datos.NombreTutor1;
			txtApellido1T1.value = datos.Apellido1Tutor1;
			txtApellido2T1.value = datos.Apellido2Tutor1;
			txtTfnoT1.value = datos.TelefonoTutor1;
			txtCorreoT1.value = datos.CorreoTutor1;

			txtNombreT2.value = datos.NombreTutor2;
			txtApellido1T2.value = datos.Apellido1Tutor2;
			txtApellido2T2.value = datos.Apellido2Tutor2;
			txtTfnoT2.value = datos.TelefonoTutor2;
			txtCorreoT2.value = datos.CorreoTutor2;
	    }
	}).catch(function(error) {
		console.log(error);
	    alert ("Ha ocurrido un error al obtener los datos.");
	});
}

function modificarUsuario(btn){
	var cbFamilias = document.getElementById("cbFamilias");
	var usuarioBBDD = cbFamilias.options[cbFamilias.selectedIndex].text;
	var docRef = firestore.collection("Usuarios").doc(usuarioBBDD);
	
	var txtNombreT1 = document.getElementById("txtNombreT1");
	var txtApellido1T1 = document.getElementById("txtApellido1T1");
	var txtApellido2T1 = document.getElementById("txtApellido2T1");
	var txtTfnoT1 = document.getElementById("txtTfnoT1");
	var txtCorreoT1 = document.getElementById("txtCorreoT1");
	
	var txtNombreT2 = document.getElementById("txtNombreT2");
	var txtApellido1T2 = document.getElementById("txtApellido1T2");
	var txtApellido2T2 = document.getElementById("txtApellido2T2");
	var txtTfnoT2 = document.getElementById("txtTfnoT2");
	var txtCorreoT2 = document.getElementById("txtCorreoT2");
	
	if ($("#" + btn.id).text() == "Modificar Usuario/Familia") {
		habilitarCampos(1);
		modificarBoton(1);
	} else {
		if (confirm ("Se va a proceder a modificar el usuario " + usuarioBBDD + " con los nuevos datos. ¿Continuar?")) {
			docRef.update({
    				"NombreTutor1"    : txtNombreT1.value,
    				"Apellido1Tutor1" : txtApellido1T1.value,
    				"Apellido2Tutor1" : txtApellido2T1.value,
    				"TelefonoTutor1"  : txtTfnoT1.value,
    				"CorreoTutor1"    : txtCorreoT1.value,
    				"NombreTutor2"    : txtNombreT2.value,
    				"Apellido1Tutor2" : txtApellido1T2.value,
    				"Apellido2Tutor2" : txtApellido2T2.value,
    				"TelefonoTutor2"  : txtTfnoT2.value,
    				"CorreoTutor2"    : txtCorreoT2.value
    			})
			.then(function() {
			    alert("Se ha modificado correctamente el usuario " + usuarioBBDD);
			}).catch(function(error) {
				alert ("Ha ocurrido un error al modificar el usuario.");
			});
			
			habilitarCampos(0);
			modificarBoton(0);
		}
	}
}

function habilitarCampos(token) {
	var cbFamilias = document.getElementById("cbFamilias");
	
	var txtNombreT1 = document.getElementById("txtNombreT1");
	var txtApellido1T1 = document.getElementById("txtApellido1T1");
	var txtApellido2T1 = document.getElementById("txtApellido2T1");
	var txtTfnoT1 = document.getElementById("txtTfnoT1");
	var txtCorreoT1 = document.getElementById("txtCorreoT1");
	
	var txtNombreT2 = document.getElementById("txtNombreT2");
	var txtApellido1T2 = document.getElementById("txtApellido1T2");
	var txtApellido2T2 = document.getElementById("txtApellido2T2");
	var txtTfnoT2 = document.getElementById("txtTfnoT2");
	var txtCorreoT2 = document.getElementById("txtCorreoT2");
	
	if (token == 1) {
		cbFamilias.disabled = true;
		txtNombreT1.disabled = false;
		txtApellido1T1.disabled = false;
		txtApellido2T1.disabled = false;
		txtTfnoT1.disabled = false;
		txtCorreoT1.disabled = false;
		
		txtNombreT2.disabled = false;
		txtApellido1T2.disabled = false;
		txtApellido2T2.disabled = false;
		txtTfnoT2.disabled = false;
		txtCorreoT2.disabled = false;
	} else if (token == 0) {
		cbFamilias.disabled = false;
		txtNombreT1.disabled = true;
		txtApellido1T1.disabled = true;
		txtApellido2T1.disabled = true;
		txtTfnoT1.disabled = true;
		txtCorreoT1.disabled = true;
		
		txtNombreT2.disabled = true;
		txtApellido1T2.disabled = true;
		txtApellido2T2.disabled = true;
		txtTfnoT2.disabled = true;
		txtCorreoT2.disabled = true;
	}
}

function modificarBoton (token) {
	if (token == 1) {
		$("#btnModificar").text("Confirmar Modificación");
		$("#btnModificar").removeClass("btn btn-primary btn-block");
		$("#btnModificar").addClass("btn btn-success btn-block");
		
		$("#btnCancelar").removeClass("btn btn-danger btn-block invisible");
		$("#btnCancelar").addClass("btn btn-danger btn-block");
	} else if (token == 0) {
		$("#btnModificar").text("Modificar Usuario/Familia");
		$("#btnModificar").removeClass("btn btn-success btn-block");
		$("#btnModificar").addClass("btn btn-primary btn-block");
		
		$("#btnCancelar").removeClass("btn btn-danger btn-block");
		$("#btnCancelar").addClass("btn btn-danger btn-block invisible");
	}
}

function cancelarModificacion() {				
	habilitarCampos(0);
	modificarBoton(0);
}
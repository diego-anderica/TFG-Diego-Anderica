
/* var txtFamilia = document.getElementById("txtFamilia");
var txtApellido1T1 = document.getElementById("txtApellido1T1");
var valFamilia = null; */

window.onbeforeunload = function() {
	return "¿Desea realmente abandonar la página?";
}

function habilitarDatosT2() {
	var btnT2 = document.getElementById("btnT2");
	var txtNombreT2 = document.getElementById("txtNombreT2");
	var txtApellido1T2 = document.getElementById("txtApellido1T2");
	var txtApellido2T2 = document.getElementById("txtApellido2T2");
	var txtTfnoT2 = document.getElementById("txtTfnoT2");
	var txtCorreoT2 = document.getElementById("txtCorreoT2");
	
	if (txtApellido1T2.disabled == true) {
		$("#btnT2").text("Deshabiliar Campos Tutor 2");
		txtNombreT2.disabled = false;
		txtApellido1T2.disabled = false;
		txtApellido2T2.disabled = false;
		txtTfnoT2.disabled = false;
		txtCorreoT2.disabled = false;
	} else if (txtApellido1T2.disabled == false) {
		$("#btnT2").text("Habiliar Campos Tutor 2");
		txtNombreT2.disabled = true;
		txtApellido1T2.disabled = true;
		txtApellido2T2.disabled = true;
		txtTfnoT2.disabled = true;
		txtCorreoT2.disabled = true;
		
		txtNombreT2.value = null;
		txtApellido1T2.value = null;
		txtApellido2T2.value = null;
		txtTfnoT2.value = null;
		txtCorreoT2.value = null;
		
		generarFamilia();
	}
}

function generarFamilia() {
	var txtFamilia = document.getElementById("txtFamilia");
	var txtApellido1T1 = document.getElementById("txtApellido1T1");
	var txtApellido1T2 = document.getElementById("txtApellido1T2");
	
	txtFamilia.value = "familia" + txtApellido1T1.value.normalize('NFD').replace(/[\u0300-\u036f]/g, "") + txtApellido1T2.value.normalize('NFD').replace(/[\u0300-\u036f]/g, "");
}

/* function rellenarFamilia(id) {
	if (id == "txtApellido1T1") {
		if (txtApellido1T1.value == "") {
			txtFamilia.value = null;
		} else {
			if (txtApellido1T2.value == "") {
				//https://stackoverflow.com/questions/990904/remove-accents-diacritics-in-a-string-in-javascript
				txtFamilia.value = "familia" + txtApellido1T1.value.normalize('NFD').replace(/[\u0300-\u036f]/g, "");
				valFamilia = "familia" + txtApellido1T1.value.normalize('NFD').replace(/[\u0300-\u036f]/g, "");
			} else {

			}
		}
	} else if (id == "txtApellido1T2") {
		txtFamilia.value = valFamilia + txtApellido1T2.value.normalize('NFD').replace(/[\u0300-\u036f]/g, "");
	}
} */

function darDeAlta() {
	if(comprobarCampos()) {
		if (confirm("¿Los datos introducidos son correctos?")) {
			document.body.style.cursor = "progress";
			crearDocumento(document.getElementById("txtFamilia").value, 0);
		}
	}
}

function comprobarCampos() {
	var continuar = false;
	var txtFamilia = document.getElementById("txtFamilia");
	var txtNombreT1 = document.getElementById("txtNombreT1");
	var txtApellido1T1 = document.getElementById("txtApellido1T1");
	var txtTfnoT1 = document.getElementById("txtTfnoT1");
	var txtCorreoT1 = document.getElementById("txtCorreoT1");
	
	var txtNombreT2 = document.getElementById("txtNombreT2");
	var txtApellido1T2 = document.getElementById("txtApellido1T2");
	var txtTfnoT2 = document.getElementById("txtTfnoT2");
	var txtCorreoT2 = document.getElementById("txtCorreoT2");
	
	if (txtNombreT1.value == "" || txtApellido1T1.value == "") {
		alert ("Escriba nombre y apellidos válidos en los campos del Tutor 1.");
	} else {
		if (txtTfnoT1.value == "" && txtCorreoT1.value == "") {
			alert ("Escriba un número de teléfono o correo válidos para el Tutor 1.");
		} else {
			if (txtTfnoT1.value != "" && txtTfnoT1.value.charAt("0") != "+") {
				alert ("Escriba un código de país válido para el número de teléfono del Tutor 1.");
			} else {
				if (txtCorreoT1.value != "" && txtCorreoT1.value.indexOf("@") == -1) {
					alert ("Escriba una dirección de correo válida para el número de teléfono del Tutor 1.");
				} else {
					if (txtNombreT2.disabled == false) {
						if (txtNombreT2.value == "" || txtApellido1T2.value == "") {
							alert ("Escriba nombre y apellidos válidos en los campos del Tutor 2.");
						} else {
							if (txtTfnoT2.value == "" && txtCorreoT2.value == "") {
								alert ("Escriba un número de teléfono o correo válidos para el Tutor 2.");
							} else {
								if (txtTfnoT2.value != "" && txtTfnoT2.value.charAt("0") != "+") {
									alert ("Escriba un código de país válido para el número de teléfono del Tutor 2.");
								} else {
									if (txtCorreoT2.value != "" && txtCorreoT2.value.indexOf("@") == -1) {
										alert ("Escriba una dirección de correo válida para el número de teléfono del Tutor 2.");
									} else {
										continuar = true;
									}
								}
							}
						}
					} else {
						continuar = true;
					}
				}
			}
		}
	}
	
	if ((txtFamilia.value == "familia" || txtFamilia.value == "") && continuar) {
		alert ("No se ha generado un nombre de familia válido. Se asignará uno automáticamente.");
		
	}
	
	generarFamilia();
	
	return continuar;
}

function crearDocumento(familia, i) {
	var txtNombreT1 = document.getElementById("txtNombreT1").value;
	var txtApellido1T1 = document.getElementById("txtApellido1T1").value;
	var txtApellido2T1 = document.getElementById("txtApellido2T1").value;
	var txtTfnoT1 = document.getElementById("txtTfnoT1").value;
	var txtCorreoT1 = document.getElementById("txtCorreoT1").value;
	
	var txtNombreT2 = document.getElementById("txtNombreT2").value;
	var txtApellido1T2 = document.getElementById("txtApellido1T2").value;
	var txtApellido2T2 = document.getElementById("txtApellido2T2").value;
	var txtTfnoT2 = document.getElementById("txtTfnoT2").value;
	var txtCorreoT2 = document.getElementById("txtCorreoT2").value;
	
	var newDoc = firestore.collection("Usuarios").doc(familia + i);
	
	newDoc.get().then(function(doc) {
	    if (doc.exists) {
	        crearDocumento(familia, i + 1);
	    } else {
	    		newDoc.set({
	    			"NombreTutor1"    : txtNombreT1,
	    			"Apellido1Tutor1" : txtApellido1T1,
	    			"Apellido2Tutor1" : txtApellido2T1,
	    			"TelefonoTutor1"  : txtTfnoT1,
	    			"CorreoTutor1"    : txtCorreoT1,
	    			"NombreTutor2"    : txtNombreT2,
	    			"Apellido1Tutor2" : txtApellido1T2,
	    			"Apellido2Tutor2" : txtApellido2T2,
	    			"TelefonoTutor2"  : txtTfnoT2,
	    			"CorreoTutor2"    : txtCorreoT2
	    		}).then(function() {
				    alert ("Se ha creado el usuario " + familia + i);
				    document.body.style.cursor = "auto";
				    location.reload();
					});
	    }
	});

}
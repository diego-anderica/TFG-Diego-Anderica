var i = 1;
			
function buscarEnBBDD() {
	var txtBusqueda = document.getElementById("txtBusqueda").value;
	var cbCampoBusqueda = document.getElementById("cbCampoBusqueda");
	var filtro = null;
	var myData = null;
	
	restaurarTabla();
	
	if (txtBusqueda == "") {
		firestore.collection("Usuarios").get().then(function(querySnapshot) {
		    querySnapshot.forEach(function(doc) {
		        myData = doc.data();
		        rellenarFila(doc.id, myData);
		        addFila();
		    });
		});
	} else {
		filtro = cbCampoBusqueda.options[cbCampoBusqueda.selectedIndex].text;
		buscarConFiltro(filtro, txtBusqueda);
	}
}

function rellenarFila(nombreFamilia, datos) {
	var familia = document.getElementById("familia" + i.toString());
	var nombreT1 = document.getElementById("nombreT1" + i.toString());
	var nombreT2 = document.getElementById("nombreT2" + i.toString());
	var tfnoT1 = document.getElementById("tfnoT1" + i.toString());
	var tfnoT2 = document.getElementById("tfnoT2" + i.toString());
	var correoT1 = document.getElementById("correoT1" + i.toString());
	var correoT2 = document.getElementById("correoT2" + i.toString());
	
	familia.value = nombreFamilia;
	nombreT1.value = datos.NombreTutor1 + " " + datos.Apellido1Tutor1 + " " + datos.Apellido2Tutor1;
	
	if (datos.NombreTutor2 != ""){
		nombreT2.value = datos.NombreTutor2 + " " + datos.Apellido1Tutor2 + " " + datos.Apellido2Tutor2;
	}
	
	tfnoT1.value = datos.TelefonoTutor1;
	tfnoT2.value = datos.TelefonoTutor2;
	correoT1.value = datos.CorreoTutor1;
	correoT2.value = datos.CorreoTutor2;
}

function addFila() {
	var numero = "<td>" + (i + 1) + "</td>";
	var familia = "<td><input type='text' name='familia' id='familia" + (i + 1) + "' placeholder='Nombre Familia' class='form-control' disabled /></td>";
	var nombreT1 = "<td><input type='text' name='nombreT1' id='nombreT1" + (i + 1) + "' placeholder='Nombre Tutor 1' class='form-control' disabled /></td>";
	var nombreT2 = "<td><input type='text' name='nombreT2' id='nombreT2" + (i + 1) + "' placeholder='Nombre Tutor 2' class='form-control' disabled /></td>";
	var tfnoT1 = "<td><input type='text' name='tfnoT1' id='tfnoT1" + (i + 1) + "' placeholder='Tfno. Tutor 1' class='form-control' disabled /></td>";
	var tfnoT2 = "<td><input type='text' name='tfnoT2' id='tfnoT2" + (i + 1) + "' placeholder='Tfno. Tutor 2' class='form-control' disabled /></td>";
	var correoT1 = "<td><input type='text' name='correoT1' id='correoT1" + (i + 1) + "' placeholder='Correo Tutor 1' class='form-control' disabled /></td>";
	var correoT2 = "<td><input type='text' name='correoT2' id='correoT2" + (i + 1) + "' placeholder='Correo Tutor 2' class='form-control' disabled /></td>";
	
	$('#addr' + i).html(numero + familia + nombreT1 + tfnoT1 + correoT1 + nombreT2 + tfnoT2 + correoT2);
	$('#tblResultados').append('<tr id="addr' + (i + 1) + '"></tr>');
	
	i++; 
}

function restaurarTabla() {
	var familia = document.getElementById("familia1");
	var nombreT1 = document.getElementById("nombreT11");
	var nombreT2 = document.getElementById("nombreT21");
	var tfnoT1 = document.getElementById("tfnoT11");
	var tfnoT2 = document.getElementById("tfnoT21");
	var correoT1 = document.getElementById("correoT11");
	var correoT2 = document.getElementById("correoT21");
	
	familia.value = "";
	nombreT1.value = "";
	nombreT2.value = "";
	tfnoT1.value = "";
	tfnoT2.value = "";
	correoT1.value = "";
	correoT2.value = "";
	
	while (i > 1) {
		$("#addr" + (i - 1)).html('');
		i--;
	}
}

function buscarConFiltro(filtro, txtBusqueda){
	var usuariosRef = firestore.collection("Usuarios");
	var campoConsulta = null;
	var consulta = null
	var myData = null;
	
	if (filtro == "Familia") {
		consulta = usuariosRef.doc(txtBusqueda);
		
		consulta.get().then(function(doc) {
		    if (doc.exists) {
		    		myData = doc.data();
		        rellenarFila(doc.id, myData);
		    } else {
		        alert("No se ha encontrado ningún usuario con los datos especificados.");
		    }
		}).catch(function(error) {
		    alert("Ha ocurrido un error en la comunicación con el servidor de base de datos.");
		});
	} else if (filtro == "Nombre Tutor 1") {
		campoConsulta = "NombreTutor1";
	} else if (filtro == "Apellido 1 Tutor 1") {
		campoConsulta = "Apellido1Tutor1";
	} else if (filtro == "Apellido 2 Tutor 1") {
		campoConsulta = "Apellido2Tutor1";
	} else if (filtro == "Nombre Tutor 2") {
		campoConsulta = "NombreTutor2";
	} else if (filtro == "Apellido 1 Tutor 2") {
		campoConsulta = "Apellido1Tutor1";
	} else if (filtro == "Apellido 2 Tutor 2") {
		campoConsulta = "Apellido1Tutor1";
	} else if (filtro == "Tfno. Tutor 1") {
		campoConsulta = "TelefonoTutor1";
	} else if (filtro == "Tfno. Tutor 2") {
		campoConsulta = "TelefonoTutor2";
	} else if (filtro == "Correo Tutor 1") {
		campoConsulta = "CorreoTutor1";
	} else if (filtro == "Correo Tutor 2") {
		campoConsulta = "CorreoTutor2";
	}
	
	if (campoConsulta != null){
		usuariosRef.where(campoConsulta, "==", txtBusqueda).get()
	    .then(function(querySnapshot) {
	        querySnapshot.forEach(function(doc) {
		    		myData = doc.data();
		        rellenarFila(doc.id, myData);
	        });
	    })
	    .catch(function(error) {
		    alert("Ha ocurrido un error en la comunicación con el servidor de base de datos.");
	    });
	}
	
}
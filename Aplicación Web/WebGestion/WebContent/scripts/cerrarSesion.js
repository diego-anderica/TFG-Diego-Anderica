function cerrarSesion(){
	if (confirm("¿Desea realmente cerrar su sesión, " + sessionStorage.getItem("correo") + "?")) {
		sessionStorage.removeItem("sesion");
		sessionStorage.removeItem("correo");
		window.location.replace("login.html");
	} else {
	    
	}
}
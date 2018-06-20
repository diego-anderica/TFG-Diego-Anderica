<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="tfg.esi.uclm.es.Cifrador"%>
<%@ page import="org.json.JSONObject"%>
<%

	String p = request.getParameter("p");
	JSONObject jso = new JSONObject(p);
	JSONObject respuesta = new JSONObject();
	
	try{
        
		String tipo = jso.optString("tipo");
        
        if(tipo.equals("LOGIN")) {
	        String cifrada = Cifrador.encriptar(jso.optString("contrasena"));
	        respuesta.put("resultado","OK");
	        respuesta.put("cifrada", cifrada);
	        respuesta.put("sesion", session.getId());
        } else {
	        	respuesta.put("resultado", "ERROR");
	        	respuesta.put("mensaje", "No se ha identificado correctamente la petición.");
        }

    }catch(Exception e){
		System.out.println("Error en el login: " + e.getMessage());
		respuesta.put("resultado", "ERROR");
		respuesta.put("mensaje", e.getMessage());
	}
	
	out.println(respuesta.toString());

%>
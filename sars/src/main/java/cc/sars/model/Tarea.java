package cc.sars.model;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import cc.sars.model.EstadosTareas;

@Entity
@Table(name = "tarea")
public class Tarea {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty("nombreTarea") 
    private String nTarea;

	@JsonProperty("usuarioAsignado") 
	@Column(name = "usuario")
    private String usuario="NADIE";
	@JsonProperty("estado") 
	@Column (name = "estadoTarea")
    private EstadosTareas estado=EstadosTareas.NoAsignado;
	

	public Tarea (String nTarea) {
		this.nTarea=nTarea;
		this.usuario="Nadie";
		this.estado=EstadosTareas.NoAsignado;
	}

	public String getUsuarioAsignado() {
        return this.usuario;
    }

    public EstadosTareas getEstadoTarea() {
        return this.estado;
    }
    
    public String getNombre() {
        return this.nTarea;
    }

	public String setUsuarioAsignado(String u) {
        this.usuario=u;
		return this.usuario;
    }

    public EstadosTareas setEstadoTarea(EstadosTareas e) {
    	this.estado=e;
        return this.estado;
    }
    
    public String setNombre(String n) {
    	this.nTarea=n;
        return this.nTarea;
    }
/*
    public String toString() {
    	return "Id: "+this.id.toString()+" N:"+this.name+" P:"+this.price.toString();
    }
*/    
	public boolean equals(Tarea t) {
        return this.getNombre() == t.getNombre();
    }
}


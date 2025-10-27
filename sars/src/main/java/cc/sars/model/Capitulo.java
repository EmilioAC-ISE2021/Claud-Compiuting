package cc.sars.model;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id; 
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import cc.sars.model.EstadosTareas;

@Entity
@Table(name = "capitulo")
public class Capitulo {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty("nombreCapitulo") 
    private String nombre;

    @ManyToMany
    @JoinTable(
        name = "cart_products",
        joinColumns = @JoinColumn(name = "cart_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
	private Set<Tarea> tareas = new HashSet<>();
	
	public Capitulo (String n) {
		this.nombre=n;
	}
	public Capitulo (String n, Set<Tarea> t) {
		this.nombre=n;
		this.tareas.addAll(t);
	}

	public void anyadirTarea(Tarea t){
        this.tareas.add(t);
    }

	public void quitarTarea(Tarea t){
        this.tareas.remove(t);
    }
	
}


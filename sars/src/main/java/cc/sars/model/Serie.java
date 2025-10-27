package cc.sars.model;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "serie")
public class Serie {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty("nombreSerie") 
    private String nombre;

	private String descripcion;
	
	@JsonProperty("capitulos") 
	@OneToMany(mappedBy = "serie", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Capitulo> capitulos = new HashSet<>();
	
	public Serie (String n, String d) {
		this.nombre=n;
		this.descripcion=d;
	}
	public Serie (String n, String d, Set<Capitulo> c) {
		this.nombre=n;
		this.descripcion=d;
		this.capitulos.addAll(c);
	}
}

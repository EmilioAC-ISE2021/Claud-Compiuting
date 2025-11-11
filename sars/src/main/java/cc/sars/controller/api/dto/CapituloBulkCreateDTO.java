package cc.sars.controller.api.dto;

import java.util.List;

public class CapituloBulkCreateDTO {
    private String nombresCapitulos;
    private List<TareaCreateDTO> tareasEnMasa; // Now a list of TareaCreateDTO

    // Getters and Setters
    public String getNombresCapitulos() {
        return nombresCapitulos;
    }

    public void setNombresCapitulos(String nombresCapitulos) {
        this.nombresCapitulos = nombresCapitulos;
    }

    public List<TareaCreateDTO> getTareasEnMasa() {
        return tareasEnMasa;
    }

    public void setTareasEnMasa(List<TareaCreateDTO> tareasEnMasa) {
        this.tareasEnMasa = tareasEnMasa;
    }
}

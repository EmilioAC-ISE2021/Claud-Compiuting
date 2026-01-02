package cc.sars.controller.api.dto;

public class TareaAsignacionUpdateDTO {
    private String liderUsername;
    private String asignadoUsername;

    // Getters y Setters
    public String getLiderUsername() {
        return liderUsername;
    }

    public void setLiderUsername(String liderUsername) {
        this.liderUsername = liderUsername;
    }

    public String getAsignadoUsername() {
        return asignadoUsername;
    }

    public void setAsignadoUsername(String asignadoUsername) {
        this.asignadoUsername = asignadoUsername;
    }
}

package cc.sars.model;

/**
 * Define los roles de usuario (Líder o Usuario).
 * Se usa 'Role' para consistencia con Spring Security (ej: ROLE_LIDER).
 */
public enum Role {
    ROLE_LIDER,
    ROLE_USER,
    ROLE_ADMIN,
    ROLE_QC
}	
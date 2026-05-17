package com.example.gestion_evenement.Entites;

import com.example.gestion_evenement.Enums.Role;
import jakarta.persistence.*;

@Entity
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    private String codeConfirmation; // Code généré pour la confirmation
    private boolean estActive; // État de l'inscription

    // Relation ManyToOne avec Evenement
    @ManyToOne
    @JoinColumn(name = "evenement_id", nullable = true) // Une relation à un événement (nullable = true si l'utilisateur peut ne pas être lié à un événement)
    private Evenement evenement;


    // Constructeurs
    public Utilisateur() {}

    public Utilisateur(String nom, String email, String password, Role role, Evenement evenement) {
        this.nom = nom;
        this.email = email;
        this.password = password;
        this.role = role;
        this.evenement = evenement;
    }

    // Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Evenement getEvenement() {
        return evenement;
    }

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
    }

    public String getCodeConfirmation() {
        return codeConfirmation;
    }

    public void setCodeConfirmation(String codeConfirmation) {
        this.codeConfirmation = codeConfirmation;
    }

    public boolean isEstActive() {
        return estActive;
    }

    public void setEstActive(boolean estActive) {
        this.estActive = estActive;
    }

}



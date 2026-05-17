package com.example.gestion_evenement.Services;

import com.example.gestion_evenement.Entites.Evenement;
import com.example.gestion_evenement.Entites.Utilisateur;
import com.example.gestion_evenement.Repositories.UtilisateurRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.gestion_evenement.Repositories.UtilisateurRepository;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Enregistrer un nouvel utilisateur
    public Utilisateur enregistrerUtilisateur(Utilisateur utilisateur) {
        // Vérifier si un utilisateur avec cet email existe déjà
        if (utilisateurRepository.findByEmail(utilisateur.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà.");
        }

        // Encoder le mot de passe avant l'enregistrement
        utilisateur.setPassword(passwordEncoder.encode(utilisateur.getPassword()));
        return utilisateurRepository.save(utilisateur);
    }

    // Mettre à jour un utilisateur existant
    public Utilisateur mettreAJourUtilisateur(Utilisateur utilisateur) {
        if (!utilisateurRepository.existsById(utilisateur.getId())) {
            throw new IllegalArgumentException("Utilisateur introuvable !");
        }
        return utilisateurRepository.save(utilisateur);
    }

    // Trouver un utilisateur par email
    public Optional<Utilisateur> trouverParEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    // Récupérer tous les utilisateurs
    public List<Utilisateur> recupererTousLesUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    // Supprimer un utilisateur par son ID
    public void supprimerUtilisateur(Long id) {
        utilisateurRepository.deleteById(id);
    }

    // Vérifier et valider le code de confirmation
    public boolean verifierCodeConfirmation(Long id, String code) {
        Utilisateur utilisateur = utilisateurRepository.findById(id).orElse(null);
        if (utilisateur != null && utilisateur.getCodeConfirmation() != null) {
            return utilisateur.getCodeConfirmation().equals(code);
        }
    return false;
    }

    // Méthode pour récupérer un utilisateur par ID
    public Utilisateur recupererUtilisateurParId(Long id) {
        Optional<Utilisateur> utilisateur = utilisateurRepository.findById(id);
        if (utilisateur.isPresent()) {
            return utilisateur.get();
        } else {
            throw new RuntimeException("Utilisateur non trouvé avec l'ID : " + id);
        }
    }
    public void enregistrerUtilisateurTemporaire(Utilisateur utilisateur) {
        // Enregistrer l'utilisateur temporairement (sans l'activer)
        utilisateurRepository.save(utilisateur);  // Utilisez votre dépôt pour enregistrer l'utilisateur dans la base de données
    }
    // Ajoutez cette méthode dans la classe UtilisateurService
    public void associerEvenement(Long utilisateurId, Evenement evenement) {
        Optional<Utilisateur> utilisateurOptional = utilisateurRepository.findById(utilisateurId);
        if (utilisateurOptional.isPresent()) {
            Utilisateur utilisateur = utilisateurOptional.get();
            utilisateur.setEvenement(evenement); // Associe l'événement à l'utilisateur
            utilisateurRepository.save(utilisateur); // Enregistre les modifications dans la base de données
        } else {
            throw new RuntimeException("Utilisateur non trouvé avec l'ID : " + utilisateurId);
        }
    }
    public String getUtilisateurConnecteEmail() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return null;
            }
            return authentication.getName();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Utilisateur recupererUtilisateurParEmail(String email) {
        try {
            return utilisateurRepository.findByEmail(email)
                    .orElse(null);  // Retourne null au lieu de lever une exception
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}





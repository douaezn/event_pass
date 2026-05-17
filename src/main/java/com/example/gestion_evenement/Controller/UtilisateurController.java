package com.example.gestion_evenement.Controller;

import com.example.gestion_evenement.Enums.Role;
import com.example.gestion_evenement.Entites.Utilisateur;
import com.example.gestion_evenement.Services.UtilisateurService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.example.gestion_evenement.Services.EmailService;
import com.example.gestion_evenement.Services.UtilisateurService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.List;
import java.util.Random;

@Controller
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;



    public UtilisateurController(UtilisateurService utilisateurService ,EmailService emailService , PasswordEncoder passwordEncoder) {
        this.utilisateurService = utilisateurService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;


    }
    @GetMapping("/register")
    public String afficherFormulaireInscription(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        model.addAttribute("roles", Role.values());
        return "register";
    }

    @PostMapping("/register")
    public String enregistrerUtilisateur(@ModelAttribute Utilisateur utilisateur,
                                         RedirectAttributes redirectAttributes) {
        try {
            // Générer un code de confirmation à 5 chiffres
            String codeConfirmation = String.format("%05d", new Random().nextInt(100000));

            // Enregistrer l'utilisateur sans l'activer
            utilisateur.setEstActive(false);  // L'utilisateur n'est pas actif avant confirmation
            utilisateur.setCodeConfirmation(codeConfirmation); // Ajouter le code de confirmation
            utilisateurService.enregistrerUtilisateur(utilisateur); // Enregistrer temporairement l'utilisateur

            // Vérification de l'enregistrement dans la session
            System.out.println("Utilisateur enregistré avec succès, email: " + utilisateur.getEmail());

            // Préparer le contenu de l'email
            String subject = "Confirmation de votre inscription";
            String htmlContent = "<p>Bonjour " + utilisateur.getNom() + ",</p>"
                    + "<p>Merci de vous être inscrit. Voici votre code de confirmation :</p>"
                    + "<h3>" + codeConfirmation + "</h3>"
                    + "<p>Veuillez entrer ce code pour activer votre compte.</p>";

            // Envoyer l'email
            emailService.sendConfirmationEmail(utilisateur.getEmail(), subject, htmlContent);

            // Ajouter un message de succès
            redirectAttributes.addFlashAttribute("message", "Inscription réussie. Vérifiez votre email pour confirmer votre compte.");

            // Rediriger vers la page de confirmation avec l'ID de l'utilisateur
            return "redirect:/confirmer/" + utilisateur.getId();  // Redirection vers la page de confirmation
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'inscription : " + e.getMessage());
            return "redirect:/register"; // Rediriger vers la page d'inscription en cas d'erreur
        }
    }


    @GetMapping("/login")
    public String afficherPageLogin() {
        return "login";
    }

    @GetMapping("/home")
    public String afficherAccueil() {
        return "home";
    }
    // Afficher la liste des utilisateurs
    @GetMapping("/liste")
    public String afficherListeUtilisateurs(Model model) {
        List<Utilisateur> utilisateurs = utilisateurService.recupererTousLesUtilisateurs();
        model.addAttribute("utilisateurs", utilisateurs);
        return "listeUtilisateurs";
    }

    // Supprimer un utilisateur
    @GetMapping("/supprimer/{id}")
    public String supprimerUtilisateur(@PathVariable Long id) {
        utilisateurService.supprimerUtilisateur(id);
        return "redirect:/liste";
    }


    // Méthode de confirmation du code
    @PostMapping("/confirmer/{id}")
    public String confirmerCode(@PathVariable Long id,
                                @ModelAttribute("code") String code,
                                RedirectAttributes redirectAttributes) {
        try {
            // Vérifier si le code de confirmation est valide
            boolean estValide = utilisateurService.verifierCodeConfirmation(id, code);

            if (estValide) {
                // Si le code est valide, activer le compte
                Utilisateur utilisateur = utilisateurService.recupererUtilisateurParId(id);
                utilisateur.setEstActive(true);  // L'utilisateur devient actif
                utilisateur.setCodeConfirmation(null);  // Supprimer le code de confirmation
                utilisateurService.mettreAJourUtilisateur(utilisateur);  // Sauvegarder l'utilisateur activé

                redirectAttributes.addFlashAttribute("message", "Votre compte a été activé avec succès !");
                return "redirect:/login"; // Rediriger vers la page de connexion si tout va bien
            } else {
                // Si le code est invalide
                redirectAttributes.addFlashAttribute("error", "Code de confirmation invalide.");
                return "redirect:/confirmer/" + id; // Renvoi à la page de confirmation si le code est incorrect
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la confirmation : " + e.getMessage());
            return "redirect:/confirmer/" + id; // Rediriger à la page de confirmation en cas d'erreur
        }
    }



    @GetMapping("/confirmer/{id}")
    public String afficherFormulaireConfirmation(@PathVariable Long id, Model model) {
        model.addAttribute("id", id);
        return "confirmer";
    }

    @GetMapping("/profil")
    public String afficherProfilUtilisateur(Model model) {
        try {
            // Récupérer l'utilisateur connecté
            String email = utilisateurService.getUtilisateurConnecteEmail();
            if (email == null) {
                return "redirect:/login"; // Redirige vers la page de connexion si l'email est null
            }

            Utilisateur utilisateur = utilisateurService.recupererUtilisateurParEmail(email);

            if (utilisateur == null) {
                return "redirect:/login"; // Redirige vers la page de connexion si l'utilisateur est introuvable
            }

            model.addAttribute("utilisateur", utilisateur);
            return "profil"; // Vue du profil utilisateur
        } catch (Exception e) {
            e.printStackTrace(); // Afficher l'exception dans les logs
            return "redirect:/login"; // Rediriger vers la page de connexion en cas d'erreur
        }
    }

    @PostMapping("/profil/modifier")
    public String modifierProfilUtilisateur(
            @ModelAttribute Utilisateur utilisateurModifie,
            RedirectAttributes redirectAttributes) {
        try {
            // Récupérer l'utilisateur connecté
            String email = utilisateurService.getUtilisateurConnecteEmail();
            Utilisateur utilisateur = utilisateurService.recupererUtilisateurParEmail(email);

            if (utilisateur == null) {
                return "redirect:/login";
            }

            // Mettre à jour les informations
            utilisateur.setNom(utilisateurModifie.getNom());
            utilisateur.setEmail(utilisateurModifie.getEmail());
            if (utilisateurModifie.getPassword() != null && !utilisateurModifie.getPassword().isEmpty()) {
                String hashedPassword = passwordEncoder.encode(utilisateurModifie.getPassword());
                utilisateur.setPassword(hashedPassword);
            }

            utilisateurService.mettreAJourUtilisateur(utilisateur);

            redirectAttributes.addFlashAttribute("message", "Profil mis à jour avec succès !");
            return "redirect:/profil";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour : " + e.getMessage());
            return "redirect:/profil";
        }
    }




}
package com.example.gestion_evenement.Controller;

import com.example.gestion_evenement.Entites.Achat;
import com.example.gestion_evenement.Entites.Evenement;
import com.example.gestion_evenement.Entites.Utilisateur;
import com.example.gestion_evenement.Services.AchatService;
import com.example.gestion_evenement.Services.EmailService;
import com.example.gestion_evenement.Services.EvenementService;
import com.example.gestion_evenement.Services.UtilisateurService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


@Controller
@SessionAttributes("achat")
public class AchatController {

    @Autowired
    private EvenementService evenementService;

    @Autowired
    private AchatService achatService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UtilisateurService utilisateurService;

    @ModelAttribute("achat")
    public Achat achat() {
        return new Achat();
    }

    @GetMapping("/achat/acheter/{evenementId}")
    public String showAchatForm(@PathVariable Long evenementId, Model model) {
        Evenement evenement = evenementService.getEvenementById(evenementId);
        model.addAttribute("evenement", evenement);
        return "acheter";
    }

    private BigDecimal calculatePrixTotal(Evenement evenement, int quantite) {
        return evenement.getPrix().multiply(BigDecimal.valueOf(quantite));
    }

    @PostMapping("/achat/acheter/{evenementId}")
    public String processAchat(@PathVariable Long evenementId,
                               @RequestParam String nom,
                               @RequestParam String email,
                               @RequestParam int quantite,
                               @ModelAttribute("achat") Achat achat,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        Evenement evenement = evenementService.getEvenementById(evenementId);

        if (quantite <= evenement.getPlacesDisponibles()) {
            // Mettre à jour l'objet Achat avec toutes les informations
            achat.setEvenement(evenement);
            achat.setNom(nom);
            achat.setEmail(email);
            achat.setQuantite(quantite);
            achat.setPrixTotal(calculatePrixTotal(evenement, quantite));

            // Conserver l'objet Achat dans la session
            model.addAttribute("achat", achat);

            return "paiement";
        } else {
            redirectAttributes.addFlashAttribute("error", "Pas assez de places disponibles.");
            return "redirect:/evenements/accueil";
        }
    }

    @PostMapping("/achat/traiter/{evenementId}")
    public String processPayment(@PathVariable Long evenementId,
                                 @RequestParam("cardNumber") String cardNumber,
                                 @RequestParam("expiryDate") String expiryDate,
                                 @RequestParam("cvv") String cvv,
                                 @ModelAttribute("achat") Achat achat,
                                 Model model,
                                 SessionStatus sessionStatus) {
        try {
            // Validation du numéro de carte
            if (cardNumber == null || cardNumber.length() != 16 || !cardNumber.matches("\\d+")) {
                model.addAttribute("message", "Numéro de carte invalide.");
                return "paiement";
            }

            // Validation de la date d'expiration
            if (expiryDate == null || !expiryDate.matches("\\d{4}-\\d{2}")) {
                model.addAttribute("message", "Date d'expiration invalide.");
                return "paiement";
            }

            // Validation du CVV
            if (cvv == null || cvv.length() != 3 || !cvv.matches("\\d+")) {
                model.addAttribute("message", "CVV invalide.");
                return "paiement";
            }

            // Vérifier à nouveau la disponibilité des places
            Evenement evenement = evenementService.getEvenementById(evenementId);
            if (achat.getQuantite() > evenement.getPlacesDisponibles()) {
                model.addAttribute("message", "Désolé, ces places ne sont plus disponibles.");
                return "paiement";
            }


            // Vérifier que toutes les données de l'achat sont présentes
            if (achat.getNom() == null || achat.getEmail() == null ||
                    achat.getEvenement() == null || achat.getPrixTotal() == null) {
                model.addAttribute("message", "Données d'achat incomplètes.");
                return "paiement";
            }

            // Si le paiement est validé, sauvegarder l'achat
            achatService.saveAchat(achat);

           // Mettre à jour le nombre de places disponibles
            evenement.setPlacesDisponibles(evenement.getPlacesDisponibles() - achat.getQuantite());
            evenementService.updateEvenement(evenement);

            // Associer l'événement à l'utilisateur
            try {
                // Récupérer l'utilisateur connecté
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String emailUtilisateurConnecte = authentication.getName(); // L'email ou le nom de l'utilisateur connecté

                // Trouver l'utilisateur dans la base de données
                Utilisateur utilisateur = utilisateurService.trouverParEmail(emailUtilisateurConnecte)
                        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

                // Associer l'événement à l'utilisateur

                utilisateurService.associerEvenement(utilisateur.getId(), evenement);
            } catch (Exception e) {
                model.addAttribute("message", "Erreur lors de l'association de l'événement à l'utilisateur.");
                return "paiement";
            }
           // Envoyer l'email de confirmation
            try {
                sendConfirmationEmail(achat);
            } catch (Exception e) {
                model.addAttribute("message", "L'achat a été effectué, mais l'email de confirmation n'a pas pu être envoyé.");
                return "succes";
            }

            // Nettoyer la session
            sessionStatus.setComplete();

            return "redirect:/achat/succes";

        } catch (Exception e) {
            model.addAttribute("message", "Une erreur inattendue s'est produite : " + e.getMessage());
            return "paiement";
        }
    }

    @GetMapping("/achat/succes")
    public String successPage(Model model) {
        model.addAttribute("message", "Achat effectué avec succès !");
        return "succes";
    }

    private void sendConfirmationEmail(Achat achat) {
        try {
            if (achat == null || achat.getEmail() == null || achat.getEvenement() == null) {
                throw new IllegalArgumentException("Les informations de l'achat sont incomplètes");
            }

            String subject = "Confirmation de votre achat - " + achat.getEvenement().getTitre();
            NumberFormat formatMonnaie = NumberFormat.getCurrencyInstance(new Locale("fr", "MA"));
            String formattedDate = achat.getEvenement().getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            String htmlContent = String.format("""
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <meta charset="UTF-8">
                                <style>
                                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                    h1 { color: #2c3e50; }
                                    h2 { color: #34495e; }
                                    ul { list-style-type: none; padding-left: 0; }
                                    li { margin: 10px 0; }
                                    strong { color: #2c3e50; }
                                    .important { background-color: #fff3cd; padding: 10px; border-radius: 4px; }
                                    .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #eee; }
                                </style>
                            </head>
                            <body>
                                <h1>Confirmation de votre achat</h1>
                            
                                <p>Cher(e) %s,</p>
                            
                                <p>Nous vous remercions pour votre achat. Voici le récapitulatif de votre commande :</p>
                            
                                <h2>Détails de l'événement :</h2>
                                <ul>
                                    <li>🎪 <strong>Événement :</strong> %s</li>
                                    <li>📅 <strong>Date :</strong> %s</li>
                                    <li>📍 <strong>Lieu :</strong> %s</li>
                                </ul>
                            
                                <h2>Détails de votre commande :</h2>
                                <ul>
                                    <li>🎟️ <strong>Nombre de places :</strong> %d</li>
                                    <li>💰 <strong>Prix unitaire :</strong> %s</li>
                                    <li>💶 <strong>Prix total :</strong> %s</li>
                                </ul>
                            
                                <p class="important"><strong>Important :</strong> Conservez précieusement cet email, il servira de justificatif d'achat.</p>
                            
                                <p>Nous vous souhaitons un excellent événement !</p>
                            
                                <div class="footer">
                                    <p>Cordialement,<br>L'équipe événementielle</p>
                                </div>
                            </body>
                            </html>
                            """,
                    achat.getNom(),
                    achat.getEvenement().getTitre(),
                    formattedDate,
                    achat.getEvenement().getLieu(),
                    achat.getQuantite(),
                    formatMonnaie.format(achat.getEvenement().getPrix()),
                    formatMonnaie.format(achat.getPrixTotal())
            );

            emailService.sendConfirmationEmail(achat.getEmail(), subject, htmlContent);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Données invalides pour l'envoi de l'email : " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email de confirmation : " + e.getMessage(), e);
        }

    }
}
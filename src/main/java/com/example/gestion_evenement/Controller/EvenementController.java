package com.example.gestion_evenement.Controller;

import com.example.gestion_evenement.DTO.EvenementDTO;
import com.example.gestion_evenement.Entites.Evenement;
import com.example.gestion_evenement.Enums.TypeEvenement;
import com.example.gestion_evenement.Services.EvenementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/evenements")
public class EvenementController {

    private final EvenementService evenementService;

    @Autowired
    public EvenementController(EvenementService evenementService) {
        this.evenementService = evenementService;
    }

    // Afficher la liste des événements dans le DashboardAdmin
    @GetMapping("/dashboard")
    public String afficherDashboard(Model model) {
        List<Evenement> evenements = evenementService.getTousLesEvenements();
        model.addAttribute("evenements", evenements);
        return "dashboardadmin";
    }

    // Afficher le formulaire pour ajouter un événement
    @GetMapping("/ajouter")
    public String afficherFormulaireAjout(Model model) {
        model.addAttribute("evenementDTO", new EvenementDTO());
        model.addAttribute("types", TypeEvenement.values());
        return "ajouter";
    }

    // Ajouter un nouvel événement
    @PostMapping("/ajouter")
    public String ajouterEvenement(@ModelAttribute("evenementDTO") EvenementDTO evenementDTO, RedirectAttributes redirectAttributes) {
        try {
            // Convertir DTO en Entité
            Evenement evenement = new Evenement();
            evenement.setTitre(evenementDTO.getTitre());
            evenement.setDescription(evenementDTO.getDescription());
            evenement.setDate(evenementDTO.getDate());
            evenement.setLieu(evenementDTO.getLieu());
            evenement.setCapacite(evenementDTO.getCapacite());
            evenement.setType(TypeEvenement.valueOf(evenementDTO.getType()));
            evenement.setPrix(evenementDTO.getPrix());
            evenement.setPlacesDisponibles(evenementDTO.getPlacesDisponibles());

            // Sauvegarder dans la base de données
            evenementService.ajouterEvenement(evenement);

            // Message de succès
            redirectAttributes.addFlashAttribute("message", "Événement ajouté avec succès !");
        } catch (Exception e) {
            // Message d'échec en cas d'exception
            redirectAttributes.addFlashAttribute("error", "Une erreur s'est produite lors de l'ajout de l'événement.");
        }
        return "redirect:/evenements/dashboard";
    }

    // Afficher le formulaire pour modifier un événement
    @GetMapping("/modifier/{id}")
    public String afficherFormulaireModification(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Evenement evenement = evenementService.getEvenementParId(id);

        if (evenement == null) {
            redirectAttributes.addFlashAttribute("error", "L'événement avec l'ID " + id + " n'existe pas.");
            return "redirect:/evenements/dashboard";
        }

        // Log pour vérifier la date
        System.out.println("Date de l'événement récupérée : " + evenement.getDate());

        model.addAttribute("evenement", evenement);
        model.addAttribute("types", TypeEvenement.values());

        return "modifierEvenement";
    }

    // Enregistrer les modifications d'un événement
    @PostMapping("/modifier")
    public String enregistrerModification(@ModelAttribute Evenement evenement, RedirectAttributes redirectAttributes) {
        try {
            // Mettre à jour l'événement dans la base de données
            evenementService.modifierEvenement(evenement);

            // Message de succès
            redirectAttributes.addFlashAttribute("message", "Événement modifié avec succès !");
        } catch (Exception e) {
            // Message d'échec en cas d'exception
            redirectAttributes.addFlashAttribute("error", "Une erreur s'est produite lors de la modification de l'événement.");
        }
        return "redirect:/evenements/dashboard";
    }

    // Supprimer un événement
    @GetMapping("/supprimer/{id}")
    public String supprimerEvenement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            evenementService.supprimerEvenement(id);

            // Message de succès
            redirectAttributes.addFlashAttribute("message", "Événement supprimé avec succès !");
        } catch (Exception e) {
            // Message d'échec en cas d'exception
            redirectAttributes.addFlashAttribute("error", "Une erreur s'est produite lors de la suppression de l'événement.");
        }
        return "redirect:/evenements/dashboard";
    }

    // Afficher la page d'accueil avec les événements
    @GetMapping("/accueil")
    public String afficherAccueil(Model model) {
        List<Evenement> evenements = evenementService.getTousLesEvenements();
        model.addAttribute("evenements", evenements);
        return "accueil";
    }
}





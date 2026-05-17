package com.example.gestion_evenement.Services;

import com.example.gestion_evenement.Entites.Evenement;
import com.example.gestion_evenement.Repositories.AchatRepository;
import com.example.gestion_evenement.Repositories.EvenementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.gestion_evenement.Entites.Achat;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EvenementService {

    private final EvenementRepository evenementRepository;
    @Autowired
    private AchatRepository achatRepository;

    public EvenementService(EvenementRepository evenementRepository) {
        this.evenementRepository = evenementRepository;
    }

    public List<Evenement> getTousLesEvenements() {
        return evenementRepository.findAll();
    }

    public Evenement ajouterEvenement(Evenement evenement) {
        return evenementRepository.save(evenement);
    }


    public void supprimerEvenement(Long id) {
        evenementRepository.deleteById(id);
    }
    public Evenement getEvenementParId(Long id) {
        // Récupérer un événement par son ID
        return evenementRepository.findById(id).orElse(null);
    }

    public void modifierEvenement(Evenement evenement) {
        // Vérifier si l'événement existe
        if (evenementRepository.existsById(evenement.getId())) {
            // Mettre à jour l'événement
            evenementRepository.save(evenement);
        }
    }
    public Evenement getEvenementById(Long id) {
        return evenementRepository.findById(id).orElseThrow(() -> new RuntimeException("Événement non trouvé"));
    }

    public void updateEvenement(Evenement evenement) {
        evenementRepository.save(evenement);
    }
}


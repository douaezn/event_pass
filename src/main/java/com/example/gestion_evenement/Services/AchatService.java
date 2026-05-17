package com.example.gestion_evenement.Services;

import com.example.gestion_evenement.Entites.Achat;
import com.example.gestion_evenement.Repositories.AchatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AchatService {

    @Autowired
    private AchatRepository achatRepository;

    public void saveAchat(Achat achat) {
        achatRepository.save(achat);
    }
}







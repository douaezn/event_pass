package com.example.gestion_evenement.Repositories;


import com.example.gestion_evenement.Entites.Evenement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvenementRepository extends JpaRepository<Evenement, Long> {
}


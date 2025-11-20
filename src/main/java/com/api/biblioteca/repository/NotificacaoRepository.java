package com.api.biblioteca.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.biblioteca.model.Notificacao;
import com.api.biblioteca.model.Usuario;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long>{

    List<Notificacao> findByDestinatarioOrderByDataCriacaoDesc(Usuario destinatario);

    long countByDestinatarioAndLidaFalse(Usuario destinatario) ;
}